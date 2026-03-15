package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ChannelVerify
@Serializable
internal data class ChannelVerifyRequest(
    @SerialName("channel_id") val channelId: String,
    val amount: String,
    @SerialName("public_key") val publicKey: String,
    val signature: String,
)

@Serializable
internal data class ChannelVerifyResponse(
    @SerialName("signature_verified") val signatureVerified: Boolean,
)
