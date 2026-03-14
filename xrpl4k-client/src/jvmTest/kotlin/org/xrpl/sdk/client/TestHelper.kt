@file:Suppress("MagicNumber")

package org.xrpl.sdk.client

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel

internal object TestHelper {
    fun jsonHeaders() = headersOf("Content-Type", ContentType.Application.Json.toString())

    /**
     * Creates an XrplClient backed by the given MockEngine handler.
     */
    fun clientWithMockEngine(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): XrplClient {
        val mockEngine = MockEngine(handler)
        return XrplClient { engine = mockEngine }
    }

    /**
     * Returns a handler that responds with {"result":{"status":"success", <resultFields>}}.
     */
    fun successResponse(resultFields: String): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
        { _ ->
            val body =
                if (resultFields.isEmpty()) {
                    """{"result":{"status":"success"}}"""
                } else {
                    """{"result":{"status":"success",$resultFields}}"""
                }
            respond(
                content = ByteReadChannel(body),
                status = HttpStatusCode.OK,
                headers = jsonHeaders(),
            )
        }

    /**
     * Returns a handler that responds with an RPC error result envelope.
     */
    fun errorResponse(
        error: String,
        errorCode: Int,
        errorMessage: String,
    ): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
        { _ ->
            respond(
                content =
                    ByteReadChannel(
                        """{"result":{"status":"error",""" +
                            """"error":"$error",""" +
                            """"error_code":$errorCode,""" +
                            """"error_message":"$errorMessage"}}""",
                    ),
                status = HttpStatusCode.OK,
                headers = jsonHeaders(),
            )
        }
}
