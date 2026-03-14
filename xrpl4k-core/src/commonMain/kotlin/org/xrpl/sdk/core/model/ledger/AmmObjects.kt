package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * A vote slot in an [Amm] representing a liquidity provider's fee vote.
 *
 * @property account The address of the voting account.
 * @property tradingFee The trading fee this account voted for (in basis points, 0-1000).
 * @property voteWeight The weight of this vote based on LP token holdings.
 */
public class AmmVoteEntry(
    public val account: Address,
    public val tradingFee: UInt,
    public val voteWeight: UInt,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AmmVoteEntry) return false
        return account == other.account &&
            tradingFee == other.tradingFee &&
            voteWeight == other.voteWeight
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + tradingFee.hashCode()
        result = 31 * result + voteWeight.hashCode()
        return result
    }

    override fun toString(): String = "AmmVoteEntry(account=$account, tradingFee=$tradingFee)"
}

/**
 * An active auction slot in an [Amm].
 *
 * @property account The address of the account that holds this auction slot.
 * @property discountedFee The discounted trading fee for the slot holder (basis points).
 * @property expiration Expiration time of this auction slot (seconds since Ripple Epoch).
 * @property price The price paid for this auction slot in LP tokens.
 * @property authAccounts Accounts authorised to trade at the discounted fee.
 */
public class AmmAuctionSlot(
    public val account: Address,
    public val discountedFee: UInt,
    public val expiration: UInt,
    public val price: CurrencyAmount? = null,
    public val authAccounts: List<Address> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AmmAuctionSlot) return false
        return account == other.account &&
            discountedFee == other.discountedFee &&
            expiration == other.expiration &&
            price == other.price &&
            authAccounts == other.authAccounts
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + discountedFee.hashCode()
        result = 31 * result + expiration.hashCode()
        result = 31 * result + (price?.hashCode() ?: 0)
        result = 31 * result + authAccounts.hashCode()
        return result
    }

    override fun toString(): String = "AmmAuctionSlot(account=$account, discountedFee=$discountedFee)"
}

/**
 * An Automated Market Maker instance for a currency pair.
 *
 * @property ammAccount The address of the special AMM account that holds the pool assets.
 * @property asset The first asset in the AMM pool.
 * @property asset2 The second asset in the AMM pool.
 * @property lpTokenBalance The current LP token balance of the AMM.
 * @property tradingFee The current trading fee (in basis points, 0-1000).
 * @property voteSlots Current fee votes from liquidity providers.
 * @property auctionSlot The current auction slot, if any.
 * @property ownerNode Hint for the owner directory page.
 * @property flags Bit-flags (reserved).
 */
public class Amm(
    override val index: Hash256,
    public val ammAccount: Address,
    public val asset: CurrencyAmount,
    public val asset2: CurrencyAmount,
    public val lpTokenBalance: CurrencyAmount,
    public val tradingFee: UInt,
    public val voteSlots: List<AmmVoteEntry> = emptyList(),
    public val auctionSlot: AmmAuctionSlot? = null,
    public val ownerNode: String? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.AMM

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Amm) return false
        return index == other.index &&
            ammAccount == other.ammAccount &&
            asset == other.asset &&
            asset2 == other.asset2 &&
            lpTokenBalance == other.lpTokenBalance &&
            tradingFee == other.tradingFee &&
            voteSlots == other.voteSlots &&
            auctionSlot == other.auctionSlot &&
            ownerNode == other.ownerNode &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + ammAccount.hashCode()
        result = 31 * result + asset.hashCode()
        result = 31 * result + asset2.hashCode()
        result = 31 * result + lpTokenBalance.hashCode()
        result = 31 * result + tradingFee.hashCode()
        result = 31 * result + voteSlots.hashCode()
        result = 31 * result + (auctionSlot?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Amm(ammAccount=$ammAccount, tradingFee=$tradingFee)"
}
