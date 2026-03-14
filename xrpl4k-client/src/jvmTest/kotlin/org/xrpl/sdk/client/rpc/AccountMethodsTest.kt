@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.errorResponse
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.AccountChannelsResult
import org.xrpl.sdk.client.model.AccountCurrenciesResult
import org.xrpl.sdk.client.model.AccountInfo
import org.xrpl.sdk.client.model.AccountLinesResult
import org.xrpl.sdk.client.model.AccountNftsResult
import org.xrpl.sdk.client.model.AccountObjectsResult
import org.xrpl.sdk.client.model.AccountOffersResult
import org.xrpl.sdk.client.model.AccountTxResult
import org.xrpl.sdk.client.model.GatewayBalancesResult
import org.xrpl.sdk.client.model.NorippleCheckResult
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops

private val GENESIS_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

class AccountMethodsTest : FunSpec({

    // ── accountInfo ──────────────────────────────────────────────

    test("accountInfo returns AccountInfo with correct fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"Balance":"100000000","Sequence":1,"OwnerCount":0,"Flags":0},""" +
                            """"ledger_index":1000,"validated":true""",
                    ),
                )
            client.use { c ->
                val result = c.accountInfo(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountInfo>>()
                val info = (result as XrplResult.Success<AccountInfo>).value
                info.balance shouldBe XrpDrops(100_000_000)
                info.sequence shouldBe 1u
                info.ownerCount shouldBe 0u
                info.flags shouldBe 0u
                info.account shouldBe GENESIS_ADDRESS
                info.ledgerIndex?.value shouldBe 1000u
            }
        }
    }

    test("accountInfo with actNotFound error returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("actNotFound", 19, "Account not found."))
            client.use { c ->
                val result = c.accountInfo(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    test("accountInfo with previousTxnId maps optional fields") {
        runTest {
            val txnId = "E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879"
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"Balance":"50000000","Sequence":5,"OwnerCount":2,"Flags":0,""" +
                            """"PreviousTxnID":"$txnId","PreviousTxnLgrSeq":500},"validated":true""",
                    ),
                )
            client.use { c ->
                val result = c.accountInfo(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountInfo>>()
                val info = (result as XrplResult.Success<AccountInfo>).value
                info.previousAffectingTransactionId?.value shouldBe txnId
                info.previousAffectingTransactionLedgerSequence shouldBe 500u
            }
        }
    }

    // ── accountLines ─────────────────────────────────────────────

    test("accountLines returns AccountLinesResult with trust lines") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"lines":[{"account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"balance":"10","currency":"USD","limit":"1000",""" +
                            """"limit_peer":"0","no_ripple":true,"no_ripple_peer":false}]""",
                    ),
                )
            client.use { c ->
                val result = c.accountLines(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountLinesResult>>()
                val lines = (result as XrplResult.Success<AccountLinesResult>).value
                lines.account shouldBe GENESIS_ADDRESS
                lines.lines shouldHaveSize 1
                val line = lines.lines.first()
                line.currency shouldBe "USD"
                line.balance shouldBe "10"
                line.limit shouldBe "1000"
                line.noRipple shouldBe true
            }
        }
    }

    test("accountLines with empty lines returns empty list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","lines":[]""",
                    ),
                )
            client.use { c ->
                val result = c.accountLines(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountLinesResult>>()
                (result as XrplResult.Success<AccountLinesResult>).value.lines shouldHaveSize 0
            }
        }
    }

    // ── accountChannels ──────────────────────────────────────────

    test("accountChannels returns list of payment channels") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"channels":[{"channel_id":"ABCD1234",""" +
                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"amount":"5000000","balance":"1000000","settle_delay":86400}]""",
                    ),
                )
            client.use { c ->
                val result = c.accountChannels(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountChannelsResult>>()
                val channels = (result as XrplResult.Success<AccountChannelsResult>).value.channels
                channels shouldHaveSize 1
                val ch = channels.first()
                ch.channelId shouldBe "ABCD1234"
                ch.amount shouldBe XrpDrops(5_000_000)
                ch.balance shouldBe XrpDrops(1_000_000)
                ch.settleDelay shouldBe 86400u
            }
        }
    }

    // ── accountCurrencies ────────────────────────────────────────

    test("accountCurrencies returns send and receive currency lists") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"receive_currencies":["USD","EUR"],"send_currencies":["BTC"]""",
                    ),
                )
            client.use { c ->
                val result = c.accountCurrencies(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountCurrenciesResult>>()
                val currencies = (result as XrplResult.Success<AccountCurrenciesResult>).value
                currencies.receiveCurrencies shouldBe listOf("USD", "EUR")
                currencies.sendCurrencies shouldBe listOf("BTC")
            }
        }
    }

    // ── accountNfts ──────────────────────────────────────────────

    test("accountNfts returns NFT list with correct fields") {
        runTest {
            val nftokenId = "000800006203F49C21D5D6E022CB16DE3538F248662FC73C0000099B00000000"
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"account_nfts":[{"Flags":8,""" +
                            """"Issuer":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"NFTokenID":"$nftokenId","NFTokenTaxon":0,"nft_serial":155}]""",
                    ),
                )
            client.use { c ->
                val result = c.accountNfts(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountNftsResult>>()
                val nfts = (result as XrplResult.Success<AccountNftsResult>).value.accountNfts
                nfts shouldHaveSize 1
                val nft = nfts.first()
                nft.nftokenId shouldBe nftokenId
                nft.nftokenTaxon shouldBe 0uL
                nft.nftSerial shouldBe 155uL
                nft.flags shouldBe 8u
            }
        }
    }

    // ── accountObjects ───────────────────────────────────────────

    test("accountObjects returns ledger objects list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"account_objects":[{"LedgerEntryType":"Offer","Flags":0}]""",
                    ),
                )
            client.use { c ->
                val result = c.accountObjects(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountObjectsResult>>()
                val objects = (result as XrplResult.Success<AccountObjectsResult>).value
                objects.account shouldBe GENESIS_ADDRESS
                objects.accountObjects shouldHaveSize 1
            }
        }
    }

    // ── accountOffers ────────────────────────────────────────────

    test("accountOffers returns offers list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"offers":[{"flags":0,"seq":42,"taker_gets":"1000000",""" +
                            """"taker_pays":{"currency":"USD",""" +
                            """"issuer":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe","value":"1.5"}}]""",
                    ),
                )
            client.use { c ->
                val result = c.accountOffers(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountOffersResult>>()
                val offers = (result as XrplResult.Success<AccountOffersResult>).value.offers
                offers shouldHaveSize 1
                val offer = offers.first()
                offer.seq shouldBe 42u
                offer.flags shouldBe 0u
            }
        }
    }

    // ── accountTx ────────────────────────────────────────────────

    test("accountTx returns transaction list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"transactions":[{"tx":{"TransactionType":"Payment"},""" +
                            """"meta":{"TransactionResult":"tesSUCCESS"},"validated":true}],""" +
                            """"ledger_index_min":1,"ledger_index_max":1000""",
                    ),
                )
            client.use { c ->
                val result = c.accountTx(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<AccountTxResult>>()
                val txResult = (result as XrplResult.Success<AccountTxResult>).value
                txResult.account shouldBe GENESIS_ADDRESS
                txResult.transactions shouldHaveSize 1
                txResult.transactions.first().validated shouldBe true
                txResult.ledgerIndexMin?.value shouldBe 1u
                txResult.ledgerIndexMax?.value shouldBe 1000u
            }
        }
    }

    // ── gatewayBalances ──────────────────────────────────────────

    test("gatewayBalances returns obligations and balances") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","obligations":{"USD":"1000","EUR":"500"}""",
                    ),
                )
            client.use { c ->
                val result = c.gatewayBalances(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<GatewayBalancesResult>>()
                val balances = (result as XrplResult.Success<GatewayBalancesResult>).value
                balances.obligations["USD"] shouldBe "1000"
                balances.obligations["EUR"] shouldBe "500"
                balances.balances shouldBe emptyMap()
                balances.assets shouldBe emptyMap()
            }
        }
    }

    // ── norippleCheck ────────────────────────────────────────────

    test("norippleCheck returns problems list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"problems":["You should enable no-ripple on rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe for USD"]""",
                    ),
                )
            client.use { c ->
                val result = c.norippleCheck(GENESIS_ADDRESS, role = "gateway")
                result.shouldBeInstanceOf<XrplResult.Success<NorippleCheckResult>>()
                val check = (result as XrplResult.Success<NorippleCheckResult>).value
                check.problems shouldHaveSize 1
            }
        }
    }

    test("norippleCheck with no problems returns empty list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""problems":[]"""),
                )
            client.use { c ->
                val result = c.norippleCheck(GENESIS_ADDRESS, role = "user")
                result.shouldBeInstanceOf<XrplResult.Success<NorippleCheckResult>>()
                (result as XrplResult.Success<NorippleCheckResult>).value.problems shouldHaveSize 0
            }
        }
    }
})
