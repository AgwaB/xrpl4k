package org.xrpl.sdk.client.rpc

import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.ChannelVerifyRequest
import org.xrpl.sdk.client.internal.dto.ChannelVerifyResponse
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.ChannelVerifyResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Checks the validity of a signature that can be used to redeem a specific
 * amount of XRP from a payment channel.
 *
 * @param channelId The Channel ID of the channel that provides the XRP (64-character hex string).
 * @param amount The amount of XRP, in drops, the provided signature authorizes.
 * @param publicKey The public key of the channel and the key pair that was used to create the signature.
 * @param signature The signature to verify, in hexadecimal.
 * @return [XrplResult] containing [ChannelVerifyResult] on success.
 */
public suspend fun XrplClient.channelVerify(
    channelId: String,
    amount: XrpDrops,
    publicKey: String,
    signature: String,
): XrplResult<ChannelVerifyResult> =
    executeRpc(
        method = "channel_verify",
        request =
            ChannelVerifyRequest(
                channelId = channelId,
                amount = amount.value.toString(),
                publicKey = publicKey,
                signature = signature,
            ),
        requestSerializer = ChannelVerifyRequest.serializer(),
        responseDeserializer = ChannelVerifyResponse.serializer(),
    ) { resp ->
        ChannelVerifyResult(
            signatureVerified = resp.signatureVerified,
        )
    }
