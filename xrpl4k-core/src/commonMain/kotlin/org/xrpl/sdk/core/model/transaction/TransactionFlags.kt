@file:Suppress("MagicNumber", "PropertyName")

package org.xrpl.sdk.core.model.transaction

/**
 * Common transaction flag constants for the XRP Ledger.
 *
 * Flags are passed as a bitmask in the `flags` field of [XrplTransaction.Unsigned].
 * Multiple flags can be combined using bitwise OR:
 * ```
 * flags = TransactionFlags.TrustSet.tfSetNoRipple or TransactionFlags.TrustSet.tfSetFreeze
 * ```
 *
 * For AccountSet, use [AccountSetFlag] values with the `setFlag`/`clearFlag` fields
 * in [AccountSetFields] rather than transaction flags.
 */
public object TransactionFlags {
    // ── Universal ────────────────────────────────────────────────────────────

    /** Require a fully-canonical signature (enabled by default since 2020). */
    public const val tfFullyCanonicalSig: UInt = 0x80000000u

    // ── Payment ──────────────────────────────────────────────────────────────

    public object Payment {
        /** Do not use the default path; only use paths included in the Paths field. */
        public const val tfNoDirectRipple: UInt = 0x00010000u

        /** Allow partial payment — deliver less than the Amount if the full amount cannot be delivered. */
        public const val tfPartialPayment: UInt = 0x00020000u

        /** Only take paths where all offers have an input:output ratio equal or better than the ratio of Amount:SendMax. */
        public const val tfLimitQuality: UInt = 0x00040000u
    }

    // ── TrustSet ─────────────────────────────────────────────────────────────

    public object TrustSet {
        /** Authorize the other party to hold currency issued by this account. */
        public const val tfSetfAuth: UInt = 0x00010000u

        /** Enable the No Ripple flag on this trust line. */
        public const val tfSetNoRipple: UInt = 0x00020000u

        /** Disable the No Ripple flag on this trust line. */
        public const val tfClearNoRipple: UInt = 0x00040000u

        /** Freeze this trust line. */
        public const val tfSetFreeze: UInt = 0x00100000u

        /** Unfreeze this trust line. */
        public const val tfClearFreeze: UInt = 0x00200000u
    }

    // ── OfferCreate ──────────────────────────────────────────────────────────

    public object OfferCreate {
        /** Treat the offer as passive — don't match with offers from the same account. */
        public const val tfPassive: UInt = 0x00010000u

        /** Treat the offer as an Immediate or Cancel order. */
        public const val tfImmediateOrCancel: UInt = 0x00020000u

        /** Treat the offer as a Fill or Kill order. */
        public const val tfFillOrKill: UInt = 0x00040000u

        /** Exchange the entire TakerGets amount, even if it means obtaining more than TakerPays. */
        public const val tfSell: UInt = 0x00080000u
    }

    // ── NFTokenMint ──────────────────────────────────────────────────────────

    public object NFTokenMint {
        /** Allow the issuer (or an entity authorized by the issuer) to destroy the minted token. */
        public const val tfBurnable: UInt = 0x00000001u

        /** The minted token may only be offered or sold for XRP. */
        public const val tfOnlyXRP: UInt = 0x00000002u

        /** Automatically create a trust line to hold transfer fees. */
        public const val tfTrustLine: UInt = 0x00000004u

        /** The minted token may be transferred to others. */
        public const val tfTransferable: UInt = 0x00000008u
    }

    // ── NFTokenCreateOffer ───────────────────────────────────────────────────

    public object NFTokenCreateOffer {
        /** This is a sell offer (the token owner is offering to sell). */
        public const val tfSellNFToken: UInt = 0x00000001u
    }

    // ── PaymentChannelClaim ──────────────────────────────────────────────────

    public object PaymentChannelClaim {
        /** Request to renew the payment channel. */
        public const val tfRenew: UInt = 0x00010000u

        /** Request to close the payment channel. */
        public const val tfClose: UInt = 0x00020000u
    }

    // ── AMMDeposit ───────────────────────────────────────────────────────────

    public object AMMDeposit {
        /** Deposit both assets proportionally to receive LP tokens. */
        public const val tfLPToken: UInt = 0x00010000u

        /** Deposit a single asset. */
        public const val tfSingleAsset: UInt = 0x00080000u

        /** Deposit two assets. */
        public const val tfTwoAsset: UInt = 0x00100000u

        /** Deposit one asset with a fixed amount of the other. */
        public const val tfOneAssetLPToken: UInt = 0x00200000u

        /** Deposit with a limit price. */
        public const val tfLimitLPToken: UInt = 0x00400000u
    }

    // ── AMMWithdraw ──────────────────────────────────────────────────────────

    public object AMMWithdraw {
        /** Withdraw both assets proportionally by specifying LP tokens. */
        public const val tfLPToken: UInt = 0x00010000u

        /** Withdraw all assets. */
        public const val tfWithdrawAll: UInt = 0x00020000u

        /** Withdraw a single asset. */
        public const val tfOneAssetWithdrawAll: UInt = 0x00040000u

        /** Withdraw a single asset. */
        public const val tfSingleAsset: UInt = 0x00080000u

        /** Withdraw two assets. */
        public const val tfTwoAsset: UInt = 0x00100000u

        /** Withdraw one asset with a fixed LP token amount. */
        public const val tfOneAssetLPToken: UInt = 0x00200000u

        /** Withdraw with a limit price. */
        public const val tfLimitLPToken: UInt = 0x00400000u
    }

    // ── XChainModifyBridge ───────────────────────────────────────────────────

    public object XChainModifyBridge {
        /** Clear the minimum create account amount. */
        public const val tfClearAccountCreateAmount: UInt = 0x00010000u
    }
}

/**
 * AccountSet flag values for use with [AccountSetFields.setFlag] and [AccountSetFields.clearFlag].
 *
 * These are **not** bitmask flags — they are integer values passed to the `SetFlag`/`ClearFlag` fields.
 */
public object AccountSetFlag {
    /** Require a destination tag for incoming payments. */
    public const val asfRequireDest: UInt = 1u

    /** Require authorization for users to hold tokens issued by this account. */
    public const val asfRequireAuth: UInt = 2u

    /** Disallow incoming XRP payments. */
    public const val asfDisallowXRP: UInt = 3u

    /** Disable the master key pair. */
    public const val asfDisableMaster: UInt = 4u

    /** Track the ID of the most recent transaction. */
    public const val asfAccountTxnID: UInt = 5u

    /** Permanently give up the ability to freeze individual trust lines or disable Global Freeze. */
    public const val asfNoFreeze: UInt = 6u

    /** Freeze all assets issued by this account. */
    public const val asfGlobalFreeze: UInt = 7u

    /** Enable rippling on this account's trust lines by default. */
    public const val asfDefaultRipple: UInt = 8u

    /** Enable Deposit Authorization on this account. */
    public const val asfDepositAuth: UInt = 9u

    /** Allow another account to mint NFTokens on behalf of this account. */
    public const val asfAuthorizedNFTokenMinter: UInt = 10u

    /** Disallow incoming NFTokenOffers. */
    public const val asfDisallowIncomingNFTokenOffer: UInt = 12u

    /** Disallow incoming Checks. */
    public const val asfDisallowIncomingCheck: UInt = 13u

    /** Disallow incoming PayChannels. */
    public const val asfDisallowIncomingPayChan: UInt = 14u

    /** Disallow incoming TrustLines. */
    public const val asfDisallowIncomingTrustline: UInt = 15u

    /** Enable clawback on this account. */
    public const val asfAllowTrustLineClawback: UInt = 16u
}
