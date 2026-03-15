@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.hashing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Tests for ledger object hash utilities.
 *
 * Expected values are taken from xrpl.js test/utils/hashes.test.ts
 * and normalized to lowercase for comparison.
 */
class LedgerHashesTest : FunSpec({

    val provider = platformCryptoProvider()

    context("hashAccountRoot") {
        test("produces known hash for genesis account") {
            val hash = hashAccountRoot("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", provider)
            hash shouldBe "2b6ac232aa4c4be41bf49d2459fa4a0347e1b543a4c92fcee0821c0201e2e9a8"
        }
    }

    context("hashSignerListId") {
        test("produces known hash for genesis account") {
            val hash = hashSignerListId("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", provider)
            hash shouldBe "778365d5180f5df3016817d1f318527ad7410d83f8636cf48c43e8af72ab49bf"
        }
    }

    context("hashOfferId") {
        test("produces known hash for offer") {
            val hash = hashOfferId("r32UufnaCGL82HubijgJGDmdE5hac7ZvLw", 137, provider)
            hash shouldBe "03f0aed09deee74cef85cd57a0429d6113507cf759c597babb4adb752f734ce3"
        }
    }

    context("hashTrustline") {
        test("produces known hash for USD trustline") {
            val hash =
                hashTrustline(
                    "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",
                    "rB5TihdPbKgMrkFqrqUC3yLdE8hhv4BdeY",
                    "USD",
                    provider,
                )
            hash shouldBe "c683b5bb928f025f1e860d9d69d6c554c2202de0d45877adb3077da4cb9e125c"
        }

        test("produces same hash regardless of address order") {
            val hash1 =
                hashTrustline(
                    "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",
                    "rB5TihdPbKgMrkFqrqUC3yLdE8hhv4BdeY",
                    "USD",
                    provider,
                )
            val hash2 =
                hashTrustline(
                    "rB5TihdPbKgMrkFqrqUC3yLdE8hhv4BdeY",
                    "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",
                    "USD",
                    provider,
                )
            hash1 shouldBe hash2
        }

        test("produces known hash for UAM trustline") {
            val hash =
                hashTrustline(
                    "r3kmLJN5D28dHuH8vZNUZpMC43pEHpaocV",
                    "rUAMuQTfVhbfqUDuro7zzy4jj4Wq57MPTj",
                    "UAM",
                    provider,
                )
            hash shouldBe "ae9addc584358e5847adfc971834e471436fc3e9de6ea1773df49f419dc0f65e"
        }
    }

    context("hashEscrow") {
        test("produces known hash") {
            val hash = hashEscrow("rDx69ebzbowuqztksVDmZXjizTd12BVr4x", 84, provider)
            hash shouldBe "61e8e8ed53fa2cebe192b23897071e9a75217bf5a410e9cb5b45aab7aeca567a"
        }
    }

    context("hashPaymentChannel") {
        test("produces known hash") {
            val hash =
                hashPaymentChannel(
                    "rDx69ebzbowuqztksVDmZXjizTd12BVr4x",
                    "rLFtVprxUEfsH54eCWKsZrEQzMDsx1wqso",
                    82,
                    provider,
                )
            hash shouldBe "e35708503b3c3143fb522d749aafcc296e8060f0fb371a9a56fae0b1ed127366"
        }
    }

    context("hashVault") {
        test("produces deterministic hash") {
            val hash1 = hashVault("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 1, provider)
            val hash2 = hashVault("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 1, provider)
            hash1 shouldBe hash2
            hash1.length shouldBe 64
        }

        test("different sequence produces different hash") {
            val hash1 = hashVault("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 1, provider)
            val hash2 = hashVault("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 2, provider)
            hash1 shouldNotBe hash2
        }

        test("different address produces different hash") {
            val hash1 = hashVault("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 1, provider)
            val hash2 = hashVault("rDx69ebzbowuqztksVDmZXjizTd12BVr4x", 1, provider)
            hash1 shouldNotBe hash2
        }
    }

    context("hashLoanBroker") {
        test("produces deterministic hash") {
            val hash1 = hashLoanBroker("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 10, provider)
            val hash2 = hashLoanBroker("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 10, provider)
            hash1 shouldBe hash2
            hash1.length shouldBe 64
        }

        test("different sequence produces different hash") {
            val hash1 = hashLoanBroker("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 10, provider)
            val hash2 = hashLoanBroker("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 20, provider)
            hash1 shouldNotBe hash2
        }
    }

    context("hashLoan") {
        test("produces deterministic hash with hex loanBrokerId") {
            // Use a known vault/broker hash as the loanBrokerId input
            val brokerId = hashLoanBroker("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 10, provider)
            val hash1 = hashLoan(brokerId, 1, provider)
            val hash2 = hashLoan(brokerId, 1, provider)
            hash1 shouldBe hash2
            hash1.length shouldBe 64
        }

        test("different loanSequence produces different hash") {
            val brokerId = hashLoanBroker("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", 10, provider)
            val hash1 = hashLoan(brokerId, 1, provider)
            val hash2 = hashLoan(brokerId, 2, provider)
            hash1 shouldNotBe hash2
        }
    }

    context("hashTx") {
        test("produces 64-char hex hash") {
            // A valid even-length hex blob for testing
            val txBlob = "12000022000000002400000003614000000000002710684000000000000064"
            val hash = hashTx(txBlob, provider)
            hash.length shouldBe 64
        }

        test("is deterministic") {
            val txBlob = "1200002200000000"
            val hash1 = hashTx(txBlob, provider)
            val hash2 = hashTx(txBlob, provider)
            hash1 shouldBe hash2
        }
    }

    context("hashSignedTx") {
        test("produces 64-char hex hash") {
            val txBlob =
                "1200002200000000240000000361D4838D7EA4C68000000000000000000000000000" +
                    "55534400000000004B4E9C06F24296074F7BC48F92A97916C6DC5EA968400000000000000C"
            val hash = hashSignedTx(txBlob, provider)
            hash.length shouldBe 64
        }

        test("differs from hashTx for same blob") {
            val txBlob = "1200002200000000"
            val signedHash = hashSignedTx(txBlob, provider)
            val txHash = hashTx(txBlob, provider)
            signedHash shouldNotBe txHash
        }
    }

    context("hashLedgerHeader") {
        test("produces 64-char hex hash") {
            // Minimal header: 4 + 8 + 32 + 32 + 32 + 4 + 4 + 1 + 1 = 118 bytes = 236 hex chars
            val header =
                "00000001" + "0".repeat(200) +
                    "0000000A0000000B0C0D"
            val hash = hashLedgerHeader(header, provider)
            hash.length shouldBe 64
        }

        test("is deterministic") {
            val header =
                "00000001" + "0".repeat(228)
            val hash1 = hashLedgerHeader(header, provider)
            val hash2 = hashLedgerHeader(header, provider)
            hash1 shouldBe hash2
        }
    }
})
