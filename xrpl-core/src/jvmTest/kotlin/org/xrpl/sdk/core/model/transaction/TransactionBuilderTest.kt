package org.xrpl.sdk.core.model.transaction

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode

class TransactionBuilderTest : FunSpec({

    val sender = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val receiver = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    val usdIssuer = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    val usdAmount =
        IssuedAmount(
            currency = CurrencyCode("USD"),
            issuer = usdIssuer,
            value = "5",
        )

    // ── OfferCreate ───────────────────────────────────────────────────────────

    context("offerCreate builder") {
        test("happy path - all required fields") {
            val tx =
                offerCreate {
                    account = sender
                    takerGets = 10.xrp
                    takerPays = usdAmount
                }
            tx.transactionType shouldBe TransactionType.OfferCreate
            tx.account shouldBe sender
            val fields = tx.fields as OfferCreateFields
            fields.takerGets shouldBe 10.xrp
            fields.takerPays shouldBe usdAmount
            fields.expiration shouldBe null
            fields.offerSequence shouldBe null
        }

        test("optional fields pass through") {
            val tx =
                offerCreate {
                    account = sender
                    takerGets = 10.xrp
                    takerPays = usdAmount
                    expiration = 720000000u
                    offerSequence = 42u
                    sourceTag = 7u
                }
            val fields = tx.fields as OfferCreateFields
            fields.expiration shouldBe 720000000u
            fields.offerSequence shouldBe 42u
            tx.sourceTag shouldBe 7u
        }

        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                offerCreate {
                    takerGets = 10.xrp
                    takerPays = usdAmount
                }
            }.message shouldBe "account is required for OfferCreate"
        }

        test("throws when takerGets is missing") {
            shouldThrow<IllegalArgumentException> {
                offerCreate {
                    account = sender
                    takerPays = usdAmount
                }
            }.message shouldBe "takerGets is required for OfferCreate"
        }

        test("throws when takerPays is missing") {
            shouldThrow<IllegalArgumentException> {
                offerCreate {
                    account = sender
                    takerGets = 10.xrp
                }
            }.message shouldBe "takerPays is required for OfferCreate"
        }
    }

    // ── OfferCancel ───────────────────────────────────────────────────────────

    context("offerCancel builder") {
        test("happy path - all required fields") {
            val tx =
                offerCancel {
                    account = sender
                    offerSequence = 100u
                }
            tx.transactionType shouldBe TransactionType.OfferCancel
            tx.account shouldBe sender
            val fields = tx.fields as OfferCancelFields
            fields.offerSequence shouldBe 100u
        }

        test("optional sourceTag passes through") {
            val tx =
                offerCancel {
                    account = sender
                    offerSequence = 100u
                    sourceTag = 3u
                }
            tx.sourceTag shouldBe 3u
        }

        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                offerCancel {
                    offerSequence = 100u
                }
            }.message shouldBe "account is required for OfferCancel"
        }

        test("throws when offerSequence is missing") {
            shouldThrow<IllegalArgumentException> {
                offerCancel {
                    account = sender
                }
            }.message shouldBe "offerSequence is required for OfferCancel"
        }
    }

    // ── TrustSet ──────────────────────────────────────────────────────────────

    context("trustSet builder") {
        val usdLimit =
            IssuedAmount(
                currency = CurrencyCode("USD"),
                issuer = usdIssuer,
                value = "100",
            )

        test("happy path - all required fields") {
            val tx =
                trustSet {
                    account = sender
                    limitAmount = usdLimit
                }
            tx.transactionType shouldBe TransactionType.TrustSet
            tx.account shouldBe sender
            val fields = tx.fields as TrustSetFields
            fields.limitAmount shouldBe usdLimit
            fields.qualityIn shouldBe null
            fields.qualityOut shouldBe null
        }

        test("optional qualityIn and qualityOut pass through") {
            val tx =
                trustSet {
                    account = sender
                    limitAmount = usdLimit
                    qualityIn = 1000000u
                    qualityOut = 2000000u
                }
            val fields = tx.fields as TrustSetFields
            fields.qualityIn shouldBe 1000000u
            fields.qualityOut shouldBe 2000000u
        }

        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                trustSet {
                    limitAmount = usdLimit
                }
            }.message shouldBe "account is required for TrustSet"
        }

        test("throws when limitAmount is missing") {
            shouldThrow<IllegalArgumentException> {
                trustSet {
                    account = sender
                }
            }.message shouldBe "limitAmount is required for TrustSet"
        }
    }

    // ── AccountSet ────────────────────────────────────────────────────────────

    context("accountSet builder") {
        test("happy path - only account required") {
            val tx =
                accountSet {
                    account = sender
                }
            tx.transactionType shouldBe TransactionType.AccountSet
            tx.account shouldBe sender
            val fields = tx.fields as AccountSetFields
            fields.clearFlag shouldBe null
            fields.setFlag shouldBe null
            fields.domain shouldBe null
            fields.emailHash shouldBe null
            fields.transferRate shouldBe null
            fields.tickSize shouldBe null
            fields.nftTokenMinter shouldBe null
        }

        test("optional fields pass through") {
            val tx =
                accountSet {
                    account = sender
                    clearFlag = 1u
                    setFlag = 2u
                    domain = "6578616D706C652E636F6D"
                    emailHash = "AABBCCDDEEFF"
                    transferRate = 1002000000u
                    tickSize = 5u
                    nftTokenMinter = receiver
                }
            val fields = tx.fields as AccountSetFields
            fields.clearFlag shouldBe 1u
            fields.setFlag shouldBe 2u
            fields.domain shouldBe "6578616D706C652E636F6D"
            fields.emailHash shouldBe "AABBCCDDEEFF"
            fields.transferRate shouldBe 1002000000u
            fields.tickSize shouldBe 5u
            fields.nftTokenMinter shouldBe receiver
        }

        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                accountSet {
                    setFlag = 1u
                }
            }.message shouldBe "account is required for AccountSet"
        }
    }

    // ── AccountDelete ─────────────────────────────────────────────────────────

    context("accountDelete builder") {
        test("happy path - all required fields") {
            val tx =
                accountDelete {
                    account = sender
                    destination = receiver
                }
            tx.transactionType shouldBe TransactionType.AccountDelete
            tx.account shouldBe sender
            val fields = tx.fields as AccountDeleteFields
            fields.destination shouldBe receiver
            fields.destinationTag shouldBe null
        }

        test("optional destinationTag passes through") {
            val tx =
                accountDelete {
                    account = sender
                    destination = receiver
                    destinationTag = 99u
                }
            val fields = tx.fields as AccountDeleteFields
            fields.destinationTag shouldBe 99u
        }

        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                accountDelete {
                    destination = receiver
                }
            }.message shouldBe "account is required for AccountDelete"
        }

        test("throws when destination is missing") {
            shouldThrow<IllegalArgumentException> {
                accountDelete {
                    account = sender
                }
            }.message shouldBe "destination is required for AccountDelete"
        }
    }

    // ── SetRegularKey ─────────────────────────────────────────────────────────

    context("setRegularKey builder") {
        test("happy path - account required, regularKey optional") {
            val tx =
                setRegularKey {
                    account = sender
                    regularKey = receiver
                }
            tx.transactionType shouldBe TransactionType.SetRegularKey
            tx.account shouldBe sender
            val fields = tx.fields as SetRegularKeyFields
            fields.regularKey shouldBe receiver
        }

        test("happy path - null regularKey removes the key") {
            val tx =
                setRegularKey {
                    account = sender
                }
            val fields = tx.fields as SetRegularKeyFields
            fields.regularKey shouldBe null
        }

        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                setRegularKey {
                    regularKey = receiver
                }
            }.message shouldBe "account is required for SetRegularKey"
        }
    }

    // ── SignerListSet ─────────────────────────────────────────────────────────

    context("signerListSet builder") {
        val entries =
            listOf(
                SignerEntry(account = receiver, signerWeight = 1u),
            )

        test("happy path - all required fields") {
            val tx =
                signerListSet {
                    account = sender
                    signerQuorum = 1u
                    signerEntries = entries
                }
            tx.transactionType shouldBe TransactionType.SignerListSet
            tx.account shouldBe sender
            val fields = tx.fields as SignerListSetFields
            fields.signerQuorum shouldBe 1u
            fields.signerEntries shouldBe entries
        }

        test("empty signerEntries is valid (deletes the signer list)") {
            val tx =
                signerListSet {
                    account = sender
                    signerQuorum = 0u
                    signerEntries = emptyList()
                }
            val fields = tx.fields as SignerListSetFields
            fields.signerEntries shouldBe emptyList()
        }

        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                signerListSet {
                    signerQuorum = 1u
                    signerEntries = entries
                }
            }.message shouldBe "account is required for SignerListSet"
        }

        test("throws when signerQuorum is missing") {
            shouldThrow<IllegalArgumentException> {
                signerListSet {
                    account = sender
                    signerEntries = entries
                }
            }.message shouldBe "signerQuorum is required for SignerListSet"
        }

        test("optional sourceTag and memos pass through") {
            val memo = Memo(memoData = "AABB")
            val tx =
                signerListSet {
                    account = sender
                    signerQuorum = 1u
                    signerEntries = entries
                    sourceTag = 55u
                    memos = listOf(memo)
                }
            tx.sourceTag shouldBe 55u
            tx.memos shouldBe listOf(memo)
        }
    }

    // ── SignerEntry ───────────────────────────────────────────────────────────

    context("SignerEntry") {
        test("constructs and exposes fields") {
            val entry = SignerEntry(account = receiver, signerWeight = 2u)
            entry.account shouldBe receiver
            entry.signerWeight shouldBe 2u
        }

        test("equals and hashCode contract") {
            val a = SignerEntry(account = receiver, signerWeight = 1u)
            val b = SignerEntry(account = receiver, signerWeight = 1u)
            a shouldBe b
            a.hashCode() shouldBe b.hashCode()
        }

        test("different signerWeight means not equal") {
            val a = SignerEntry(account = receiver, signerWeight = 1u)
            val b = SignerEntry(account = receiver, signerWeight = 2u)
            a shouldNotBe b
        }

        test("toString includes account and signerWeight") {
            val entry = SignerEntry(account = receiver, signerWeight = 1u)
            val s = entry.toString()
            s.contains("SignerEntry") shouldBe true
            s.contains("signerWeight=1") shouldBe true
        }
    }
})
