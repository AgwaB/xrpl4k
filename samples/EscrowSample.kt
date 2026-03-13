import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.escrowCancel
import org.xrpl.sdk.core.model.transaction.escrowCreate
import org.xrpl.sdk.core.model.transaction.escrowFinish
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.Wallet

/**
 * Demonstrates time-based escrow on the XRPL.
 *
 * Escrow locks XRP in a ledger object until certain conditions are met:
 *   - Time-based: finishAfter / cancelAfter (Ripple epoch seconds)
 *   - Crypto-condition: PREIMAGE-SHA-256 condition + fulfillment
 *   - Or both combined
 *
 * The lifecycle:
 *   1. EscrowCreate  — lock XRP with conditions
 *   2. EscrowFinish  — release funds after conditions are met
 *   3. EscrowCancel  — reclaim funds after cancelAfter time passes
 *
 * Time values use the Ripple epoch (seconds since 2000-01-01T00:00:00Z).
 * To convert from Unix epoch: rippleTime = unixTime - 946684800
 *
 * Use cases:
 *   - Delayed payments (payroll, vesting schedules)
 *   - Conditional payments (release on delivery confirmation)
 *   - Two-party agreements with timeout fallback
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val sender   = Wallet.fromSeed("sSenderSeedHere")
        val receiver = Address("rReceiverAddressHere")

        // --- 1. Create a time-based escrow ---
        // Lock 50 XRP that can be finished after a certain time,
        // and auto-cancellable after a later time.
        //
        // finishAfter: earliest time the receiver can claim the funds
        // cancelAfter: after this time, the sender can reclaim the funds
        val now = (System.currentTimeMillis() / 1000 - 946684800).toUInt()  // current Ripple time
        val finishTime = now + 60u       // claimable in 1 minute
        val cancelTime = now + 86400u    // cancellable after 24 hours

        val createTx = escrowCreate {
            account     = sender.address
            destination = receiver
            amount      = 50.xrp
            finishAfter = finishTime
            cancelAfter = cancelTime
        }

        val createResult = client.submitAndWait(createTx, sender).getOrThrow()
        println("Escrow created!")
        println("  Hash:     ${createResult.hash}")
        println("  Ledger:   ${createResult.ledgerIndex}")
        println("  Result:   ${createResult.engineResult}")
        println("  Claimable after Ripple time: $finishTime")
        println("  Cancellable after:           $cancelTime")

        // The sequence number of the EscrowCreate tx is needed for Finish/Cancel.
        // In production, get this from the autofilled transaction or from tx metadata.
        val escrowSequence = 12345u  // replace with actual sequence

        // --- 2. Finish the escrow (after finishAfter has passed) ---
        // Anyone can submit EscrowFinish, but the funds always go to the destination.
        val finishTx = escrowFinish {
            account       = receiver                // the claimer (can be anyone)
            owner         = sender.address          // who created the escrow
            offerSequence = escrowSequence
        }

        // In a real scenario, wait until finishAfter has passed before submitting.
        val receiverWallet = Wallet.fromSeed("sReceiverSeedHere")
        when (val finishResult = client.submitAndWait(finishTx, receiverWallet)) {
            is XrplResult.Success ->
                println("\nEscrow finished! Funds released: ${finishResult.value.engineResult}")
            is XrplResult.Failure ->
                println("\nEscrow finish failed (probably too early): ${finishResult.error}")
        }

        // --- 3. Cancel the escrow (after cancelAfter has passed) ---
        // Only possible after cancelAfter. Funds return to the original sender.
        val cancelTx = escrowCancel {
            account       = sender.address
            owner         = sender.address
            offerSequence = escrowSequence
        }

        when (val cancelResult = client.submitAndWait(cancelTx, sender)) {
            is XrplResult.Success ->
                println("\nEscrow cancelled. Funds returned: ${cancelResult.value.engineResult}")
            is XrplResult.Failure ->
                println("\nEscrow cancel failed (too early or already finished): ${cancelResult.error}")
        }
    }
}
