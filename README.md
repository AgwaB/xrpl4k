# xrpl4k

A Kotlin Multiplatform SDK for the [XRP Ledger](https://xrpl.org/).

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![API Docs](https://img.shields.io/badge/API_Docs-coming_soon-lightgrey)]()

> **Status**: Under active development — not yet published to Maven Central.

## Features

- **Kotlin Multiplatform** — single codebase targets JVM, JS/Node.js, iOS, macOS, and Linux
- **Type-safe transaction DSL** — builders for Payment, OfferCreate, TrustSet, EscrowCreate, NFTokenMint, and [20+ more](xrpl-core/src/commonMain/kotlin/org/xrpl/sdk/core/model/transaction/)
- **Full transaction lifecycle** — `autofill()` → `sign()` → `submit()` → `submitAndWait()`
- **Binary codec** — canonical XRPL binary serialization compatible with `rippled`
- **Crypto** — Ed25519 and secp256k1 key generation, signing, verification, and address derivation
- **29 RPC methods** — account, ledger, transaction, order book, path finding, NFT, and AMM queries
- **WebSocket subscriptions** — Kotlin `Flow`-based streams for ledger, transaction, and account events
- **Auto-pagination** — `Flow` helpers that walk marker-based cursors automatically
- **Multi-signing** — sign with multiple wallets and combine into a single multisig transaction
- **Client-side validation** — `TransactionValidator` catches errors locally before submission
- **Java interop** — `XrplClientJava` wraps every operation as `CompletableFuture` for Java callers
- **Structured error handling** — `XrplResult<T>` sealed type throughout; no unchecked exceptions

## Quickstart

### Installation

```kotlin
// build.gradle.kts
dependencies {
    // BOM for consistent version alignment
    implementation(platform("org.xrpl:xrpl-bom:1.0.0-SNAPSHOT"))

    // Pick the modules you need
    implementation("org.xrpl:xrpl-client")  // HTTP + WebSocket client
    implementation("org.xrpl:xrpl-crypto")  // Key generation & signing
}
```

<details>
<summary>Gradle (Groovy)</summary>

```groovy
dependencies {
    implementation platform('org.xrpl:xrpl-bom:1.0.0-SNAPSHOT')
    implementation 'org.xrpl:xrpl-client'
    implementation 'org.xrpl:xrpl-crypto'
}
```
</details>

### Send a Payment

```kotlin
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.Wallet

suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->
        val wallet = Wallet.fromSeed("sYourSecretSeedHere")

        // Build a payment with the type-safe DSL
        val tx = payment {
            account     = wallet.address
            destination = Address("rDestinationAddressHere")
            amount      = 10.xrp
        }

        // Autofill → sign → submit → wait for validation (one call)
        val result = client.submitAndWait(tx, wallet).getOrThrow()
        println("Validated in ledger ${result.ledgerIndex}: ${result.engineResult}")
    }
}
```

### Query an Account

```kotlin
XrplClient { network = Network.Mainnet }.use { client ->
    val address = Address("rN7n3473SaZBCG4dFL83w7p1W9cgZw6XRR")

    // Account info
    val info = client.accountInfo(address).getOrThrow()
    println("Sequence: ${info.sequence}, Balance: ${info.balance}")

    // XRP balance as a human-readable value
    val xrpBalance = client.getXrpBalance(address).getOrThrow()
    println("XRP: ${xrpBalance.toXrp()}")
}
```

### Subscribe to Ledger Events

```kotlin
XrplClient { network = Network.Testnet }.use { client ->
    client.subscribeToLedger()
        .take(5)
        .collect { ledger ->
            println("Ledger #${ledger.ledgerIndex} closed at ${ledger.ledgerTime}")
        }
}
```

### Java

```java
try (XrplClientJava client = XrplClientJava.create(config -> {
    config.setNetwork(Network.Testnet);
    return Unit.INSTANCE;
})) {
    XrplResult<AccountInfo> result = client.accountInfo(
        new Address("rN7n3473SaZBCG4dFL83w7p1W9cgZw6XRR")
    ).get();  // CompletableFuture → blocking get
}
```

## Modules

| Module | Description |
|--------|-------------|
| [`xrpl-core`](xrpl-core/) | Types, transaction DSL builders, validation, and `XrplResult<T>` |
| [`xrpl-binary-codec`](xrpl-binary-codec/) | Canonical XRPL binary serialization and deserialization |
| [`xrpl-crypto`](xrpl-crypto/) | Ed25519 / secp256k1 key generation, signing, and address encoding |
| [`xrpl-client`](xrpl-client/) | HTTP JSON-RPC client, WebSocket subscriptions, autofill, and sugar functions |
| [`xrpl-bom`](xrpl-bom/) | Maven BOM for consistent version alignment |

### Dependency Graph

```
xrpl-client
├── xrpl-core
├── xrpl-binary-codec
│   └── xrpl-core
└── xrpl-crypto
    └── xrpl-core
```

## Supported Platforms

| Platform | Target | Engine |
|----------|--------|--------|
| **JVM 11+** | `jvm` | OkHttp (HTTP), OkHttp (WebSocket) |
| JS / Node.js | `js` | Ktor JS engine |
| iOS | `iosArm64`, `iosSimulatorArm64` | Ktor Darwin engine |
| macOS | `macosArm64` | Ktor Darwin engine |
| Linux | `linuxX64` | Ktor CIO engine |

> JVM is the primary target with full test coverage. Other platforms compile and link but have limited integration testing.

## Examples

The [`samples/`](samples/) directory contains runnable examples covering every major SDK feature.

| Sample | Description |
|--------|-------------|
| [Client Setup](samples/ClientSetupSample.kt) | Network configuration, retry policies, WebSocket setup, timeouts |
| [Query Account](samples/QueryAccountSample.kt) | `accountInfo`, `accountLines`, `getXrpBalance`, `getBalances` |
| [Submit Payment](samples/SubmitPaymentSample.kt) | Payment DSL, `autofill`, `sign`, `submit`, `submitAndWait` |
| [Subscriptions](samples/SubscriptionSample.kt) | `subscribeToLedger`, `subscribeToTransactions`, `subscribeToAccount` |
| [NFT Queries](samples/NftQuerySample.kt) | `nftBuyOffers`, `nftSellOffers`, `nftInfo`, `nftHistory` |
| [NFT Transactions](samples/NftTransactionSample.kt) | Full NFT lifecycle: mint, create offer, accept offer, burn |
| [AMM](samples/AmmSample.kt) | `ammInfo` — query Automated Market Maker pools |
| [Order Book](samples/OrderBookSample.kt) | `bookOffers`, `bookChanges` — DEX order book and OHLCV data |
| [Path Finding](samples/PathFindSample.kt) | `ripplePathFind` — cross-currency payment path discovery |
| [Transaction Utils](samples/TransactionUtilsSample.kt) | `hashSignedTx`, `verifyTransaction`, `parseBalanceChanges`, `getNFTokenID` |
| [Multi-signing](samples/MultisignSample.kt) | `multiSignTransaction`, `combineSignatures`, `submitMultisigned` |
| [Pagination](samples/PaginationSample.kt) | `allAccountLines`, `accountTransactions` — auto-paginating `Flow` |
| [Validation](samples/ValidationSample.kt) | `TransactionValidator` — client-side validation before submission |
| [Java Interop](samples/JavaInteropSample.java) | `XrplClientJava` — `CompletableFuture`-based API for Java callers |

See [`samples/README.md`](samples/README.md) for details on each example.

## RPC Methods

<details>
<summary>29 methods across 7 categories (click to expand)</summary>

**Account** — `accountInfo`, `accountLines`, `accountCurrencies`, `accountNfts`, `accountObjects`, `accountOffers`, `accountTransactions`, `gatewayBalances`

**Ledger** — `ledger`, `ledgerCurrent`, `ledgerData`, `ledgerEntry`

**Transaction** — `submit`, `submitMultisigned`, `tx`, `transactionEntry`

**Server** — `serverInfo`, `serverState`, `fee`, `manifest`

**Order Book** — `bookOffers`, `bookChanges`

**Path** — `ripplePathFind`

**NFT** — `nftBuyOffers`, `nftSellOffers`, `nftInfo`, `nftHistory`

**AMM** — `ammInfo`

</details>

## Building

```bash
# Run tests, check API compatibility, and lint
./gradlew jvmTest apiCheck ktlintCheck

# Full build (all platforms)
./gradlew build

# Auto-fix lint issues
./gradlew ktlintFormat
```

## Project Structure

```
xrpl4k/
├── xrpl-core/              # Types, models, transaction DSL, validation
├── xrpl-binary-codec/      # Binary serialization (rippled-compatible)
├── xrpl-crypto/            # Key generation, signing, address encoding
├── xrpl-client/            # RPC client, WebSocket, autofill, sugar
├── xrpl-bom/               # Maven BOM
├── xrpl-test-fixtures/     # Shared test utilities
├── samples/                # Runnable examples
├── build-logic/            # Gradle convention plugins
└── docs/                   # Design documents and conventions
```

## Contributing

Contributions are welcome. Please open an issue before submitting a pull request for non-trivial changes.

1. Fork the repository
2. Create a feature branch
3. Ensure `./gradlew jvmTest apiCheck ktlintCheck` passes
4. Open a pull request against `main`

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## Related Projects

- [xrpl4j](https://github.com/XRPLF/xrpl4j) — Official Java SDK for the XRP Ledger
- [xrpl.js](https://github.com/XRPLF/xrpl.js) — Official JavaScript/TypeScript SDK for the XRP Ledger
- [xrpl-py](https://github.com/XRPLF/xrpl-py) — Official Python SDK for the XRP Ledger

## License

MIT License — see [LICENSE](LICENSE) for details.
