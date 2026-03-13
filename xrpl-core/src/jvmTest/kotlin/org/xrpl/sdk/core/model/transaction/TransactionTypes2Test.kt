package org.xrpl.sdk.core.model.transaction

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode

class TransactionTypes2Test : FunSpec({

    val alice = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val bob = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    val usdAmount =
        IssuedAmount(
            currency = CurrencyCode("USD"),
            issuer = bob,
            value = "100",
        )
    val channelId = "AABBCCDDEEFF0011223344556677889900112233445566778899AABBCCDDEEFF00"
    val checkId = "AABBCCDDEEFF0011223344556677889900112233445566778899AABBCCDDEEFF01"
    val nftId = "000800006203F49C21D5D6E022CB16DE3538F248662FC7C700000001"

    // ── EscrowCreate ──────────────────────────────────────────────────────────

    context("EscrowCreateFields") {
        test("constructs and exposes required fields") {
            val fields =
                EscrowCreateFields(
                    destination = bob,
                    amount = 10.xrp,
                )
            fields.destination shouldBe bob
            fields.amount shouldBe 10.xrp
            fields.finishAfter shouldBe null
            fields.cancelAfter shouldBe null
            fields.condition shouldBe null
            fields.destinationTag shouldBe null
        }

        test("equals and hashCode contract") {
            val a = EscrowCreateFields(destination = bob, amount = 10.xrp)
            val b = EscrowCreateFields(destination = bob, amount = 10.xrp)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("optional fields participate in equality") {
            val a = EscrowCreateFields(destination = bob, amount = 10.xrp, cancelAfter = 1000u)
            val b = EscrowCreateFields(destination = bob, amount = 10.xrp, cancelAfter = 2000u)
            a shouldNotBe b
        }

        test("toString includes class name and fields") {
            val f = EscrowCreateFields(destination = bob, amount = 10.xrp, condition = "A0258020")
            val s = f.toString()
            s.contains("EscrowCreateFields") shouldBe true
            s.contains("condition=A0258020") shouldBe true
        }
    }

    context("EscrowCreateBuilder") {
        test("happy path - required fields") {
            val builder = EscrowCreateBuilder()
            builder.destination = bob
            builder.amount = 10.xrp
            val fields = builder.build()
            fields.destination shouldBe bob
            fields.amount shouldBe 10.xrp
        }

        test("optional fields pass through") {
            val builder = EscrowCreateBuilder()
            builder.destination = bob
            builder.amount = 10.xrp
            builder.finishAfter = 720000000u
            builder.cancelAfter = 730000000u
            builder.condition = "A0258020"
            builder.destinationTag = 42u
            val fields = builder.build()
            fields.finishAfter shouldBe 720000000u
            fields.cancelAfter shouldBe 730000000u
            fields.condition shouldBe "A0258020"
            fields.destinationTag shouldBe 42u
        }

        test("throws when destination is missing") {
            val builder = EscrowCreateBuilder()
            builder.amount = 10.xrp
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "destination is required"
        }

        test("throws when amount is missing") {
            val builder = EscrowCreateBuilder()
            builder.destination = bob
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "amount is required"
        }
    }

    // ── EscrowFinish ──────────────────────────────────────────────────────────

    context("EscrowFinishFields") {
        test("constructs with required fields") {
            val fields = EscrowFinishFields(owner = alice, offerSequence = 5u)
            fields.owner shouldBe alice
            fields.offerSequence shouldBe 5u
            fields.condition shouldBe null
            fields.fulfillment shouldBe null
        }

        test("equals and hashCode contract") {
            val a = EscrowFinishFields(owner = alice, offerSequence = 5u)
            val b = EscrowFinishFields(owner = alice, offerSequence = 5u)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different offerSequence means not equal") {
            val a = EscrowFinishFields(owner = alice, offerSequence = 5u)
            val b = EscrowFinishFields(owner = alice, offerSequence = 6u)
            a shouldNotBe b
        }
    }

    context("EscrowFinishBuilder") {
        test("happy path") {
            val builder = EscrowFinishBuilder()
            builder.owner = alice
            builder.offerSequence = 5u
            val fields = builder.build()
            fields.owner shouldBe alice
            fields.offerSequence shouldBe 5u
        }

        test("optional condition and fulfillment pass through") {
            val builder = EscrowFinishBuilder()
            builder.owner = alice
            builder.offerSequence = 5u
            builder.condition = "A0258020"
            builder.fulfillment = "A0228020"
            val fields = builder.build()
            fields.condition shouldBe "A0258020"
            fields.fulfillment shouldBe "A0228020"
        }

        test("throws when owner is missing") {
            val builder = EscrowFinishBuilder()
            builder.offerSequence = 5u
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "owner is required"
        }
    }

    // ── EscrowCancel ──────────────────────────────────────────────────────────

    context("EscrowCancelFields") {
        test("constructs with required fields") {
            val fields = EscrowCancelFields(owner = alice, offerSequence = 3u)
            fields.owner shouldBe alice
            fields.offerSequence shouldBe 3u
        }

        test("equals and hashCode contract") {
            val a = EscrowCancelFields(owner = alice, offerSequence = 3u)
            val b = EscrowCancelFields(owner = alice, offerSequence = 3u)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("toString includes fields") {
            val f = EscrowCancelFields(owner = alice, offerSequence = 3u)
            val s = f.toString()
            s.contains("EscrowCancelFields") shouldBe true
            s.contains("offerSequence=3") shouldBe true
        }
    }

    context("EscrowCancelBuilder") {
        test("happy path") {
            val builder = EscrowCancelBuilder()
            builder.owner = alice
            builder.offerSequence = 3u
            val fields = builder.build()
            fields.owner shouldBe alice
            fields.offerSequence shouldBe 3u
        }

        test("throws when owner is missing") {
            val builder = EscrowCancelBuilder()
            builder.offerSequence = 3u
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "owner is required"
        }
    }

    // ── PaymentChannelCreate ──────────────────────────────────────────────────

    context("PaymentChannelCreateFields") {
        test("constructs with required fields") {
            val fields =
                PaymentChannelCreateFields(
                    destination = bob,
                    amount = 10.xrp,
                    settleDelay = 86400u,
                    publicKey = "ED5A5D99967157E9516B7DEF228B56B5F0E38A09B8B06E49E60B7C47A5EF0EC5B",
                )
            fields.destination shouldBe bob
            fields.amount shouldBe 10.xrp
            fields.settleDelay shouldBe 86400u
            fields.publicKey shouldBe "ED5A5D99967157E9516B7DEF228B56B5F0E38A09B8B06E49E60B7C47A5EF0EC5B"
            fields.cancelAfter shouldBe null
            fields.destinationTag shouldBe null
        }

        test("equals and hashCode contract") {
            val a =
                PaymentChannelCreateFields(
                    destination = bob,
                    amount = 10.xrp,
                    settleDelay = 86400u,
                    publicKey = "ED5A",
                )
            val b =
                PaymentChannelCreateFields(
                    destination = bob,
                    amount = 10.xrp,
                    settleDelay = 86400u,
                    publicKey = "ED5A",
                )
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different publicKey means not equal") {
            val a =
                PaymentChannelCreateFields(
                    destination = bob,
                    amount = 10.xrp,
                    settleDelay = 86400u,
                    publicKey = "ED5A",
                )
            val b =
                PaymentChannelCreateFields(
                    destination = bob,
                    amount = 10.xrp,
                    settleDelay = 86400u,
                    publicKey = "ED5B",
                )
            a shouldNotBe b
        }
    }

    context("PaymentChannelCreateBuilder") {
        test("happy path") {
            val builder = PaymentChannelCreateBuilder()
            builder.destination = bob
            builder.amount = 10.xrp
            builder.settleDelay = 86400u
            builder.publicKey = "ED5A"
            val fields = builder.build()
            fields.destination shouldBe bob
            fields.amount shouldBe 10.xrp
            fields.settleDelay shouldBe 86400u
            fields.publicKey shouldBe "ED5A"
        }

        test("optional cancelAfter and destinationTag pass through") {
            val builder = PaymentChannelCreateBuilder()
            builder.destination = bob
            builder.amount = 10.xrp
            builder.settleDelay = 86400u
            builder.publicKey = "ED5A"
            builder.cancelAfter = 720000000u
            builder.destinationTag = 7u
            val fields = builder.build()
            fields.cancelAfter shouldBe 720000000u
            fields.destinationTag shouldBe 7u
        }

        test("throws when destination is missing") {
            val builder = PaymentChannelCreateBuilder()
            builder.amount = 10.xrp
            builder.settleDelay = 86400u
            builder.publicKey = "ED5A"
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "destination is required"
        }

        test("throws when amount is missing") {
            val builder = PaymentChannelCreateBuilder()
            builder.destination = bob
            builder.settleDelay = 86400u
            builder.publicKey = "ED5A"
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "amount is required"
        }

        test("throws when publicKey is missing") {
            val builder = PaymentChannelCreateBuilder()
            builder.destination = bob
            builder.amount = 10.xrp
            builder.settleDelay = 86400u
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "publicKey is required"
        }
    }

    // ── PaymentChannelFund ────────────────────────────────────────────────────

    context("PaymentChannelFundFields") {
        test("constructs with required fields") {
            val fields = PaymentChannelFundFields(channel = channelId, amount = 5.xrp)
            fields.channel shouldBe channelId
            fields.amount shouldBe 5.xrp
            fields.expiration shouldBe null
        }

        test("equals and hashCode contract") {
            val a = PaymentChannelFundFields(channel = channelId, amount = 5.xrp)
            val b = PaymentChannelFundFields(channel = channelId, amount = 5.xrp)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different channel means not equal") {
            val a = PaymentChannelFundFields(channel = "AAAA", amount = 5.xrp)
            val b = PaymentChannelFundFields(channel = "BBBB", amount = 5.xrp)
            a shouldNotBe b
        }
    }

    context("PaymentChannelFundBuilder") {
        test("happy path") {
            val builder = PaymentChannelFundBuilder()
            builder.channel = channelId
            builder.amount = 5.xrp
            val fields = builder.build()
            fields.channel shouldBe channelId
            fields.amount shouldBe 5.xrp
        }

        test("optional expiration passes through") {
            val builder = PaymentChannelFundBuilder()
            builder.channel = channelId
            builder.amount = 5.xrp
            builder.expiration = 720000000u
            val fields = builder.build()
            fields.expiration shouldBe 720000000u
        }

        test("throws when channel is missing") {
            val builder = PaymentChannelFundBuilder()
            builder.amount = 5.xrp
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "channel is required"
        }

        test("throws when amount is missing") {
            val builder = PaymentChannelFundBuilder()
            builder.channel = channelId
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "amount is required"
        }
    }

    // ── PaymentChannelClaim ───────────────────────────────────────────────────

    context("PaymentChannelClaimFields") {
        test("constructs with required channel only") {
            val fields = PaymentChannelClaimFields(channel = channelId)
            fields.channel shouldBe channelId
            fields.balance shouldBe null
            fields.amount shouldBe null
            fields.signature shouldBe null
            fields.publicKey shouldBe null
        }

        test("equals and hashCode contract") {
            val a = PaymentChannelClaimFields(channel = channelId, amount = 5.xrp)
            val b = PaymentChannelClaimFields(channel = channelId, amount = 5.xrp)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }
    }

    context("PaymentChannelClaimBuilder") {
        test("happy path - channel only") {
            val builder = PaymentChannelClaimBuilder()
            builder.channel = channelId
            val fields = builder.build()
            fields.channel shouldBe channelId
        }

        test("all optional fields pass through") {
            val builder = PaymentChannelClaimBuilder()
            builder.channel = channelId
            builder.balance = 2.xrp
            builder.amount = 5.xrp
            builder.signature = "DEADBEEF"
            builder.publicKey = "ED5A"
            val fields = builder.build()
            fields.balance shouldBe 2.xrp
            fields.amount shouldBe 5.xrp
            fields.signature shouldBe "DEADBEEF"
            fields.publicKey shouldBe "ED5A"
        }

        test("throws when channel is missing") {
            val builder = PaymentChannelClaimBuilder()
            builder.amount = 5.xrp
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "channel is required"
        }
    }

    // ── CheckCreate ───────────────────────────────────────────────────────────

    context("CheckCreateFields") {
        test("constructs with required fields") {
            val fields = CheckCreateFields(destination = bob, sendMax = 10.xrp)
            fields.destination shouldBe bob
            fields.sendMax shouldBe 10.xrp
            fields.destinationTag shouldBe null
            fields.expiration shouldBe null
            fields.invoiceId shouldBe null
        }

        test("equals and hashCode contract") {
            val a = CheckCreateFields(destination = bob, sendMax = 10.xrp)
            val b = CheckCreateFields(destination = bob, sendMax = 10.xrp)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different sendMax means not equal") {
            val a = CheckCreateFields(destination = bob, sendMax = 10.xrp)
            val b = CheckCreateFields(destination = bob, sendMax = 20.xrp)
            a shouldNotBe b
        }

        test("toString includes class name") {
            val f = CheckCreateFields(destination = bob, sendMax = 10.xrp)
            f.toString().contains("CheckCreateFields") shouldBe true
        }
    }

    context("CheckCreateBuilder") {
        test("happy path") {
            val builder = CheckCreateBuilder()
            builder.destination = bob
            builder.sendMax = 10.xrp
            val fields = builder.build()
            fields.destination shouldBe bob
            fields.sendMax shouldBe 10.xrp
        }

        test("optional fields pass through") {
            val builder = CheckCreateBuilder()
            builder.destination = bob
            builder.sendMax = 10.xrp
            builder.destinationTag = 8u
            builder.expiration = 720000000u
            builder.invoiceId = "AABB"
            val fields = builder.build()
            fields.destinationTag shouldBe 8u
            fields.expiration shouldBe 720000000u
            fields.invoiceId shouldBe "AABB"
        }

        test("throws when destination is missing") {
            val builder = CheckCreateBuilder()
            builder.sendMax = 10.xrp
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "destination is required"
        }

        test("throws when sendMax is missing") {
            val builder = CheckCreateBuilder()
            builder.destination = bob
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "sendMax is required"
        }
    }

    // ── CheckCash ─────────────────────────────────────────────────────────────

    context("CheckCashFields") {
        test("constructs with checkId only") {
            val fields = CheckCashFields(checkId = checkId)
            fields.checkId shouldBe checkId
            fields.amount shouldBe null
            fields.deliverMin shouldBe null
        }

        test("equals and hashCode contract") {
            val a = CheckCashFields(checkId = checkId, amount = 5.xrp)
            val b = CheckCashFields(checkId = checkId, amount = 5.xrp)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }
    }

    context("CheckCashBuilder") {
        test("happy path") {
            val builder = CheckCashBuilder()
            builder.checkId = checkId
            builder.amount = 5.xrp
            val fields = builder.build()
            fields.checkId shouldBe checkId
            fields.amount shouldBe 5.xrp
        }

        test("deliverMin optional field passes through") {
            val builder = CheckCashBuilder()
            builder.checkId = checkId
            builder.deliverMin = 1.xrp
            val fields = builder.build()
            fields.deliverMin shouldBe 1.xrp
        }

        test("throws when checkId is missing") {
            val builder = CheckCashBuilder()
            builder.amount = 5.xrp
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "checkId is required"
        }
    }

    // ── CheckCancel ───────────────────────────────────────────────────────────

    context("CheckCancelFields") {
        test("constructs with checkId") {
            val fields = CheckCancelFields(checkId = checkId)
            fields.checkId shouldBe checkId
        }

        test("equals and hashCode contract") {
            val a = CheckCancelFields(checkId = checkId)
            val b = CheckCancelFields(checkId = checkId)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different checkId means not equal") {
            val a = CheckCancelFields(checkId = "AAAA")
            val b = CheckCancelFields(checkId = "BBBB")
            a shouldNotBe b
        }

        test("toString includes checkId") {
            val f = CheckCancelFields(checkId = checkId)
            f.toString().contains("CheckCancelFields") shouldBe true
        }
    }

    context("CheckCancelBuilder") {
        test("happy path") {
            val builder = CheckCancelBuilder()
            builder.checkId = checkId
            val fields = builder.build()
            fields.checkId shouldBe checkId
        }

        test("throws when checkId is missing") {
            val builder = CheckCancelBuilder()
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "checkId is required"
        }
    }

    // ── NFTokenMint ───────────────────────────────────────────────────────────

    context("NFTokenMintFields") {
        test("constructs with required taxon") {
            val fields = NFTokenMintFields(nfTokenTaxon = 1u)
            fields.nfTokenTaxon shouldBe 1u
            fields.issuer shouldBe null
            fields.transferFee shouldBe null
            fields.uri shouldBe null
            fields.flags shouldBe null
        }

        test("equals and hashCode contract") {
            val a = NFTokenMintFields(nfTokenTaxon = 1u, uri = "https://example.com")
            val b = NFTokenMintFields(nfTokenTaxon = 1u, uri = "https://example.com")
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different taxon means not equal") {
            val a = NFTokenMintFields(nfTokenTaxon = 1u)
            val b = NFTokenMintFields(nfTokenTaxon = 2u)
            a shouldNotBe b
        }
    }

    context("NFTokenMintBuilder") {
        test("happy path - uses default taxon 0") {
            val builder = NFTokenMintBuilder()
            val fields = builder.build()
            fields.nfTokenTaxon shouldBe 0u
        }

        test("optional fields pass through") {
            val builder = NFTokenMintBuilder()
            builder.nfTokenTaxon = 1u
            builder.issuer = alice
            builder.transferFee = 5000u
            builder.uri = "https://example.com/nft/1"
            builder.flags = 8u
            val fields = builder.build()
            fields.nfTokenTaxon shouldBe 1u
            fields.issuer shouldBe alice
            fields.transferFee shouldBe 5000u
            fields.uri shouldBe "https://example.com/nft/1"
            fields.flags shouldBe 8u
        }
    }

    // ── NFTokenBurn ───────────────────────────────────────────────────────────

    context("NFTokenBurnFields") {
        test("constructs with nfTokenId") {
            val fields = NFTokenBurnFields(nfTokenId = nftId)
            fields.nfTokenId shouldBe nftId
        }

        test("equals and hashCode contract") {
            val a = NFTokenBurnFields(nfTokenId = nftId)
            val b = NFTokenBurnFields(nfTokenId = nftId)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different nfTokenId means not equal") {
            val a = NFTokenBurnFields(nfTokenId = "AAAA")
            val b = NFTokenBurnFields(nfTokenId = "BBBB")
            a shouldNotBe b
        }
    }

    context("NFTokenBurnBuilder") {
        test("happy path") {
            val builder = NFTokenBurnBuilder()
            builder.nfTokenId = nftId
            val fields = builder.build()
            fields.nfTokenId shouldBe nftId
        }

        test("throws when nfTokenId is missing") {
            val builder = NFTokenBurnBuilder()
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "nfTokenId is required"
        }
    }

    // ── NFTokenCreateOffer ────────────────────────────────────────────────────

    context("NFTokenCreateOfferFields") {
        test("constructs with required fields") {
            val fields = NFTokenCreateOfferFields(nfTokenId = nftId, amount = 10.xrp)
            fields.nfTokenId shouldBe nftId
            fields.amount shouldBe 10.xrp
            fields.destination shouldBe null
            fields.owner shouldBe null
            fields.expiration shouldBe null
            fields.flags shouldBe null
        }

        test("equals and hashCode contract") {
            val a = NFTokenCreateOfferFields(nfTokenId = nftId, amount = 10.xrp)
            val b = NFTokenCreateOfferFields(nfTokenId = nftId, amount = 10.xrp)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different amount means not equal") {
            val a = NFTokenCreateOfferFields(nfTokenId = nftId, amount = 10.xrp)
            val b = NFTokenCreateOfferFields(nfTokenId = nftId, amount = 20.xrp)
            a shouldNotBe b
        }
    }

    context("NFTokenCreateOfferBuilder") {
        test("happy path") {
            val builder = NFTokenCreateOfferBuilder()
            builder.nfTokenId = nftId
            builder.amount = 10.xrp
            val fields = builder.build()
            fields.nfTokenId shouldBe nftId
            fields.amount shouldBe 10.xrp
        }

        test("optional fields pass through") {
            val builder = NFTokenCreateOfferBuilder()
            builder.nfTokenId = nftId
            builder.amount = 10.xrp
            builder.destination = bob
            builder.owner = alice
            builder.expiration = 720000000u
            builder.flags = 1u
            val fields = builder.build()
            fields.destination shouldBe bob
            fields.owner shouldBe alice
            fields.expiration shouldBe 720000000u
            fields.flags shouldBe 1u
        }

        test("throws when nfTokenId is missing") {
            val builder = NFTokenCreateOfferBuilder()
            builder.amount = 10.xrp
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "nfTokenId is required"
        }

        test("throws when amount is missing") {
            val builder = NFTokenCreateOfferBuilder()
            builder.nfTokenId = nftId
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "amount is required"
        }
    }

    // ── NFTokenCancelOffer ────────────────────────────────────────────────────

    context("NFTokenCancelOfferFields") {
        test("constructs with offer list") {
            val offers = listOf("OFFER_ID_1", "OFFER_ID_2")
            val fields = NFTokenCancelOfferFields(nfTokenOffers = offers)
            fields.nfTokenOffers shouldBe offers
        }

        test("equals and hashCode contract") {
            val offers = listOf("OFFER_ID_1")
            val a = NFTokenCancelOfferFields(nfTokenOffers = offers)
            val b = NFTokenCancelOfferFields(nfTokenOffers = offers)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different offer lists means not equal") {
            val a = NFTokenCancelOfferFields(nfTokenOffers = listOf("A"))
            val b = NFTokenCancelOfferFields(nfTokenOffers = listOf("B"))
            a shouldNotBe b
        }

        test("toString includes class name") {
            val f = NFTokenCancelOfferFields(nfTokenOffers = listOf("OFFER_ID_1"))
            f.toString().contains("NFTokenCancelOfferFields") shouldBe true
        }
    }

    context("NFTokenCancelOfferBuilder") {
        test("happy path") {
            val builder = NFTokenCancelOfferBuilder()
            builder.nfTokenOffers = listOf("OFFER_ID_1", "OFFER_ID_2")
            val fields = builder.build()
            fields.nfTokenOffers shouldBe listOf("OFFER_ID_1", "OFFER_ID_2")
        }

        test("throws when nfTokenOffers is empty") {
            val builder = NFTokenCancelOfferBuilder()
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "nfTokenOffers must not be empty"
        }
    }

    // ── NFTokenAcceptOffer ────────────────────────────────────────────────────

    context("NFTokenAcceptOfferFields") {
        test("constructs with all optional fields") {
            val fields =
                NFTokenAcceptOfferFields(
                    nfTokenSellOffer = "SELL_OFFER_ID",
                    nfTokenBuyOffer = null,
                    nfTokenBrokerFee = null,
                )
            fields.nfTokenSellOffer shouldBe "SELL_OFFER_ID"
            fields.nfTokenBuyOffer shouldBe null
            fields.nfTokenBrokerFee shouldBe null
        }

        test("equals and hashCode contract") {
            val a = NFTokenAcceptOfferFields(nfTokenSellOffer = "SELL_OFFER_ID")
            val b = NFTokenAcceptOfferFields(nfTokenSellOffer = "SELL_OFFER_ID")
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different sell offer means not equal") {
            val a = NFTokenAcceptOfferFields(nfTokenSellOffer = "AAA")
            val b = NFTokenAcceptOfferFields(nfTokenSellOffer = "BBB")
            a shouldNotBe b
        }
    }

    context("NFTokenAcceptOfferBuilder") {
        test("happy path - sell offer only") {
            val builder = NFTokenAcceptOfferBuilder()
            builder.nfTokenSellOffer = "SELL_OFFER_ID"
            val fields = builder.build()
            fields.nfTokenSellOffer shouldBe "SELL_OFFER_ID"
            fields.nfTokenBuyOffer shouldBe null
        }

        test("broker fee passes through") {
            val builder = NFTokenAcceptOfferBuilder()
            builder.nfTokenSellOffer = "SELL_OFFER_ID"
            builder.nfTokenBuyOffer = "BUY_OFFER_ID"
            builder.nfTokenBrokerFee = 1.xrp
            val fields = builder.build()
            fields.nfTokenBuyOffer shouldBe "BUY_OFFER_ID"
            fields.nfTokenBrokerFee shouldBe 1.xrp
        }

        test("empty builder builds without throwing (all fields optional)") {
            val builder = NFTokenAcceptOfferBuilder()
            val fields = builder.build()
            fields.nfTokenSellOffer shouldBe null
            fields.nfTokenBuyOffer shouldBe null
            fields.nfTokenBrokerFee shouldBe null
        }
    }

    // ── NFTokenModify ─────────────────────────────────────────────────────────

    context("NFTokenModifyFields") {
        test("constructs with required nfTokenId") {
            val fields = NFTokenModifyFields(nfTokenId = nftId)
            fields.nfTokenId shouldBe nftId
            fields.owner shouldBe null
            fields.uri shouldBe null
        }

        test("equals and hashCode contract") {
            val a = NFTokenModifyFields(nfTokenId = nftId, uri = "https://example.com")
            val b = NFTokenModifyFields(nfTokenId = nftId, uri = "https://example.com")
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different uri means not equal") {
            val a = NFTokenModifyFields(nfTokenId = nftId, uri = "https://a.com")
            val b = NFTokenModifyFields(nfTokenId = nftId, uri = "https://b.com")
            a shouldNotBe b
        }
    }

    context("NFTokenModifyBuilder") {
        test("happy path") {
            val builder = NFTokenModifyBuilder()
            builder.nfTokenId = nftId
            val fields = builder.build()
            fields.nfTokenId shouldBe nftId
        }

        test("optional owner and uri pass through") {
            val builder = NFTokenModifyBuilder()
            builder.nfTokenId = nftId
            builder.owner = alice
            builder.uri = "https://example.com/nft/1"
            val fields = builder.build()
            fields.owner shouldBe alice
            fields.uri shouldBe "https://example.com/nft/1"
        }

        test("throws when nfTokenId is missing") {
            val builder = NFTokenModifyBuilder()
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "nfTokenId is required"
        }
    }

    // ── DepositPreauth ────────────────────────────────────────────────────────

    context("DepositPreauthFields") {
        test("constructs with authorize address") {
            val fields = DepositPreauthFields(authorize = alice)
            fields.authorize shouldBe alice
            fields.unauthorize shouldBe null
        }

        test("constructs with unauthorize address") {
            val fields = DepositPreauthFields(unauthorize = alice)
            fields.unauthorize shouldBe alice
            fields.authorize shouldBe null
        }

        test("equals and hashCode contract") {
            val a = DepositPreauthFields(authorize = alice)
            val b = DepositPreauthFields(authorize = alice)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("authorize vs unauthorize means not equal") {
            val a = DepositPreauthFields(authorize = alice)
            val b = DepositPreauthFields(unauthorize = alice)
            a shouldNotBe b
        }

        test("toString includes class name") {
            val f = DepositPreauthFields(authorize = alice)
            f.toString().contains("DepositPreauthFields") shouldBe true
        }
    }

    context("DepositPreauthBuilder") {
        test("happy path - authorize") {
            val builder = DepositPreauthBuilder()
            builder.authorize = alice
            val fields = builder.build()
            fields.authorize shouldBe alice
            fields.unauthorize shouldBe null
        }

        test("happy path - unauthorize") {
            val builder = DepositPreauthBuilder()
            builder.unauthorize = bob
            val fields = builder.build()
            fields.unauthorize shouldBe bob
            fields.authorize shouldBe null
        }

        test("builder with no fields set produces empty DepositPreauthFields") {
            val builder = DepositPreauthBuilder()
            val fields = builder.build()
            fields.authorize shouldBe null
            fields.unauthorize shouldBe null
        }
    }

    // ── Clawback ──────────────────────────────────────────────────────────────

    context("ClawbackFields") {
        test("constructs with required amount") {
            val fields = ClawbackFields(amount = usdAmount)
            fields.amount shouldBe usdAmount
            fields.holder shouldBe null
        }

        test("equals and hashCode contract") {
            val a = ClawbackFields(amount = usdAmount, holder = alice)
            val b = ClawbackFields(amount = usdAmount, holder = alice)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different holder means not equal") {
            val a = ClawbackFields(amount = usdAmount, holder = alice)
            val b = ClawbackFields(amount = usdAmount, holder = bob)
            a shouldNotBe b
        }

        test("toString includes class name") {
            val f = ClawbackFields(amount = usdAmount)
            f.toString().contains("ClawbackFields") shouldBe true
        }
    }

    context("ClawbackBuilder") {
        test("happy path") {
            val builder = ClawbackBuilder()
            builder.amount = usdAmount
            val fields = builder.build()
            fields.amount shouldBe usdAmount
        }

        test("optional holder passes through") {
            val builder = ClawbackBuilder()
            builder.amount = usdAmount
            builder.holder = alice
            val fields = builder.build()
            fields.holder shouldBe alice
        }

        test("throws when amount is missing") {
            val builder = ClawbackBuilder()
            shouldThrow<IllegalArgumentException> { builder.build() }
                .message shouldBe "amount is required"
        }
    }
})
