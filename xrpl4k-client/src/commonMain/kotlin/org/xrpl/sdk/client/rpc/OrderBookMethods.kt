package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.BookChangesRequest
import org.xrpl.sdk.client.internal.dto.BookChangesResponseDto
import org.xrpl.sdk.client.internal.dto.BookOffersRequest
import org.xrpl.sdk.client.internal.dto.BookOffersResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.BookChange
import org.xrpl.sdk.client.model.BookChangesResult
import org.xrpl.sdk.client.model.BookOffer
import org.xrpl.sdk.client.model.BookOffersResult
import org.xrpl.sdk.core.model.amount.CurrencySpec
import org.xrpl.sdk.core.model.amount.toJson
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex

/**
 * Retrieves offers for a currency pair from the order book.
 *
 * @param takerGets The currency the account taking the offer would receive.
 * @param takerPays The currency the account taking the offer would pay.
 * @param taker Optional account to use as the perspective for offer quality.
 * @param limit Maximum number of offers to return.
 * @param ledgerIndex Which ledger version to use. Defaults to "validated".
 * @return [XrplResult] containing [BookOffersResult] on success.
 */
public suspend fun XrplClient.bookOffers(
    takerGets: JsonElement,
    takerPays: JsonElement,
    taker: Address? = null,
    limit: Int? = null,
    ledgerIndex: String? = null,
): XrplResult<BookOffersResult> =
    executeRpc(
        method = "book_offers",
        request =
            BookOffersRequest(
                takerGets = takerGets,
                takerPays = takerPays,
                taker = taker?.value,
                limit = limit,
                ledgerIndex = ledgerIndex,
            ),
        requestSerializer = BookOffersRequest.serializer(),
        responseDeserializer = BookOffersResponseDto.serializer(),
    ) { resp ->
        BookOffersResult(
            offers =
                resp.offers.map { o ->
                    BookOffer(
                        account = Address(o.account),
                        takerGets = o.takerGets,
                        takerPays = o.takerPays,
                        quality = o.quality,
                        flags = o.flags.toUInt(),
                        sequence = o.sequence,
                        ownerFunds = o.ownerFunds,
                    )
                },
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }

/**
 * Retrieves offers for a currency pair from the order book using typed currency specs.
 *
 * This is a convenience overload that accepts [CurrencySpec] instead of raw [JsonElement].
 *
 * ```kotlin
 * val usd = CurrencySpec.Issued(CurrencyCode("USD"), issuerAddress)
 * client.bookOffers(takerGets = usd, takerPays = CurrencySpec.Xrp)
 * ```
 *
 * @param takerGets The currency the account taking the offer would receive.
 * @param takerPays The currency the account taking the offer would pay.
 * @param taker Optional account to use as the perspective for offer quality.
 * @param limit Maximum number of offers to return.
 * @param ledgerIndex Which ledger version to use. Defaults to "validated".
 * @return [XrplResult] containing [BookOffersResult] on success.
 */
public suspend fun XrplClient.bookOffers(
    takerGets: CurrencySpec,
    takerPays: CurrencySpec,
    taker: Address? = null,
    limit: Int? = null,
    ledgerIndex: String? = null,
): XrplResult<BookOffersResult> =
    bookOffers(
        takerGets = takerGets.toJson(),
        takerPays = takerPays.toJson(),
        taker = taker,
        limit = limit,
        ledgerIndex = ledgerIndex,
    )

/**
 * Retrieves order book changes that occurred in a given ledger.
 *
 * @param ledgerIndex Which ledger version to use. Defaults to "validated".
 * @return [XrplResult] containing [BookChangesResult] on success.
 */
public suspend fun XrplClient.bookChanges(ledgerIndex: String? = null): XrplResult<BookChangesResult> =
    executeRpc(
        method = "book_changes",
        request = BookChangesRequest(ledgerIndex = ledgerIndex),
        requestSerializer = BookChangesRequest.serializer(),
        responseDeserializer = BookChangesResponseDto.serializer(),
    ) { resp ->
        BookChangesResult(
            changes =
                resp.changes.map { c ->
                    BookChange(
                        currencyA = c.currencyA,
                        currencyB = c.currencyB,
                        volumeA = c.volumeA,
                        volumeB = c.volumeB,
                        high = c.high,
                        low = c.low,
                        open = c.open,
                        close = c.close,
                    )
                },
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
