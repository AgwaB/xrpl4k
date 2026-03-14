@file:Suppress("MagicNumber")

package org.xrpl.sdk.test

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import org.xrpl.sdk.client.XrplClient

/**
 * A mock [XrplClient] for use in tests.
 *
 * Wraps Ktor's [MockEngine] to allow tests to set up canned JSON responses
 * without hitting a real XRPL node.
 *
 * Usage:
 * ```kotlin
 * val mock = MockXrplClient()
 * mock.respondWith(JsonFixtures.accountInfoResponse)
 * mock.client.use { c ->
 *     val result = c.accountInfo(TestAddresses.ALICE)
 * }
 * mock.close()
 * ```
 */
public class MockXrplClient : AutoCloseable {
    private var nextHandler: suspend (Any) -> Pair<String, HttpStatusCode> = { _ ->
        """{"result":{"status":"success"}}""" to HttpStatusCode.OK
    }

    private val mockEngine: MockEngine =
        MockEngine { _ ->
            val (content, status) = nextHandler(Unit)
            respond(
                content = ByteReadChannel(content),
                status = status,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }

    /** The [XrplClient] backed by this mock engine. Use this to invoke RPC methods. */
    public val client: XrplClient = XrplClient { engine = mockEngine }

    /**
     * Configures the mock to respond with the given raw JSON string on the next request.
     *
     * The [json] should be a complete JSON-RPC response body, e.g.:
     * ```json
     * {"result":{"status":"success","account_data":{...}}}
     * ```
     */
    public fun respondWith(json: String) {
        nextHandler = { _ -> json to HttpStatusCode.OK }
    }

    /**
     * Configures the mock to respond with an HTTP error status and a plain-text [message].
     *
     * This simulates transport-level errors (e.g., 503 Service Unavailable), not RPC-level
     * errors. For RPC-level errors use [respondWithRpcError].
     */
    public fun respondWithError(
        code: Int,
        message: String,
    ) {
        nextHandler = { _ -> message to HttpStatusCode.fromValue(code) }
    }

    /**
     * Configures the mock to respond with an XRPL RPC-level error envelope.
     *
     * The response uses HTTP 200 but carries an error result, matching real XRPL node behaviour:
     * ```json
     * {"result":{"status":"error","error":"<error>","error_code":<code>,"error_message":"<message>"}}
     * ```
     */
    public fun respondWithRpcError(
        error: String,
        errorCode: Int,
        errorMessage: String,
    ) {
        val body =
            """{"result":{"status":"error",""" +
                """"error":"$error",""" +
                """"error_code":$errorCode,""" +
                """"error_message":"$errorMessage"}}"""
        nextHandler = { _ -> body to HttpStatusCode.OK }
    }

    override fun close() {
        client.close()
    }
}
