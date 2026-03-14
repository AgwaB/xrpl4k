package org.xrpl.sdk.core.model.amount

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode

class CurrencySpecCommonTest : FunSpec({

    val issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

    // -- Xrp.toJson() --

    context("CurrencySpec.Xrp") {
        test("toJson produces correct JSON") {
            val json = CurrencySpec.Xrp.toJson()
            json.toString() shouldBe """{"currency":"XRP"}"""
        }

        test("toJson has exactly one key") {
            val jsonObj = CurrencySpec.Xrp.toJson() as JsonObject
            jsonObj.size shouldBe 1
            jsonObj["currency"]!!.jsonPrimitive.content shouldBe "XRP"
        }
    }

    // -- Issued.toJson() --

    context("CurrencySpec.Issued") {
        test("toJson produces correct JSON with currency and issuer") {
            val spec = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            val json = spec.toJson()
            val jsonObj = json as JsonObject
            jsonObj["currency"]!!.jsonPrimitive.content shouldBe "USD"
            jsonObj["issuer"]!!.jsonPrimitive.content shouldBe "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
        }

        test("toJson has exactly two keys") {
            val spec = CurrencySpec.Issued(CurrencyCode("EUR"), issuer)
            val jsonObj = spec.toJson() as JsonObject
            jsonObj.size shouldBe 2
        }

        test("toJson with different currency code") {
            val spec = CurrencySpec.Issued(CurrencyCode("BTC"), issuer)
            val jsonObj = spec.toJson() as JsonObject
            jsonObj["currency"]!!.jsonPrimitive.content shouldBe "BTC"
        }
    }

    // -- Equality --

    context("equality") {
        test("Xrp equals Xrp") {
            CurrencySpec.Xrp shouldBe CurrencySpec.Xrp
        }

        test("Issued equals Issued with same currency and issuer") {
            val a = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            val b = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            a shouldBe b
        }

        test("Issued not equal with different currency") {
            val a = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            val b = CurrencySpec.Issued(CurrencyCode("EUR"), issuer)
            a shouldNotBe b
        }

        test("Issued not equal with different issuer") {
            val issuer2 = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
            val a = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            val b = CurrencySpec.Issued(CurrencyCode("USD"), issuer2)
            a shouldNotBe b
        }

        test("Xrp not equal to Issued") {
            val issued = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            (CurrencySpec.Xrp == issued) shouldBe false
        }

        test("equal Issued instances have equal hashCodes") {
            val a = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            val b = CurrencySpec.Issued(CurrencyCode("USD"), issuer)
            a.hashCode() shouldBe b.hashCode()
        }
    }

    // -- Sealed interface exhaustive when --

    test("exhaustive when covers Xrp and Issued") {
        val specs: List<CurrencySpec> =
            listOf(
                CurrencySpec.Xrp,
                CurrencySpec.Issued(CurrencyCode("USD"), issuer),
            )
        val labels =
            specs.map { spec ->
                when (spec) {
                    is CurrencySpec.Xrp -> "xrp"
                    is CurrencySpec.Issued -> "issued"
                }
            }
        labels shouldBe listOf("xrp", "issued")
    }
})
