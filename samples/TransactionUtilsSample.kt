import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.xrpl.sdk.client.util.getNFTokenID
import org.xrpl.sdk.client.util.hashSignedTx
import org.xrpl.sdk.client.util.parseBalanceChanges
import org.xrpl.sdk.client.util.verifyTransaction
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Demonstrates the transaction utility functions in TransactionUtils.kt.
 *
 * These utilities operate on raw JSON data (hex blobs and metadata objects) and
 * do not require a live client connection. They are useful for:
 *   - Offline verification of transaction signatures before broadcasting
 *   - Post-submission analysis of balance changes from metadata
 *   - Extracting the NFTokenID after a mint from on-ledger metadata
 *   - Recomputing the tx hash from a blob as a sanity check
 *
 * All functions accept a CryptoProvider — the platform default is used here
 * and covers both Ed25519 and secp256k1 keys.
 */
suspend fun main() {
    val provider = platformCryptoProvider()

    // --- 1. hashSignedTx ---
    // Recomputes the transaction hash from a hex-encoded signed blob.
    // The hash is SHA-512Half(0x54584E00 prefix + decoded blob bytes).
    // This is useful for confirming the hash matches what was returned by submit().
    val txBlob = "YOUR_HEX_SIGNED_BLOB_HERE"
    val txHash = hashSignedTx(txBlob, provider)
    println("Computed tx hash: ${txHash.value}")

    // --- 2. verifyTransaction ---
    // Cryptographically verifies the signature in a signed transaction blob.
    // Works for both Ed25519 (public key starts with "ED") and secp256k1 keys.
    // Returns true only if the signature over the signing-encoded bytes is valid.
    val isValid = verifyTransaction(txBlob, provider)
    println("Signature valid: $isValid")

    // --- 3. parseBalanceChanges ---
    // Parses AffectedNodes from transaction metadata to compute balance deltas.
    //
    // Returns a Map<Address, List<BalanceChange>> where each BalanceChange has:
    //   - currency:     "XRP" or an IOU currency code
    //   - value:        signed decimal string (negative = debit, positive = credit)
    //   - counterparty: the issuer address for IOUs; null for XRP
    //
    // The metadata below is a minimal example; in practice fetch it via client.tx().
    val metadataJson = """
        {
          "AffectedNodes": [
            {
              "ModifiedNode": {
                "LedgerEntryType": "AccountRoot",
                "FinalFields":    {"Account": "rSenderAddressHere",   "Balance": "990000000"},
                "PreviousFields": {"Balance": "1000000000"}
              }
            },
            {
              "ModifiedNode": {
                "LedgerEntryType": "AccountRoot",
                "FinalFields":    {"Account": "rReceiverAddressHere", "Balance": "10000000"},
                "PreviousFields": {"Balance": "0"}
              }
            }
          ]
        }
    """.trimIndent()

    val metadata: JsonObject = Json.parseToJsonElement(metadataJson).jsonObject
    val balanceChanges = parseBalanceChanges(metadata)

    println("\nBalance changes:")
    for ((address, changes) in balanceChanges) {
        for (change in changes) {
            val counterpartyStr = change.counterparty?.let { " (issuer: $it)" } ?: ""
            println("  $address  ${change.currency}: ${change.value}$counterpartyStr")
        }
    }

    // --- 4. getNFTokenID ---
    // Extracts the newly-minted NFTokenID from the metadata of an NFTokenMint tx.
    // Searches AffectedNodes for a modified or created NFTokenPage and returns
    // the ID of the token that appears in FinalFields but not in PreviousFields.
    //
    // Pass the `meta` JsonObject from a validated NFTokenMint transaction result.
    val mintMetadataJson = """
        {
          "AffectedNodes": [
            {
              "ModifiedNode": {
                "LedgerEntryType": "NFTokenPage",
                "FinalFields": {
                  "NFTokens": [
                    {"NFToken": {"NFTokenID": "000800006B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B"}}
                  ]
                },
                "PreviousFields": {
                  "NFTokens": []
                }
              }
            }
          ]
        }
    """.trimIndent()

    val mintMetadata: JsonObject = Json.parseToJsonElement(mintMetadataJson).jsonObject
    val nftTokenId = getNFTokenID(mintMetadata)
    println("\nMinted NFTokenID: $nftTokenId")
}
