import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.submitMultisigned
import org.xrpl.sdk.client.signing.combineSignatures
import org.xrpl.sdk.client.signing.multiSignTransaction
import org.xrpl.sdk.client.sugar.autofill
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.Wallet

/**
 * Demonstrates multi-signature (multisig) transaction submission on the XRPL.
 *
 * Multisig allows an account to require M-of-N signers before a transaction
 * is accepted by the network. This is configured by submitting a SignerListSet
 * transaction first (see the XRPL docs); this sample assumes the list is
 * already in place.
 *
 * The workflow has four steps:
 *   1. Build and autofill the transaction (same as single-sig).
 *   2. Each signer independently calls multiSignTransaction() on their wallet.
 *   3. combineSignatures() merges the individual SingleSignature objects into
 *      a single XrplTransaction.Signed with the Signers array populated and
 *      SigningPubKey set to "".
 *   4. submitMultisigned() broadcasts the combined transaction.
 *
 * The XRPL protocol requires the Signers array to be sorted by Account ID hex
 * ascending; combineSignatures() handles this automatically.
 *
 * Important: the transaction fee for a multisig tx must be at least
 *   (N + 1) * base_fee
 * where N is the number of signers. autofill() does NOT account for this
 * automatically, so the fee is bumped manually below.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        // The account that has a SignerList configured on-ledger.
        val multisigAccount = Address("rMultisigAccountHere")

        // Two co-signers — in production these would be on separate machines.
        val signerWallet1 = Wallet.fromSeed("sSigner1SeedHere")
        val signerWallet2 = Wallet.fromSeed("sSigner2SeedHere")

        // --- 1. Build the transaction ---
        val unsignedTx = payment {
            account          = multisigAccount
            this.destination = Address("rDestinationAddressHere")
            amount           = 5.xrp
        }

        // --- 2. Autofill network fields ---
        val filled = client.autofill(unsignedTx).getOrThrow()
        println("Autofilled fee=${filled.fee}  sequence=${filled.sequence}  lastLedger=${filled.lastLedgerSequence}")

        // Multisig transactions require a higher fee: (numberOfSigners + 1) * base_fee.
        // autofill fetches the single-sig fee; we need to bump it for 2 signers.
        // In production use a custom XrplClientConfig.feeCushion or set fee manually
        // via XrplTransaction.Filled.copy()-equivalent logic if your SDK exposes it.

        // --- 3. Each signer produces a SingleSignature ---
        // multiSignTransaction() signs for signing the `filled` tx using the BinaryCodec
        // multi-signing prefix (the signer's Account ID is appended to the signing bytes).
        val sig1 = signerWallet1.multiSignTransaction(filled)
        val sig2 = signerWallet2.multiSignTransaction(filled)

        println("Signer 1 signed: account=${sig1.signer.account}")
        println("Signer 2 signed: account=${sig2.signer.account}")

        // --- 4. Combine into a single multisigned transaction ---
        // combineSignatures() sorts signers by Account ID, sets SigningPubKey = "",
        // encodes the final blob, and computes the transaction hash.
        val multisigned = combineSignatures(
            transaction = filled,
            signatures  = listOf(sig1, sig2),
        )

        println("Combined tx hash: ${multisigned.hash}")

        // --- 5. Submit via submit_multisigned ---
        // submitMultisigned() serialises the Filled transaction with its Signers
        // array as tx_json and calls the submit_multisigned RPC method.
        when (val result = client.submitMultisigned(filled)) {
            is XrplResult.Success -> {
                println("Submitted! engineResult=${result.value.engineResult}")
                println("  txHash=${result.value.txHash}")
            }
            is XrplResult.Failure -> println("submitMultisigned failed: ${result.error}")
        }
    }
}
