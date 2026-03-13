import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.sugar.autofill
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.core.validation.TransactionValidator
import org.xrpl.sdk.core.validation.ValidationResult
import org.xrpl.sdk.core.validation.validate

/**
 * Demonstrates client-side transaction validation using TransactionValidator.
 *
 * TransactionValidator performs all checks locally without a network call.
 * It accumulates every error rather than stopping at the first one, so callers
 * can surface all problems in a single pass.
 *
 * Two overloads are available:
 *   - validate(XrplTransaction.Unsigned): checks account and type-specific rules
 *   - validate(XrplTransaction.Filled, maxFee): also checks fee, sequence,
 *     and lastLedgerSequence filled in by autofill()
 *
 * Extension functions on the transaction types delegate to the same validator:
 *   unsigned.validate()
 *   filled.validate(maxFee = XrpDrops(5_000_000))
 *
 * Rules enforced per transaction type:
 *   Payment:     destination != account, amount > 0, sendMax > 0, deliverMin > 0
 *   OfferCreate: takerGets > 0, takerPays > 0
 *   TrustSet:    limitAmount currency must not be "XRP"
 */
suspend fun main() {

    // --- 1. Validate an unsigned transaction ---
    // Only account-level and type-specific rules are checked here.
    val goodTx = payment {
        account          = Address("rSenderAddressHere")
        this.destination = Address("rReceiverAddressHere")
        amount           = 10.xrp
    }

    when (val result = goodTx.validate()) {
        is ValidationResult.Valid   -> println("Unsigned tx is valid.")
        is ValidationResult.Invalid -> println("Unsigned tx has errors: ${result.errors}")
    }

    // --- 2. Detect a broken unsigned transaction ---
    // Sending to yourself is caught immediately, before any network call.
    val selfPayment = payment {
        account          = Address("rSenderAddressHere")
        this.destination = Address("rSenderAddressHere")  // same as sender — invalid
        amount           = 5.xrp
    }

    when (val result = TransactionValidator.validate(selfPayment)) {
        is ValidationResult.Valid   -> println("Valid (unexpected)")
        is ValidationResult.Invalid -> println("Self-payment rejected: ${result.errors}")
    }

    // --- 3. Validate a filled transaction ---
    // After autofill() the fee, sequence, and lastLedgerSequence are present.
    // The validator also checks that fee <= maxFee.
    XrplClient { network = Network.Testnet }.use { client ->

        val filled = client.autofill(goodTx).getOrThrow()
        println("\nFilled: fee=${filled.fee}  seq=${filled.sequence}  lastLedger=${filled.lastLedgerSequence}")

        // Default maxFee is 10 XRP (TransactionValidator.MAX_FEE_DROPS).
        when (val result = filled.validate()) {
            is ValidationResult.Valid   -> println("Filled tx is valid.")
            is ValidationResult.Invalid -> println("Filled tx has errors: ${result.errors}")
        }

        // --- 4. Enforce a tighter fee ceiling ---
        // Reject if the autofilled fee exceeds 0.001 XRP (1000 drops).
        val tightCeiling = XrpDrops(1_000L)
        when (val result = filled.validate(maxFee = tightCeiling)) {
            is ValidationResult.Valid   -> println("Fee is within tight ceiling.")
            is ValidationResult.Invalid -> println("Fee check failed: ${result.errors}")
        }

        // --- 5. Validate using the object-level TransactionValidator directly ---
        // Useful when you want to check multiple transactions with the same maxFee.
        val maxFee = XrpDrops(500_000L)  // 0.5 XRP
        val validationResult = TransactionValidator.validate(filled, maxFee = maxFee)
        println("\nDirect validator result: $validationResult")
    }
}
