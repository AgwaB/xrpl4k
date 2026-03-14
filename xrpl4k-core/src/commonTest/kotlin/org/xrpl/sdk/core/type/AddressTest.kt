package org.xrpl.sdk.core.type

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class AddressTest : FunSpec({

    context("valid addresses") {
        test("well-known genesis address is accepted") {
            val address = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
            address.value shouldBe "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
        }

        test("25-character address is accepted") {
            shouldNotThrow<IllegalArgumentException> {
                Address("r" + "1".repeat(24))
            }
        }

        test("35-character address is accepted") {
            shouldNotThrow<IllegalArgumentException> {
                Address("r" + "1".repeat(34))
            }
        }
    }

    context("invalid prefix") {
        test("address starting with 'X' should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    Address("XHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
                }
            exception.message shouldContain "must start with 'r'"
        }

        test("address starting with '1' should be rejected") {
            shouldThrow<IllegalArgumentException> {
                Address("1Hb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
            }
        }

        test("empty string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                Address("")
            }
        }
    }

    context("invalid length") {
        test("too short address (24 chars) should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    Address("r" + "1".repeat(23))
                }
            exception.message shouldContain "25-35 characters"
        }

        test("too long address (36 chars) should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    Address("r" + "1".repeat(35))
                }
            exception.message shouldContain "25-35 characters"
        }
    }

    context("invalid characters") {
        test("address with '0' (zero) should be rejected") {
            shouldThrow<IllegalArgumentException> {
                Address("r0b9CJAWyB4rj91VRWn96DkukG4bwdty")
            }
        }

        test("address with 'O' (uppercase O) should be rejected") {
            shouldThrow<IllegalArgumentException> {
                Address("rOb9CJAWyB4rj91VRWn96DkukG4bwdty")
            }
        }

        test("address with 'I' (uppercase I) should be rejected") {
            shouldThrow<IllegalArgumentException> {
                Address("rIb9CJAWyB4rj91VRWn96DkukG4bwdty")
            }
        }

        test("address with 'l' (lowercase L) should be rejected") {
            shouldThrow<IllegalArgumentException> {
                Address("rlb9CJAWyB4rj91VRWn96DkukG4bwdty")
            }
        }
    }

    context("String.toAddress extension") {
        test("valid string converts to Address") {
            val address = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh".toAddress()
            address.value shouldBe "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
        }
    }
})
