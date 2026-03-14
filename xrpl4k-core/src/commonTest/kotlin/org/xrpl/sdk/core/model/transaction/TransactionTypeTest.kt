package org.xrpl.sdk.core.model.transaction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TransactionTypeTest : FunSpec({

    // -------------------------------------------------------------------------
    // Named constants — correct string values
    // -------------------------------------------------------------------------

    context("Account Management constants") {
        test("AccountSet has correct value") { TransactionType.AccountSet.value shouldBe "AccountSet" }
        test("AccountDelete has correct value") { TransactionType.AccountDelete.value shouldBe "AccountDelete" }
        test("SetRegularKey has correct value") { TransactionType.SetRegularKey.value shouldBe "SetRegularKey" }
        test("SignerListSet has correct value") { TransactionType.SignerListSet.value shouldBe "SignerListSet" }
        test("DepositPreauth has correct value") { TransactionType.DepositPreauth.value shouldBe "DepositPreauth" }
    }

    context("Payment constants") {
        test("Payment has correct value") { TransactionType.Payment.value shouldBe "Payment" }
    }

    context("Trust & Tokens constants") {
        test("TrustSet has correct value") { TransactionType.TrustSet.value shouldBe "TrustSet" }
        test("Clawback has correct value") { TransactionType.Clawback.value shouldBe "Clawback" }
    }

    context("DEX constants") {
        test("OfferCreate has correct value") { TransactionType.OfferCreate.value shouldBe "OfferCreate" }
        test("OfferCancel has correct value") { TransactionType.OfferCancel.value shouldBe "OfferCancel" }
    }

    context("AMM constants") {
        test("AMMCreate has correct value") { TransactionType.AMMCreate.value shouldBe "AMMCreate" }
        test("AMMDeposit has correct value") { TransactionType.AMMDeposit.value shouldBe "AMMDeposit" }
        test("AMMWithdraw has correct value") { TransactionType.AMMWithdraw.value shouldBe "AMMWithdraw" }
        test("AMMVote has correct value") { TransactionType.AMMVote.value shouldBe "AMMVote" }
        test("AMMBid has correct value") { TransactionType.AMMBid.value shouldBe "AMMBid" }
        test("AMMDelete has correct value") { TransactionType.AMMDelete.value shouldBe "AMMDelete" }
        test("AMMClawback has correct value") { TransactionType.AMMClawback.value shouldBe "AMMClawback" }
    }

    context("Escrow constants") {
        test("EscrowCreate has correct value") { TransactionType.EscrowCreate.value shouldBe "EscrowCreate" }
        test("EscrowFinish has correct value") { TransactionType.EscrowFinish.value shouldBe "EscrowFinish" }
        test("EscrowCancel has correct value") { TransactionType.EscrowCancel.value shouldBe "EscrowCancel" }
    }

    context("Payment Channels constants") {
        test("PaymentChannelCreate has correct value") {
            TransactionType.PaymentChannelCreate.value shouldBe "PaymentChannelCreate"
        }
        test(
            "PaymentChannelFund has correct value",
        ) { TransactionType.PaymentChannelFund.value shouldBe "PaymentChannelFund" }
        test("PaymentChannelClaim has correct value") {
            TransactionType.PaymentChannelClaim.value shouldBe "PaymentChannelClaim"
        }
    }

    context("Check constants") {
        test("CheckCreate has correct value") { TransactionType.CheckCreate.value shouldBe "CheckCreate" }
        test("CheckCash has correct value") { TransactionType.CheckCash.value shouldBe "CheckCash" }
        test("CheckCancel has correct value") { TransactionType.CheckCancel.value shouldBe "CheckCancel" }
    }

    context("NFT constants") {
        test("NFTokenMint has correct value") { TransactionType.NFTokenMint.value shouldBe "NFTokenMint" }
        test("NFTokenBurn has correct value") { TransactionType.NFTokenBurn.value shouldBe "NFTokenBurn" }
        test(
            "NFTokenCreateOffer has correct value",
        ) { TransactionType.NFTokenCreateOffer.value shouldBe "NFTokenCreateOffer" }
        test(
            "NFTokenCancelOffer has correct value",
        ) { TransactionType.NFTokenCancelOffer.value shouldBe "NFTokenCancelOffer" }
        test(
            "NFTokenAcceptOffer has correct value",
        ) { TransactionType.NFTokenAcceptOffer.value shouldBe "NFTokenAcceptOffer" }
        test("NFTokenModify has correct value") { TransactionType.NFTokenModify.value shouldBe "NFTokenModify" }
    }

    context("MPT constants") {
        test("MPTokenIssuanceCreate has correct value") {
            TransactionType.MPTokenIssuanceCreate.value shouldBe "MPTokenIssuanceCreate"
        }
        test("MPTokenIssuanceDestroy has correct value") {
            TransactionType.MPTokenIssuanceDestroy.value shouldBe "MPTokenIssuanceDestroy"
        }
        test(
            "MPTokenIssuanceSet has correct value",
        ) { TransactionType.MPTokenIssuanceSet.value shouldBe "MPTokenIssuanceSet" }
        test(
            "MPTokenAuthorize has correct value",
        ) { TransactionType.MPTokenAuthorize.value shouldBe "MPTokenAuthorize" }
    }

    context("DID constants") {
        test("DIDSet has correct value") { TransactionType.DIDSet.value shouldBe "DIDSet" }
        test("DIDDelete has correct value") { TransactionType.DIDDelete.value shouldBe "DIDDelete" }
    }

    context("Oracle constants") {
        test("OracleSet has correct value") { TransactionType.OracleSet.value shouldBe "OracleSet" }
        test("OracleDelete has correct value") { TransactionType.OracleDelete.value shouldBe "OracleDelete" }
    }

    context("Credential constants") {
        test(
            "CredentialCreate has correct value",
        ) { TransactionType.CredentialCreate.value shouldBe "CredentialCreate" }
        test(
            "CredentialAccept has correct value",
        ) { TransactionType.CredentialAccept.value shouldBe "CredentialAccept" }
        test(
            "CredentialDelete has correct value",
        ) { TransactionType.CredentialDelete.value shouldBe "CredentialDelete" }
    }

    context("Ticket constants") {
        test("TicketCreate has correct value") { TransactionType.TicketCreate.value shouldBe "TicketCreate" }
    }

    context("XChain constants") {
        test(
            "XChainCreateBridge has correct value",
        ) { TransactionType.XChainCreateBridge.value shouldBe "XChainCreateBridge" }
        test(
            "XChainModifyBridge has correct value",
        ) { TransactionType.XChainModifyBridge.value shouldBe "XChainModifyBridge" }
        test("XChainCreateClaimID has correct value") {
            TransactionType.XChainCreateClaimID.value shouldBe "XChainCreateClaimID"
        }
        test("XChainCommit has correct value") { TransactionType.XChainCommit.value shouldBe "XChainCommit" }
        test("XChainClaim has correct value") { TransactionType.XChainClaim.value shouldBe "XChainClaim" }
        test("XChainAccountCreateCommit has correct value") {
            TransactionType.XChainAccountCreateCommit.value shouldBe "XChainAccountCreateCommit"
        }
        test("XChainAddClaimAttestation has correct value") {
            TransactionType.XChainAddClaimAttestation.value shouldBe "XChainAddClaimAttestation"
        }
        test("XChainAddAccountCreateAttestation has correct value") {
            TransactionType.XChainAddAccountCreateAttestation.value shouldBe "XChainAddAccountCreateAttestation"
        }
    }

    context("PermissionedDomain constants") {
        test("PermissionedDomainSet has correct value") {
            TransactionType.PermissionedDomainSet.value shouldBe "PermissionedDomainSet"
        }
        test("PermissionedDomainDelete has correct value") {
            TransactionType.PermissionedDomainDelete.value shouldBe "PermissionedDomainDelete"
        }
    }

    context("Batch constants") {
        test("Batch has correct value") { TransactionType.Batch.value shouldBe "Batch" }
    }

    context("Delegate constants") {
        test("DelegateSet has correct value") { TransactionType.DelegateSet.value shouldBe "DelegateSet" }
    }

    context("Vault constants") {
        test("VaultCreate has correct value") { TransactionType.VaultCreate.value shouldBe "VaultCreate" }
        test("VaultSet has correct value") { TransactionType.VaultSet.value shouldBe "VaultSet" }
        test("VaultDelete has correct value") { TransactionType.VaultDelete.value shouldBe "VaultDelete" }
        test("VaultDeposit has correct value") { TransactionType.VaultDeposit.value shouldBe "VaultDeposit" }
        test("VaultWithdraw has correct value") { TransactionType.VaultWithdraw.value shouldBe "VaultWithdraw" }
        test("VaultClawback has correct value") { TransactionType.VaultClawback.value shouldBe "VaultClawback" }
    }

    context("Loan constants") {
        test("LoanSet has correct value") { TransactionType.LoanSet.value shouldBe "LoanSet" }
        test("LoanDelete has correct value") { TransactionType.LoanDelete.value shouldBe "LoanDelete" }
        test("LoanManage has correct value") { TransactionType.LoanManage.value shouldBe "LoanManage" }
        test("LoanPay has correct value") { TransactionType.LoanPay.value shouldBe "LoanPay" }
        test("LoanBrokerSet has correct value") { TransactionType.LoanBrokerSet.value shouldBe "LoanBrokerSet" }
        test(
            "LoanBrokerDelete has correct value",
        ) { TransactionType.LoanBrokerDelete.value shouldBe "LoanBrokerDelete" }
        test("LoanBrokerCoverDeposit has correct value") {
            TransactionType.LoanBrokerCoverDeposit.value shouldBe "LoanBrokerCoverDeposit"
        }
        test("LoanBrokerCoverWithdraw has correct value") {
            TransactionType.LoanBrokerCoverWithdraw.value shouldBe "LoanBrokerCoverWithdraw"
        }
        test("LoanBrokerCoverClawback has correct value") {
            TransactionType.LoanBrokerCoverClawback.value shouldBe "LoanBrokerCoverClawback"
        }
    }

    context("Pseudo-transaction constants") {
        test("EnableAmendment has correct value") { TransactionType.EnableAmendment.value shouldBe "EnableAmendment" }
        test("SetFee has correct value") { TransactionType.SetFee.value shouldBe "SetFee" }
        test("UNLModify has correct value") { TransactionType.UNLModify.value shouldBe "UNLModify" }
        test("LedgerStateFix has correct value") { TransactionType.LedgerStateFix.value shouldBe "LedgerStateFix" }
    }

    context("Invalid sentinel") {
        test("Invalid has correct value") { TransactionType.Invalid.value shouldBe "Invalid" }
    }

    // -------------------------------------------------------------------------
    // Forward compatibility — unknown types must construct without error
    // -------------------------------------------------------------------------

    context("forward compatibility") {
        test("unknown future type constructs without error") {
            val future = TransactionType("FutureType")
            future.value shouldBe "FutureType"
        }

        test("arbitrary string constructs without error") {
            val arbitrary = TransactionType("SomeAmendmentNotYetDefined")
            arbitrary.value shouldBe "SomeAmendmentNotYetDefined"
        }
    }

    // -------------------------------------------------------------------------
    // Equality
    // -------------------------------------------------------------------------

    context("equality") {
        test("TransactionType constructed from string equals named constant") {
            TransactionType("Payment") shouldBe TransactionType.Payment
        }

        test("two TransactionTypes with same value are equal") {
            TransactionType("AMMCreate") shouldBe TransactionType("AMMCreate")
        }

        test("two TransactionTypes with different values are not equal") {
            TransactionType("Payment") shouldNotBe TransactionType("EscrowCreate")
        }

        test("named constant equals itself") {
            TransactionType.OfferCreate shouldBe TransactionType.OfferCreate
        }
    }

    // -------------------------------------------------------------------------
    // when() requires else branch (value class is not a sealed class)
    // -------------------------------------------------------------------------

    context("when expression requires else branch") {
        test("when over TransactionType value requires else to compile and handle unknown values") {
            val tt = TransactionType("FutureType")
            val result =
                when (tt) {
                    TransactionType.Payment -> "payment"
                    TransactionType.EscrowCreate -> "escrow-create"
                    else -> "other"
                }
            result shouldBe "other"
        }

        test("when over known type matches correctly") {
            val tt = TransactionType.Payment
            val result =
                when (tt) {
                    TransactionType.Payment -> "payment"
                    else -> "other"
                }
            result shouldBe "payment"
        }
    }
})
