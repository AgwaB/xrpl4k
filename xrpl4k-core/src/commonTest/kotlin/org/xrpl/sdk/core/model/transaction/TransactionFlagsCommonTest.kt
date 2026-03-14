@file:Suppress("MagicNumber")

package org.xrpl.sdk.core.model.transaction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TransactionFlagsCommonTest : FunSpec({

    // -- Universal --

    test("tfFullyCanonicalSig matches XRPL spec value") {
        TransactionFlags.tfFullyCanonicalSig shouldBe 0x80000000u
    }

    // -- Payment flags --

    context("Payment flags") {
        test("tfNoDirectRipple") {
            TransactionFlags.Payment.tfNoDirectRipple shouldBe 0x00010000u
        }

        test("tfPartialPayment") {
            TransactionFlags.Payment.tfPartialPayment shouldBe 0x00020000u
        }

        test("tfLimitQuality") {
            TransactionFlags.Payment.tfLimitQuality shouldBe 0x00040000u
        }
    }

    // -- TrustSet flags --

    context("TrustSet flags") {
        test("tfSetfAuth") {
            TransactionFlags.TrustSet.tfSetfAuth shouldBe 0x00010000u
        }

        test("tfSetNoRipple") {
            TransactionFlags.TrustSet.tfSetNoRipple shouldBe 0x00020000u
        }

        test("tfClearNoRipple") {
            TransactionFlags.TrustSet.tfClearNoRipple shouldBe 0x00040000u
        }

        test("tfSetFreeze") {
            TransactionFlags.TrustSet.tfSetFreeze shouldBe 0x00100000u
        }

        test("tfClearFreeze") {
            TransactionFlags.TrustSet.tfClearFreeze shouldBe 0x00200000u
        }
    }

    // -- OfferCreate flags --

    context("OfferCreate flags") {
        test("tfPassive") {
            TransactionFlags.OfferCreate.tfPassive shouldBe 0x00010000u
        }

        test("tfImmediateOrCancel") {
            TransactionFlags.OfferCreate.tfImmediateOrCancel shouldBe 0x00020000u
        }

        test("tfFillOrKill") {
            TransactionFlags.OfferCreate.tfFillOrKill shouldBe 0x00040000u
        }

        test("tfSell") {
            TransactionFlags.OfferCreate.tfSell shouldBe 0x00080000u
        }
    }

    // -- NFTokenMint flags --

    context("NFTokenMint flags") {
        test("tfBurnable") {
            TransactionFlags.NFTokenMint.tfBurnable shouldBe 0x00000001u
        }

        test("tfOnlyXRP") {
            TransactionFlags.NFTokenMint.tfOnlyXRP shouldBe 0x00000002u
        }

        test("tfTrustLine") {
            TransactionFlags.NFTokenMint.tfTrustLine shouldBe 0x00000004u
        }

        test("tfTransferable") {
            TransactionFlags.NFTokenMint.tfTransferable shouldBe 0x00000008u
        }
    }

    // -- NFTokenCreateOffer flags --

    test("NFTokenCreateOffer tfSellNFToken") {
        TransactionFlags.NFTokenCreateOffer.tfSellNFToken shouldBe 0x00000001u
    }

    // -- PaymentChannelClaim flags --

    context("PaymentChannelClaim flags") {
        test("tfRenew") {
            TransactionFlags.PaymentChannelClaim.tfRenew shouldBe 0x00010000u
        }

        test("tfClose") {
            TransactionFlags.PaymentChannelClaim.tfClose shouldBe 0x00020000u
        }
    }

    // -- AMMDeposit flags --

    context("AMMDeposit flags") {
        test("tfLPToken") {
            TransactionFlags.AMMDeposit.tfLPToken shouldBe 0x00010000u
        }

        test("tfSingleAsset") {
            TransactionFlags.AMMDeposit.tfSingleAsset shouldBe 0x00080000u
        }

        test("tfTwoAsset") {
            TransactionFlags.AMMDeposit.tfTwoAsset shouldBe 0x00100000u
        }

        test("tfOneAssetLPToken") {
            TransactionFlags.AMMDeposit.tfOneAssetLPToken shouldBe 0x00200000u
        }

        test("tfLimitLPToken") {
            TransactionFlags.AMMDeposit.tfLimitLPToken shouldBe 0x00400000u
        }
    }

    // -- AMMWithdraw flags --

    context("AMMWithdraw flags") {
        test("tfLPToken") {
            TransactionFlags.AMMWithdraw.tfLPToken shouldBe 0x00010000u
        }

        test("tfWithdrawAll") {
            TransactionFlags.AMMWithdraw.tfWithdrawAll shouldBe 0x00020000u
        }

        test("tfOneAssetWithdrawAll") {
            TransactionFlags.AMMWithdraw.tfOneAssetWithdrawAll shouldBe 0x00040000u
        }

        test("tfSingleAsset") {
            TransactionFlags.AMMWithdraw.tfSingleAsset shouldBe 0x00080000u
        }

        test("tfTwoAsset") {
            TransactionFlags.AMMWithdraw.tfTwoAsset shouldBe 0x00100000u
        }

        test("tfOneAssetLPToken") {
            TransactionFlags.AMMWithdraw.tfOneAssetLPToken shouldBe 0x00200000u
        }

        test("tfLimitLPToken") {
            TransactionFlags.AMMWithdraw.tfLimitLPToken shouldBe 0x00400000u
        }
    }

    // -- XChainModifyBridge --

    test("XChainModifyBridge tfClearAccountCreateAmount") {
        TransactionFlags.XChainModifyBridge.tfClearAccountCreateAmount shouldBe 0x00010000u
    }

    // -- Bitwise OR combination --

    context("bitwise OR combination") {
        test("NFTokenMint tfTransferable or tfBurnable combines correctly") {
            val combined =
                TransactionFlags.NFTokenMint.tfTransferable or
                    TransactionFlags.NFTokenMint.tfBurnable
            combined shouldBe 0x00000009u
            (combined and TransactionFlags.NFTokenMint.tfTransferable) shouldBe
                TransactionFlags.NFTokenMint.tfTransferable
            (combined and TransactionFlags.NFTokenMint.tfBurnable) shouldBe
                TransactionFlags.NFTokenMint.tfBurnable
        }

        test("Payment tfPartialPayment or tfLimitQuality combines correctly") {
            val combined =
                TransactionFlags.Payment.tfPartialPayment or
                    TransactionFlags.Payment.tfLimitQuality
            combined shouldBe 0x00060000u
        }

        test("TrustSet tfSetNoRipple or tfSetFreeze combines correctly") {
            val combined =
                TransactionFlags.TrustSet.tfSetNoRipple or
                    TransactionFlags.TrustSet.tfSetFreeze
            combined shouldBe 0x00120000u
        }

        test("combining three NFTokenMint flags") {
            val combined =
                TransactionFlags.NFTokenMint.tfBurnable or
                    TransactionFlags.NFTokenMint.tfOnlyXRP or
                    TransactionFlags.NFTokenMint.tfTransferable
            combined shouldBe 0x0000000Bu
        }
    }

    // -- AccountSetFlag values --

    context("AccountSetFlag values match XRPL spec") {
        test("asfRequireDest") {
            AccountSetFlag.asfRequireDest shouldBe 1u
        }

        test("asfRequireAuth") {
            AccountSetFlag.asfRequireAuth shouldBe 2u
        }

        test("asfDisallowXRP") {
            AccountSetFlag.asfDisallowXRP shouldBe 3u
        }

        test("asfDisableMaster") {
            AccountSetFlag.asfDisableMaster shouldBe 4u
        }

        test("asfAccountTxnID") {
            AccountSetFlag.asfAccountTxnID shouldBe 5u
        }

        test("asfNoFreeze") {
            AccountSetFlag.asfNoFreeze shouldBe 6u
        }

        test("asfGlobalFreeze") {
            AccountSetFlag.asfGlobalFreeze shouldBe 7u
        }

        test("asfDefaultRipple") {
            AccountSetFlag.asfDefaultRipple shouldBe 8u
        }

        test("asfDepositAuth") {
            AccountSetFlag.asfDepositAuth shouldBe 9u
        }

        test("asfAuthorizedNFTokenMinter") {
            AccountSetFlag.asfAuthorizedNFTokenMinter shouldBe 10u
        }

        test("asfDisallowIncomingNFTokenOffer") {
            AccountSetFlag.asfDisallowIncomingNFTokenOffer shouldBe 12u
        }

        test("asfDisallowIncomingCheck") {
            AccountSetFlag.asfDisallowIncomingCheck shouldBe 13u
        }

        test("asfDisallowIncomingPayChan") {
            AccountSetFlag.asfDisallowIncomingPayChan shouldBe 14u
        }

        test("asfDisallowIncomingTrustline") {
            AccountSetFlag.asfDisallowIncomingTrustline shouldBe 15u
        }

        test("asfAllowTrustLineClawback") {
            AccountSetFlag.asfAllowTrustLineClawback shouldBe 16u
        }
    }

    // -- AccountSetFlag values are sequential --

    test("AccountSetFlag values are sequential integers, not bitmasks") {
        AccountSetFlag.asfRequireDest shouldBe 1u
        AccountSetFlag.asfRequireAuth shouldBe 2u
        AccountSetFlag.asfDisallowXRP shouldBe 3u
    }
})
