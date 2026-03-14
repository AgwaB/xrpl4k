package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.XrplJson
import org.xrpl.sdk.client.internal.dto.SimulateRequest
import org.xrpl.sdk.client.internal.dto.SimulateResponseDto
import org.xrpl.sdk.client.internal.dto.SubmitMultisignedRequest
import org.xrpl.sdk.client.internal.dto.SubmitRequest
import org.xrpl.sdk.client.internal.dto.SubmitResponseDto
import org.xrpl.sdk.client.internal.dto.TransactionEntryRequest
import org.xrpl.sdk.client.internal.dto.TransactionEntryResponseDto
import org.xrpl.sdk.client.internal.dto.TxRequest
import org.xrpl.sdk.client.internal.dto.TxResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.LedgerSpecifier
import org.xrpl.sdk.client.model.SimulateResult
import org.xrpl.sdk.client.model.SubmitResult
import org.xrpl.sdk.client.model.TransactionResult
import org.xrpl.sdk.client.model.ValidatedTransaction
import org.xrpl.sdk.client.signing.FilledTransactionSerializer
import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Submits a signed transaction to the XRPL network.
 *
 * @param transaction The signed transaction to submit.
 * @return [XrplResult] containing [SubmitResult] or a categorized failure.
 */
public suspend fun XrplClient.submit(transaction: XrplTransaction.Signed): XrplResult<SubmitResult> {
    val request = SubmitRequest(txBlob = transaction.txBlob)
    return executeRpc(
        method = "submit",
        request = request,
        requestSerializer = SubmitRequest.serializer(),
        responseDeserializer = SubmitResponseDto.serializer(),
    ) { dto ->
        SubmitResult(
            engineResult = dto.engineResult,
            engineResultCode = dto.engineResultCode,
            engineResultMessage = dto.engineResultMessage,
            txHash =
                dto.txJson
                    ?.let { extractHashFromTxJson(it) }
                    ?: runCatching { transaction.hash }.getOrNull(),
            accepted = dto.accepted,
            applied = dto.applied,
            broadcast = dto.broadcast,
            kept = dto.kept,
            queued = dto.queued,
            openLedgerCost = dto.openLedgerCost?.toLongOrNull()?.let { XrpDrops(it) },
            validatedLedgerIndex = dto.validatedLedgerIndex?.let { LedgerIndex(it) },
        )
    }
}

/**
 * Submits a multi-signed transaction to the XRPL network.
 *
 * @param transaction The filled (multi-signed) transaction to submit.
 * @return [XrplResult] containing [SubmitResult] or a categorized failure.
 */
public suspend fun XrplClient.submitMultisigned(transaction: XrplTransaction.Filled): XrplResult<SubmitResult> {
    val txJsonString =
        FilledTransactionSerializer.mapToJsonString(
            FilledTransactionSerializer.toCodecMap(transaction),
        )
    val txJsonElement: JsonElement = XrplJson.decodeFromString(JsonElement.serializer(), txJsonString)
    val request = SubmitMultisignedRequest(tx_json = txJsonElement)
    return executeRpc(
        method = "submit_multisigned",
        request = request,
        requestSerializer = SubmitMultisignedRequest.serializer(),
        responseDeserializer = SubmitResponseDto.serializer(),
    ) { dto ->
        SubmitResult(
            engineResult = dto.engineResult,
            engineResultCode = dto.engineResultCode,
            engineResultMessage = dto.engineResultMessage,
            txHash = dto.txJson?.let { extractHashFromTxJson(it) },
            accepted = dto.accepted,
            applied = dto.applied,
            broadcast = dto.broadcast,
            kept = dto.kept,
            queued = dto.queued,
            openLedgerCost = dto.openLedgerCost?.toLongOrNull()?.let { XrpDrops(it) },
            validatedLedgerIndex = dto.validatedLedgerIndex?.let { LedgerIndex(it) },
        )
    }
}

/**
 * Looks up a transaction by its hash.
 *
 * @param hash The 64-character hex transaction hash.
 * @param binary Whether to return the transaction as a binary hex blob.
 * @param minLedger The earliest ledger to search (inclusive).
 * @param maxLedger The latest ledger to search (inclusive).
 * @return [XrplResult] containing [TransactionResult] or a categorized failure.
 */
public suspend fun XrplClient.tx(
    hash: TxHash,
    binary: Boolean = false,
    minLedger: LedgerIndex? = null,
    maxLedger: LedgerIndex? = null,
): XrplResult<TransactionResult> {
    val request =
        TxRequest(
            transaction = hash.value,
            binary = binary,
            minLedger = minLedger?.value,
            maxLedger = maxLedger?.value,
        )
    return executeRpc(
        method = "tx",
        request = request,
        requestSerializer = TxRequest.serializer(),
        responseDeserializer = TxResponseDto.serializer(),
    ) { dto ->
        // Try to extract engine result from meta JSON if not top-level
        val metaObj = dto.meta as? JsonObject
        val engineResult =
            dto.engineResult
                ?: (metaObj?.get("TransactionResult") as? JsonPrimitive)?.takeIf { it.isString }?.content
        val engineResultCode = dto.engineResultCode
        TransactionResult(
            hash = dto.hash?.takeIf { it.length == 64 }?.let { TxHash(it) },
            ledgerIndex = (dto.ledgerIndex ?: dto.inLedger)?.let { LedgerIndex(it) },
            meta = dto.meta,
            validated = dto.validated,
            txJson = dto.txJson,
            closeTimeIso = dto.closeTimeIso,
            engineResult = engineResult,
            engineResultCode = engineResultCode,
        )
    }
}

/**
 * Retrieves a transaction from a specific validated ledger by its hash.
 *
 * Unlike [tx], this method only searches within a specific ledger version.
 *
 * @param txHash The 64-character hex transaction hash.
 * @param ledger The ledger version to search.
 * @return [XrplResult] containing [ValidatedTransaction] or a categorized failure.
 */
public suspend fun XrplClient.transactionEntry(
    txHash: TxHash,
    ledger: LedgerSpecifier = LedgerSpecifier.Validated,
): XrplResult<ValidatedTransaction> {
    val (paramKey, paramValue) = ledger.toParamPair()
    val request =
        TransactionEntryRequest(
            txHash = txHash.value,
            ledgerIndex = if (paramKey == "ledger_index") paramValue else null,
            ledgerHash = if (paramKey == "ledger_hash") paramValue else null,
        )
    return executeRpc(
        method = "transaction_entry",
        request = request,
        requestSerializer = TransactionEntryRequest.serializer(),
        responseDeserializer = TransactionEntryResponseDto.serializer(),
    ) { dto ->
        ValidatedTransaction(
            txJson = dto.txJson,
            metadata = dto.metadata,
            ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it) },
            ledgerHash = dto.ledgerHash?.takeIf { it.length == 64 }?.let { Hash256(it) },
            hash = null,
            engineResult = null,
            engineResultCode = null,
            meta = null,
        )
    }
}

/**
 * Simulates the execution of a transaction without submitting it to the network.
 *
 * This is an experimental API that may change without notice.
 *
 * @param transaction The signed transaction to simulate.
 * @param binary Whether to return results in binary format.
 * @return [XrplResult] containing [SimulateResult] or a categorized failure.
 */
@ExperimentalXrplApi
public suspend fun XrplClient.simulate(
    transaction: XrplTransaction.Signed,
    binary: Boolean = false,
): XrplResult<SimulateResult> {
    val request = SimulateRequest(txBlob = transaction.txBlob, binary = binary)
    return executeRpc(
        method = "simulate",
        request = request,
        requestSerializer = SimulateRequest.serializer(),
        responseDeserializer = SimulateResponseDto.serializer(),
    ) { dto ->
        SimulateResult(
            engineResult = dto.engineResult,
            engineResultCode = dto.engineResultCode,
            engineResultMessage = dto.engineResultMessage,
            txJson = dto.txJson,
            meta = dto.meta,
        )
    }
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

private fun extractHashFromTxJson(txJson: JsonElement): TxHash? {
    if (txJson !is JsonObject) return null
    val hashStr = (txJson["hash"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
    return runCatching { TxHash(hashStr) }.getOrNull()
}
