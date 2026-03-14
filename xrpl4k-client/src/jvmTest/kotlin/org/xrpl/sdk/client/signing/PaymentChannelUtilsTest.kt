package org.xrpl.sdk.client.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

class PaymentChannelUtilsTest : FunSpec({

    val provider = platformCryptoProvider()
    val channelId = "5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"
    val amount = "1000000"

    context("verifyPaymentChannelClaim") {

        test("sign then verify round-trip (Ed25519)") {
            val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
            val wallet = generated.wallet

            val signature = signPaymentChannelClaim(wallet, channelId, amount)

            verifyPaymentChannelClaim(
                channelId = channelId,
                amount = amount,
                signature = signature,
                publicKey = wallet.publicKey.value,
            ) shouldBe true
        }

        test("sign then verify round-trip (Secp256k1)") {
            val generated = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
            val wallet = generated.wallet

            val signature = signPaymentChannelClaim(wallet, channelId, amount)

            verifyPaymentChannelClaim(
                channelId = channelId,
                amount = amount,
                signature = signature,
                publicKey = wallet.publicKey.value,
            ) shouldBe true
        }

        test("wrong signature returns false (Ed25519)") {
            val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
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
            val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
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

        test("wrong amount returns false") {
            val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
            val wallet = generated.wallet

            val signature = signPaymentChannelClaim(wallet, channelId, amount)

            verifyPaymentChannelClaim(
                channelId = channelId,
                amount = "9999999",
                signature = signature,
                publicKey = wallet.publicKey.value,
            ) shouldBe false
        }

        test("wrong public key returns false") {
            val generated1 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
            val generated2 = Wallet.generate(KeyAlgorithm.Ed25519, provider)

            val signature = signPaymentChannelClaim(generated1.wallet, channelId, amount)

            verifyPaymentChannelClaim(
                channelId = channelId,
                amount = amount,
                signature = signature,
                publicKey = generated2.wallet.publicKey.value,
            ) shouldBe false
        }
    }

    context("signPaymentChannelClaim") {
        test("produces deterministic signatures for Ed25519") {
            val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
            val wallet = generated.wallet
            val sig1 = signPaymentChannelClaim(wallet, channelId, amount)
            val sig2 = signPaymentChannelClaim(wallet, channelId, amount)
            sig1 shouldBe sig2
        }

        test("different amounts produce different signatures") {
            val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
            val wallet = generated.wallet
            val sig1 = signPaymentChannelClaim(wallet, channelId, "1000000")
            val sig2 = signPaymentChannelClaim(wallet, channelId, "2000000")
            (sig1 == sig2) shouldBe false
        }
    }
})
