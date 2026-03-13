package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

public class AccountInfo(
    public val account: Address,
    public val balance: XrpDrops,
    public val sequence: UInt,
    public val ownerCount: UInt,
    public val flags: UInt,
    public val previousAffectingTransactionId: TxHash?,
    public val previousAffectingTransactionLedgerSequence: UInt?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountInfo) return false
        return account == other.account &&
            balance == other.balance &&
            sequence == other.sequence &&
            ownerCount == other.ownerCount &&
            flags == other.flags &&
            previousAffectingTransactionId == other.previousAffectingTransactionId &&
            previousAffectingTransactionLedgerSequence == other.previousAffectingTransactionLedgerSequence &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + ownerCount.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (previousAffectingTransactionId?.hashCode() ?: 0)
        result = 31 * result + (previousAffectingTransactionLedgerSequence?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountInfo(" +
            "account=$account, " +
            "balance=$balance, " +
            "sequence=$sequence, " +
            "ownerCount=$ownerCount, " +
            "flags=$flags, " +
            "previousAffectingTransactionId=$previousAffectingTransactionId, " +
            "previousAffectingTransactionLedgerSequence=$previousAffectingTransactionLedgerSequence, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class TrustLine(
    public val account: Address,
    public val balance: String,
    public val currency: String,
    public val limit: String,
    public val limitPeer: String,
    public val noRipple: Boolean?,
    public val noRipplePeer: Boolean?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrustLine) return false
        return account == other.account &&
            balance == other.balance &&
            currency == other.currency &&
            limit == other.limit &&
            limitPeer == other.limitPeer &&
            noRipple == other.noRipple &&
            noRipplePeer == other.noRipplePeer
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + limit.hashCode()
        result = 31 * result + limitPeer.hashCode()
        result = 31 * result + (noRipple?.hashCode() ?: 0)
        result = 31 * result + (noRipplePeer?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "TrustLine(" +
            "account=$account, " +
            "balance=$balance, " +
            "currency=$currency, " +
            "limit=$limit, " +
            "limitPeer=$limitPeer, " +
            "noRipple=$noRipple, " +
            "noRipplePeer=$noRipplePeer" +
            ")"
}

public class AccountLinesResult(
    public val account: Address,
    public val lines: List<TrustLine>,
    public val marker: JsonElement?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountLinesResult) return false
        return account == other.account &&
            lines == other.lines &&
            marker == other.marker &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + lines.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountLinesResult(" +
            "account=$account, " +
            "lines=$lines, " +
            "marker=$marker, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class AccountObjectsResult(
    public val account: Address,
    public val accountObjects: List<JsonElement>,
    public val marker: JsonElement?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountObjectsResult) return false
        return account == other.account &&
            accountObjects == other.accountObjects &&
            marker == other.marker &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + accountObjects.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountObjectsResult(" +
            "account=$account, " +
            "accountObjects=$accountObjects, " +
            "marker=$marker, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class AccountOffer(
    public val flags: UInt,
    public val seq: UInt,
    public val takerGets: JsonElement,
    public val takerPays: JsonElement,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountOffer) return false
        return flags == other.flags &&
            seq == other.seq &&
            takerGets == other.takerGets &&
            takerPays == other.takerPays
    }

    override fun hashCode(): Int {
        var result = flags.hashCode()
        result = 31 * result + seq.hashCode()
        result = 31 * result + takerGets.hashCode()
        result = 31 * result + takerPays.hashCode()
        return result
    }

    override fun toString(): String =
        "AccountOffer(" +
            "flags=$flags, " +
            "seq=$seq, " +
            "takerGets=$takerGets, " +
            "takerPays=$takerPays" +
            ")"
}

public class AccountOffersResult(
    public val account: Address,
    public val offers: List<AccountOffer>,
    public val marker: JsonElement?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountOffersResult) return false
        return account == other.account &&
            offers == other.offers &&
            marker == other.marker &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + offers.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountOffersResult(" +
            "account=$account, " +
            "offers=$offers, " +
            "marker=$marker, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class AccountTxEntry(
    public val tx: JsonElement?,
    public val meta: JsonElement?,
    public val validated: Boolean?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountTxEntry) return false
        return tx == other.tx &&
            meta == other.meta &&
            validated == other.validated
    }

    override fun hashCode(): Int {
        var result = (tx?.hashCode() ?: 0)
        result = 31 * result + (meta?.hashCode() ?: 0)
        result = 31 * result + (validated?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountTxEntry(" +
            "tx=$tx, " +
            "meta=$meta, " +
            "validated=$validated" +
            ")"
}

public class AccountTxResult(
    public val account: Address,
    public val transactions: List<AccountTxEntry>,
    public val marker: JsonElement?,
    public val ledgerIndexMin: LedgerIndex?,
    public val ledgerIndexMax: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountTxResult) return false
        return account == other.account &&
            transactions == other.transactions &&
            marker == other.marker &&
            ledgerIndexMin == other.ledgerIndexMin &&
            ledgerIndexMax == other.ledgerIndexMax
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + transactions.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndexMin?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndexMax?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountTxResult(" +
            "account=$account, " +
            "transactions=$transactions, " +
            "marker=$marker, " +
            "ledgerIndexMin=$ledgerIndexMin, " +
            "ledgerIndexMax=$ledgerIndexMax" +
            ")"
}

public class PaymentChannel(
    public val channelId: String,
    public val account: Address,
    public val destinationAccount: Address,
    public val amount: XrpDrops,
    public val balance: XrpDrops,
    public val settleDelay: UInt,
    public val publicKey: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaymentChannel) return false
        return channelId == other.channelId &&
            account == other.account &&
            destinationAccount == other.destinationAccount &&
            amount == other.amount &&
            balance == other.balance &&
            settleDelay == other.settleDelay &&
            publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        var result = channelId.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + destinationAccount.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + settleDelay.hashCode()
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PaymentChannel(" +
            "channelId=$channelId, " +
            "account=$account, " +
            "destinationAccount=$destinationAccount, " +
            "amount=$amount, " +
            "balance=$balance, " +
            "settleDelay=$settleDelay, " +
            "publicKey=$publicKey" +
            ")"
}

public class AccountChannelsResult(
    public val account: Address,
    public val channels: List<PaymentChannel>,
    public val marker: JsonElement?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountChannelsResult) return false
        return account == other.account &&
            channels == other.channels &&
            marker == other.marker &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + channels.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountChannelsResult(" +
            "account=$account, " +
            "channels=$channels, " +
            "marker=$marker, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class AccountCurrenciesResult(
    public val receiveCurrencies: List<String>,
    public val sendCurrencies: List<String>,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountCurrenciesResult) return false
        return receiveCurrencies == other.receiveCurrencies &&
            sendCurrencies == other.sendCurrencies &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = receiveCurrencies.hashCode()
        result = 31 * result + sendCurrencies.hashCode()
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountCurrenciesResult(" +
            "receiveCurrencies=$receiveCurrencies, " +
            "sendCurrencies=$sendCurrencies, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class Nft(
    public val flags: UInt,
    public val issuer: Address,
    public val nftokenId: String,
    public val nftokenTaxon: ULong,
    public val nftSerial: ULong?,
    public val uri: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Nft) return false
        return flags == other.flags &&
            issuer == other.issuer &&
            nftokenId == other.nftokenId &&
            nftokenTaxon == other.nftokenTaxon &&
            nftSerial == other.nftSerial &&
            uri == other.uri
    }

    override fun hashCode(): Int {
        var result = flags.hashCode()
        result = 31 * result + issuer.hashCode()
        result = 31 * result + nftokenId.hashCode()
        result = 31 * result + nftokenTaxon.hashCode()
        result = 31 * result + (nftSerial?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Nft(" +
            "flags=$flags, " +
            "issuer=$issuer, " +
            "nftokenId=$nftokenId, " +
            "nftokenTaxon=$nftokenTaxon, " +
            "nftSerial=$nftSerial, " +
            "uri=$uri" +
            ")"
}

public class AccountNftsResult(
    public val account: Address,
    public val accountNfts: List<Nft>,
    public val marker: JsonElement?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountNftsResult) return false
        return account == other.account &&
            accountNfts == other.accountNfts &&
            marker == other.marker &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + accountNfts.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountNftsResult(" +
            "account=$account, " +
            "accountNfts=$accountNfts, " +
            "marker=$marker, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class IssuedCurrencyBalance(
    public val currency: String,
    public val value: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuedCurrencyBalance) return false
        return currency == other.currency && value == other.value
    }

    override fun hashCode(): Int {
        var result = currency.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String = "IssuedCurrencyBalance(currency=$currency, value=$value)"
}

public class GatewayBalancesResult(
    public val account: Address,
    public val obligations: Map<String, String>,
    public val balances: Map<String, List<IssuedCurrencyBalance>>,
    public val assets: Map<String, List<IssuedCurrencyBalance>>,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GatewayBalancesResult) return false
        return account == other.account &&
            obligations == other.obligations &&
            balances == other.balances &&
            assets == other.assets &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + obligations.hashCode()
        result = 31 * result + balances.hashCode()
        result = 31 * result + assets.hashCode()
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "GatewayBalancesResult(" +
            "account=$account, " +
            "obligations=$obligations, " +
            "balances=$balances, " +
            "assets=$assets, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}

public class NorippleCheckResult(
    public val problems: List<String>,
    public val transactions: List<JsonElement>?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NorippleCheckResult) return false
        return problems == other.problems &&
            transactions == other.transactions &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = problems.hashCode()
        result = 31 * result + (transactions?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NorippleCheckResult(" +
            "problems=$problems, " +
            "transactions=$transactions, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}
