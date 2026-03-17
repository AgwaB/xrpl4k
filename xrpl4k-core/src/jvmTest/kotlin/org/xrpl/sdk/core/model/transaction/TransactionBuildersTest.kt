package org.xrpl.sdk.core.model.transaction

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.core.type.XrpDrops

class TransactionBuildersTest : FunSpec({

    val sender = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val receiver = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    val xrpAmount = XrpAmount(XrpDrops(1_000_000L))
    val usdIssuer = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    val usdAmount =
        IssuedAmount(
            currency = CurrencyCode("USD"),
            issuer = usdIssuer,
            value = "100",
        )

    // ── Payment: account parameter overload ──────────────────────────────────

    context("payment(account) overload") {
        test("sets account from parameter") {
            val tx =
                payment(sender) {
                    destination = receiver
                    amount = xrpAmount
                }
            tx.transactionType shouldBe TransactionType.Payment
            tx.account shouldBe sender
            val fields = tx.fields as PaymentFields
            fields.destination shouldBe receiver
            fields.amount shouldBe xrpAmount
        }

        test("account parameter wins over block assignment") {
            val other = Address("rN7n3473SaZBCG4dFL83w7p1W9cgZB6ZBR")
            val tx =
                payment(sender) {
                    account = other
                    destination = receiver
                    amount = xrpAmount
                }
            tx.account shouldBe sender
        }
    }

    // ── Payment: flags propagation ───────────────────────────────────────────

    context("payment flags") {
        test("flags propagated to Unsigned") {
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                    flags = TransactionFlags.Payment.tfPartialPayment
                }
            tx.flags shouldBe TransactionFlags.Payment.tfPartialPayment
        }

        test("combined flags propagated") {
            val combined =
                TransactionFlags.Payment.tfPartialPayment or
                    TransactionFlags.Payment.tfNoDirectRipple
            val tx =
                payment(sender) {
                    destination = receiver
                    amount = xrpAmount
                    flags = combined
                }
            tx.flags shouldBe combined
        }

        test("null flags by default") {
            val tx =
                payment(sender) {
                    destination = receiver
                    amount = xrpAmount
                }
            tx.flags shouldBe null
        }
    }

    // ── TrustSet: account overload + flags ───────────────────────────────────

    context("trustSet(account) overload") {
        test("sets account from parameter") {
            val tx =
                trustSet(sender) {
                    limitAmount = usdAmount
                }
            tx.transactionType shouldBe TransactionType.TrustSet
            tx.account shouldBe sender
            val fields = tx.fields as TrustSetFields
            fields.limitAmount shouldBe usdAmount
        }
    }

    context("trustSet flags") {
        test("flags propagated to Unsigned") {
            val tx =
                trustSet(sender) {
                    limitAmount = usdAmount
                    flags = TransactionFlags.TrustSet.tfSetNoRipple
                }
            tx.flags shouldBe TransactionFlags.TrustSet.tfSetNoRipple
        }
    }

    // ── OfferCreate: account overload + flags ────────────────────────────────

    context("offerCreate(account) overload") {
        test("sets account from parameter") {
            val tx =
                offerCreate(sender) {
                    takerGets = 10.xrp
                    takerPays = usdAmount
                }
            tx.transactionType shouldBe TransactionType.OfferCreate
            tx.account shouldBe sender
        }
    }

    context("offerCreate flags") {
        test("flags propagated to Unsigned") {
            val tx =
                offerCreate(sender) {
                    takerGets = 10.xrp
                    takerPays = usdAmount
                    flags = TransactionFlags.OfferCreate.tfImmediateOrCancel
                }
            tx.flags shouldBe TransactionFlags.OfferCreate.tfImmediateOrCancel
        }
    }

    // ── OfferCancel: account overload ────────────────────────────────────────

    context("offerCancel(account) overload") {
        test("sets account from parameter") {
            val tx =
                offerCancel(sender) {
                    offerSequence = 42u
                }
            tx.transactionType shouldBe TransactionType.OfferCancel
            tx.account shouldBe sender
            val fields = tx.fields as OfferCancelFields
            fields.offerSequence shouldBe 42u
        }
    }

    // ── AccountSet: account overload + AccountSetFlag ────────────────────────

    context("accountSet(account) overload") {
        test("sets account from parameter") {
            val tx =
                accountSet(sender) {
                    setFlag = AccountSetFlag.asfRequireDest
                }
            tx.transactionType shouldBe TransactionType.AccountSet
            tx.account shouldBe sender
            val fields = tx.fields as AccountSetFields
            fields.setFlag shouldBe AccountSetFlag.asfRequireDest
        }

        test("clearFlag propagated") {
            val tx =
                accountSet(sender) {
                    clearFlag = AccountSetFlag.asfDisallowXRP
                }
            val fields = tx.fields as AccountSetFields
            fields.clearFlag shouldBe AccountSetFlag.asfDisallowXRP
        }

        test("optional fields default to null") {
            val tx = accountSet(sender) {}
            val fields = tx.fields as AccountSetFields
            fields.clearFlag shouldBe null
            fields.setFlag shouldBe null
            fields.domain shouldBe null
            fields.emailHash shouldBe null
            fields.transferRate shouldBe null
            fields.tickSize shouldBe null
            fields.nftTokenMinter shouldBe null
        }
    }

    // ── SignerListSet: account overload ───────────────────────────────────────

    context("signerListSet(account) overload") {
        val entries =
            listOf(
                SignerEntry(account = receiver, signerWeight = 1u),
            )

        test("sets account from parameter") {
            val tx =
                signerListSet(sender) {
                    signerQuorum = 1u
                    signerEntries = entries
                }
            tx.transactionType shouldBe TransactionType.SignerListSet
            tx.account shouldBe sender
            val fields = tx.fields as SignerListSetFields
            fields.signerQuorum shouldBe 1u
            fields.signerEntries shouldBe entries
        }
    }

    // ── EscrowCreate ─────────────────────────────────────────────────────────

    context("escrowCreate builder") {
        test("happy path - required fields") {
            val tx =
                escrowCreate(sender) {
                    destination = receiver
                    amount = xrpAmount
                }
            tx.transactionType shouldBe TransactionType.EscrowCreate
            tx.account shouldBe sender
            val fields = tx.fields as EscrowCreateFields
            fields.destination shouldBe receiver
            fields.amount shouldBe xrpAmount
            fields.finishAfter shouldBe null
            fields.cancelAfter shouldBe null
            fields.condition shouldBe null
            fields.destinationTag shouldBe null
        }

        test("optional fields pass through") {
            val tx =
                escrowCreate(sender) {
                    destination = receiver
                    amount = xrpAmount
                    finishAfter = 533257958u
                    cancelAfter = 533344358u
                    condition = "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"
                    destinationTag = 42u
                }
            val fields = tx.fields as EscrowCreateFields
            fields.finishAfter shouldBe 533257958u
            fields.cancelAfter shouldBe 533344358u
            fields.condition shouldBe "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"
            fields.destinationTag shouldBe 42u
        }

        test("throws when destination is missing") {
            shouldThrow<IllegalArgumentException> {
                escrowCreate(sender) {
                    amount = xrpAmount
                }
            }.message shouldBe "destination is required"
        }

        test("throws when amount is missing") {
            shouldThrow<IllegalArgumentException> {
                escrowCreate(sender) {
                    destination = receiver
                }
            }.message shouldBe "amount is required"
        }
    }

    // ── EscrowFinish ─────────────────────────────────────────────────────────

    context("escrowFinish builder") {
        test("happy path - required fields") {
            val tx =
                escrowFinish(sender) {
                    owner = receiver
                    offerSequence = 7u
                }
            tx.transactionType shouldBe TransactionType.EscrowFinish
            tx.account shouldBe sender
            val fields = tx.fields as EscrowFinishFields
            fields.owner shouldBe receiver
            fields.offerSequence shouldBe 7u
            fields.condition shouldBe null
            fields.fulfillment shouldBe null
        }

        test("optional condition and fulfillment pass through") {
            val tx =
                escrowFinish(sender) {
                    owner = receiver
                    offerSequence = 7u
                    condition = "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"
                    fulfillment = "A0028000"
                }
            val fields = tx.fields as EscrowFinishFields
            fields.condition shouldBe "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"
            fields.fulfillment shouldBe "A0028000"
        }

        test("throws when owner is missing") {
            shouldThrow<IllegalArgumentException> {
                escrowFinish(sender) {
                    offerSequence = 7u
                }
            }.message shouldBe "owner is required"
        }
    }

    // ── EscrowCancel ─────────────────────────────────────────────────────────

    context("escrowCancel builder") {
        test("happy path - required fields") {
            val tx =
                escrowCancel(sender) {
                    owner = receiver
                    offerSequence = 7u
                }
            tx.transactionType shouldBe TransactionType.EscrowCancel
            tx.account shouldBe sender
            val fields = tx.fields as EscrowCancelFields
            fields.owner shouldBe receiver
            fields.offerSequence shouldBe 7u
        }

        test("throws when owner is missing") {
            shouldThrow<IllegalArgumentException> {
                escrowCancel(sender) {
                    offerSequence = 7u
                }
            }.message shouldBe "owner is required"
        }
    }

    // ── CheckCreate ──────────────────────────────────────────────────────────

    context("checkCreate builder") {
        test("happy path - required fields") {
            val tx =
                checkCreate(sender) {
                    destination = receiver
                    sendMax = xrpAmount
                }
            tx.transactionType shouldBe TransactionType.CheckCreate
            tx.account shouldBe sender
            val fields = tx.fields as CheckCreateFields
            fields.destination shouldBe receiver
            fields.sendMax shouldBe xrpAmount
            fields.destinationTag shouldBe null
            fields.expiration shouldBe null
            fields.invoiceId shouldBe null
        }

        test("optional fields pass through") {
            val tx =
                checkCreate(sender) {
                    destination = receiver
                    sendMax = xrpAmount
                    destinationTag = 1u
                    expiration = 570113521u
                    invoiceId = "46060241FABCF692D4D934BA2A6C4427CD4279083E38C77CBE642243E43BE291"
                }
            val fields = tx.fields as CheckCreateFields
            fields.destinationTag shouldBe 1u
            fields.expiration shouldBe 570113521u
            fields.invoiceId shouldBe "46060241FABCF692D4D934BA2A6C4427CD4279083E38C77CBE642243E43BE291"
        }

        test("throws when destination is missing") {
            shouldThrow<IllegalArgumentException> {
                checkCreate(sender) {
                    sendMax = xrpAmount
                }
            }.message shouldBe "destination is required"
        }

        test("throws when sendMax is missing") {
            shouldThrow<IllegalArgumentException> {
                checkCreate(sender) {
                    destination = receiver
                }
            }.message shouldBe "sendMax is required"
        }
    }

    // ── CheckCash ────────────────────────────────────────────────────────────

    context("checkCash builder") {
        val checkHash = "838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"

        test("happy path - with amount") {
            val tx =
                checkCash(sender) {
                    checkId = checkHash
                    amount = xrpAmount
                }
            tx.transactionType shouldBe TransactionType.CheckCash
            tx.account shouldBe sender
            val fields = tx.fields as CheckCashFields
            fields.checkId shouldBe checkHash
            fields.amount shouldBe xrpAmount
            fields.deliverMin shouldBe null
        }

        test("happy path - with deliverMin") {
            val tx =
                checkCash(sender) {
                    checkId = checkHash
                    deliverMin = xrpAmount
                }
            val fields = tx.fields as CheckCashFields
            fields.amount shouldBe null
            fields.deliverMin shouldBe xrpAmount
        }

        test("throws when checkId is missing") {
            shouldThrow<IllegalArgumentException> {
                checkCash(sender) {
                    amount = xrpAmount
                }
            }.message shouldBe "checkId is required"
        }
    }

    // ── CheckCancel ──────────────────────────────────────────────────────────

    context("checkCancel builder") {
        val checkHash = "838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"

        test("happy path") {
            val tx =
                checkCancel(sender) {
                    checkId = checkHash
                }
            tx.transactionType shouldBe TransactionType.CheckCancel
            tx.account shouldBe sender
            val fields = tx.fields as CheckCancelFields
            fields.checkId shouldBe checkHash
        }

        test("throws when checkId is missing") {
            shouldThrow<IllegalArgumentException> {
                checkCancel(sender) {
                    // no checkId
                }
            }.message shouldBe "checkId is required"
        }
    }

    // ── NFTokenMint ──────────────────────────────────────────────────────────

    context("nfTokenMint builder") {
        test("happy path - required fields") {
            val tx =
                nfTokenMint(sender) {
                    nfTokenTaxon = 1u
                }
            tx.transactionType shouldBe TransactionType.NFTokenMint
            tx.account shouldBe sender
            val fields = tx.fields as NFTokenMintFields
            fields.nfTokenTaxon shouldBe 1u
            fields.issuer shouldBe null
            fields.transferFee shouldBe null
            fields.uri shouldBe null
            fields.flags shouldBe null
        }

        test("optional fields pass through") {
            val tx =
                nfTokenMint(sender) {
                    nfTokenTaxon = 0u
                    issuer = receiver
                    transferFee = 5000u
                    uri = "68747470733A2F2F6578616D706C652E636F6D"
                    flags = TransactionFlags.NFTokenMint.tfTransferable or
                        TransactionFlags.NFTokenMint.tfBurnable
                }
            val fields = tx.fields as NFTokenMintFields
            fields.nfTokenTaxon shouldBe 0u
            fields.issuer shouldBe receiver
            fields.transferFee shouldBe 5000u
            fields.uri shouldBe "68747470733A2F2F6578616D706C652E636F6D"
            fields.flags shouldBe (
                TransactionFlags.NFTokenMint.tfTransferable or
                    TransactionFlags.NFTokenMint.tfBurnable
            )
        }
    }

    // ── NFTokenMint: flags propagation to Unsigned ────────────────────────────

    context("nfTokenMint flags propagation") {
        test("flags from fields propagate to Unsigned") {
            val tx =
                nfTokenMint(sender) {
                    nfTokenTaxon = 0u
                    flags = TransactionFlags.NFTokenMint.tfTransferable
                }
            tx.flags shouldBe TransactionFlags.NFTokenMint.tfTransferable
        }

        test("null flags results in null on Unsigned") {
            val tx =
                nfTokenMint(sender) {
                    nfTokenTaxon = 0u
                }
            tx.flags shouldBe null
        }
    }

    // ── NFTokenCreateOffer: flags propagation to Unsigned ───────────────────

    context("nfTokenCreateOffer flags propagation") {
        val tokenId = "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"

        test("flags from fields propagate to Unsigned") {
            val tx =
                nfTokenCreateOffer(sender) {
                    nfTokenId = tokenId
                    amount = xrpAmount
                    flags = TransactionFlags.NFTokenCreateOffer.tfSellNFToken
                }
            tx.flags shouldBe TransactionFlags.NFTokenCreateOffer.tfSellNFToken
        }
    }

    // ── NFTokenBurn ──────────────────────────────────────────────────────────

    context("nfTokenBurn builder") {
        val tokenId = "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"

        test("happy path") {
            val tx =
                nfTokenBurn(sender) {
                    nfTokenId = tokenId
                }
            tx.transactionType shouldBe TransactionType.NFTokenBurn
            tx.account shouldBe sender
            val fields = tx.fields as NFTokenBurnFields
            fields.nfTokenId shouldBe tokenId
        }

        test("throws when nfTokenId is missing") {
            shouldThrow<IllegalArgumentException> {
                nfTokenBurn(sender) {}
            }.message shouldBe "nfTokenId is required"
        }
    }

    // ── NFTokenCreateOffer ───────────────────────────────────────────────────

    context("nfTokenCreateOffer builder") {
        val tokenId = "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"

        test("happy path - sell offer") {
            val tx =
                nfTokenCreateOffer(sender) {
                    nfTokenId = tokenId
                    amount = 10.xrp
                    flags = TransactionFlags.NFTokenCreateOffer.tfSellNFToken
                }
            tx.transactionType shouldBe TransactionType.NFTokenCreateOffer
            tx.account shouldBe sender
            val fields = tx.fields as NFTokenCreateOfferFields
            fields.nfTokenId shouldBe tokenId
            fields.amount shouldBe 10.xrp
            fields.flags shouldBe TransactionFlags.NFTokenCreateOffer.tfSellNFToken
            fields.destination shouldBe null
            fields.owner shouldBe null
            fields.expiration shouldBe null
        }

        test("buy offer with owner") {
            val tx =
                nfTokenCreateOffer(sender) {
                    nfTokenId = tokenId
                    amount = 10.xrp
                    owner = receiver
                }
            val fields = tx.fields as NFTokenCreateOfferFields
            fields.owner shouldBe receiver
        }

        test("throws when nfTokenId is missing") {
            shouldThrow<IllegalArgumentException> {
                nfTokenCreateOffer(sender) {
                    amount = 10.xrp
                }
            }.message shouldBe "nfTokenId is required"
        }

        test("throws when amount is missing") {
            shouldThrow<IllegalArgumentException> {
                nfTokenCreateOffer(sender) {
                    nfTokenId = tokenId
                }
            }.message shouldBe "amount is required"
        }
    }

    // ── NFTokenAcceptOffer ───────────────────────────────────────────────────

    context("nfTokenAcceptOffer builder") {
        val offerHash = "68CD311F3EC361A0E5F54F7BC2FAC3AB5EB922DD17C4FE67E2B5BCA62B32E5B7"

        test("happy path - accept sell offer") {
            val tx =
                nfTokenAcceptOffer(sender) {
                    nfTokenSellOffer = offerHash
                }
            tx.transactionType shouldBe TransactionType.NFTokenAcceptOffer
            tx.account shouldBe sender
            val fields = tx.fields as NFTokenAcceptOfferFields
            fields.nfTokenSellOffer shouldBe offerHash
            fields.nfTokenBuyOffer shouldBe null
            fields.nfTokenBrokerFee shouldBe null
        }

        test("accept buy offer") {
            val tx =
                nfTokenAcceptOffer(sender) {
                    nfTokenBuyOffer = offerHash
                }
            val fields = tx.fields as NFTokenAcceptOfferFields
            fields.nfTokenBuyOffer shouldBe offerHash
        }

        test("brokered mode with both offers and fee") {
            val sellOffer = "AAAA11111111111111111111111111111111111111111111111111111111111111"
            val buyOffer = "BBBB22222222222222222222222222222222222222222222222222222222222222"
            val tx =
                nfTokenAcceptOffer(sender) {
                    nfTokenSellOffer = sellOffer
                    nfTokenBuyOffer = buyOffer
                    nfTokenBrokerFee = 1.xrp
                }
            val fields = tx.fields as NFTokenAcceptOfferFields
            fields.nfTokenSellOffer shouldBe sellOffer
            fields.nfTokenBuyOffer shouldBe buyOffer
            fields.nfTokenBrokerFee shouldBe 1.xrp
        }

        test("all optional fields default to null") {
            val tx = nfTokenAcceptOffer(sender) {}
            val fields = tx.fields as NFTokenAcceptOfferFields
            fields.nfTokenSellOffer shouldBe null
            fields.nfTokenBuyOffer shouldBe null
            fields.nfTokenBrokerFee shouldBe null
        }
    }

    // ── TicketCreate ─────────────────────────────────────────────────────────

    context("ticketCreate builder") {
        test("happy path") {
            val tx =
                ticketCreate(sender) {
                    ticketCount = 5u
                }
            tx.transactionType shouldBe TransactionType.TicketCreate
            tx.account shouldBe sender
            val fields = tx.fields as TicketCreateFields
            fields.ticketCount shouldBe 5u
        }

        test("default ticketCount is 0") {
            val tx = ticketCreate(sender) {}
            val fields = tx.fields as TicketCreateFields
            fields.ticketCount shouldBe 0u
        }
    }

    // ── PaymentChannelCreate ─────────────────────────────────────────────────

    context("paymentChannelCreate builder") {
        val pubKey = "0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020"

        test("happy path - required fields") {
            val tx =
                paymentChannelCreate(sender) {
                    destination = receiver
                    amount = 10.xrp
                    settleDelay = 86400u
                    publicKey = pubKey
                }
            tx.transactionType shouldBe TransactionType.PaymentChannelCreate
            tx.account shouldBe sender
            val fields = tx.fields as PaymentChannelCreateFields
            fields.destination shouldBe receiver
            fields.amount shouldBe 10.xrp
            fields.settleDelay shouldBe 86400u
            fields.publicKey shouldBe pubKey
            fields.cancelAfter shouldBe null
            fields.destinationTag shouldBe null
        }

        test("optional fields pass through") {
            val tx =
                paymentChannelCreate(sender) {
                    destination = receiver
                    amount = 10.xrp
                    settleDelay = 86400u
                    publicKey = pubKey
                    cancelAfter = 533171558u
                    destinationTag = 12u
                }
            val fields = tx.fields as PaymentChannelCreateFields
            fields.cancelAfter shouldBe 533171558u
            fields.destinationTag shouldBe 12u
        }

        test("throws when destination is missing") {
            shouldThrow<IllegalArgumentException> {
                paymentChannelCreate(sender) {
                    amount = 10.xrp
                    settleDelay = 86400u
                    publicKey = pubKey
                }
            }.message shouldBe "destination is required"
        }

        test("throws when amount is missing") {
            shouldThrow<IllegalArgumentException> {
                paymentChannelCreate(sender) {
                    destination = receiver
                    settleDelay = 86400u
                    publicKey = pubKey
                }
            }.message shouldBe "amount is required"
        }

        test("throws when publicKey is missing") {
            shouldThrow<IllegalArgumentException> {
                paymentChannelCreate(sender) {
                    destination = receiver
                    amount = 10.xrp
                    settleDelay = 86400u
                }
            }.message shouldBe "publicKey is required"
        }
    }

    // ── SetRegularKey: account overload ──────────────────────────────────────

    context("setRegularKey(account) overload") {
        test("sets account from parameter") {
            val tx =
                setRegularKey(sender) {
                    regularKey = receiver
                }
            tx.transactionType shouldBe TransactionType.SetRegularKey
            tx.account shouldBe sender
            val fields = tx.fields as SetRegularKeyFields
            fields.regularKey shouldBe receiver
        }
    }

    // ── Account parameter wins over block assignment (all overloads) ─────────

    context("account parameter wins over block assignment") {
        val other = Address("rN7n3473SaZBCG4dFL83w7p1W9cgZB6ZBR")

        test("offerCreate") {
            val tx =
                offerCreate(sender) {
                    account = other
                    takerGets = 10.xrp
                    takerPays = usdAmount
                }
            tx.account shouldBe sender
        }

        test("trustSet") {
            val tx =
                trustSet(sender) {
                    account = other
                    limitAmount = usdAmount
                }
            tx.account shouldBe sender
        }

        test("accountSet") {
            val tx =
                accountSet(sender) {
                    account = other
                }
            tx.account shouldBe sender
        }

        test("signerListSet") {
            val tx =
                signerListSet(sender) {
                    account = other
                    signerQuorum = 0u
                    signerEntries = emptyList()
                }
            tx.account shouldBe sender
        }

        test("setRegularKey") {
            val tx =
                setRegularKey(sender) {
                    account = other
                    regularKey = receiver
                }
            tx.account shouldBe sender
        }

        test("offerCancel") {
            val tx =
                offerCancel(sender) {
                    account = other
                    offerSequence = 1u
                }
            tx.account shouldBe sender
        }
    }

    // ── Cross-cutting: T9 builder transaction type correctness ───────────────

    context("T9 builders set correct transactionType") {
        test("escrowCreate") {
            escrowCreate(sender) {
                destination = receiver
                amount = xrpAmount
            }.transactionType shouldBe TransactionType.EscrowCreate
        }

        test("escrowFinish") {
            escrowFinish(sender) {
                owner = receiver
                offerSequence = 1u
            }.transactionType shouldBe TransactionType.EscrowFinish
        }

        test("escrowCancel") {
            escrowCancel(sender) {
                owner = receiver
                offerSequence = 1u
            }.transactionType shouldBe TransactionType.EscrowCancel
        }

        test("checkCreate") {
            checkCreate(sender) {
                destination = receiver
                sendMax = xrpAmount
            }.transactionType shouldBe TransactionType.CheckCreate
        }

        test("checkCash") {
            checkCash(sender) {
                checkId = "AABB"
                amount = xrpAmount
            }.transactionType shouldBe TransactionType.CheckCash
        }

        test("checkCancel") {
            checkCancel(sender) {
                checkId = "AABB"
            }.transactionType shouldBe TransactionType.CheckCancel
        }

        test("nfTokenMint") {
            nfTokenMint(sender) {
                nfTokenTaxon = 0u
            }.transactionType shouldBe TransactionType.NFTokenMint
        }

        test("nfTokenBurn") {
            nfTokenBurn(sender) {
                nfTokenId = "AABB"
            }.transactionType shouldBe TransactionType.NFTokenBurn
        }

        test("nfTokenCreateOffer") {
            nfTokenCreateOffer(sender) {
                nfTokenId = "AABB"
                amount = xrpAmount
            }.transactionType shouldBe TransactionType.NFTokenCreateOffer
        }

        test("nfTokenAcceptOffer") {
            nfTokenAcceptOffer(sender) {
                nfTokenSellOffer = "AABB"
            }.transactionType shouldBe TransactionType.NFTokenAcceptOffer
        }

        test("ticketCreate") {
            ticketCreate(sender) {
                ticketCount = 1u
            }.transactionType shouldBe TransactionType.TicketCreate
        }

        test("paymentChannelCreate") {
            paymentChannelCreate(sender) {
                destination = receiver
                amount = xrpAmount
                settleDelay = 100u
                publicKey = "AABB"
            }.transactionType shouldBe TransactionType.PaymentChannelCreate
        }
    }

    // ── Cross-cutting: all T9 builders use the provided account ──────────────

    context("T9 builders use the provided account") {
        test("escrowCreate") {
            escrowCreate(sender) {
                destination = receiver
                amount = xrpAmount
            }.account shouldBe sender
        }

        test("checkCreate") {
            checkCreate(sender) {
                destination = receiver
                sendMax = xrpAmount
            }.account shouldBe sender
        }

        test("nfTokenMint") {
            nfTokenMint(sender) {
                nfTokenTaxon = 0u
            }.account shouldBe sender
        }

        test("ticketCreate") {
            ticketCreate(sender) {
                ticketCount = 1u
            }.account shouldBe sender
        }

        test("paymentChannelCreate") {
            paymentChannelCreate(sender) {
                destination = receiver
                amount = xrpAmount
                settleDelay = 100u
                publicKey = "AABB"
            }.account shouldBe sender
        }
    }
})
