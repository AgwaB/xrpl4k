package org.xrpl.sdk.client.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts the XChainClaimID from the metadata of an XChainCreateClaimID transaction.
 *
 * Searches the `AffectedNodes` array for a `CreatedNode` with
 * `LedgerEntryType == "XChainOwnedClaimID"` and returns its
 * `NewFields.XChainClaimID` value.
 *
 * @param metadata The transaction metadata JSON object.
 * @return The XChainClaimID string, or null if the transaction was not successful
 *         or no XChainOwnedClaimID was created.
 * @throws IllegalArgumentException if metadata is missing required fields
 *         (AffectedNodes or TransactionResult).
 */
public fun getXChainClaimId(metadata: JsonObject): String? {
    val affectedNodes =
        metadata["AffectedNodes"] as? JsonArray
            ?: throw IllegalArgumentException("metadata is missing AffectedNodes field")

    val transactionResult =
        metadata["TransactionResult"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("metadata is missing TransactionResult field")

    if (transactionResult != "tesSUCCESS") return null

    for (nodeElement in affectedNodes) {
        val nodeWrapper = nodeElement as? JsonObject ?: continue
        val createdNode = nodeWrapper["CreatedNode"] as? JsonObject ?: continue

        val ledgerEntryType =
            createdNode["LedgerEntryType"]?.jsonPrimitive?.content ?: continue
        if (ledgerEntryType != "XChainOwnedClaimID") continue

        val newFields = createdNode["NewFields"]?.jsonObject ?: continue
        return newFields["XChainClaimID"]?.jsonPrimitive?.content
    }

    return null
}
