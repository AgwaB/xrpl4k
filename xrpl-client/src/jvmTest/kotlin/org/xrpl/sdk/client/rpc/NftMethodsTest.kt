@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.NftHistoryResult
import org.xrpl.sdk.client.model.NftInfo
import org.xrpl.sdk.client.model.NftOffersResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

private const val NFT_ID = "000800006203F49C21D5D6E022CB16DE3538F248662FC73C0000099B00000000"
private const val OFFER_IDX = "ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890"
private val NFT_OWNER = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

class NftMethodsTest : FunSpec({

    // ── nftBuyOffers ──────────────────────────────────────────────

    test("nftBuyOffers returns NftOffersResult with offer fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"nft_id":"$NFT_ID",""" +
                            """"offers":[{"amount":"50000000","flags":0,""" +
                            """"nft_offer_index":"$OFFER_IDX",""" +
                            """"owner":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"expiration":1234567890}]""",
                    ),
                )
            client.use { c ->
                val result = c.nftBuyOffers(nftId = NFT_ID)
                result.shouldBeInstanceOf<XrplResult.Success<NftOffersResult>>()
                val value = (result as XrplResult.Success<NftOffersResult>).value
                value.nftId shouldBe NFT_ID
                value.offers shouldHaveSize 1
                val offer = value.offers.first()
                offer.flags shouldBe 0u
                offer.owner shouldBe NFT_OWNER
                offer.destination shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
                offer.expiration shouldBe 1234567890L
                offer.nftOfferIndex shouldBe OFFER_IDX
            }
        }
    }

    test("nftBuyOffers with empty offers returns empty list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""nft_id":"$NFT_ID","offers":[]"""),
                )
            client.use { c ->
                val result = c.nftBuyOffers(nftId = NFT_ID)
                result.shouldBeInstanceOf<XrplResult.Success<NftOffersResult>>()
                (result as XrplResult.Success<NftOffersResult>).value.offers shouldHaveSize 0
            }
        }
    }

    // ── nftSellOffers ─────────────────────────────────────────────

    test("nftSellOffers returns NftOffersResult") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"nft_id":"$NFT_ID",""" +
                            """"offers":[{"amount":"100000000","flags":1,""" +
                            """"owner":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"}]""",
                    ),
                )
            client.use { c ->
                val result = c.nftSellOffers(nftId = NFT_ID)
                result.shouldBeInstanceOf<XrplResult.Success<NftOffersResult>>()
                val value = (result as XrplResult.Success<NftOffersResult>).value
                value.nftId shouldBe NFT_ID
                value.offers shouldHaveSize 1
                val offer = value.offers.first()
                offer.flags shouldBe 1u
                offer.owner shouldBe NFT_OWNER
                offer.destination shouldBe null
                offer.expiration shouldBe null
            }
        }
    }

    // ── nftInfo ───────────────────────────────────────────────────

    test("nftInfo returns NftInfo with all fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"nft_id":"$NFT_ID","owner":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"flags":8,"uri":"68747470733A2F2F6578616D706C652E636F6D2F6E6674","is_burned":false,""" +
                            """"nft_taxon":0,"nft_serial":155,""" +
                            """"issuer":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe","transfer_fee":500""",
                    ),
                )
            client.use { c ->
                val result = c.nftInfo(nftId = NFT_ID)
                result.shouldBeInstanceOf<XrplResult.Success<NftInfo>>()
                val info = (result as XrplResult.Success<NftInfo>).value
                info.nftId shouldBe NFT_ID
                info.owner shouldBe NFT_OWNER
                info.flags shouldBe 8u
                info.uri shouldBe "68747470733A2F2F6578616D706C652E636F6D2F6E6674"
                info.isBurned shouldBe false
                info.nftTaxon shouldBe 0L
                info.nftSerial shouldBe 155L
                info.issuer shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
                info.transferFee shouldBe 500L
            }
        }
    }

    test("nftInfo isBurned true maps correctly") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"nft_id":"$NFT_ID","owner":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"flags":0,"is_burned":true""",
                    ),
                )
            client.use { c ->
                val result = c.nftInfo(nftId = NFT_ID)
                result.shouldBeInstanceOf<XrplResult.Success<NftInfo>>()
                (result as XrplResult.Success<NftInfo>).value.isBurned shouldBe true
            }
        }
    }

    // ── nftHistory ────────────────────────────────────────────────

    test("nftHistory returns NftHistoryResult with transactions") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"nft_id":"$NFT_ID",""" +
                            """"transactions":[{"tx":{"TransactionType":"NFTokenMint"},""" +
                            """"meta":{"TransactionResult":"tesSUCCESS"},"validated":true}]""",
                    ),
                )
            client.use { c ->
                val result = c.nftHistory(nftId = NFT_ID)
                result.shouldBeInstanceOf<XrplResult.Success<NftHistoryResult>>()
                val value = (result as XrplResult.Success<NftHistoryResult>).value
                value.nftId shouldBe NFT_ID
                value.transactions shouldHaveSize 1
                val entry = value.transactions.first()
                entry.validated shouldBe true
            }
        }
    }
})
