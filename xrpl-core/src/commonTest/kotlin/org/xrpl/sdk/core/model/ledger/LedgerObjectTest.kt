@file:OptIn(ExperimentalXrplApi::class)

package org.xrpl.sdk.core.model.ledger

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.XrpDrops

class LedgerObjectTest : FunSpec({

    // Reusable test constants
    val testHash = Hash256("A".repeat(64))
    val testHash2 = Hash256("B".repeat(64))
    val testAddress = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val testAddress2 = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")

    context("AccountRoot") {
        test("construction with full fields") {
            val root =
                AccountRoot(
                    index = testHash,
                    account = testAddress,
                    balance = XrpDrops(100_000_000L),
                    sequence = 42u,
                    ownerCount = 3u,
                    previousTxnID = testHash2,
                    previousTxnLgrSeq = 1000u,
                    flags = 0u,
                    domain = "6578616D706C652E636F6D",
                    regularKey = testAddress2,
                    ticketCount = 2u,
                    transferRate = 1_005_000_000u,
                )
            root.ledgerObjectType shouldBe LedgerObjectType.AccountRoot
            root.account shouldBe testAddress
            root.balance shouldBe XrpDrops(100_000_000L)
            root.sequence shouldBe 42u
            root.ownerCount shouldBe 3u
            root.previousTxnID shouldBe testHash2
            root.previousTxnLgrSeq shouldBe 1000u
            root.domain shouldBe "6578616D706C652E636F6D"
            root.regularKey shouldBe testAddress2
            root.ticketCount shouldBe 2u
            root.transferRate shouldBe 1_005_000_000u
        }

        test("optional fields default to null") {
            val root =
                AccountRoot(
                    index = testHash,
                    account = testAddress,
                    balance = XrpDrops(0L),
                    sequence = 1u,
                    ownerCount = 0u,
                    previousTxnID = testHash2,
                    previousTxnLgrSeq = 1u,
                )
            root.accountTxnID shouldBe null
            root.domain shouldBe null
            root.emailHash shouldBe null
            root.messageKey shouldBe null
            root.regularKey shouldBe null
            root.ticketCount shouldBe null
            root.tickSize shouldBe null
            root.transferRate shouldBe null
            root.nfTokenMinter shouldBe null
            root.ammID shouldBe null
        }

        test("equals and hashCode") {
            val a =
                AccountRoot(
                    index = testHash,
                    account = testAddress,
                    balance = XrpDrops(100L),
                    sequence = 1u,
                    ownerCount = 0u,
                    previousTxnID = testHash2,
                    previousTxnLgrSeq = 1u,
                )
            val b =
                AccountRoot(
                    index = testHash,
                    account = testAddress,
                    balance = XrpDrops(100L),
                    sequence = 1u,
                    ownerCount = 0u,
                    previousTxnID = testHash2,
                    previousTxnLgrSeq = 1u,
                )
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different balance means not equal") {
            val a =
                AccountRoot(
                    index = testHash,
                    account = testAddress,
                    balance = XrpDrops(100L),
                    sequence = 1u,
                    ownerCount = 0u,
                    previousTxnID = testHash2,
                    previousTxnLgrSeq = 1u,
                )
            val b =
                AccountRoot(
                    index = testHash,
                    account = testAddress,
                    balance = XrpDrops(200L),
                    sequence = 1u,
                    ownerCount = 0u,
                    previousTxnID = testHash2,
                    previousTxnLgrSeq = 1u,
                )
            a shouldNotBe b
        }

        test("toString contains account and balance") {
            val root =
                AccountRoot(
                    index = testHash,
                    account = testAddress,
                    balance = XrpDrops(1_000_000L),
                    sequence = 1u,
                    ownerCount = 0u,
                    previousTxnID = testHash2,
                    previousTxnLgrSeq = 1u,
                )
            val str = root.toString()
            (str.contains("AccountRoot")) shouldBe true
            (str.contains(testAddress.value)) shouldBe true
        }
    }

    context("RippleState") {
        val usdCode = CurrencyCode("USD")

        test("construction with full fields") {
            val state =
                RippleState(
                    index = testHash,
                    balance = IssuedAmount(currency = usdCode, issuer = testAddress, value = "100"),
                    lowLimit = IssuedAmount(currency = usdCode, issuer = testAddress, value = "1000"),
                    highLimit = IssuedAmount(currency = usdCode, issuer = testAddress2, value = "500"),
                    flags = 0x20000u,
                    lowQualityIn = 1_000_000u,
                    lowQualityOut = 1_000_000u,
                )
            state.ledgerObjectType shouldBe LedgerObjectType.RippleState
            state.balance.value shouldBe "100"
            state.lowLimit.value shouldBe "1000"
            state.highLimit.value shouldBe "500"
            state.flags shouldBe 0x20000u
            state.lowQualityIn shouldBe 1_000_000u
        }

        test("equals and hashCode") {
            val a =
                RippleState(
                    index = testHash,
                    balance = IssuedAmount(currency = usdCode, issuer = testAddress, value = "50"),
                    lowLimit = IssuedAmount(currency = usdCode, issuer = testAddress, value = "1000"),
                    highLimit = IssuedAmount(currency = usdCode, issuer = testAddress2, value = "500"),
                )
            val b =
                RippleState(
                    index = testHash,
                    balance = IssuedAmount(currency = usdCode, issuer = testAddress, value = "50"),
                    lowLimit = IssuedAmount(currency = usdCode, issuer = testAddress, value = "1000"),
                    highLimit = IssuedAmount(currency = usdCode, issuer = testAddress2, value = "500"),
                )
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }
    }

    context("Offer") {
        test("construction with full fields") {
            val offer =
                Offer(
                    index = testHash,
                    account = testAddress,
                    takerGets = XrpAmount(XrpDrops(1_000_000L)),
                    takerPays =
                        IssuedAmount(
                            currency = CurrencyCode("USD"),
                            issuer = testAddress2,
                            value = "10",
                        ),
                    sequence = 5u,
                    flags = 0x00020000u,
                    bookDirectory = testHash2,
                    bookNode = "0",
                    ownerNode = "0",
                    expiration = 700000000u,
                )
            offer.ledgerObjectType shouldBe LedgerObjectType.Offer
            offer.account shouldBe testAddress
            offer.sequence shouldBe 5u
            offer.flags shouldBe 0x00020000u
            offer.bookDirectory shouldBe testHash2
            offer.expiration shouldBe 700000000u
        }

        test("equals and hashCode") {
            val a =
                Offer(
                    index = testHash,
                    account = testAddress,
                    takerGets = XrpAmount(XrpDrops(100L)),
                    takerPays = XrpAmount(XrpDrops(200L)),
                    sequence = 1u,
                )
            val b =
                Offer(
                    index = testHash,
                    account = testAddress,
                    takerGets = XrpAmount(XrpDrops(100L)),
                    takerPays = XrpAmount(XrpDrops(200L)),
                    sequence = 1u,
                )
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }
    }

    context("DirectoryNode") {
        test("construction") {
            val dir =
                DirectoryNode(
                    index = testHash,
                    rootIndex = testHash2,
                    indexes = listOf(testHash, testHash2),
                    owner = testAddress,
                )
            dir.ledgerObjectType shouldBe LedgerObjectType.DirectoryNode
            dir.rootIndex shouldBe testHash2
            dir.indexes.size shouldBe 2
            dir.owner shouldBe testAddress
        }
    }

    context("UnknownLedgerObject") {
        test("holds arbitrary fields") {
            val futureType = LedgerObjectType("FutureThing")
            val obj =
                UnknownLedgerObject(
                    ledgerObjectType = futureType,
                    index = testHash,
                    fields =
                        mapOf(
                            "foo" to "bar",
                            "count" to 42,
                            "nested" to null,
                        ),
                )
            obj.ledgerObjectType shouldBe futureType
            obj.index shouldBe testHash
            obj.fields["foo"] shouldBe "bar"
            obj.fields["count"] shouldBe 42
            obj.fields["nested"] shouldBe null
            obj.fields.size shouldBe 3
        }

        test("equals and hashCode") {
            val futureType = LedgerObjectType("FutureThing")
            val fields = mapOf("x" to 1)
            val a = UnknownLedgerObject(futureType, testHash, fields)
            val b = UnknownLedgerObject(futureType, testHash, fields)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different fields means not equal") {
            val futureType = LedgerObjectType("FutureThing")
            val a = UnknownLedgerObject(futureType, testHash, mapOf("x" to 1))
            val b = UnknownLedgerObject(futureType, testHash, mapOf("x" to 2))
            a shouldNotBe b
        }
    }

    context("sealed interface exhaustive when") {
        test("when expression covers known subtypes with else") {
            val objects: List<LedgerObject> =
                listOf(
                    AccountRoot(
                        index = testHash,
                        account = testAddress,
                        balance = XrpDrops(0L),
                        sequence = 1u,
                        ownerCount = 0u,
                        previousTxnID = testHash2,
                        previousTxnLgrSeq = 1u,
                    ),
                    RippleState(
                        index = testHash,
                        balance =
                            IssuedAmount(
                                currency = CurrencyCode("USD"),
                                issuer = testAddress,
                                value = "0",
                            ),
                        lowLimit =
                            IssuedAmount(
                                currency = CurrencyCode("USD"),
                                issuer = testAddress,
                                value = "0",
                            ),
                        highLimit =
                            IssuedAmount(
                                currency = CurrencyCode("USD"),
                                issuer = testAddress2,
                                value = "0",
                            ),
                    ),
                    Offer(
                        index = testHash,
                        account = testAddress,
                        takerGets = XrpAmount(XrpDrops(0L)),
                        takerPays = XrpAmount(XrpDrops(0L)),
                        sequence = 1u,
                    ),
                    UnknownLedgerObject(
                        ledgerObjectType = LedgerObjectType("Unknown"),
                        index = testHash,
                        fields = emptyMap(),
                    ),
                )
            val labels =
                objects.map { obj ->
                    when (obj) {
                        is AccountRoot -> "account-root"
                        is RippleState -> "ripple-state"
                        is Offer -> "offer"
                        is UnknownLedgerObject -> "unknown"
                        else -> "other"
                    }
                }
            labels shouldBe listOf("account-root", "ripple-state", "offer", "unknown")
        }
    }
})
