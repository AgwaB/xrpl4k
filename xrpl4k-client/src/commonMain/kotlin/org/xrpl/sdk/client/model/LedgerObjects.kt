package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Sealed interface representing a typed XRPL ledger entry object.
 *
 * Each subtype corresponds to a `LedgerEntryType` returned by `account_objects`.
 * Use [parseLedgerObject] to convert raw [JsonElement] responses into typed objects.
 */
public sealed interface LedgerObject {
    /** The ledger object index (hash). */
    public val index: String
}

/** A Check ledger object representing a deferred payment. */
public class CheckObject(
    override val index: String,
    public val account: Address,
    public val destination: Address,
    public val sendMax: JsonElement,
    public val sequence: UInt,
    public val expiration: UInt? = null,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CheckObject) return false
        return index == other.index &&
            account == other.account &&
            destination == other.destination &&
            sendMax == other.sendMax &&
            sequence == other.sequence &&
            expiration == other.expiration
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + sendMax.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + (expiration?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "CheckObject(index=$index, account=$account, destination=$destination, " +
            "sendMax=$sendMax, sequence=$sequence, expiration=$expiration)"
}

/** An Escrow ledger object representing a conditional XRP payment held in escrow. */
public class EscrowObject(
    override val index: String,
    public val account: Address,
    public val destination: Address,
    public val amount: XrpDrops,
    public val finishAfter: UInt? = null,
    public val cancelAfter: UInt? = null,
    public val condition: String? = null,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EscrowObject) return false
        return index == other.index &&
            account == other.account &&
            destination == other.destination &&
            amount == other.amount &&
            finishAfter == other.finishAfter &&
            cancelAfter == other.cancelAfter &&
            condition == other.condition
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (finishAfter?.hashCode() ?: 0)
        result = 31 * result + (cancelAfter?.hashCode() ?: 0)
        result = 31 * result + (condition?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "EscrowObject(index=$index, account=$account, destination=$destination, " +
            "amount=$amount, finishAfter=$finishAfter, cancelAfter=$cancelAfter, condition=$condition)"
}

/** An Offer ledger object representing an order on the decentralized exchange. */
public class OfferObject(
    override val index: String,
    public val account: Address,
    public val takerGets: JsonElement,
    public val takerPays: JsonElement,
    public val sequence: UInt,
    public val flags: UInt = 0u,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OfferObject) return false
        return index == other.index &&
            account == other.account &&
            takerGets == other.takerGets &&
            takerPays == other.takerPays &&
            sequence == other.sequence &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + takerGets.hashCode()
        result = 31 * result + takerPays.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String =
        "OfferObject(index=$index, account=$account, takerGets=$takerGets, " +
            "takerPays=$takerPays, sequence=$sequence, flags=$flags)"
}

/** A PayChannel ledger object representing a unidirectional XRP payment channel. */
public class PaymentChannelObject(
    override val index: String,
    public val account: Address,
    public val destination: Address,
    public val amount: XrpDrops,
    public val balance: XrpDrops,
    public val settleDelay: UInt,
    public val publicKey: String? = null,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaymentChannelObject) return false
        return index == other.index &&
            account == other.account &&
            destination == other.destination &&
            amount == other.amount &&
            balance == other.balance &&
            settleDelay == other.settleDelay &&
            publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + settleDelay.hashCode()
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PaymentChannelObject(index=$index, account=$account, destination=$destination, " +
            "amount=$amount, balance=$balance, settleDelay=$settleDelay, publicKey=$publicKey)"
}

/** A SignerList ledger object representing a list of authorized multi-signers. */
public class SignerListObject(
    override val index: String,
    public val signerQuorum: UInt,
    public val signerEntries: JsonElement,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignerListObject) return false
        return index == other.index &&
            signerQuorum == other.signerQuorum &&
            signerEntries == other.signerEntries
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + signerQuorum.hashCode()
        result = 31 * result + signerEntries.hashCode()
        return result
    }

    override fun toString(): String =
        "SignerListObject(index=$index, signerQuorum=$signerQuorum, signerEntries=$signerEntries)"
}

/** A Ticket ledger object representing a reserved sequence number for future use. */
public class TicketObject(
    override val index: String,
    public val account: Address,
    public val ticketSequence: UInt,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TicketObject) return false
        return index == other.index &&
            account == other.account &&
            ticketSequence == other.ticketSequence
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + ticketSequence.hashCode()
        return result
    }

    override fun toString(): String = "TicketObject(index=$index, account=$account, ticketSequence=$ticketSequence)"
}

/** A RippleState ledger object representing a trust line between two accounts. */
public class RippleStateObject(
    override val index: String,
    public val balance: JsonElement,
    public val highLimit: JsonElement,
    public val lowLimit: JsonElement,
    public val flags: UInt = 0u,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RippleStateObject) return false
        return index == other.index &&
            balance == other.balance &&
            highLimit == other.highLimit &&
            lowLimit == other.lowLimit &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + highLimit.hashCode()
        result = 31 * result + lowLimit.hashCode()
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String =
        "RippleStateObject(index=$index, balance=$balance, highLimit=$highLimit, " +
            "lowLimit=$lowLimit, flags=$flags)"
}

/** A DepositPreauth ledger object representing a preauthorization for deposit. */
public class DepositPreauthObject(
    override val index: String,
    public val account: Address,
    public val authorize: Address,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DepositPreauthObject) return false
        return index == other.index &&
            account == other.account &&
            authorize == other.authorize
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + authorize.hashCode()
        return result
    }

    override fun toString(): String = "DepositPreauthObject(index=$index, account=$account, authorize=$authorize)"
}

/** An NFTokenOffer ledger object representing a buy or sell offer for an NFT. */
public class NftOfferObject(
    override val index: String,
    public val owner: Address,
    public val nfTokenId: String,
    public val amount: JsonElement,
    public val flags: UInt = 0u,
    public val destination: Address? = null,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftOfferObject) return false
        return index == other.index &&
            owner == other.owner &&
            nfTokenId == other.nfTokenId &&
            amount == other.amount &&
            flags == other.flags &&
            destination == other.destination
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + nfTokenId.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (destination?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NftOfferObject(index=$index, owner=$owner, nfTokenId=$nfTokenId, " +
            "amount=$amount, flags=$flags, destination=$destination)"
}

/** Fallback for ledger entry types not yet modeled. */
public class UnknownLedgerObject(
    override val index: String,
    public val ledgerEntryType: String,
    public val raw: JsonElement,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownLedgerObject) return false
        return index == other.index &&
            ledgerEntryType == other.ledgerEntryType &&
            raw == other.raw
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + ledgerEntryType.hashCode()
        result = 31 * result + raw.hashCode()
        return result
    }

    override fun toString(): String = "UnknownLedgerObject(index=$index, ledgerEntryType=$ledgerEntryType, raw=$raw)"
}

// ── Parser ──────────────────────────────────────────────────────────────

/**
 * Parses a raw [JsonElement] (as returned by `account_objects`) into the
 * appropriate [LedgerObject] subtype based on the `LedgerEntryType` field.
 *
 * Returns [UnknownLedgerObject] for unrecognized entry types.
 */
public fun parseLedgerObject(json: JsonElement): LedgerObject {
    val obj = json.jsonObject
    val entryType = obj.string("LedgerEntryType") ?: "Unknown"
    val index = obj.string("index") ?: ""

    return when (entryType) {
        "Check" ->
            CheckObject(
                index = index,
                account = Address(obj.requireString("Account")),
                destination = Address(obj.requireString("Destination")),
                sendMax = obj.getValue("SendMax"),
                sequence = obj.uInt("Sequence"),
                expiration = obj.uIntOrNull("Expiration"),
            )

        "Escrow" ->
            EscrowObject(
                index = index,
                account = Address(obj.requireString("Account")),
                destination = Address(obj.requireString("Destination")),
                amount = XrpDrops(obj.requireString("Amount").toLong()),
                finishAfter = obj.uIntOrNull("FinishAfter"),
                cancelAfter = obj.uIntOrNull("CancelAfter"),
                condition = obj.string("Condition"),
            )

        "Offer" ->
            OfferObject(
                index = index,
                account = Address(obj.requireString("Account")),
                takerGets = obj.getValue("TakerGets"),
                takerPays = obj.getValue("TakerPays"),
                sequence = obj.uInt("Sequence"),
                flags = obj.uIntOrNull("Flags") ?: 0u,
            )

        "PayChannel" ->
            PaymentChannelObject(
                index = index,
                account = Address(obj.requireString("Account")),
                destination = Address(obj.requireString("Destination")),
                amount = XrpDrops(obj.requireString("Amount").toLong()),
                balance = XrpDrops(obj.requireString("Balance").toLong()),
                settleDelay = obj.uInt("SettleDelay"),
                publicKey = obj.string("PublicKey"),
            )

        "SignerList" ->
            SignerListObject(
                index = index,
                signerQuorum = obj.uInt("SignerQuorum"),
                signerEntries = obj.getValue("SignerEntries"),
            )

        "Ticket" ->
            TicketObject(
                index = index,
                account = Address(obj.requireString("Account")),
                ticketSequence = obj.uInt("TicketSequence"),
            )

        "RippleState" ->
            RippleStateObject(
                index = index,
                balance = obj.getValue("Balance"),
                highLimit = obj.getValue("HighLimit"),
                lowLimit = obj.getValue("LowLimit"),
                flags = obj.uIntOrNull("Flags") ?: 0u,
            )

        "DepositPreauth" ->
            DepositPreauthObject(
                index = index,
                account = Address(obj.requireString("Account")),
                authorize = Address(obj.requireString("Authorize")),
            )

        "NFTokenOffer" ->
            NftOfferObject(
                index = index,
                owner = Address(obj.requireString("Owner")),
                nfTokenId = obj.requireString("NFTokenID"),
                amount = obj.getValue("Amount"),
                flags = obj.uIntOrNull("Flags") ?: 0u,
                destination = obj.string("Destination")?.let { Address(it) },
            )

        else ->
            UnknownLedgerObject(
                index = index,
                ledgerEntryType = entryType,
                raw = json,
            )
    }
}

// ── Internal helpers ────────────────────────────────────────────────────

private fun JsonObject.string(key: String): String? = get(key)?.jsonPrimitive?.content

private fun JsonObject.requireString(key: String): String = getValue(key).jsonPrimitive.content

private fun JsonObject.uInt(key: String): UInt = getValue(key).jsonPrimitive.long.toUInt()

private fun JsonObject.uIntOrNull(key: String): UInt? = get(key)?.jsonPrimitive?.longOrNull?.toUInt()
