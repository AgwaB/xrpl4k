package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.AmmInfoRequest
import org.xrpl.sdk.client.internal.dto.AmmInfoResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.AmmInfo
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex

/**
 * Retrieves information about an Automated Market Maker (AMM) pool.
 *
 * @param asset The first asset in the AMM pool, as a currency specifier JSON object.
 * @param asset2 The second asset in the AMM pool, as a currency specifier JSON object.
 * @return [XrplResult] containing [AmmInfo] on success.
 */
public suspend fun XrplClient.ammInfo(
    asset: JsonElement,
    asset2: JsonElement? = null,
): XrplResult<AmmInfo> =
    executeRpc(
        method = "amm_info",
        request = AmmInfoRequest(asset = asset, asset2 = asset2),
        requestSerializer = AmmInfoRequest.serializer(),
        responseDeserializer = AmmInfoResponseDto.serializer(),
    ) { resp ->
        val amm = resp.amm
        AmmInfo(
            account = amm?.account?.let { Address(it) },
            amount = amm?.amount,
            amount2 = amm?.amount2,
            tradingFee = amm?.tradingFee,
            lpToken = amm?.lpToken,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
