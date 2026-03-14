@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops

private fun parse(raw: String): LedgerObject = parseLedgerObject(Json.parseToJsonElement(raw))

class LedgerObjectsTest : FunSpec({

    test("parses Check object") {
        val obj =
            parse(
                """{"LedgerEntryType":"Check","index":"ABC123",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"Destination":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                    """"SendMax":"100000000","Sequence":5,"Expiration":700000000}""",
            )
        obj.shouldBeInstanceOf<CheckObject>()
        obj.index shouldBe "ABC123"
        obj.account shouldBe Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
        obj.destination shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
        obj.sequence shouldBe 5u
        obj.expiration shouldBe 700000000u
    }

    test("parses Escrow object") {
        val obj =
            parse(
                """{"LedgerEntryType":"Escrow","index":"DEF456",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"Destination":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                    """"Amount":"5000000","FinishAfter":600000000,"Condition":"A02580"}""",
            )
        obj.shouldBeInstanceOf<EscrowObject>()
        obj.amount shouldBe XrpDrops(5_000_000)
        obj.finishAfter shouldBe 600000000u
        obj.cancelAfter shouldBe null
        obj.condition shouldBe "A02580"
    }

    test("parses Offer object") {
        val obj =
            parse(
                """{"LedgerEntryType":"Offer","index":"OFF789",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"TakerGets":"1000000","TakerPays":{"currency":"USD",""" +
                    """"issuer":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe","value":"1.5"},""" +
                    """"Sequence":42,"Flags":131072}""",
            )
        obj.shouldBeInstanceOf<OfferObject>()
        obj.sequence shouldBe 42u
        obj.flags shouldBe 131072u
    }

    test("parses PayChannel object") {
        val obj =
            parse(
                """{"LedgerEntryType":"PayChannel","index":"PCH001",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"Destination":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                    """"Amount":"10000000","Balance":"3000000","SettleDelay":86400,""" +
                    """"PublicKey":"023693F15967AE357D0327974AD46FE3C127113B1110D6044B924638B53F6EB735"}""",
            )
        obj.shouldBeInstanceOf<PaymentChannelObject>()
        obj.amount shouldBe XrpDrops(10_000_000)
        obj.balance shouldBe XrpDrops(3_000_000)
        obj.settleDelay shouldBe 86400u
        obj.publicKey shouldBe "023693F15967AE357D0327974AD46FE3C127113B1110D6044B924638B53F6EB735"
    }

    test("parses SignerList object") {
        val obj =
            parse(
                """{"LedgerEntryType":"SignerList","index":"SIG001",""" +
                    """"SignerQuorum":3,"SignerEntries":[{"SignerEntry":{"Account":"rXXX","SignerWeight":1}}]}""",
            )
        obj.shouldBeInstanceOf<SignerListObject>()
        obj.signerQuorum shouldBe 3u
    }

    test("parses Ticket object") {
        val obj =
            parse(
                """{"LedgerEntryType":"Ticket","index":"TKT001",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","TicketSequence":10}""",
            )
        obj.shouldBeInstanceOf<TicketObject>()
        obj.ticketSequence shouldBe 10u
    }

    test("parses RippleState object") {
        val obj =
            parse(
                """{"LedgerEntryType":"RippleState","index":"RS001",""" +
                    """"Balance":{"currency":"USD","issuer":"rrrrrrrrrrrrrrrrrrrrBZbvji","value":"10"},""" +
                    """"HighLimit":{"currency":"USD","issuer":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe","value":"100"},""" +
                    """"LowLimit":{"currency":"USD","issuer":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","value":"0"},""" +
                    """"Flags":65536}""",
            )
        obj.shouldBeInstanceOf<RippleStateObject>()
        obj.flags shouldBe 65536u
    }

    test("parses DepositPreauth object") {
        val obj =
            parse(
                """{"LedgerEntryType":"DepositPreauth","index":"DP001",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"Authorize":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"}""",
            )
        obj.shouldBeInstanceOf<DepositPreauthObject>()
        obj.authorize shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    }

    test("parses NFTokenOffer object") {
        val obj =
            parse(
                """{"LedgerEntryType":"NFTokenOffer","index":"NF001",""" +
                    """"Owner":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"NFTokenID":"000800006203F49C21D5D6E022CB16DE3538F248662FC73C00000000",""" +
                    """"Amount":"1000000","Flags":1,""" +
                    """"Destination":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"}""",
            )
        obj.shouldBeInstanceOf<NftOfferObject>()
        obj.nfTokenId shouldBe "000800006203F49C21D5D6E022CB16DE3538F248662FC73C00000000"
        obj.flags shouldBe 1u
        obj.destination shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    }

    test("unknown LedgerEntryType returns UnknownLedgerObject") {
        val obj =
            parse(
                """{"LedgerEntryType":"FutureType","index":"UNK001","SomeField":"value"}""",
            )
        obj.shouldBeInstanceOf<UnknownLedgerObject>()
        obj.ledgerEntryType shouldBe "FutureType"
        obj.index shouldBe "UNK001"
    }

    test("AccountObjectsResult.objects returns typed list") {
        val json1 =
            Json.parseToJsonElement(
                """{"LedgerEntryType":"Ticket","index":"T1",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","TicketSequence":1}""",
            )
        val json2 =
            Json.parseToJsonElement(
                """{"LedgerEntryType":"Offer","index":"O1",""" +
                    """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"TakerGets":"100","TakerPays":"200","Sequence":1,"Flags":0}""",
            )
        val result =
            AccountObjectsResult(
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                accountObjects = listOf(json1, json2),
                marker = null,
                ledgerIndex = null,
            )
        result.objects[0].shouldBeInstanceOf<TicketObject>()
        result.objects[1].shouldBeInstanceOf<OfferObject>()
    }
})
