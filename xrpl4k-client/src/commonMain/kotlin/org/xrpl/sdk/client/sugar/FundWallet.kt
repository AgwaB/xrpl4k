package org.xrpl.sdk.client.sugar

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.result.getOrNull
import org.xrpl.sdk.crypto.Wallet

/**
 * Result of funding a wallet via a test/dev faucet.
 *
 * @property wallet The funded wallet.
 * @property balance The wallet's XRP balance in drops after funding.
 */
public data class FundWalletResult(
    val wallet: Wallet,
    val balance: Long,
)

/**
 * Options for [fundWallet].
 *
 * @property faucetHost Custom faucet hostname. If null, resolved from the client's network.
 * @property faucetPath Custom faucet path. Defaults to `"/accounts"`.
 * @property amount Custom XRP amount to fund. If null, the faucet default is used.
 * @property usageContext Optional context hint for the faucet (e.g. "integration-test").
 */
public data class FundWalletOptions(
    val faucetHost: String? = null,
    val faucetPath: String? = null,
    val amount: String? = null,
    val usageContext: String? = null,
)

private const val POLL_INTERVAL_MS = 1_000L
private const val MAX_POLL_ATTEMPTS = 20

/**
 * Resolves the faucet hostname from the connection URL.
 *
 * @param connectionUrl The RPC URL of the connected network.
 * @return The faucet hostname.
 * @throws IllegalStateException if the URL belongs to Mainnet.
 */
internal fun resolveFaucetHost(connectionUrl: String): String {
    val url = connectionUrl.lowercase()

    if (url.contains("xrplcluster.com") || url.contains("s1.ripple.com") || url.contains("s2.ripple.com")) {
        throw IllegalStateException("fundWallet is not supported on Mainnet")
    }

    return when {
        url.contains("devnet") -> "faucet.devnet.rippletest.net"
        url.contains("sidechain") -> "faucet.devnet.rippletest.net"
        url.contains("altnet") || url.contains("testnet") -> "faucet.altnet.rippletest.net"
        else -> "faucet.altnet.rippletest.net"
    }
}

/**
 * Funds a wallet using the XRPL test/dev faucet.
 *
 * If no wallet is provided, a new one is generated.
 * Polls for the funded balance up to [MAX_POLL_ATTEMPTS] times at [POLL_INTERVAL_MS] intervals.
 *
 * @param wallet An existing wallet to fund, or null to generate a new one.
 * @param options Faucet configuration options.
 * @return A [FundWalletResult] with the wallet and its balance in drops.
 * @throws IllegalStateException if called against Mainnet.
 * @throws IllegalStateException if the faucet request fails or the balance is not received in time.
 */
public suspend fun XrplClient.fundWallet(
    wallet: Wallet? = null,
    options: FundWalletOptions = FundWalletOptions(),
): FundWalletResult {
    val network = config.network

    // Mainnet guard using sealed-class identity
    if (network is Network.Mainnet) {
        throw IllegalStateException("fundWallet is not supported on Mainnet")
    }

    val targetWallet = wallet ?: Wallet.generate().wallet

    val faucetHost = options.faucetHost ?: resolveFaucetHost(network.rpcUrl)
    val faucetPath = options.faucetPath ?: "/accounts"

    val requestBody = buildJsonObject {
        put("destination", targetWallet.address.value)
        if (options.amount != null) {
            put("xrpAmount", options.amount)
        }
        if (options.usageContext != null) {
            put("usageContext", options.usageContext)
        }
        put("userAgent", "xrpl4k")
    }

    val httpClient = HttpClient()
    try {
        val response = httpClient.post("https://$faucetHost$faucetPath") {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            throw IllegalStateException(
                "Faucet request failed with status ${response.status.value}: $body",
            )
        }
    } finally {
        httpClient.close()
    }

    // Poll for balance
    var balance = 0L
    for (attempt in 1..MAX_POLL_ATTEMPTS) {
        delay(POLL_INTERVAL_MS)
        val result = getXrpBalance(targetWallet.address)
        val drops = result.getOrNull()
        if (drops != null && drops.value > 0L) {
            balance = drops.value
            break
        }
    }

    if (balance == 0L) {
        throw IllegalStateException(
            "Failed to fund wallet ${targetWallet.address.value}: " +
                "balance did not appear after $MAX_POLL_ATTEMPTS attempts",
        )
    }

    return FundWalletResult(
        wallet = targetWallet,
        balance = balance,
    )
}
