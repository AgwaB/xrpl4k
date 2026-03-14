@file:Suppress("MagicNumber")

package org.xrpl.sdk.test

import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.model.transaction.offerCreate
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.model.transaction.trustSet
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.core.type.Seed
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Well-known XRPL test addresses for use in unit and integration tests.
 *
 * These are canonical addresses used across XRPL tooling. The genesis address
 * is the account created from the all-zeros seed and holds the initial XRP supply
 * on test ledgers.
 */
public object TestAddresses {
    /** The XRPL genesis account (all-zeros seed). Holds initial XRP supply on test ledgers. */
    public val GENESIS: Address = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

    /** A well-known test account representing Alice. */
    public val ALICE: Address = Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59")

    /** A well-known test account representing Bob. */
    public val BOB: Address = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")

    /** A well-known test account representing Charlie. */
    public val CHARLIE: Address = Address("rN7n3473SaZBCG4dFL83w7PB7mGHkgHuAN")
}

/**
 * Well-known test seeds for deterministic wallet creation in tests.
 *
 * These seeds correspond to the [TestAddresses] accounts when used with the
 * secp256k1 key algorithm.
 */
public object TestSeeds {
    /** Seed corresponding to the genesis account (all-zeros). */
    public val GENESIS: Seed = Seed("00000000000000000000000000000000")

    /** Seed for the Alice test account. */
    public val ALICE: Seed = Seed("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")

    /** Seed for the Bob test account. */
    public val BOB: Seed = Seed("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")

    /** Seed for the Charlie test account. */
    public val CHARLIE: Seed = Seed("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")
}

/**
 * Pre-built [XrplTransaction.Unsigned] examples for use in tests.
 *
 * Each factory property returns a fresh transaction instance with realistic
 * field values using [TestAddresses] accounts.
 */
public object TestTransactions {
    /**
     * A Payment transaction sending 1 XRP from [TestAddresses.ALICE] to [TestAddresses.BOB].
     */
    public val payment: XrplTransaction.Unsigned
        get() =
            payment {
                account = TestAddresses.ALICE
                destination = TestAddresses.BOB
                amount = XrpAmount(TestAmounts.ONE_XRP)
            }

    /**
     * An OfferCreate transaction where [TestAddresses.ALICE] offers 100 XRP for 1 USD.
     */
    public val offerCreate: XrplTransaction.Unsigned
        get() =
            offerCreate {
                account = TestAddresses.ALICE
                takerGets = XrpAmount(TestAmounts.HUNDRED_XRP)
                takerPays =
                    IssuedAmount(
                        currency = CurrencyCode("USD"),
                        issuer = TestAddresses.BOB,
                        value = "1",
                    )
            }

    /**
     * A TrustSet transaction where [TestAddresses.ALICE] sets a 1000 USD trust line with [TestAddresses.BOB].
     */
    public val trustSet: XrplTransaction.Unsigned
        get() =
            trustSet {
                account = TestAddresses.ALICE
                limitAmount =
                    IssuedAmount(
                        currency = CurrencyCode("USD"),
                        issuer = TestAddresses.BOB,
                        value = "1000",
                    )
            }
}

/**
 * Common XRP drop amounts for use in test assertions and transaction construction.
 */
public object TestAmounts {
    /** 1 XRP expressed in drops (1,000,000 drops). */
    public val ONE_XRP: XrpDrops = XrpDrops(1_000_000L)

    /** 100 XRP expressed in drops (100,000,000 drops). */
    public val HUNDRED_XRP: XrpDrops = XrpDrops(100_000_000L)

    /** 1000 XRP expressed in drops (1,000,000,000 drops). */
    public val THOUSAND_XRP: XrpDrops = XrpDrops(1_000_000_000L)

    /** Minimum transaction fee in drops (10 drops). */
    public val MIN_FEE: XrpDrops = XrpDrops(10L)
}
