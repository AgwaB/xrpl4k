# xrpl-kotlin

A Kotlin Multiplatform SDK for the [XRP Ledger](https://xrpl.org/).

![CI](https://img.shields.io/badge/CI-passing-brightgreen)
![Maven Central](https://img.shields.io/badge/Maven_Central-1.0.0--SNAPSHOT-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF)
![License](https://img.shields.io/badge/License-Apache_2.0-green)

> **Status**: Under construction — not yet ready for production use.

## Features

- **Kotlin Multiplatform** — single codebase targets JVM, JS/Node.js, iOS, macOS, and Linux
- **Type-safe transactions** — DSL builders for Payment, OfferCreate, TrustSet, and more
- **Full transaction lifecycle** — `autofill` + `sign` + `submit` + `waitForValidation`
- **Binary codec** — canonical XRPL binary serialization and deserialization
- **Crypto** — Ed25519 and secp256k1 key generation, signing, and address encoding
- **WebSocket subscriptions** — Kotlin Flow-based ledger, transaction, and account streams
- **Structured error handling** — `XrplResult<T>` throughout; no unchecked exceptions by default

## Quickstart

Add the dependency:

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.xrpl:xrpl-client:1.0.0-SNAPSHOT")
}
```

Query an account and send a payment:

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
    val client = XrplClient { network = Network.Testnet }
    val wallet = Wallet.fromSeed("sYourSecretSeedHere")

    // Query account info
    val info = client.accountInfo(wallet.address).getOrThrow()
    println("Balance: ${info.balance.toXrp()} XRP")

    // Send a payment
    val tx = payment {
        account     = wallet.address
        destination = Address("rDestinationAddressHere")
        amount      = 10.xrp
    }
    val result = client.submitAndWait(tx, wallet).getOrThrow()
    println("Validated in ledger ${result.ledgerIndex}: ${result.engineResult}")

    client.close()
}
```

## Modules

| Module | Description |
|--------|-------------|
| `xrpl-core` | Types, models, transaction DSL builders, and `XrplResult<T>` |
| `xrpl-binary-codec` | Canonical XRPL binary serialization and deserialization |
| `xrpl-crypto` | Key generation (Ed25519 / secp256k1), signing, and address encoding |
| `xrpl-client` | HTTP JSON-RPC and WebSocket client with autofill and subscription support |
| `xrpl-bom` | Maven BOM for consistent version alignment across modules |

Use the BOM to align versions without repeating them:

```kotlin
dependencies {
    implementation(platform("org.xrpl:xrpl-bom:1.0.0-SNAPSHOT"))
    implementation("org.xrpl:xrpl-client")
    implementation("org.xrpl:xrpl-crypto")
}
```

## Supported Platforms

| Platform | Status |
|----------|--------|
| JVM 11+ | Phase 0 |
| JS (Browser + Node.js) | Planned |
| iOS (arm64, x64, simulatorArm64) | Planned |
| macOS (arm64, x64) | Planned |
| Linux (x64) | Planned |

## API Docs

API reference: _coming soon_

## Building

```bash
./gradlew jvmTest apiCheck ktlintCheck
```

## Contributing

Contributions are welcome. Please open an issue before submitting a pull request for non-trivial changes.

1. Fork the repository
2. Create a feature branch
3. Ensure `./gradlew jvmTest apiCheck ktlintCheck` passes
4. Open a pull request against `main`

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
