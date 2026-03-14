@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.platformCryptoProvider

class TransactionUtilsTest : FunSpec({

    val provider = platformCryptoProvider()

    // ── hashSignedTx ──────────────────────────────────────────────────────────

    test("hashSignedTx: known Ed25519 blob produces expected TxHash") {
        val txBlob =
            "1200002400000001201B000000646140000000000F424068400000000000000C" +
                "7321EDA57EBBCB502C2009EFE17229E8DC865DCCB192C52D7888D624DC9EBADDB815F0" +
                "7440E80EEB1AEF4470371964BAE9AE81ED04527F09E9F5E0FDE703D12A23A8CE8E425A" +
                "524F8BCE3C2DB2005A117C976486B1810ED56F90B75499D118073306777C0E" +
                "8114A6070B8A1822E3322676A99F0C804EE2D15B82708314F667B0CA50CC7709A220B0561B85E53A48461FA8"
        val expectedHash = "F049ADE3E5F6F601C581781D8526163D68FD807F6076F5458E9E6F9D1D1E212A"

        val result = hashSignedTx(txBlob, provider)

        result.value shouldBe expectedHash
    }

    test("hashSignedTx: known secp256k1 blob produces expected TxHash") {
        val txBlob =
            "1200002400000001201B000000646140000000000F424068400000000000000C" +
                "73210390A196799EE412284A5D80BF78C3E84CBB80E1437A0AECD9ADF94D7FEAAFA284" +
                "7446304402207C06E5A98DF36DEAA3B7EDA2B27BD34BAFFCD18ED47E3E3C47D90AE962EA18E2" +
                "02207BAD4C7BCE70DDF6DDAABA2F9C4376146D9707835F7B36B74900284FE97FE8D" +
                "B8114ABDE56C21ACFBF94172489A1A01A9A6A95C4849D8314F667B0CA50CC7709A220B0561B85E53A48461FA8"
        val expectedHash = "604DA61CA8A7A5F996E53030CDD2632A886EEF698A71B64D4A1058ED093A4298"

        val result = hashSignedTx(txBlob, provider)

        result.value shouldBe expectedHash
    }

    test("hashSignedTx: result is 64-character uppercase hex string") {
        val txBlob =
            "1200002400000001201B000000646140000000000F424068400000000000000C" +
                "7321EDA57EBBCB502C2009EFE17229E8DC865DCCB192C52D7888D624DC9EBADDB815F0" +
                "7440E80EEB1AEF4470371964BAE9AE81ED04527F09E9F5E0FDE703D12A23A8CE8E425A" +
                "524F8BCE3C2DB2005A117C976486B1810ED56F90B75499D118073306777C0E" +
                "8114A6070B8A1822E3322676A99F0C804EE2D15B82708314F667B0CA50CC7709A220B0561B85E53A48461FA8"

        val result = hashSignedTx(txBlob, provider)

        result.value.length shouldBe 64
        result.value shouldBe result.value.uppercase()
    }

    // ── parseBalanceChanges ───────────────────────────────────────────────────

    test("parseBalanceChanges: empty AffectedNodes returns empty map") {
        val metadata =
            buildJsonObject {
                put("AffectedNodes", buildJsonArray {})
            }

        parseBalanceChanges(metadata).shouldBeEmpty()
    }

    test("parseBalanceChanges: missing AffectedNodes returns empty map") {
        val metadata = buildJsonObject {}

        parseBalanceChanges(metadata).shouldBeEmpty()
    }

    test("parseBalanceChanges: ModifiedNode AccountRoot produces XRP balance delta") {
        val account = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
        val metadata =
            buildJsonObject {
                put(
                    "AffectedNodes",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put(
                                    "ModifiedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "AccountRoot")
                                        put(
                                            "PreviousFields",
                                            buildJsonObject { put("Balance", "1000000") },
                                        )
                                        put(
                                            "FinalFields",
                                            buildJsonObject {
                                                put("Account", account)
                                                put("Balance", "999988")
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }

        val changes = parseBalanceChanges(metadata)

        changes shouldHaveSize 1
        val accountChanges = changes[Address(account)]
        accountChanges.shouldNotBeNull()
        accountChanges.size shouldBe 1
        accountChanges[0].currency shouldBe "XRP"
        accountChanges[0].value shouldBe "-12"
        accountChanges[0].counterparty.shouldBeNull()
    }

    test("parseBalanceChanges: AccountRoot without PreviousFields.Balance is skipped") {
        val account = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
        val metadata =
            buildJsonObject {
                put(
                    "AffectedNodes",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put(
                                    "ModifiedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "AccountRoot")
                                        put("PreviousFields", buildJsonObject {})
                                        put(
                                            "FinalFields",
                                            buildJsonObject {
                                                put("Account", account)
                                                put("Balance", "999988")
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }

        parseBalanceChanges(metadata).shouldBeEmpty()
    }

    test("parseBalanceChanges: RippleState IOU change attributed to both low and high accounts") {
        val lowAccount = "rGCkuB7PBr5tNy68tPEABEtcdno4hE6Y7f"
        val highAccount = "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"
        val metadata =
            buildJsonObject {
                put(
                    "AffectedNodes",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put(
                                    "ModifiedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "RippleState")
                                        put(
                                            "PreviousFields",
                                            buildJsonObject { put("Balance", "10.0") },
                                        )
                                        put(
                                            "FinalFields",
                                            buildJsonObject {
                                                put("Balance", "20.0")
                                                put(
                                                    "LowLimit",
                                                    buildJsonObject {
                                                        put("issuer", lowAccount)
                                                        put("currency", "USD")
                                                        put("value", "200")
                                                    },
                                                )
                                                put(
                                                    "HighLimit",
                                                    buildJsonObject {
                                                        put("issuer", highAccount)
                                                        put("currency", "USD")
                                                        put("value", "0")
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }

        val changes = parseBalanceChanges(metadata)

        changes shouldHaveSize 2

        val lowChanges = changes[Address(lowAccount)]
        lowChanges.shouldNotBeNull()
        lowChanges.size shouldBe 1
        lowChanges[0].currency shouldBe "USD"
        lowChanges[0].counterparty shouldBe Address(highAccount)

        val highChanges = changes[Address(highAccount)]
        highChanges.shouldNotBeNull()
        highChanges.size shouldBe 1
        highChanges[0].currency shouldBe "USD"
        highChanges[0].counterparty shouldBe Address(lowAccount)
    }

    test("parseBalanceChanges: CreatedNode and DeletedNode without PreviousFields.Balance are skipped") {
        val account = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
        val metadata =
            buildJsonObject {
                put(
                    "AffectedNodes",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put(
                                    "CreatedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "AccountRoot")
                                        put(
                                            "NewFields",
                                            buildJsonObject {
                                                put("Account", account)
                                                put("Balance", "200000000")
                                            },
                                        )
                                    },
                                )
                            },
                        )
                        add(
                            buildJsonObject {
                                put(
                                    "DeletedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "AccountRoot")
                                        put(
                                            "FinalFields",
                                            buildJsonObject {
                                                put("Account", account)
                                                put("Balance", "0")
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }

        parseBalanceChanges(metadata).shouldBeEmpty()
    }

    // ── getNFTokenID ──────────────────────────────────────────────────────────

    test("getNFTokenID: empty AffectedNodes returns null") {
        val metadata =
            buildJsonObject {
                put("AffectedNodes", buildJsonArray {})
            }

        getNFTokenID(metadata).shouldBeNull()
    }

    test("getNFTokenID: no NFTokenPage node returns null") {
        val metadata =
            buildJsonObject {
                put(
                    "AffectedNodes",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put(
                                    "ModifiedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "AccountRoot")
                                        put(
                                            "FinalFields",
                                            buildJsonObject { put("Account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh") },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }

        getNFTokenID(metadata).shouldBeNull()
    }

    test("getNFTokenID: ModifiedNode NFTokenPage returns newly minted token ID") {
        val newTokenId = "000800006203F49C21D5D6E022CB16DE3538F248662FC73C4AE36AD0000000C"
        val existingTokenId = "000800006203F49C21D5D6E022CB16DE3538F248662FC73C4AE36AD000000008"
        val metadata =
            buildJsonObject {
                put(
                    "AffectedNodes",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put(
                                    "ModifiedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "NFTokenPage")
                                        put(
                                            "PreviousFields",
                                            buildJsonObject {
                                                put(
                                                    "NFTokens",
                                                    buildJsonArray {
                                                        add(
                                                            buildJsonObject {
                                                                put(
                                                                    "NFToken",
                                                                    buildJsonObject {
                                                                        put("NFTokenID", existingTokenId)
                                                                    },
                                                                )
                                                            },
                                                        )
                                                    },
                                                )
                                            },
                                        )
                                        put(
                                            "FinalFields",
                                            buildJsonObject {
                                                put(
                                                    "NFTokens",
                                                    buildJsonArray {
                                                        add(
                                                            buildJsonObject {
                                                                put(
                                                                    "NFToken",
                                                                    buildJsonObject {
                                                                        put("NFTokenID", existingTokenId)
                                                                    },
                                                                )
                                                            },
                                                        )
                                                        add(
                                                            buildJsonObject {
                                                                put(
                                                                    "NFToken",
                                                                    buildJsonObject {
                                                                        put("NFTokenID", newTokenId)
                                                                    },
                                                                )
                                                            },
                                                        )
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }

        getNFTokenID(metadata) shouldBe newTokenId
    }

    test("getNFTokenID: CreatedNode NFTokenPage with NewFields returns first token ID") {
        val tokenId = "000800006203F49C21D5D6E022CB16DE3538F248662FC73C4AE36AD000000001"
        val metadata =
            buildJsonObject {
                put(
                    "AffectedNodes",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put(
                                    "CreatedNode",
                                    buildJsonObject {
                                        put("LedgerEntryType", "NFTokenPage")
                                        put(
                                            "NewFields",
                                            buildJsonObject {
                                                put(
                                                    "NFTokens",
                                                    buildJsonArray {
                                                        add(
                                                            buildJsonObject {
                                                                put(
                                                                    "NFToken",
                                                                    buildJsonObject {
                                                                        put("NFTokenID", tokenId)
                                                                    },
                                                                )
                                                            },
                                                        )
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }

        getNFTokenID(metadata) shouldBe tokenId
    }

    // ── BalanceChange ─────────────────────────────────────────────────────────

    test("BalanceChange: equals and hashCode are consistent") {
        val issuer = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
        val a = BalanceChange(currency = "USD", value = "10.5", counterparty = issuer)
        val b = BalanceChange(currency = "USD", value = "10.5", counterparty = issuer)

        (a == b) shouldBe true
        a.hashCode() shouldBe b.hashCode()
    }

    test("BalanceChange: not equal when fields differ") {
        val issuer = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
        val xrp = BalanceChange(currency = "XRP", value = "-12")
        val iou = BalanceChange(currency = "USD", value = "10.5", counterparty = issuer)

        (xrp == iou) shouldBe false
    }

    test("BalanceChange: toString includes currency, value, and counterparty") {
        val issuer = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
        val change = BalanceChange(currency = "EUR", value = "5.0", counterparty = issuer)
        val str = change.toString()

        str shouldStartWith "BalanceChange("
        str.contains("EUR") shouldBe true
        str.contains("5.0") shouldBe true
        str.contains(issuer.value) shouldBe true
    }

    // ── verifyTransaction ─────────────────────────────────────────────────

    test("verifyTransaction: valid Ed25519 signed blob returns true") {
        val txBlob =
            "1200002400000001201B000000646140000000000F424068400000000000000C" +
                "7321EDA57EBBCB502C2009EFE17229E8DC865DCCB192C52D7888D624DC9EBADDB815F0" +
                "7440E80EEB1AEF4470371964BAE9AE81ED04527F09E9F5E0FDE703D12A23A8CE8E425A" +
                "524F8BCE3C2DB2005A117C976486B1810ED56F90B75499D118073306777C0E" +
                "8114A6070B8A1822E3322676A99F0C804EE2D15B82708314F667B0CA50CC7709A220B0561B85E53A48461FA8"

        verifyTransaction(txBlob, provider) shouldBe true
    }

    test("verifyTransaction: valid secp256k1 signed blob returns true") {
        val txBlob =
            "1200002400000001201B000000646140000000000F424068400000000000000C" +
                "73210390A196799EE412284A5D80BF78C3E84CBB80E1437A0AECD9ADF94D7FEAAFA284" +
                "7446304402207C06E5A98DF36DEAA3B7EDA2B27BD34BAFFCD18ED47E3E3C47D90AE962EA18E2" +
                "02207BAD4C7BCE70DDF6DDAABA2F9C4376146D9707835F7B36B74900284FE97FE8D" +
                "B8114ABDE56C21ACFBF94172489A1A01A9A6A95C4849D8314F667B0CA50CC7709A220B0561B85E53A48461FA8"

        verifyTransaction(txBlob, provider) shouldBe true
    }

    test("verifyTransaction: tampered blob returns false") {
        // Change a byte in the middle of the blob (amount area)
        val txBlob =
            "1200002400000001201B000000646140000000000F424168400000000000000C" +
                "7321EDA57EBBCB502C2009EFE17229E8DC865DCCB192C52D7888D624DC9EBADDB815F0" +
                "7440E80EEB1AEF4470371964BAE9AE81ED04527F09E9F5E0FDE703D12A23A8CE8E425A" +
                "524F8BCE3C2DB2005A117C976486B1810ED56F90B75499D118073306777C0E" +
                "8114A6070B8A1822E3322676A99F0C804EE2D15B82708314F667B0CA50CC7709A220B0561B85E53A48461FA8"

        verifyTransaction(txBlob, provider) shouldBe false
    }
})
