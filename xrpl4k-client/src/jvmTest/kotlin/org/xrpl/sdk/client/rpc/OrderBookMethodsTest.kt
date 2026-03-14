@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.BookChangesResult
import org.xrpl.sdk.client.model.BookOffersResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

private val TAKER_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

class OrderBookMethodsTest : FunSpec({

    // ── bookOffers ────────────────────────────────────────────────

    test("bookOffers returns BookOffersResult with offer fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"offers":[{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"taker_gets":"1000000","taker_pays":"500000",""" +
                            """"quality":"0.5","Flags":0,"Sequence":42,"owner_funds":"999000"}],""" +
                            """"ledger_index":1000""",
                    ),
                )
            client.use { c ->
                val result =
                    c.bookOffers(
                        takerGets = JsonPrimitive("XRP"),
                        takerPays = JsonPrimitive("USD"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<BookOffersResult>>()
                val value = (result as XrplResult.Success<BookOffersResult>).value
                value.offers shouldHaveSize 1
                val offer = value.offers.first()
                offer.account shouldBe TAKER_ADDRESS
                offer.quality shouldBe "0.5"
                offer.flags shouldBe 0u
                offer.sequence shouldBe 42L
                offer.ownerFunds shouldBe "999000"
                value.ledgerIndex?.value shouldBe 1000u
            }
        }
    }

    test("bookOffers with empty offers returns empty list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""offers":[]"""),
                )
            client.use { c ->
                val result =
                    c.bookOffers(
                        takerGets = JsonPrimitive("XRP"),
                        takerPays = JsonPrimitive("USD"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<BookOffersResult>>()
                (result as XrplResult.Success<BookOffersResult>).value.offers shouldHaveSize 0
            }
        }
    }

    test("bookOffers with taker and limit passes through correctly") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"offers":[{"Account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"taker_gets":"2000000","taker_pays":"800000","Flags":131072}]""",
                    ),
                )
            client.use { c ->
                val result =
                    c.bookOffers(
                        takerGets = JsonPrimitive("XRP"),
                        takerPays = JsonPrimitive("EUR"),
                        taker = TAKER_ADDRESS,
                        limit = 10,
                    )
                result.shouldBeInstanceOf<XrplResult.Success<BookOffersResult>>()
                val offer = (result as XrplResult.Success<BookOffersResult>).value.offers.first()
                offer.flags shouldBe 131072u
                offer.account shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
            }
        }
    }

    // ── bookChanges ───────────────────────────────────────────────

    test("bookChanges returns BookChangesResult with change fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"changes":[{"currency_a":"XRP_drops",""" +
                            """"currency_b":"USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"volume_a":"10000000","volume_b":"5000","high":"0.0005","low":"0.0004",""" +
                            """"open":"0.00045","close":"0.00048"}],"ledger_index":2000""",
                    ),
                )
            client.use { c ->
                val result = c.bookChanges()
                result.shouldBeInstanceOf<XrplResult.Success<BookChangesResult>>()
                val value = (result as XrplResult.Success<BookChangesResult>).value
                value.changes shouldHaveSize 1
                val change = value.changes.first()
                change.currencyA shouldBe "XRP_drops"
                change.currencyB shouldBe "USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
                change.volumeA shouldBe "10000000"
                change.volumeB shouldBe "5000"
                change.high shouldBe "0.0005"
                change.low shouldBe "0.0004"
                change.open shouldBe "0.00045"
                change.close shouldBe "0.00048"
                value.ledgerIndex?.value shouldBe 2000u
            }
        }
    }

    test("bookChanges with ledgerIndex maps ledgerIndex correctly") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""changes":[],"ledger_index":99999"""),
                )
            client.use { c ->
                val result = c.bookChanges(ledgerIndex = "validated")
                result.shouldBeInstanceOf<XrplResult.Success<BookChangesResult>>()
                val value = (result as XrplResult.Success<BookChangesResult>).value
                value.changes shouldHaveSize 0
                value.ledgerIndex?.value shouldBe 99999u
            }
        }
    }
})
