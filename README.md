# xrpl4k

A Kotlin Multiplatform SDK for the [XRP Ledger](https://xrpl.org/).

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.agwab/xrpl-core)](https://central.sonatype.com/namespace/io.github.agwab)
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
    implementation(platform("io.github.agwab:xrpl-bom:0.1.0"))

    implementation("io.github.agwab:xrpl-client")  // HTTP + WebSocket client
    implementation("io.github.agwab:xrpl-crypto")  // Key generation & signing
}
```

<details>
<summary>Gradle (Groovy)</summary>

```groovy
dependencies {
    implementation platform('io.github.agwab:xrpl-bom:0.1.0')
    implementation 'io.github.agwab:xrpl-client'
    implementation 'io.github.agwab:xrpl-crypto'
}
```
</details>

### Send XRP

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
