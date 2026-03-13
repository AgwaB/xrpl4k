package org.xrpl.sdk.client.internal

/**
 * Marker interface for all internal RPC request DTOs.
 *
 * Every `@Serializable` request class used by the RPC executor
 * should implement this interface for type-safety and documentation.
 */
internal interface RpcRequest
