@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.MptAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.AccountDeleteFields
import org.xrpl.sdk.core.model.transaction.AccountSetFields
import org.xrpl.sdk.core.model.transaction.CheckCancelFields
import org.xrpl.sdk.core.model.transaction.CheckCashFields
import org.xrpl.sdk.core.model.transaction.CheckCreateFields
import org.xrpl.sdk.core.model.transaction.ClawbackFields
import org.xrpl.sdk.core.model.transaction.DepositPreauthFields
import org.xrpl.sdk.core.model.transaction.EscrowCancelFields
import org.xrpl.sdk.core.model.transaction.EscrowCreateFields
import org.xrpl.sdk.core.model.transaction.EscrowFinishFields
import org.xrpl.sdk.core.model.transaction.OfferCancelFields
import org.xrpl.sdk.core.model.transaction.OfferCreateFields
import org.xrpl.sdk.core.model.transaction.PaymentChannelClaimFields
import org.xrpl.sdk.core.model.transaction.PaymentChannelCreateFields
import org.xrpl.sdk.core.model.transaction.PaymentChannelFundFields
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.SetRegularKeyFields
import org.xrpl.sdk.core.model.transaction.SignerListSetFields
import org.xrpl.sdk.core.model.transaction.TicketCreateFields
import org.xrpl.sdk.core.model.transaction.TransactionFields
import org.xrpl.sdk.core.model.transaction.TrustSetFields
import org.xrpl.sdk.core.model.transaction.UnknownTransactionFields
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.codec.AddressCodec

/**
 * Serializes a [XrplTransaction.Filled] to the JSON format expected by [org.xrpl.sdk.codec.BinaryCodec].
 *
 * The BinaryCodec requires:
 * - TransactionType as an integer code (e.g. Payment = 0), not a string name
 * - AccountID fields as 40-char lowercase hex (20-byte account IDs), not classic r-addresses
 * - UInt32 fields (Sequence, LastLedgerSequence, etc.) as Long
 * - UInt16 fields (TransactionType) as Int
 */
internal object FilledTransactionSerializer {
    private val json = Json { encodeDefaults = false }

    /**
     * Maps XRPL transaction type names to their integer codes.
     * Sourced from the canonical ripple-binary-codec definitions.json TRANSACTION_TYPES section.
     */
    private val TRANSACTION_TYPE_CODES: Map<String, Int> =
        mapOf(
            "AMMBid" to 39,
            "AMMClawback" to 31,
            "AMMCreate" to 35,
            "AMMDelete" to 40,
            "AMMDeposit" to 36,
            "AMMVote" to 38,
            "AMMWithdraw" to 37,
            "AccountDelete" to 21,
            "AccountSet" to 3,
            "Batch" to 71,
            "CheckCancel" to 18,
            "CheckCash" to 17,
            "CheckCreate" to 16,
            "Clawback" to 30,
            "CredentialAccept" to 59,
            "CredentialCreate" to 58,
            "CredentialDelete" to 60,
            "DIDDelete" to 50,
            "DIDSet" to 49,
            "DelegateSet" to 64,
            "DepositPreauth" to 19,
            "EnableAmendment" to 100,
            "EscrowCancel" to 4,
            "EscrowCreate" to 1,
            "EscrowFinish" to 2,
            "Invalid" to -1,
            "LedgerStateFix" to 53,
            "LoanBrokerCoverClawback" to 78,
            "LoanBrokerCoverDeposit" to 76,
            "LoanBrokerCoverWithdraw" to 77,
            "LoanBrokerDelete" to 75,
            "LoanBrokerSet" to 74,
            "LoanDelete" to 81,
            "LoanManage" to 82,
            "LoanPay" to 84,
            "LoanSet" to 80,
            "MPTokenAuthorize" to 57,
            "MPTokenIssuanceCreate" to 54,
            "MPTokenIssuanceDestroy" to 55,
            "MPTokenIssuanceSet" to 56,
            "NFTokenAcceptOffer" to 29,
            "NFTokenBurn" to 26,
            "NFTokenCancelOffer" to 28,
            "NFTokenCreateOffer" to 27,
            "NFTokenMint" to 25,
            "NFTokenModify" to 61,
            "OfferCancel" to 8,
            "OfferCreate" to 7,
            "OracleDelete" to 52,
            "OracleSet" to 51,
            "Payment" to 0,
            "PaymentChannelClaim" to 15,
            "PaymentChannelCreate" to 13,
            "PaymentChannelFund" to 14,
            "PermissionedDomainDelete" to 63,
            "PermissionedDomainSet" to 62,
            "SetFee" to 101,
            "SetRegularKey" to 5,
            "SignerListSet" to 12,
            "TicketCreate" to 10,
            "TrustSet" to 20,
            "UNLModify" to 102,
            "VaultClawback" to 70,
            "VaultCreate" to 65,
            "VaultDelete" to 67,
            "VaultDeposit" to 68,
            "VaultSet" to 66,
            "VaultWithdraw" to 69,
            "XChainAccountCreateCommit" to 44,
            "XChainAddAccountCreateAttestation" to 46,
            "XChainAddClaimAttestation" to 45,
            "XChainClaim" to 43,
            "XChainCommit" to 42,
            "XChainCreateBridge" to 48,
            "XChainCreateClaimID" to 41,
            "XChainModifyBridge" to 47,
        )

    /**
     * Converts a Filled transaction to a Map<String, Any?> suitable for BinaryCodec.
     */
    fun toCodecMap(transaction: XrplTransaction.Filled): Map<String, Any?> {
        val map = linkedMapOf<String, Any?>()

        // TransactionType must be an Int (the numeric code), not the string name
        val txTypeCode =
            TRANSACTION_TYPE_CODES[transaction.transactionType.value]
                ?: error("Unknown transaction type: ${transaction.transactionType.value}")
        map["TransactionType"] = txTypeCode

        // AccountID fields must be 40-char lowercase hex, not r-addresses
        map["Account"] = addressToHex(transaction.account)
        map["Fee"] = transaction.fee.value.toString()
        // UInt32 fields must be Long
        map["Sequence"] = transaction.sequence.toLong()
        map["LastLedgerSequence"] = transaction.lastLedgerSequence.toLong()

        // Optional common fields
        transaction.sourceTag?.let { map["SourceTag"] = it.toLong() }
        transaction.accountTxnId?.let { map["AccountTxnID"] = it.value }
        transaction.ticketSequence?.let { map["TicketSequence"] = it.toLong() }
        transaction.networkId?.let { map["NetworkID"] = it.toLong() }

        // Memos
        if (transaction.memos.isNotEmpty()) {
            map["Memos"] =
                transaction.memos.map { memo ->
                    val memoObj = linkedMapOf<String, Any?>()
                    memo.memoData?.let { memoObj["MemoData"] = it }
                    memo.memoType?.let { memoObj["MemoType"] = it }
                    memo.memoFormat?.let { memoObj["MemoFormat"] = it }
                    mapOf("Memo" to memoObj)
                }
        }

        // Signers
        if (transaction.signers.isNotEmpty()) {
            map["Signers"] =
                transaction.signers.map { signer ->
                    mapOf(
                        "Signer" to
                            mapOf(
                                "Account" to addressToHex(signer.account),
                                "TxnSignature" to signer.txnSignature,
                                "SigningPubKey" to signer.signingPubKey,
                            ),
                    )
                }
        }

        // Transaction-type-specific fields
        serializeFields(transaction.fields, map)

        return map
    }

    /**
     * Converts a Filled transaction to a JSON string suitable for BinaryCodec.
     */
    fun toJsonString(transaction: XrplTransaction.Filled): String {
        val map = toCodecMap(transaction)
        return mapToJsonString(map)
    }

    private fun addressToHex(address: Address): String = AddressCodec.decodeAddress(address).hex

    private fun serializeFields(
        fields: TransactionFields,
        map: MutableMap<String, Any?>,
    ) {
        when (fields) {
            is PaymentFields -> serializePaymentFields(fields, map)
            is OfferCreateFields -> serializeOfferCreateFields(fields, map)
            is OfferCancelFields -> serializeOfferCancelFields(fields, map)
            is TrustSetFields -> serializeTrustSetFields(fields, map)
            is AccountSetFields -> serializeAccountSetFields(fields, map)
            is AccountDeleteFields -> serializeAccountDeleteFields(fields, map)
            is EscrowCreateFields -> serializeEscrowCreateFields(fields, map)
            is EscrowFinishFields -> serializeEscrowFinishFields(fields, map)
            is EscrowCancelFields -> serializeEscrowCancelFields(fields, map)
            is SetRegularKeyFields -> serializeSetRegularKeyFields(fields, map)
            is SignerListSetFields -> serializeSignerListSetFields(fields, map)
            is TicketCreateFields -> serializeTicketCreateFields(fields, map)
            is CheckCreateFields -> serializeCheckCreateFields(fields, map)
            is CheckCashFields -> serializeCheckCashFields(fields, map)
            is CheckCancelFields -> serializeCheckCancelFields(fields, map)
            is PaymentChannelCreateFields -> serializePaymentChannelCreateFields(fields, map)
            is PaymentChannelFundFields -> serializePaymentChannelFundFields(fields, map)
            is PaymentChannelClaimFields -> serializePaymentChannelClaimFields(fields, map)
            is DepositPreauthFields -> serializeDepositPreauthFields(fields, map)
            is ClawbackFields -> serializeClawbackFields(fields, map)
            is UnknownTransactionFields -> serializeUnknownFields(fields, map)
            else -> { /* unknown type — no fields emitted */ }
        }
    }

    private fun serializePaymentFields(
        fields: PaymentFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Destination"] = addressToHex(fields.destination)
        map["Amount"] = serializeAmount(fields.amount)
        fields.sendMax?.let { map["SendMax"] = serializeAmount(it) }
        fields.deliverMin?.let { map["DeliverMin"] = serializeAmount(it) }
        fields.destinationTag?.let { map["DestinationTag"] = it.toLong() }
        fields.invoiceId?.let { map["InvoiceID"] = it }
        fields.paths?.let { pathsList ->
            map["Paths"] =
                pathsList.map { path ->
                    path.map { step ->
                        val stepMap = linkedMapOf<String, Any?>()
                        step.account?.let { stepMap["account"] = addressToHex(it) }
                        step.currency?.let { stepMap["currency"] = it }
                        step.issuer?.let { stepMap["issuer"] = addressToHex(it) }
                        stepMap
                    }
                }
        }
    }

    private fun serializeOfferCreateFields(
        fields: OfferCreateFields,
        map: MutableMap<String, Any?>,
    ) {
        map["TakerGets"] = serializeAmount(fields.takerGets)
        map["TakerPays"] = serializeAmount(fields.takerPays)
        fields.expiration?.let { map["Expiration"] = it.toLong() }
        fields.offerSequence?.let { map["OfferSequence"] = it.toLong() }
    }

    private fun serializeOfferCancelFields(
        fields: OfferCancelFields,
        map: MutableMap<String, Any?>,
    ) {
        map["OfferSequence"] = fields.offerSequence.toLong()
    }

    private fun serializeTrustSetFields(
        fields: TrustSetFields,
        map: MutableMap<String, Any?>,
    ) {
        map["LimitAmount"] = serializeIssuedAmount(fields.limitAmount)
        fields.qualityIn?.let { map["QualityIn"] = it.toLong() }
        fields.qualityOut?.let { map["QualityOut"] = it.toLong() }
    }

    private fun serializeAccountSetFields(
        fields: AccountSetFields,
        map: MutableMap<String, Any?>,
    ) {
        fields.clearFlag?.let { map["ClearFlag"] = it.toLong() }
        fields.setFlag?.let { map["SetFlag"] = it.toLong() }
        fields.domain?.let { map["Domain"] = it }
        fields.emailHash?.let { map["EmailHash"] = it }
        fields.transferRate?.let { map["TransferRate"] = it.toLong() }
        fields.tickSize?.let { map["TickSize"] = it.toLong() }
        fields.nftTokenMinter?.let { map["NFTokenMinter"] = addressToHex(it) }
    }

    private fun serializeAccountDeleteFields(
        fields: AccountDeleteFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Destination"] = addressToHex(fields.destination)
        fields.destinationTag?.let { map["DestinationTag"] = it.toLong() }
    }

    private fun serializeEscrowCreateFields(
        fields: EscrowCreateFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Destination"] = addressToHex(fields.destination)
        map["Amount"] = serializeAmount(fields.amount)
        fields.finishAfter?.let { map["FinishAfter"] = it.toLong() }
        fields.cancelAfter?.let { map["CancelAfter"] = it.toLong() }
        fields.condition?.let { map["Condition"] = it }
        fields.destinationTag?.let { map["DestinationTag"] = it.toLong() }
    }

    private fun serializeEscrowFinishFields(
        fields: EscrowFinishFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Owner"] = addressToHex(fields.owner)
        map["OfferSequence"] = fields.offerSequence.toLong()
        fields.condition?.let { map["Condition"] = it }
        fields.fulfillment?.let { map["Fulfillment"] = it }
    }

    private fun serializeEscrowCancelFields(
        fields: EscrowCancelFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Owner"] = addressToHex(fields.owner)
        map["OfferSequence"] = fields.offerSequence.toLong()
    }

    private fun serializeSetRegularKeyFields(
        fields: SetRegularKeyFields,
        map: MutableMap<String, Any?>,
    ) {
        fields.regularKey?.let { map["RegularKey"] = addressToHex(it) }
    }

    private fun serializeSignerListSetFields(
        fields: SignerListSetFields,
        map: MutableMap<String, Any?>,
    ) {
        map["SignerQuorum"] = fields.signerQuorum.toLong()
        if (fields.signerEntries.isNotEmpty()) {
            map["SignerEntries"] =
                fields.signerEntries.map { entry ->
                    mapOf(
                        "SignerEntry" to
                            mapOf(
                                "Account" to addressToHex(entry.account),
                                "SignerWeight" to entry.signerWeight.toLong(),
                            ),
                    )
                }
        }
    }

    private fun serializeTicketCreateFields(
        fields: TicketCreateFields,
        map: MutableMap<String, Any?>,
    ) {
        map["TicketCount"] = fields.ticketCount.toLong()
    }

    private fun serializeCheckCreateFields(
        fields: CheckCreateFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Destination"] = addressToHex(fields.destination)
        map["SendMax"] = serializeAmount(fields.sendMax)
        fields.destinationTag?.let { map["DestinationTag"] = it.toLong() }
        fields.expiration?.let { map["Expiration"] = it.toLong() }
        fields.invoiceId?.let { map["InvoiceID"] = it }
    }

    private fun serializeCheckCashFields(
        fields: CheckCashFields,
        map: MutableMap<String, Any?>,
    ) {
        map["CheckID"] = fields.checkId
        fields.amount?.let { map["Amount"] = serializeAmount(it) }
        fields.deliverMin?.let { map["DeliverMin"] = serializeAmount(it) }
    }

    private fun serializeCheckCancelFields(
        fields: CheckCancelFields,
        map: MutableMap<String, Any?>,
    ) {
        map["CheckID"] = fields.checkId
    }

    private fun serializePaymentChannelCreateFields(
        fields: PaymentChannelCreateFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Destination"] = addressToHex(fields.destination)
        map["Amount"] = serializeAmount(fields.amount)
        map["SettleDelay"] = fields.settleDelay.toLong()
        map["PublicKey"] = fields.publicKey
        fields.cancelAfter?.let { map["CancelAfter"] = it.toLong() }
        fields.destinationTag?.let { map["DestinationTag"] = it.toLong() }
    }

    private fun serializePaymentChannelFundFields(
        fields: PaymentChannelFundFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Channel"] = fields.channel
        map["Amount"] = serializeAmount(fields.amount)
        fields.expiration?.let { map["Expiration"] = it.toLong() }
    }

    private fun serializePaymentChannelClaimFields(
        fields: PaymentChannelClaimFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Channel"] = fields.channel
        fields.balance?.let { map["Balance"] = serializeAmount(it) }
        fields.amount?.let { map["Amount"] = serializeAmount(it) }
        fields.signature?.let { map["Signature"] = it }
        fields.publicKey?.let { map["PublicKey"] = it }
    }

    private fun serializeDepositPreauthFields(
        fields: DepositPreauthFields,
        map: MutableMap<String, Any?>,
    ) {
        fields.authorize?.let { map["Authorize"] = addressToHex(it) }
        fields.unauthorize?.let { map["Unauthorize"] = addressToHex(it) }
    }

    private fun serializeClawbackFields(
        fields: ClawbackFields,
        map: MutableMap<String, Any?>,
    ) {
        map["Amount"] = serializeAmount(fields.amount)
        fields.holder?.let { map["Holder"] = addressToHex(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun serializeUnknownFields(
        fields: UnknownTransactionFields,
        map: MutableMap<String, Any?>,
    ) {
        map.putAll(fields.fields)
    }

    private fun serializeAmount(amount: CurrencyAmount): Any =
        when (amount) {
            is XrpAmount -> amount.drops.value.toString()
            is IssuedAmount -> serializeIssuedAmount(amount)
            is MptAmount ->
                mapOf(
                    "mpt_issuance_id" to amount.mptIssuanceId,
                    "value" to amount.value,
                )
        }

    private fun serializeIssuedAmount(amount: IssuedAmount): Map<String, String> =
        mapOf(
            "currency" to amount.currency.value,
            "issuer" to addressToHex(amount.issuer),
            "value" to amount.value,
        )

    // -- JSON serialization helpers --

    internal fun mapToJsonString(map: Map<String, Any?>): String {
        val jsonObject = mapToJsonObject(map)
        return json.encodeToString(JsonObject.serializer(), jsonObject)
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToJsonObject(map: Map<String, Any?>): JsonObject {
        val content = linkedMapOf<String, JsonElement>()
        for ((key, value) in map) {
            content[key] = valueToJsonElement(value)
        }
        return JsonObject(content)
    }

    @Suppress("UNCHECKED_CAST")
    private fun valueToJsonElement(value: Any?): JsonElement =
        when (value) {
            null -> JsonPrimitive(null as String?)
            is String -> JsonPrimitive(value)
            is Int -> JsonPrimitive(value)
            is Long -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> mapToJsonObject(value as Map<String, Any?>)
            is List<*> -> JsonArray(value.map { valueToJsonElement(it) })
            else -> JsonPrimitive(value.toString())
        }
}
