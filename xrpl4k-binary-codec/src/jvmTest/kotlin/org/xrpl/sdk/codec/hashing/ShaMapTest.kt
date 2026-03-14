package org.xrpl.sdk.codec.hashing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.crypto.platformCryptoProvider

class ShaMapTest : FunSpec({

    val provider = platformCryptoProvider()
    val zeroHash = "0".repeat(64)

    test("empty ShaMap has all-zeros hash") {
        val map = ShaMap(provider)
        map.hash shouldBe zeroHash
    }

    test("adding a single item produces a non-zero hash") {
        val map = ShaMap(provider)
        val tag = "A" .repeat(64)
        val data = "DEADBEEF"
        map.addItem(tag, data, ShaMapNodeType.ACCOUNT_STATE)
        map.hash shouldNotBe zeroHash
    }

    test("hash is deterministic for same inputs") {
        val tag = "B".repeat(64)
        val data = "0102030405"

        val map1 = ShaMap(provider)
        map1.addItem(tag, data, ShaMapNodeType.ACCOUNT_STATE)

        val map2 = ShaMap(provider)
        map2.addItem(tag, data, ShaMapNodeType.ACCOUNT_STATE)

        map1.hash shouldBe map2.hash
    }

    test("different data produces different hashes") {
        val tag = "C".repeat(64)

        val map1 = ShaMap(provider)
        map1.addItem(tag, "0102030405", ShaMapNodeType.ACCOUNT_STATE)

        val map2 = ShaMap(provider)
        map2.addItem(tag, "0102030406", ShaMapNodeType.ACCOUNT_STATE)

        map1.hash shouldNotBe map2.hash
    }

    test("different node types produce different hashes") {
        val tag = "D".repeat(64)
        val data = "AABBCCDD"

        val map1 = ShaMap(provider)
        map1.addItem(tag, data, ShaMapNodeType.ACCOUNT_STATE)

        val map2 = ShaMap(provider)
        map2.addItem(tag, data, ShaMapNodeType.TRANSACTION_METADATA)

        map1.hash shouldNotBe map2.hash
    }

    test("multiple items produce a valid tree hash") {
        val map = ShaMap(provider)
        // Tags start with different nibbles, so they go to different branches
        map.addItem(
            "1" + "0".repeat(63),
            "AABB",
            ShaMapNodeType.ACCOUNT_STATE,
        )
        map.addItem(
            "2" + "0".repeat(63),
            "CCDD",
            ShaMapNodeType.ACCOUNT_STATE,
        )
        map.hash shouldNotBe zeroHash
        map.hash.length shouldBe 64
    }

    test("items with same first nibble create deeper inner nodes") {
        val map = ShaMap(provider)
        // Both start with 'A', so they'll collide at depth 0 and split at depth 1
        map.addItem(
            "A1" + "0".repeat(62),
            "AABB",
            ShaMapNodeType.ACCOUNT_STATE,
        )
        map.addItem(
            "A2" + "0".repeat(62),
            "CCDD",
            ShaMapNodeType.ACCOUNT_STATE,
        )
        map.hash shouldNotBe zeroHash
        map.hash.length shouldBe 64
    }

    test("TRANSACTION_NO_META hashing ignores tag in hash") {
        val tag1 = "E".repeat(64)
        val tag2 = "F".repeat(64)
        val data = "112233"

        // For TRANSACTION_NO_META, hash = sha512Half(TRANSACTION_ID + data)
        // So two leaves with same data but different tags will have the same leaf hash
        // but the tree hash differs because the tag determines placement
        val map1 = ShaMap(provider)
        map1.addItem(tag1, data, ShaMapNodeType.TRANSACTION_NO_META)

        val map2 = ShaMap(provider)
        map2.addItem(tag2, data, ShaMapNodeType.TRANSACTION_NO_META)

        // Tree hashes differ because the tag placement differs
        map1.hash shouldNotBe map2.hash
    }

    test("hash is a valid 64-char lowercase hex string") {
        val map = ShaMap(provider)
        map.addItem("A".repeat(64), "FF", ShaMapNodeType.ACCOUNT_STATE)
        val hash = map.hash
        hash.length shouldBe 64
        hash.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
    }
})
