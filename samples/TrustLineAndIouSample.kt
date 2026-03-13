import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountLines
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.model.transaction.trustSet
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.crypto.Wallet

/**
 * Demonstrates trust lines and IOU (issued currency) payments.
 *
 * Unlike XRP which is native to the ledger, issued currencies (IOUs) require
 * a trust relationship. Before Alice can hold USD issued by Gateway, Alice
 * must create a trust line to Gateway for USD with a limit.
 *
 * The workflow:
 *   1. Receiver creates a trust line to the issuer (TrustSet)
 *   2. Issuer sends the IOU to the receiver (Payment with IssuedAmount)
 *   3. Receiver can now send that IOU to others who also trust the issuer
 *
 * Key concepts:
 *   - CurrencyCode: 3-letter ISO code ("USD") or 40-char hex for non-standard codes
 *   - IssuedAmount: (currency, issuer, value) — the value is a decimal string
 *   - Trust line limit: the maximum amount the account is willing to hold
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val gateway  = Wallet.fromSeed("sGatewayIssuerSeedHere")   // the currency issuer
        val alice    = Wallet.fromSeed("sAliceSeedHere")            // wants to hold USD
        val bob      = Wallet.fromSeed("sBobSeedHere")              // also holds USD

        val usd = CurrencyCode("USD")

        // --- Step 1: Alice creates a trust line to Gateway for USD ---
        // This allows Alice to hold up to 1,000,000 USD issued by Gateway.
        // Without this, any USD payment to Alice would fail with tecNO_LINE.
        val trustTx = trustSet {
            account     = alice.address
            limitAmount = IssuedAmount(
                currency = usd,
                issuer   = gateway.address,
                value    = "1000000",   // max Alice is willing to hold
            )
        }

        val trustResult = client.submitAndWait(trustTx, alice).getOrThrow()
        println("Trust line created: ${trustResult.engineResult}")

        // --- Step 2: Gateway issues 100 USD to Alice ---
        // The issuer sends the IOU. This is how new IOUs enter circulation.
        val issueTx = payment {
            account          = gateway.address
            this.destination = alice.address
            amount           = IssuedAmount(
                currency = usd,
                issuer   = gateway.address,
                value    = "100",
            )
        }

        val issueResult = client.submitAndWait(issueTx, gateway).getOrThrow()
        println("100 USD issued to Alice: ${issueResult.engineResult}")

        // --- Step 3: Alice sends 25 USD to Bob ---
        // Bob must also have a trust line to Gateway for USD (omitted here for brevity).
        val sendTx = payment {
            account          = alice.address
            this.destination = bob.address
            amount           = IssuedAmount(
                currency = usd,
                issuer   = gateway.address,
                value    = "25",
            )
        }

        val sendResult = client.submitAndWait(sendTx, alice).getOrThrow()
        println("25 USD sent Alice -> Bob: ${sendResult.engineResult}")

        // --- Step 4: Verify trust lines ---
        val lines = client.accountLines(alice.address).getOrThrow()
        for (line in lines.lines) {
            println("  ${line.currency} / ${line.account}  balance=${line.balance}  limit=${line.limit}")
        }
    }
}
