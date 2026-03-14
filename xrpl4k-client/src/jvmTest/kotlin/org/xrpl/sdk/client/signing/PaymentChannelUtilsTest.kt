package org.xrpl.sdk.client.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.crypto.Wallet

class PaymentChannelUtilsTest : FunSpec({

    val channelId = "5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"
    val amount = "1000000"

    context("verifyPaymentChannelClaim") {

        test("sign then verify round-trip (Ed25519)") {
            val generated = Wallet.generate()
            val wallet = generated.wallet

            val signature = signPaymentChannelClaim(wallet, channelId, amount)

            verifyPaymentChannelClaim(
                channelId = channelId,
                amount = amount,
                signature = signature,
                publicKey = wallet.publicKey.value,
            ) shouldBe true
        }

        test("wrong signature returns false") {
            val generated = Wallet.generate()
            val wallet = generated.wallet

            val signature = signPaymentChannelClaim(wallet, channelId, amount)

            // Flip a byte in the signature to make it invalid
            val badSignature = signature.replaceRange(0, 2, if (signature.startsWith("00")) "FF" else "00")

            verifyPaymentChannelClaim(
                channelId = channelId,
                amount = amount,
                signature = badSignature,
                publicKey = wallet.publicKey.value,
            ) shouldBe false
        }

        test("wrong channel ID returns false") {
            val generated = Wallet.generate()
            val wallet = generated.wallet

            val signature = signPaymentChannelClaim(wallet, channelId, amount)

            val wrongChannelId = "0000000000000000000000000000000000000000000000000000000000000000"

            verifyPaymentChannelClaim(
                channelId = wrongChannelId,
                amount = amount,
                signature = signature,
                publicKey = wallet.publicKey.value,
            ) shouldBe false
        }
    }
})
