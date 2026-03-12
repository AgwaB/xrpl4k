# XRPL Kotlin SDK — Development Plan

> Last updated: 2026-03-12
> This is the master development plan. Each phase has clear deliverables and exit criteria.

---

## Reference Documents

| Doc | Path | Use When |
|-----|------|----------|
| **Feature Analysis** | [`docs/01-xrpl-feature-analysis.md`](docs/01-xrpl-feature-analysis.md) | Checking which RPC methods, transaction types, ledger objects to implement |
| **SDK Development Guide** | [`docs/02-sdk-development-guide.md`](docs/02-sdk-development-guide.md) | Looking up design patterns (DSL, Flow, sealed class, KMP expect/actual) |
| **Project Conventions** | [`docs/03-project-conventions.md`](docs/03-project-conventions.md) | Writing any code — naming, formatting, type design, error handling, testing |
| **Development Lessons** | [`docs/04-sdk-development-lessons.md`](docs/04-sdk-development-lessons.md) | Before making architectural decisions — anti-patterns, Kotlin pitfalls, DX |

---

## Phase 0: Project Scaffolding

**Goal**: Empty but buildable KMP project with CI.

### TODO

- [ ] **P0-1** Initialize Gradle KMP project with module structure
  - `xrpl-core` — types, models, utilities (stdlib + kotlinx-serialization only)
  - `xrpl-binary-codec` — binary serialization/deserialization
  - `xrpl-crypto` — key generation, signing, address encoding
  - `xrpl-client` — HTTP + WebSocket client (ktor-client, kotlinx-coroutines)
  - `xrpl-bom` — Maven BOM (version alignment, no code)
  - `xrpl-test-fixtures` — MockClient, golden files, test helpers
  - **Ref**: [Conventions §2 — Module Structure](docs/03-project-conventions.md#2-package--module-structure)
- [ ] **P0-2** Set up `gradle/libs.versions.toml` with all dependency versions
  - **Ref**: [Conventions §15.2 — Version Catalog](docs/03-project-conventions.md#152-version-catalog)
- [ ] **P0-3** Set up `build-logic/` composite build with convention plugins
  - `KmpLibraryPlugin` — shared KMP target config (JVM, JS, iOS, Native)
  - `PublishingPlugin` — vanniktech maven-publish config
  - `LintPlugin` — ktlint + explicit API mode + BCV
  - **Ref**: [Lessons §4.3 — Gradle Build Optimization](docs/04-sdk-development-lessons.md#43-gradle-build-optimization)
- [ ] **P0-4** Enable quality gates from day one
  - `kotlin { explicitApi() }` — forces explicit visibility + return types
  - Binary Compatibility Validator — `.api` file tracking
  - ktlint with trailing comma + 120 char line length
  - **Ref**: [Conventions §7 — Public API Rules](docs/03-project-conventions.md#7-public-api-rules)
- [ ] **P0-5** Set up GitHub Actions CI
  - PR: `./gradlew check apiCheck ktlintCheck` on `ubuntu-latest`
  - iOS tests on `macos-latest` (label-triggered)
  - Cache: `gradle/actions/setup-gradle@v4` with encryption key
  - **Ref**: [Lessons §4.2 — CI/CD Pipeline](docs/04-sdk-development-lessons.md#42-cicd-pipeline)
- [ ] **P0-6** Create `.editorconfig`, `LICENSE` (Apache 2.0), root `README.md` (placeholder)
- [ ] **P0-7** Create `@XrplDsl` annotation, `@ExperimentalXrplApi` annotation
  - **Ref**: [Conventions §8.1 — @DslMarker Required](docs/03-project-conventions.md#81-dslmarker-required)

### Exit Criteria
- `./gradlew check` passes on empty project
- `./gradlew apiCheck` passes
- `./gradlew ktlintCheck` passes
- CI green on GitHub Actions

---

## Phase 1: Core Types & Binary Codec

**Goal**: All domain types, value classes, transaction models, and binary codec — no network needed.

### 1A: Value Classes & Base Types

- [ ] **P1A-1** Domain value classes in `xrpl-core`
  - `Address`, `XAddress`, `TxHash`, `Hash256`, `LedgerIndex`, `Seed`, `PublicKey`
  - `XrpDrops` with arithmetic operators
  - `CurrencyCode`, `IssuedAmount`, `MptAmount`
  - **Ref**: [Conventions §6.2 — Value Class](docs/03-project-conventions.md#62-value-class--domain-primitives), [Feature Analysis §1.5 — Cryptography](docs/01-xrpl-feature-analysis.md)
- [ ] **P1A-2** Amount type hierarchy
  - `CurrencyAmount` sealed interface → `XrpAmount`, `IssuedAmount`, `MptAmount`
  - Literal extensions: `10.xrp`, `12L.drops`
  - **Ref**: [Conventions §10.3 — Amount Literals](docs/03-project-conventions.md#103-amount-literals)
- [ ] **P1A-3** `XrplResult<T>` sealed class hierarchy
  - `Success`, `Failure.RpcError`, `Failure.NetworkError`, `Failure.ValidationError`, `Failure.TecError`, `Failure.NotFound`
  - Convenience extensions: `getOrNull()`, `getOrThrow()`, `map()`, `onSuccess()`, `onFailure()`
  - **Ref**: [Conventions §12 — Error Handling](docs/03-project-conventions.md#12-error-handling)
- [ ] **P1A-4** `Network` sealed class
  - `Mainnet`, `Testnet`, `Devnet`, `Custom(rpc, ws)`
  - **Ref**: [SDK Guide §7.3 — Network Abstraction](docs/02-sdk-development-guide.md)

### 1B: Transaction Model

- [ ] **P1B-1** `TransactionType` — sealed class or value class + companion constants for all 68+ types
  - Include all types from Feature Analysis: Payment through Batch/Delegate/Vault/Loan
  - Unknown types must not crash (forward compatibility)
  - **Ref**: [Feature Analysis §1.3 — Transaction Types](docs/01-xrpl-feature-analysis.md), [Conventions §6.6 — Enum](docs/03-project-conventions.md#66-enum--closed-constant-sets)
- [ ] **P1B-2** Transaction lifecycle sealed interface
  - `XrplTransaction { Unsigned, Filled, Signed }`
  - Type system enforces: `Unsigned → (autofill) → Filled → (sign) → Signed → (submit)`
  - **Ref**: [Conventions §6.3 — Sealed Class](docs/03-project-conventions.md#63-sealed-class--state-machines--sum-types), [SDK Guide §7.1 — Transaction Lifecycle](docs/02-sdk-development-guide.md)
- [ ] **P1B-3** Common transaction fields
  - `Account`, `Fee`, `Sequence`, `LastLedgerSequence`, `Memos`, `Signers`, `SourceTag`, `TicketSequence`, `NetworkID`
- [ ] **P1B-4** Transaction-specific models — Phase 1 priority types
  - `Payment`, `OfferCreate`, `OfferCancel`, `TrustSet`, `AccountSet`, `AccountDelete`
  - `SetRegularKey`, `SignerListSet`
  - Each with DSL builder (`payment { ... }`, `trustSet { ... }`)
  - **Ref**: [Conventions §8.2 — Builder Pattern](docs/03-project-conventions.md#82-builder-pattern)
- [ ] **P1B-5** Transaction-specific models — Phase 2 types
  - `EscrowCreate/Finish/Cancel`, `PaymentChannelCreate/Fund/Claim`, `CheckCreate/Cash/Cancel`
  - `NFTokenMint/Burn/CreateOffer/CancelOffer/AcceptOffer`
  - `DepositPreauth`, `Clawback`
- [ ] **P1B-6** Transaction-specific models — Phase 3 types
  - `AMMCreate/Deposit/Withdraw/Vote/Bid/Delete/Clawback`
  - `DIDSet/Delete`, `OracleSet/Delete`
  - `MPTokenIssuanceCreate/Set/Destroy`, `MPTokenAuthorize`
  - `CredentialCreate/Accept/Delete`
  - `VaultCreate/Set/Delete/Deposit/Withdraw/Clawback`
  - `LoanCreate/Set/Delete/Payment/Foreclosure`
  - `BatchSubmit`, `DelegateSet`
  - `XChainBridge*` (8 types)

### 1C: Ledger Objects

- [ ] **P1C-1** Ledger object sealed class hierarchy — 30 types
  - Priority: `AccountRoot`, `RippleState`, `Offer`, `DirectoryNode`, `Amendments`, `FeeSettings`
  - Then: `NFTokenPage`, `AMM`, `Check`, `Escrow`, `PayChannel`, `SignerList`
  - Then: remaining objects
  - **Ref**: [Feature Analysis §1.4 — Ledger Object Types](docs/01-xrpl-feature-analysis.md)

### 1D: Binary Codec

- [ ] **P1D-1** Field definitions and type codes from `definitions.json`
- [ ] **P1D-2** Binary serializer — 19 primitive types
  - `UInt16`, `UInt32`, `UInt64`, `Hash128`, `Hash160`, `Hash256`, `Amount`, `Blob`, `AccountID`, `STObject`, `STArray`, `PathSet`, `Vector256`, `Issue`, `XChainBridge`, `Currency`
  - **Ref**: [Feature Analysis §1.6 — Binary Codec](docs/01-xrpl-feature-analysis.md)
- [ ] **P1D-3** Transaction binary encoding/decoding
- [ ] **P1D-4** Signing data serialization (HashPrefix)
- [ ] **P1D-5** Golden file tests — verify encode/decode against known vectors
  - **Ref**: [Conventions §13.6 — Golden File Testing](docs/03-project-conventions.md#136-golden-file-testing)
- [ ] **P1D-6** Property-based tests — encode/decode roundtrip identity
  - **Ref**: [Conventions §13.5 — Property-Based Testing](docs/03-project-conventions.md#135-property-based-testing)

### Exit Criteria
- All value classes with init validation
- Transaction sealed hierarchy compiles with exhaustive `when`
- Binary codec encode/decode roundtrip passes for all 19 types
- Golden file tests pass against xrpl.js/xrpl4j reference vectors
- `apiCheck` passes — public API surface locked

---

## Phase 2: Cryptography & Wallet

**Goal**: Key generation, address derivation, transaction signing.

- [ ] **P2-1** `CryptoProvider` interface + `expect fun platformCryptoProvider()`
  - SHA-256, SHA-512Half, RIPEMD-160
  - JVM: `java.security.MessageDigest` + BouncyCastle
  - Native: cryptography-kotlin or platform crypto
  - **Ref**: [Conventions §17.3 — expect/actual Pattern](docs/03-project-conventions.md#173-expectactual-pattern)
- [ ] **P2-2** Key derivation
  - Ed25519 key pairs from seed
  - secp256k1 key pairs from seed
  - Seed generation from entropy
  - **Ref**: [Feature Analysis §1.5.1 — Key Derivation](docs/01-xrpl-feature-analysis.md)
- [ ] **P2-3** Address codec
  - Classic address encoding/decoding (Base58Check with XRP alphabet)
  - X-address encoding/decoding (mainnet/testnet flag + destination tag)
  - **Ref**: [Feature Analysis §1.5.3 — Address Encoding](docs/01-xrpl-feature-analysis.md)
- [ ] **P2-4** `Wallet` class
  - `Wallet.generate(algorithm = Ed25519)` — new random wallet
  - `Wallet.fromSeed(seed)` — restore from seed
  - `Wallet.fromEntropy(entropy)` — restore from entropy
  - `AutoCloseable` with key zeroing on `close()`
  - **Ref**: [Conventions §18.1 — Secret Data Handling](docs/03-project-conventions.md#181-secret-data-handling)
- [ ] **P2-5** `TransactionSigner` interface + `InMemorySigner`
  - `sign(key, Filled) → Signed`
  - `multiSign(key, Filled) → SingleSignature`
  - **Ref**: [SDK Guide §7.2 — Key Management / HSM Abstraction](docs/02-sdk-development-guide.md)
- [ ] **P2-6** Signing integration tests
  - Sign known transactions, verify against xrpl.js reference outputs
  - Ed25519 + secp256k1 both tested
  - Multi-sign test with sorted Signers array

### Exit Criteria
- `Wallet.generate()` produces valid addresses
- `Wallet.fromSeed()` reproduces known addresses
- Transaction signing matches xrpl.js/xrpl4j reference outputs
- Ed25519 and secp256k1 both work
- Multi-signing works with correct Signer ordering

---

## Phase 3: Client & Network

**Goal**: Working HTTP + WebSocket client with RPC methods.

### 3A: Transport Layer

- [ ] **P3A-1** `HttpTransport` — JSON-RPC over HTTP via Ktor
  - Request/response serialization with `XrplJson`
  - Error mapping: HTTP errors → `XrplResult.Failure`
  - **Ref**: [Conventions §11 — Serialization](docs/03-project-conventions.md#11-serialization)
- [ ] **P3A-2** `WebSocketTransport` — persistent connection via Ktor WebSockets
  - Connection state as `StateFlow<ConnectionState>`
  - Auto-reconnect with exponential backoff
  - Message routing (request/response matching + subscription events)
- [ ] **P3A-3** Engine abstraction — `HttpClientEngine` injected via constructor
  - `MockEngine` for testing without network
  - **Ref**: [SDK Guide §2.1 — Ktor Engine Abstraction](docs/02-sdk-development-guide.md)

### 3B: XrplClient

- [ ] **P3B-1** `XrplClient` with config DSL
  - `XrplClient { network = Network.Testnet; timeout = 60.seconds }`
  - Transport selection: HTTP-only, WebSocket-only, or Both
  - **Ref**: [Conventions §8.4 — Config DSL](docs/03-project-conventions.md#84-config-dsl)
- [ ] **P3B-2** RPC methods — Phase 1 (29 methods)
  - Account (10): `accountInfo`, `accountLines`, `accountObjects`, `accountOffers`, `accountTx`, `accountChannels`, `accountCurrencies`, `accountNfts`, `gatewayBalances`, `norippleCheck`
  - Ledger (5): `ledger`, `ledgerClosed`, `ledgerCurrent`, `ledgerData`, `ledgerEntry`
  - Transaction (5): `submit`, `submitMultisigned`, `tx`, `transactionEntry`, `simulate`
  - Server (7): `serverInfo`, `serverState`, `serverDefinitions`, `fee`, `manifest`, `feature`, `version`
  - Utility (2): `ping`, `random`
  - **Ref**: [Feature Analysis §1.1 — Client API Methods](docs/01-xrpl-feature-analysis.md)
- [ ] **P3B-3** Request/response models for all Phase 1 RPC methods
  - Internal `@Serializable` models → mapped to public domain models
  - **Ref**: [Conventions §11.2 — Serializable Rules](docs/03-project-conventions.md#112-serializable-rules)
- [ ] **P3B-4** Auto-pagination via Flow
  - `cursorFlow()` helper for marker-based pagination
  - `accountTransactions()`, `accountObjects()`, `accountLines()` return `Flow<T>`
  - `take(N)` for cooperative cancellation
  - **Ref**: [SDK Guide §4.6 — Pagination → Flow](docs/02-sdk-development-guide.md)
- [ ] **P3B-5** Retry middleware
  - Configurable max attempts, initial delay, max delay
  - Exponential backoff with jitter
  - Retries only for idempotent operations + transient errors
  - **Ref**: [Lessons §1.2 — No Retry Logic](docs/04-sdk-development-lessons.md#no-retry-logic)

### 3C: Sugar Functions

- [ ] **P3C-1** `autofill(tx: Unsigned) → Filled`
  - Auto-fill: `Fee` (from `fee` RPC), `Sequence` (from `account_info`), `LastLedgerSequence` (current + 20)
  - Fee cushion and max fee cap from config
  - **Ref**: [Feature Analysis §2.5 — Sugar/Helper Functions](docs/01-xrpl-feature-analysis.md)
- [ ] **P3C-2** `submitAndWait(tx, wallet) → XrplResult<ValidatedTransaction>`
  - Autofill → sign → submit → poll for validation
  - Timeout with configurable duration
  - **Ref**: [SDK Guide §9 — Target DX](docs/02-sdk-development-guide.md)
- [ ] **P3C-3** Convenience methods
  - `getXrpBalance(address)` — simple XRP balance query
  - `getBalances(address)` — all balances (XRP + tokens)
  - `fundWallet(wallet?)` — testnet/devnet faucet

### 3D: Subscriptions (WebSocket)

- [ ] **P3D-1** `subscribe` / `unsubscribe` RPC methods
- [ ] **P3D-2** Stream types as Flow
  - `subscribeToLedger(): Flow<LedgerEvent>`
  - `subscribeToTransactions(): Flow<TransactionEvent>`
  - `subscribeToAccount(address): Flow<AccountEvent>`
  - `subscribeToOrderBook(pair): Flow<OrderBookEvent>`
  - **Ref**: [SDK Guide §4.5 — Flow Streaming](docs/02-sdk-development-guide.md), [Conventions §9.5 — Flow Rules](docs/03-project-conventions.md#95-flow-rules)

### Exit Criteria
- All 29 Phase 1 RPC methods work against Testnet
- `submitAndWait` successfully sends Payment on Testnet
- Auto-pagination fetches all account transactions
- WebSocket subscription receives ledger close events
- `MockEngine` tests pass without network
- Retry middleware handles transient failures

---

## Phase 4: Testing & Validation

**Goal**: Comprehensive test suite, cross-platform validation.

- [ ] **P4-1** Integration test suite against Testnet
  - Full lifecycle: create wallet → fund → send payment → verify
  - Tag with `@Tag("integration")`, run with `-Pintegration`
  - **Ref**: [Conventions §13.4 — Test Categories](docs/03-project-conventions.md#134-test-categories)
- [ ] **P4-2** Cross-SDK compatibility tests
  - Sign transactions, verify output matches xrpl.js and xrpl4j
  - Decode transactions from xrpl.js, verify parsing
  - **Ref**: [Feature Analysis §3 — xrpl4j Comparison](docs/01-xrpl-feature-analysis.md)
- [ ] **P4-3** MockEngine test suite
  - Every RPC method tested with fixture-based responses
  - Error scenarios: RPC error, network timeout, invalid response
- [ ] **P4-4** Platform-specific tests
  - JVM: full test suite
  - JS: basic compilation + core type tests
  - iOS: basic compilation + core type tests (simulator)
- [ ] **P4-5** `xrpl-test-fixtures` module
  - `MockXrplClient` for consumers
  - JSON fixture files for common responses
  - Golden file set for binary codec

### Exit Criteria
- Unit tests: >80% line coverage on `xrpl-core`
- Integration tests: full payment lifecycle on Testnet
- Cross-SDK: binary codec output matches xrpl.js
- All platforms compile and pass core tests

---

## Phase 5: Documentation & Release

**Goal**: Publishable 1.0.0-alpha01 on Maven Central.

- [ ] **P5-1** README with quickstart (< 20 lines, < 5 minutes)
- [ ] **P5-2** Dokka setup — auto-generated API reference
  - **Ref**: [Conventions §14 — Documentation](docs/03-project-conventions.md#14-documentation)
- [ ] **P5-3** KDoc on all public API (classes, functions, properties)
  - **Ref**: [Conventions §14.1 — KDoc Required Targets](docs/03-project-conventions.md#141-kdoc-required-targets)
- [ ] **P5-4** `samples/` directory with compilable examples
  - `ClientSetupSample.kt`, `SubmitPaymentSample.kt`, `SubscriptionSample.kt`
- [ ] **P5-5** Error code reference document
- [ ] **P5-6** Maven Central publishing setup
  - GPG key generation and CI secrets
  - vanniktech plugin configuration
  - BOM module publishing
  - **Ref**: [Lessons §4.1 — Maven Central Publishing](docs/04-sdk-development-lessons.md#41-maven-central-publishing-current--central-portal)
- [ ] **P5-7** Release workflow
  - Tag-triggered: `v*` → build → sign → publish → GitHub Release
  - CHANGELOG.md auto-generation from Conventional Commits
  - **Ref**: [Conventions §16 — Git & Commits](docs/03-project-conventions.md#16-git--commits)
- [ ] **P5-8** ProGuard consumer rules for Android
  - Ship `consumer-rules.pro` with serialization + coroutine keep rules
  - **Ref**: [Lessons §2.4 — kotlinx.serialization Edge Cases](docs/04-sdk-development-lessons.md#24-kotlinxserialization-edge-cases)

### Exit Criteria
- `1.0.0-alpha01` published on Maven Central
- README quickstart runs in under 5 minutes
- Dokka API docs generated and hosted
- All `.api` files committed
- CI green on all platforms

---

## Phase 6: Expanded Features (Post-Alpha)

**Goal**: Full feature parity with xrpl.js + xrpl4j advantages.

- [ ] **P6-1** Remaining RPC methods (16+)
  - Path/OrderBook: `bookOffers`, `bookChanges`, `pathFind`, `ripplePathFind`
  - NFT: `nftBuyOffers`, `nftSellOffers`
  - AMM: `ammInfo`
  - Clio-specific: `nftHistory`, `nftInfo`
- [ ] **P6-2** All remaining transaction types (40+)
  - See P1B-5 and P1B-6 above
- [ ] **P6-3** `HsmSigner` — HSM/KMS integration
  - `TransactionSigner<PrivateKeyReference>` for AWS KMS, Azure Key Vault, etc.
  - **Ref**: [SDK Guide §7.2 — Key Management](docs/02-sdk-development-guide.md)
- [ ] **P6-4** Java interop layer (if needed)
  - `@JvmStatic`, `@JvmOverloads`, `@Throws`
  - `CompletableFuture` wrappers for suspend functions
  - **Ref**: [Lessons §2.3 — Java Interop](docs/04-sdk-development-lessons.md#23-java-interop)
- [ ] **P6-5** Advanced utilities
  - `parseBalanceChanges(metadata)` — balance change extraction from tx metadata
  - `getNFTokenID(metadata)` — NFT ID extraction from mint metadata
  - `hashSignedTx(txBlob)` — compute tx hash from signed blob
  - `verifySignature(tx)` — verify transaction signature
  - **Ref**: [Feature Analysis §2.5.3 — Utility Functions](docs/01-xrpl-feature-analysis.md)
- [ ] **P6-6** Client-side transaction validation
  - Validate transaction fields before submission
  - Check amount limits, address formats, required fields
  - **Ref**: [Feature Analysis §2.7 — Validation](docs/01-xrpl-feature-analysis.md)

---

## Priority Summary

```
Phase 0 ──→ Phase 1A ──→ Phase 1B ──→ Phase 1C ──→ Phase 1D
  (scaffold)  (types)     (tx model)   (ledger obj) (codec)
                                            │
Phase 2 ←──────────────────────────────────┘
  (crypto & wallet)
       │
Phase 3A ──→ Phase 3B ──→ Phase 3C ──→ Phase 3D
  (transport)  (client)    (sugar)      (subscriptions)
                                            │
Phase 4 ←──────────────────────────────────┘
  (testing & validation)
       │
Phase 5 ──→ Phase 6
  (docs & release)  (expanded features)
```

**Critical path**: P0 → P1A → P1B → P1D → P2 → P3A → P3B → P3C → P5

---

## Decision Log

| # | Decision | Rationale | Ref |
|---|----------|-----------|-----|
| D1 | KMP from day one (JVM P0, JS/iOS P1) | Maximize reach; retrofitting KMP is painful | [Guide §Key Decision 4](docs/02-sdk-development-guide.md) |
| D2 | Ktor Client for HTTP + WebSocket | KMP-compatible, MockEngine for testing | [Guide §Key Decision 6](docs/02-sdk-development-guide.md) |
| D3 | kotlinx.serialization only (no Gson/Jackson) | Only KMP-compatible JSON library | [Guide §Key Decision 5](docs/02-sdk-development-guide.md) |
| D4 | Sealed class for transaction lifecycle | Compile-time enforcement of Unsigned→Filled→Signed | [Guide §Key Decision 3](docs/02-sdk-development-guide.md) |
| D5 | No public `data class` | Binary compatibility protection | [Conventions §6.4](docs/03-project-conventions.md#64-regular-class--public-api-models) |
| D6 | Sealed result instead of exceptions | Expected failures are data, not exceptions | [Conventions §12.1](docs/03-project-conventions.md#121-error-strategy-sealed-result) |
| D7 | HTTP + WebSocket dual transport | xrpl4j is HTTP-only, xrpl.js is WS-only; we do both | [Guide §Key Decision 6](docs/02-sdk-development-guide.md) |
| D8 | `TransactionType` as value class + constants (not enum) | Protocol adds new types via amendments; enum is closed | [Conventions §6.6](docs/03-project-conventions.md#66-enum--closed-constant-sets) |
| D9 | `expect fun` + interface over `expect class` | Testable, flexible, no Beta opt-in | [Conventions §17.3](docs/03-project-conventions.md#173-expectactual-pattern) |
| D10 | Explicit API mode + BCV from day one | Prevents accidental API leaks | [Lessons §2.5](docs/04-sdk-development-lessons.md#25-binary-compatibility-traps) |
