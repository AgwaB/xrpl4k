@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.hashing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
})
