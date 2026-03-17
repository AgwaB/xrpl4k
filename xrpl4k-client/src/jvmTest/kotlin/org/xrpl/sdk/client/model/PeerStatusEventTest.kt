package org.xrpl.sdk.client.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json
import org.xrpl.sdk.client.internal.dto.PeerStatusEventDto

/**
 * Tests for PeerStatusEvent model fields (Bug 4).
 */
class PeerStatusEventTest : FunSpec({

    test("PeerStatusEvent has correct fields") {
        val event =
            PeerStatusEvent(
                action = "CLOSING_LEDGER",
                date = 755914091L,
                ledgerHash = "ABCD1234",
                ledgerIndex = 1000L,
                ledgerIndexMax = 1500L,
                ledgerIndexMin = 500L,
            )
        event.action shouldBe "CLOSING_LEDGER"
        event.date shouldBe 755914091L
        event.ledgerHash shouldBe "ABCD1234"
        event.ledgerIndex shouldBe 1000L
        event.ledgerIndexMax shouldBe 1500L
        event.ledgerIndexMin shouldBe 500L
    }

    test("PeerStatusEvent toString includes ledgerHash and ledgerIndex") {
        val event =
            PeerStatusEvent(
                action = "CLOSING_LEDGER",
                date = 755914091L,
                ledgerHash = "ABCD1234",
                ledgerIndex = 1000L,
                ledgerIndexMax = null,
                ledgerIndexMin = null,
            )
        event.toString() shouldContain "ABCD1234"
        event.toString() shouldContain "1000"
    }

    test("PeerStatusEvent equality based on action, date, ledgerHash, ledgerIndex") {
        val a =
            PeerStatusEvent(
                action = "CLOSING_LEDGER",
                date = 100L,
                ledgerHash = "HASH",
                ledgerIndex = 42L,
                ledgerIndexMax = null,
                ledgerIndexMin = null,
            )
        val b =
            PeerStatusEvent(
                action = "CLOSING_LEDGER",
                date = 100L,
                ledgerHash = "HASH",
                ledgerIndex = 42L,
                ledgerIndexMax = 999L,
                ledgerIndexMin = 1L,
            )
        (a == b) shouldBe true
    }

    test("PeerStatusEventDto deserializes with date as Long") {
        val json =
            """{"action":"CLOSING_LEDGER","date":755914091,""" +
                """"ledger_hash":"AB","ledger_index":100,"type":"peerStatusChange"}"""
        val dto = Json.decodeFromString(PeerStatusEventDto.serializer(), json)
        dto.action shouldBe "CLOSING_LEDGER"
        dto.date shouldBe 755914091L
        dto.ledgerHash shouldBe "AB"
        dto.ledgerIndex shouldBe 100L
    }

    test("PeerStatusEvent nullable fields") {
        val event =
            PeerStatusEvent(
                action = null,
                date = null,
                ledgerHash = null,
                ledgerIndex = null,
                ledgerIndexMax = null,
                ledgerIndexMin = null,
            )
        event.action shouldBe null
        event.date shouldBe null
        event.ledgerHash shouldBe null
        event.ledgerIndex shouldBe null
    }
})
