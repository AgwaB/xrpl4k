@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.json.JsonObject
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.submit
import org.xrpl.sdk.client.signing.signTransaction
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider
import kotlin.time.Duration.Companion.seconds

/** Tag for integration tests — excluded unless `-Pintegration` is set. */
object Integration : Tag()

/** Genesis account address (holds all XRP in standalone mode). */
val GENESIS_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

/** Genesis account seed for standalone mode. */
const val GENESIS_SEED = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb"

/** Default funding amount: 1000 XRP. */
val FUNDING_AMOUNT = XrpDrops(1_000_000_000L)

private val provider = platformCryptoProvider()

/**
 * Base class for integration tests against a local rippled standalone node.
 *
 * Requires rippled running in standalone mode:
 * ```
 * docker compose up -d rippled
 * ./gradlew jvmTest -Pintegration
 * ```
 */
abstract class IntegrationTestBase(body: FunSpec.() -> Unit = {}) : FunSpec({
    tags(Integration)
    body()
}) {
    companion object {
        /**
         * Creates an XrplClient pointing at the local rippled node.
         */
        fun createClient(): XrplClient {
            val rpcUrl = System.getenv("XRPL_RPC_URL") ?: "http://localhost:5005"
            val wsUrl = System.getenv("XRPL_WS_URL") ?: "ws://localhost:6006"
            return XrplClient {
                network = Network.Custom(rpcUrl = rpcUrl, wsUrl = wsUrl)
                timeout = 15.seconds
            }
        }

        /**
         * Creates a new wallet and funds it from the genesis account.
         * Calls `ledger_accept` to close the ledger after funding.
         */
        suspend fun fundNewWallet(
            client: XrplClient,
            amount: XrpDrops = FUNDING_AMOUNT,
        ): Wallet {
            val generated = Wallet.generate(provider = provider)
            val genesis = Wallet.fromSeed(GENESIS_SEED, provider)

            // Get genesis sequence
            val accountInfo = client.accountInfo(GENESIS_ADDRESS).getOrThrow()
            val sequence = accountInfo.sequence

            // Build and sign funding payment
            val filled =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = GENESIS_ADDRESS,
                    fields =
                        PaymentFields(
                            destination = generated.wallet.address,
                            amount = XrpAmount(amount),
                        ),
                    fee = XrpDrops(12),
                    sequence = sequence,
                    lastLedgerSequence = UInt.MAX_VALUE,
                )
            val signed = genesis.signTransaction(filled, provider)

            // Submit
            val submitResult = client.submit(signed)
            check(submitResult is XrplResult.Success) {
                "Failed to submit funding tx: $submitResult"
            }

            // Advance ledger
            ledgerAccept(client)

            genesis.close()
            return generated.wallet
        }

        /**
         * Advances the ledger in standalone mode.
         */
        suspend fun ledgerAccept(client: XrplClient) {
            client.httpTransport.request(
                "ledger_accept",
                JsonObject(emptyMap()),
                JsonObject.serializer(),
            )
        }
    }
}
