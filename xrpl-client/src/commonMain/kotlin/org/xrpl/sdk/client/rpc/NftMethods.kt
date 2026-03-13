package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.NftHistoryRequest
import org.xrpl.sdk.client.internal.dto.NftHistoryResponseDto
import org.xrpl.sdk.client.internal.dto.NftInfoRequest
import org.xrpl.sdk.client.internal.dto.NftInfoResponseDto
import org.xrpl.sdk.client.internal.dto.NftOffersRequest
import org.xrpl.sdk.client.internal.dto.NftOffersResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.NftHistoryEntry
import org.xrpl.sdk.client.model.NftHistoryResult
import org.xrpl.sdk.client.model.NftInfo
import org.xrpl.sdk.client.model.NftOffer
import org.xrpl.sdk.client.model.NftOffersResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

/**
 * Retrieves the current buy offers for a given NFT.
 *
 * @param nftId The unique identifier of the NFToken.
 * @param limit Maximum number of offers to return.
 * @param marker Pagination marker from a previous response.
 * @return [XrplResult] containing [NftOffersResult] on success.
 */
public suspend fun XrplClient.nftBuyOffers(
    nftId: String,
    limit: Int? = null,
    marker: JsonElement? = null,
): XrplResult<NftOffersResult> =
    executeRpc(
        method = "nft_buy_offers",
        request = NftOffersRequest(nftId = nftId, limit = limit, marker = marker),
        requestSerializer = NftOffersRequest.serializer(),
        responseDeserializer = NftOffersResponseDto.serializer(),
    ) { resp ->
        NftOffersResult(
            nftId = resp.nftId,
            offers =
                resp.offers.map { o ->
                    NftOffer(
                        amount = o.amount,
                        flags = o.flags.toUInt(),
                        nftOfferIndex = o.nftOfferIndex,
                        owner = Address(o.owner),
                        destination = o.destination?.let { Address(it) },
                        expiration = o.expiration,
                    )
                },
            marker = resp.marker,
        )
    }

/**
 * Retrieves the current sell offers for a given NFT.
 *
 * @param nftId The unique identifier of the NFToken.
 * @param limit Maximum number of offers to return.
 * @param marker Pagination marker from a previous response.
 * @return [XrplResult] containing [NftOffersResult] on success.
 */
public suspend fun XrplClient.nftSellOffers(
    nftId: String,
    limit: Int? = null,
    marker: JsonElement? = null,
): XrplResult<NftOffersResult> =
    executeRpc(
        method = "nft_sell_offers",
        request = NftOffersRequest(nftId = nftId, limit = limit, marker = marker),
        requestSerializer = NftOffersRequest.serializer(),
        responseDeserializer = NftOffersResponseDto.serializer(),
    ) { resp ->
        NftOffersResult(
            nftId = resp.nftId,
            offers =
                resp.offers.map { o ->
                    NftOffer(
                        amount = o.amount,
                        flags = o.flags.toUInt(),
                        nftOfferIndex = o.nftOfferIndex,
                        owner = Address(o.owner),
                        destination = o.destination?.let { Address(it) },
                        expiration = o.expiration,
                    )
                },
            marker = resp.marker,
        )
    }

/**
 * Retrieves information about a specific NFT (Clio server only).
 *
 * @param nftId The unique identifier of the NFToken.
 * @return [XrplResult] containing [NftInfo] on success.
 */
public suspend fun XrplClient.nftInfo(nftId: String): XrplResult<NftInfo> =
    executeRpc(
        method = "nft_info",
        request = NftInfoRequest(nftId = nftId),
        requestSerializer = NftInfoRequest.serializer(),
        responseDeserializer = NftInfoResponseDto.serializer(),
    ) { resp ->
        NftInfo(
            nftId = resp.nftId,
            owner = Address(resp.owner),
            flags = resp.flags.toUInt(),
            uri = resp.uri,
            isBurned = resp.isBurned,
            nftTaxon = resp.nftTaxon,
            nftSerial = resp.nftSerial,
            issuer = resp.issuer?.let { Address(it) },
            transferFee = resp.transferFee,
        )
    }

/**
 * Retrieves the transaction history for a specific NFT (Clio server only).
 *
 * @param nftId The unique identifier of the NFToken.
 * @param limit Maximum number of transactions to return.
 * @param marker Pagination marker from a previous response.
 * @return [XrplResult] containing [NftHistoryResult] on success.
 */
public suspend fun XrplClient.nftHistory(
    nftId: String,
    limit: Int? = null,
    marker: JsonElement? = null,
): XrplResult<NftHistoryResult> =
    executeRpc(
        method = "nft_history",
        request = NftHistoryRequest(nftId = nftId, limit = limit, marker = marker),
        requestSerializer = NftHistoryRequest.serializer(),
        responseDeserializer = NftHistoryResponseDto.serializer(),
    ) { resp ->
        NftHistoryResult(
            nftId = resp.nftId,
            transactions =
                resp.transactions.map { t ->
                    NftHistoryEntry(
                        tx = t.tx,
                        meta = t.meta,
                        validated = t.validated,
                    )
                },
            marker = resp.marker,
        )
    }
