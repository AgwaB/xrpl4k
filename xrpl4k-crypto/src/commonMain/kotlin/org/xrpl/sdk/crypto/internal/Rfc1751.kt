@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.internal

/**
 * RFC 1751: A Convention for Human-Readable 128-bit Keys.
 *
 * Converts between 128-bit keys and a 12-word English mnemonic.
 * This is the encoding used by rippled's `wallet_propose` command.
 *
 * XRPL-specific twist: the 128-bit key is byte-swapped (swap128) before/after
 * encoding/decoding, matching rippled's behavior.
 *
 * Ported from xrpl.js `packages/xrpl/src/Wallet/rfc1751.ts` (public domain).
 */
internal object Rfc1751 {
    private val BINARY =
        arrayOf(
            "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111",
            "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111",
        )

    /**
     * Convert an English mnemonic (12 words) to a 128-bit key, following
     * rippled's modified RFC 1751 standard.
     *
     * @param english Space-separated mnemonic words (case-insensitive).
     * @return 16-byte key.
     * @throws IllegalArgumentException if a word is not in the RFC 1751 word list.
     * @throws IllegalStateException if parity check fails.
     */
    fun mnemonicToKey(english: String): ByteArray {
        val words = english.trim().split(Regex("\\s+"))
        var key = intArrayOf()

        for (index in words.indices step 6) {
            val subKey = getSubKey(words, index)

            // check parity
            val skbin = keyToBinary(subKey)
            var parity = 0
            var j = 0
            while (j < 64) {
                parity += extract(skbin, j, 2)
                j += 2
            }
            val cs0 = extract(skbin, 64, 2)
            val cs1 = parity and 3
            check(cs0 == cs1) {
                "Parity error at '${words[minOf(index + 5, words.lastIndex)]}'"
            }

            key = key + subKey.sliceArray(0 until 8)
        }

        // XRPL-specific: swap128 the resulting key
        return swap128(key.map { it.toByte() }.toByteArray())
    }

    private fun getSubKey(
        words: List<String>,
        startIndex: Int,
    ): IntArray {
        val sublist = words.subList(startIndex, minOf(startIndex + 6, words.size))
        var bits = 0
        val ch = IntArray(9)

        for (word in sublist) {
            val idx = RFC1751_WORD_LIST.indexOf(word.uppercase())
            require(idx != -1) {
                "Expected an RFC1751 word, but received '$word'. " +
                    "For the full list of words in the RFC1751 encoding see " +
                    "https://datatracker.ietf.org/doc/html/rfc1751"
            }
            val shift = (8 - ((bits + 11) % 8)) % 8
            val y = idx shl shift
            val cl = y shr 16
            val cc = (y shr 8) and 0xFF
            val cr = y and 0xFF
            val t = bits / 8
            if (shift > 5) {
                ch[t] = ch[t] or cl
                ch[t + 1] = ch[t + 1] or cc
                ch[t + 2] = ch[t + 2] or cr
            } else if (shift > -3) {
                ch[t] = ch[t] or cc
                ch[t + 1] = ch[t + 1] or cr
            } else {
                ch[t] = ch[t] or cr
            }
            bits += 11
        }
        return ch
    }

    private fun keyToBinary(key: IntArray): String {
        val sb = StringBuilder()
        for (num in key) {
            sb.append(BINARY[(num shr 4) and 0x0F])
            sb.append(BINARY[num and 0x0F])
        }
        return sb.toString()
    }

    private fun extract(
        key: String,
        start: Int,
        length: Int,
    ): Int {
        val sub = key.substring(start, start + length)
        var acc = 0
        for (ch in sub) {
            acc = acc * 2 + (ch.code - 48)
        }
        return acc
    }

    /**
     * Swap byte order of a 128-bit (16-byte) array.
     *
     * First swap each 64-bit half internally, then swap the two halves.
     * This matches the XRPL-specific endianness conversion.
     */
    private fun swap128(arr: ByteArray): ByteArray {
        val copy = arr.copyOf()
        swap64(copy)
        // swap the two 64-bit halves
        val result = ByteArray(copy.size)
        copy.copyInto(result, 0, 8, 16)
        copy.copyInto(result, 8, 0, 8)
        return result
    }

    private fun swap64(arr: ByteArray) {
        var i = 0
        while (i < arr.size) {
            swap(arr, i, i + 7)
            swap(arr, i + 1, i + 6)
            swap(arr, i + 2, i + 5)
            swap(arr, i + 3, i + 4)
            i += 8
        }
    }

    private fun swap(
        arr: ByteArray,
        n: Int,
        m: Int,
    ) {
        val tmp = arr[n]
        arr[n] = arr[m]
        arr[m] = tmp
    }
}
