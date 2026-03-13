package org.xrpl.sdk.core.model.ledger

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class LedgerObjectTypeTest : FunSpec({

    test("AccountRoot constant has correct value") {
        LedgerObjectType.AccountRoot.value shouldBe "AccountRoot"
    }

    test("SignerList constant has correct value") {
        LedgerObjectType.SignerList.value shouldBe "SignerList"
    }

    test("DirectoryNode constant has correct value") {
        LedgerObjectType.DirectoryNode.value shouldBe "DirectoryNode"
    }

    test("Ticket constant has correct value") {
        LedgerObjectType.Ticket.value shouldBe "Ticket"
    }

    test("Offer constant has correct value") {
        LedgerObjectType.Offer.value shouldBe "Offer"
    }

    test("RippleState constant has correct value") {
        LedgerObjectType.RippleState.value shouldBe "RippleState"
    }

    test("Escrow constant has correct value") {
        LedgerObjectType.Escrow.value shouldBe "Escrow"
    }

    test("PayChannel constant has correct value") {
        LedgerObjectType.PayChannel.value shouldBe "PayChannel"
    }

    test("Check constant has correct value") {
        LedgerObjectType.Check.value shouldBe "Check"
    }

    test("DepositPreauth constant has correct value") {
        LedgerObjectType.DepositPreauth.value shouldBe "DepositPreauth"
    }

    test("NFTokenPage constant has correct value") {
        LedgerObjectType.NFTokenPage.value shouldBe "NFTokenPage"
    }

    test("NFTokenOffer constant has correct value") {
        LedgerObjectType.NFTokenOffer.value shouldBe "NFTokenOffer"
    }

    test("AMM constant has correct value") {
        LedgerObjectType.AMM.value shouldBe "AMM"
    }

    test("MPToken constant has correct value") {
        LedgerObjectType.MPToken.value shouldBe "MPToken"
    }

    test("MPTokenIssuance constant has correct value") {
        LedgerObjectType.MPTokenIssuance.value shouldBe "MPTokenIssuance"
    }

    test("DID constant has correct value") {
        LedgerObjectType.DID.value shouldBe "DID"
    }

    test("Credential constant has correct value") {
        LedgerObjectType.Credential.value shouldBe "Credential"
    }

    test("Amendments constant has correct value") {
        LedgerObjectType.Amendments.value shouldBe "Amendments"
    }

    test("FeeSettings constant has correct value") {
        LedgerObjectType.FeeSettings.value shouldBe "FeeSettings"
    }

    test("LedgerHashes constant has correct value") {
        LedgerObjectType.LedgerHashes.value shouldBe "LedgerHashes"
    }

    test("NegativeUNL constant has correct value") {
        LedgerObjectType.NegativeUNL.value shouldBe "NegativeUNL"
    }

    test("Bridge constant has correct value") {
        LedgerObjectType.Bridge.value shouldBe "Bridge"
    }

    test("XChainOwnedClaimID constant has correct value") {
        LedgerObjectType.XChainOwnedClaimID.value shouldBe "XChainOwnedClaimID"
    }

    test("XChainOwnedCreateAccountClaimID constant has correct value") {
        LedgerObjectType.XChainOwnedCreateAccountClaimID.value shouldBe "XChainOwnedCreateAccountClaimID"
    }

    test("Oracle constant has correct value") {
        LedgerObjectType.Oracle.value shouldBe "Oracle"
    }

    test("PermissionedDomain constant has correct value") {
        LedgerObjectType.PermissionedDomain.value shouldBe "PermissionedDomain"
    }

    test("Delegate constant has correct value") {
        LedgerObjectType.Delegate.value shouldBe "Delegate"
    }

    test("Vault constant has correct value") {
        LedgerObjectType.Vault.value shouldBe "Vault"
    }

    test("Loan constant has correct value") {
        LedgerObjectType.Loan.value shouldBe "Loan"
    }

    test("LoanBroker constant has correct value") {
        LedgerObjectType.LoanBroker.value shouldBe "LoanBroker"
    }

    test("Unknown type constructs without error") {
        val future = LedgerObjectType("FutureObject")
        future.value shouldBe "FutureObject"
    }

    test("Equality: constructed instance equals named constant") {
        LedgerObjectType("AccountRoot") shouldBe LedgerObjectType.AccountRoot
    }

    test("Inequality: different values are not equal") {
        LedgerObjectType("AccountRoot") shouldNotBe LedgerObjectType.Offer
    }
})
