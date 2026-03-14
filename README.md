# xrpl4k

A Kotlin Multiplatform SDK for the [XRP Ledger](https://xrpl.org/).

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.agwab/xrpl4k-core)](https://central.sonatype.com/namespace/io.github.agwab)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## Requirements

- **JVM 11+** (primary target with full test coverage)
- **Kotlin 2.1+**
- Gradle 8.x

Other supported platforms: JS/Node.js, iOS, macOS, Linux (see [Supported Platforms](docs/PLATFORMS.md))

## Quickstart

### Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation(platform("io.github.agwab:xrpl4k-bom:0.1.0"))

    implementation("io.github.agwab:xrpl4k-client")  // HTTP + WebSocket client
    implementation("io.github.agwab:xrpl4k-crypto")  // Key generation & signing
}
```

<details>
<summary>Gradle (Groovy)</summary>

```groovy
dependencies {
    implementation platform('io.github.agwab:xrpl4k-bom:0.1.0')
    implementation 'io.github.agwab:xrpl4k-client'
    implementation 'io.github.agwab:xrpl4k-crypto'
}
```
</details>

### Quick Start

Connect to a network, generate a wallet, and send XRP in a few lines:

```kotlin
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.sugar.autofill
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.Wallet

suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->
        // Generate a new wallet (or restore with Wallet.fromSeed("s..."))
        val (wallet, seed) = Wallet.generate()
        println("Address: ${wallet.address}  Seed: $seed")

        val tx = payment {
            account     = wallet.address
            destination = Address("rDestinationAddressHere")
            amount      = 10.xrp
        }

        val result = client.submitAndWait(tx, wallet).getOrThrow()
        println("Validated in ledger ${result.ledgerIndex}: ${result.engineResult}")
    }
}
```

### Transaction Flags

Use `TransactionFlags` constants to set bitmask flags on transactions. Combine multiple flags with `or`:

```kotlin
import org.xrpl.sdk.core.model.transaction.TransactionFlags
import org.xrpl.sdk.core.model.transaction.payment

val tx = payment {
    account     = wallet.address
    destination = Address("rDest...")
    amount      = 25.xrp
    flags       = TransactionFlags.Payment.tfPartialPayment or
                  TransactionFlags.Payment.tfLimitQuality
}
```

### Multi-sig

When submitting on behalf of a multi-signed account, pass `multisigSigners` to `autofill`
so the fee is correctly multiplied by `(1 + signerCount)`:

```kotlin
import org.xrpl.sdk.client.sugar.autofill

val unsigned = payment {
    account     = multisigAddress
    destination = Address("rDest...")
    amount      = 5.xrp
}

// 3 signers -> fee = baseFee * 4
val filled = client.autofill(unsigned, multisigSigners = 3).getOrThrow()
```

### Tickets

Use a pre-created ticket instead of the account's next sequence number:

```kotlin
import org.xrpl.sdk.client.sugar.autofill

val filled = client.autofill(unsigned, ticketSequence = 42u).getOrThrow()
// filled.sequence == 0, filled.ticketSequence == 42
```

### Subscriptions

Subscribe to real-time events via WebSocket using Kotlin `Flow`:

```kotlin
import org.xrpl.sdk.client.subscription.subscribeToLedger
import org.xrpl.sdk.client.subscription.subscribeToAccount

// Stream ledger close events
client.subscribeToLedger().collect { event ->
    println("Ledger ${event.ledgerIndex} closed with ${event.txnCount} txns")
}

// Stream events affecting a specific account
client.subscribeToAccount(Address("rAccount...")).collect { event ->
    println("Tx ${event.hash}: ${event.engineResult}")
}
```

### Typed Ledger Objects

Query account-owned objects with type-safe filters and get parsed results:

```kotlin
import org.xrpl.sdk.client.rpc.accountObjects
import org.xrpl.sdk.client.rpc.AccountObjectType
import org.xrpl.sdk.client.model.TicketObject

val result = client.accountObjects(
    account = wallet.address,
    type = AccountObjectType.Ticket,
).getOrThrow()

// Objects are parsed into typed subtypes of LedgerObject
result.objects.filterIsInstance<TicketObject>().forEach { ticket ->
    println("Ticket #${ticket.ticketSequence}")
}
```

### CurrencySpec

Use `CurrencySpec` for type-safe order book queries instead of raw JSON:

```kotlin
import org.xrpl.sdk.core.model.amount.CurrencySpec
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.client.rpc.bookOffers

val usd = CurrencySpec.Issued(CurrencyCode("USD"), Address("rIssuer..."))

val offers = client.bookOffers(
    takerGets = usd,
    takerPays = CurrencySpec.Xrp,
    limit = 10,
).getOrThrow()

offers.offers.forEach { offer ->
    println("${offer.account}: ${offer.quality}")
}
```

## Examples

| Example | Description |
|---------|-------------|
| [Send XRP](samples/SendXrpSample.kt) | Simplest XRP transfer — check balance, send, confirm |
| [Trust Lines & IOU](samples/TrustLineAndIouSample.kt) | `trustSet` + issued currency payments |
| [DEX Trading](samples/DexTradingSample.kt) | Place and cancel orders on the built-in DEX |
| [Subscriptions](samples/SubscriptionSample.kt) | WebSocket `Flow`-based ledger/transaction streams |
| [NFT Lifecycle](samples/NftTransactionSample.kt) | Mint, sell, buy, burn NFTs |

See [`samples/`](samples/) for 19 runnable examples covering every major SDK feature.

## Contributing

Contributions are welcome. Please open an issue before submitting a pull request for non-trivial changes.

1. Fork the repository
2. Create a feature branch
3. Ensure `./gradlew jvmTest apiCheck ktlintCheck` passes
4. Open a pull request against `main`

## License

MIT License — see [LICENSE](LICENSE) for details.
