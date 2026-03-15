package org.xrpl.sdk.client.sugar

import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.model.BookOffer
import org.xrpl.sdk.client.rpc.bookOffers
import org.xrpl.sdk.core.model.amount.CurrencySpec
import org.xrpl.sdk.core.model.amount.toJson
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrNull
import org.xrpl.sdk.core.type.Address

/**
 * Both sides of an order book for a currency pair.
 *
 * @property buy Offers to buy `takerGets` (sorted by ascending quality / best rate first).
 * @property sell Offers to sell `takerGets` (sorted by ascending quality / best rate first).
 */
public class OrderBook(
    public val buy: List<BookOffer>,
    public val sell: List<BookOffer>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderBook) return false
        return buy == other.buy && sell == other.sell
    }

    override fun hashCode(): Int {
        var result = buy.hashCode()
        result = 31 * result + sell.hashCode()
        return result
    }

    override fun toString(): String = "OrderBook(buy=$buy, sell=$sell)"
}

/**
 * Fetches both sides of an order book for a currency pair.
 *
 * Makes two `book_offers` RPC calls:
 * - **sell side**: offers where taker gets `takerGets` and pays `takerPays`
 * - **buy side**: offers where taker gets `takerPays` and pays `takerGets` (reversed)
 *
 * Both sides are sorted by quality (exchange rate) in ascending order so the
 * best offers appear first.
 *
 * @param takerGets The currency the taker would receive.
 * @param takerPays The currency the taker would pay.
 * @param limit Maximum number of offers to return per side.
 * @param taker Optional account to use as the perspective for offer quality.
 * @param ledgerIndex Which ledger version to use.
 * @return [XrplResult] containing an [OrderBook] on success.
 */
public suspend fun XrplClient.getOrderbook(
    takerGets: CurrencySpec,
    takerPays: CurrencySpec,
    limit: Int? = null,
    taker: Address? = null,
    ledgerIndex: String? = null,
): XrplResult<OrderBook> {
    // Sell side: takerGets -> takerPays
    val sellResult =
        bookOffers(
            takerGets = takerGets.toJson(),
            takerPays = takerPays.toJson(),
            taker = taker,
            limit = limit,
            ledgerIndex = ledgerIndex,
        )
    val sellOffers =
        sellResult.getOrNull()
            ?: return XrplResult.Failure((sellResult as XrplResult.Failure).error)

    // Buy side: reversed (takerPays -> takerGets)
    val buyResult =
        bookOffers(
            takerGets = takerPays.toJson(),
            takerPays = takerGets.toJson(),
            taker = taker,
            limit = limit,
            ledgerIndex = ledgerIndex,
        )
    val buyOffers =
        buyResult.getOrNull()
            ?: return XrplResult.Failure((buyResult as XrplResult.Failure).error)

    return XrplResult.Success(
        OrderBook(
            buy = sortByQuality(buyOffers.offers),
            sell = sortByQuality(sellOffers.offers),
        ),
    )
}

/**
 * Sorts offers by quality (exchange rate) in ascending order.
 * Offers without a quality value are placed at the end.
 */
private fun sortByQuality(offers: List<BookOffer>): List<BookOffer> =
    offers.sortedWith(compareBy(nullsLast()) { it.quality?.toDoubleOrNull() })
