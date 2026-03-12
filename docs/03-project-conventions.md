# XRPL Kotlin SDK — Project Conventions

> Last updated: 2026-03-12
> This document defines the **code conventions** for the project. All code must follow these rules.
> If an exception is needed, the rationale must be explicitly stated in the PR.

---

## Table of Contents

1. [Core Principles](#1-core-principles)
2. [Package & Module Structure](#2-package--module-structure)
3. [Naming](#3-naming)
4. [File Structure](#4-file-structure)
5. [Formatting](#5-formatting)
6. [Type Design](#6-type-design)
7. [Public API Rules](#7-public-api-rules)
8. [DSL & Builders](#8-dsl--builders)
9. [Coroutines](#9-coroutines)
10. [Extension Functions](#10-extension-functions)
11. [Serialization](#11-serialization)
12. [Error Handling](#12-error-handling)
13. [Testing](#13-testing)
14. [Documentation](#14-documentation)
15. [Dependency Management](#15-dependency-management)
16. [Git & Commits](#16-git--commits)
17. [KMP (Kotlin Multiplatform)](#17-kmp-kotlin-multiplatform)
18. [Security](#18-security)

---

## 1. Core Principles

| # | Principle | Description |
|---|-----------|-------------|
| P1 | **Idiomatic Kotlin** | Use Kotlin idioms, not Java patterns. Nullable instead of `Optional`, `Sequence`/`Flow` instead of `Stream`. |
| P2 | **Immutable by Default** | `val` > `var`, `List` > `MutableList`. Isolate mutable state internally when needed. |
| P3 | **Explicit over Implicit** | Declare return types, visibility, and side effects explicitly in public API. Avoid "magic". |
| P4 | **Compile-Time Safety** | Prefer type system enforcement over runtime validation. Sealed classes, value classes, generic constraints. |
| P5 | **Minimal Surface** | Don't expose what isn't needed. `internal` is the default; `public` is a deliberate decision. |
| P6 | **Zero-Surprise** | Function names must fully describe behavior. Reflect side effects in the name. |
| P7 | **Testable by Design** | Inject dependencies via constructors; hide them behind interfaces. Ship test doubles with the library. |

---

## 2. Package & Module Structure

### 2.1 Base Package

```
org.xrpl.sdk
```

### 2.2 Module Structure

```
xrpl-kotlin/
├── xrpl-core/              # Core types, models, utilities (minimal dependencies)
│   └── org.xrpl.sdk.core
│       ├── model/           # Transaction, LedgerObject, Amount, etc.
│       │   ├── transaction/ # Transaction types
│       │   ├── ledger/      # Ledger object types
│       │   └── amount/      # XrpAmount, IssuedAmount, MPTAmount
│       ├── type/            # Value class wrappers (Address, TxHash, etc.)
│       ├── result/          # XrplResult sealed hierarchy
│       └── util/            # Shared utilities
│
├── xrpl-binary-codec/       # Binary serialization/deserialization
│   └── org.xrpl.sdk.codec
│
├── xrpl-crypto/             # Key generation, signing, address encoding
│   └── org.xrpl.sdk.crypto
│       ├── signer/          # TransactionSigner interface + implementations
│       └── wallet/          # Wallet creation, restoration
│
├── xrpl-client/             # Network client (HTTP + WebSocket)
│   └── org.xrpl.sdk.client
│       ├── request/         # RPC request models
│       ├── response/        # RPC response models
│       └── transport/       # HTTP/WebSocket transports
│
├── xrpl-bom/                # Maven BOM (unified version for all modules)
└── xrpl-test-fixtures/      # Test doubles, MockClient, golden files
    └── org.xrpl.sdk.test
```

### 2.3 Package Rules

| Rule | Description |
|------|-------------|
| **One package = one concern** | Don't merge `transaction/`, `ledger/`, `amount/` into a single `model/` (co-locating in files is fine). |
| **No circular dependencies** | Only allow `xrpl-core` ← `xrpl-crypto` ← `xrpl-client` direction. If reverse reference is needed, lift the interface to `core`. |
| **Internal packages** | Module-internal code goes under `internal/` sub-package. `@PublishedApi internal` is forbidden. |

---

## 3. Naming

### 3.1 General Rules

| Target | Convention | Example |
|--------|------------|---------|
| Package | lowercase, dot-separated | `org.xrpl.sdk.core.model` |
| Class / Interface | PascalCase | `PaymentTransaction`, `TransactionSigner` |
| Function | camelCase, **starts with verb** | `submitTransaction()`, `decodeHex()` |
| Property | camelCase, **noun** | `ledgerIndex`, `accountRoot` |
| Constant | SCREAMING_SNAKE_CASE | `MAX_FEE_DROPS`, `CODEC_VERSION` |
| Value class | PascalCase, **domain noun** | `Address`, `TxHash`, `XrpDrops` |
| Sealed class member | PascalCase, **state/kind noun** | `Unsigned`, `Signed`, `RpcError` |
| Type parameter | Single uppercase letter or meaningful name | `T`, `K : PrivateKeyable` |
| Test function | backtick + natural language | `` `payment with zero amount should fail validation` `` |
| Extension function | `to{Target}` (convert), `as{View}` (wrap), `is{Condition}` (check) | `toAddress()`, `asFlow()`, `isValidated()` |

### 3.2 XRPL Domain Naming

| Rule | Description | Example |
|------|-------------|---------|
| **Preserve protocol names** | Keep XRPL protocol field names as-is | `TransactionType` (not `TxType`), `DestinationTag` (not `destTag`) |
| **RPC method → function name** | snake_case RPC → camelCase function | `account_info` → `accountInfo()` |
| **Transaction type names** | Use official XRPL names exactly | `OfferCreate`, `TrustSet`, `PaymentChannelClaim` |
| **Ledger object type names** | Official XRPL name, no suffix | `AccountRoot`, `RippleState`, `Offer` |
| **Amount distinction** | XRP = `XrpAmount`, IOU = `IssuedAmount`, unified = `CurrencyAmount` | — |
| **Abbreviations** | 2-letter abbreviations fully uppercase; 3+ letters capitalize only first | `ID`, `TX` vs `Rpc`, `Amm`, `Nft` |

### 3.3 Naming Anti-patterns

```kotlin
// BAD — abbreviated, ambiguous
fun proc(t: Tx): Res
val acctBal: Long
class Mgr

// GOOD — clear, complete
fun processTransaction(transaction: SignedTransaction): SubmitResult
val accountBalance: XrpDrops
class ConnectionManager
```

---

## 4. File Structure

### 4.1 Declaration Order Within Files

```kotlin
// 1. File header (license)
// 2. Package declaration
// 3. Imports (no wildcards)

// 4. Declarations in order:
//    4a. Constants (top-level const val)
//    4b. Type aliases (typealias)
//    4c. Main class/interface
//    4d. Related extension functions
//    4e. Private helper functions
```

### 4.2 Declaration Order Within Classes

```kotlin
class XrplClient(...) {
    // 1. companion object (factories, constants)
    companion object { ... }

    // 2. Properties — public val → public var → private val → private var
    val config: ClientConfig
    private val httpClient: HttpClient

    // 3. init block
    init { ... }

    // 4. Public functions — by usage frequency (core functionality first)
    suspend fun accountInfo(address: Address): AccountInfo { ... }
    suspend fun submit(tx: SignedTransaction): SubmitResult { ... }

    // 5. Internal functions
    internal fun buildRequest(method: String): JsonObject { ... }

    // 6. Private functions
    private fun validateConnection() { ... }

    // 7. Nested classes/interfaces
    class Builder { ... }
}
```

### 4.3 File Size & Splitting Criteria

| Rule | Threshold |
|------|-----------|
| **Max line count** | 300 lines recommended. Must split if exceeding 500 lines. |
| **One public class per file** | Related sealed class members and value classes may share a file. |
| **Extension function files** | Named `{Type}Extensions.kt` (e.g., `AddressExtensions.kt`) |
| **Test mirroring** | `Foo.kt` → `FooTest.kt`, same package structure. |

---

## 5. Formatting

### 5.1 Base Settings

| Item | Value |
|------|-------|
| Indentation | 4 spaces (no tabs) |
| Max line length | 120 characters |
| Trailing comma | **Required** (all multi-line lists, parameters, enum entries) |
| Blank lines | 1 line between functions, 1 line for section separation. Never 2+ consecutive. |
| Imports | No wildcards (`*`). Use IDE auto-organize. |
| Braces | K&R style (opening brace on same line) |

### 5.2 Trailing Comma

```kotlin
// GOOD — trailing comma
data class Payment(
    val account: Address,
    val destination: Address,
    val amount: CurrencyAmount,
    val destinationTag: UInt? = null,  // ← trailing comma
)

enum class TransactionType {
    PAYMENT,
    OFFER_CREATE,
    TRUST_SET,  // ← trailing comma
}
```

**Rationale**: Adding a new item doesn't create a diff on the existing last line. Keeps git blame clean.

### 5.3 Method Chaining Format

```kotlin
// 3 or fewer → single line
val result = list.filter { it.isValid }.map { it.name }

// 4+ or exceeds 120 chars → line breaks
val payments = client.accountTransactions(address)
    .filter { it.transactionType == TransactionType.PAYMENT }
    .map { it as PaymentTransaction }
    .sortedByDescending { it.ledgerIndex }
    .take(10)
```

### 5.4 When Expressions

```kotlin
// Exhaustive when should always be used as an expression (no else needed)
val message = when (result) {
    is XrplResult.Success -> "Validated: ${result.data.hash}"
    is XrplResult.Failure.RpcError -> "RPC ${result.code}: ${result.message}"
    is XrplResult.Failure.NetworkError -> "Network: ${result.cause.message}"
    is XrplResult.Failure.ValidationError -> "Invalid ${result.field}: ${result.reason}"
    is XrplResult.Failure.TecError -> "Tec: ${result.tecCode}"
    XrplResult.Failure.NotFound -> "Not found"
}
```

### 5.5 Lint & Format Tools

```kotlin
// build.gradle.kts
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "<latest>"
}

ktlint {
    version.set("<latest>")
    android.set(false)
    additionalEditorconfig.set(mapOf(
        "max_line_length" to "120",
        "trailing_comma_on_call_site" to "true",
        "trailing_comma_on_declaration_site" to "true",
        "ktlint_function_naming_rule" to "disabled",  // Allow backtick test names
    ))
}
```

**CI must pass `./gradlew ktlintCheck`.**

---

## 6. Type Design

### 6.1 Type Selection Decision Tree

```
Need a new type?
├── Adding domain meaning to a primitive? ──────────── → value class
├── Closed set of states/kinds? ────────────────────── → sealed class / sealed interface
├── Pure data carrier (internal only)? ─────────────── → data class (internal)
├── Pure data carrier (public API)? ────────────────── → regular class + explicit copy/equals
├── Defining a behavior contract? ──────────────────── → interface
├── Fixed set of constants? ────────────────────────── → enum class
└── Platform-specific implementation needed? ───────── → expect fun + interface
```

### 6.2 Value Class — Domain Primitives

Wrap all domain identifiers and units in value classes.

```kotlin
@JvmInline
value class Address(val value: String) {
    init {
        require(value.startsWith("r") || value.startsWith("X")) {
            "Address must start with 'r' (classic) or 'X' (X-address): $value"
        }
    }
}

@JvmInline
value class TxHash(val value: String) {
    init {
        require(value.length == 64 && value.all { it in HEX_CHARS }) {
            "TxHash must be 64-char hex string"
        }
    }
}

@JvmInline
value class XrpDrops(val value: Long) {
    init { require(value >= 0) { "XRP drops cannot be negative: $value" } }
    operator fun plus(other: XrpDrops) = XrpDrops(this.value + other.value)
    operator fun minus(other: XrpDrops) = XrpDrops(this.value - other.value)
    operator fun compareTo(other: XrpDrops) = this.value.compareTo(other.value)
    fun toXrp(): String = BigDecimal(value).movePointLeft(6).toPlainString()
}

@JvmInline
value class LedgerIndex(val value: UInt)

@JvmInline
value class Seed(val value: String)
```

**Rules:**
- Validate invariants in the `init` block (fail-fast).
- Include the actual value in error messages (aids debugging).
- Arithmetic operators only between the same value class (`XrpDrops + XrpDrops`, not `XrpDrops + Long`).
- Do not override `toString()` — value classes automatically delegate to the wrapped value's `toString()`.

### 6.3 Sealed Class — State Machines & Sum Types

```kotlin
// Transaction lifecycle: types prevent illegal transitions
sealed interface XrplTransaction {
    val account: Address
    val transactionType: TransactionType

    // Before autofill
    data class Unsigned(
        override val account: Address,
        override val transactionType: TransactionType,
        val fields: TransactionFields,
    ) : XrplTransaction

    // After autofill, before signing
    data class Filled(
        override val account: Address,
        override val transactionType: TransactionType,
        val fields: TransactionFields,
        val fee: XrpDrops,
        val sequence: UInt,
        val lastLedgerSequence: UInt,
    ) : XrplTransaction

    // After signing
    data class Signed(
        override val account: Address,
        override val transactionType: TransactionType,
        val txBlob: String,
        val hash: TxHash,
    ) : XrplTransaction
}
```

**Rules:**
- Sealed class members that **only hold data** → `data class`; those with **behavior** → regular class.
- `data class` is allowed inside sealed hierarchies (adding members has limited blast radius within the sealed type).
- Prefer `sealed interface` for top-level sealed types (allows multiple inheritance).
- Use `object` for stateless variants (`data object` preferred — guarantees `toString()`).

### 6.4 Regular Class — Public API Models

```kotlin
// BAD — data class in public API
data class AccountInfo(
    val account: Address,
    val balance: XrpDrops,
    val sequence: UInt,
    // Adding fields in v2 → changes copy() signature → binary compatibility break
)

// GOOD — regular class, expose only what's needed
class AccountInfo(
    val account: Address,
    val balance: XrpDrops,
    val sequence: UInt,
) {
    override fun equals(other: Any?): Boolean =
        other is AccountInfo && account == other.account && balance == other.balance && sequence == other.sequence

    override fun hashCode(): Int = Objects.hash(account, balance, sequence)

    override fun toString(): String = "AccountInfo(account=$account, balance=$balance, sequence=$sequence)"
}
```

**Exception**: `data class` is allowed inside sealed class members.

### 6.5 Interface — Behavior Contracts

```kotlin
// Crypto abstraction — swappable implementations
interface TransactionSigner<K : PrivateKeyable> {
    fun sign(key: K, transaction: XrplTransaction.Filled): XrplTransaction.Signed
    fun multiSign(key: K, transaction: XrplTransaction.Filled): SingleSignature
}

// Avoid default implementations in public interfaces (binary compatibility issues)
// Use abstract class if defaults are needed
```

### 6.6 Enum — Closed Constant Sets

```kotlin
enum class KeyAlgorithm(val xrplName: String) {
    ED25519("ed25519"),
    SECP256K1("secp256k1"),
    ;  // trailing semicolon + comma

    companion object {
        fun fromXrplName(name: String): KeyAlgorithm =
            entries.find { it.xrplName.equals(name, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown key algorithm: $name")
    }
}
```

**Rules:**
- Use enum only when values are closed and unlikely to change in the XRPL protocol.
- For extensible values like transaction types (which grow with protocol amendments), use sealed class or `@JvmInline value class` + companion constants instead.

---

## 7. Public API Rules

### 7.1 Visibility

```kotlin
// All declarations must have explicit visibility modifiers
public class XrplClient          // ✓ explicit public
class XrplClient                 // ✗ implicit public forbidden

internal class RequestBuilder    // ✓ module-internal
private fun validate()           // ✓ class-private
```

**Enforce via ktlint rule**: `ktlint_standard_no-implicit-public` (or detekt `ExplicitVisibility`).

### 7.2 Return Types

```kotlin
// BAD — inferred type leaks implementation details
public fun createClient() = OkHttpXrplClient(config)

// GOOD — return interface/supertype
public fun createClient(): XrplClient = OkHttpXrplClient(config)
```

**Explicit return type is required on all `public` and `protected` functions and properties.**

### 7.3 No Adding Default Arguments to Existing Functions

```kotlin
// v1.0
public fun submitTransaction(tx: SignedTransaction): SubmitResult

// v1.1 — BAD: adding default argument → binary compatibility break
public fun submitTransaction(tx: SignedTransaction, timeout: Duration = 30.seconds): SubmitResult

// v1.1 — GOOD: add overload
public fun submitTransaction(tx: SignedTransaction): SubmitResult
public fun submitTransaction(tx: SignedTransaction, timeout: Duration): SubmitResult
```

### 7.4 Deprecation Process

```kotlin
// Phase 1: WARNING (at least 1 minor release)
@Deprecated(
    message = "Use submitAndWait() instead.",
    replaceWith = ReplaceWith("submitAndWait(tx, wallet)"),
    level = DeprecationLevel.WARNING,
)
public fun sendTransaction(tx: Transaction, wallet: Wallet): Result

// Phase 2: ERROR (next minor release)
// Phase 3: HIDDEN (remove in major release)
```

### 7.5 Experimental APIs

```kotlin
@RequiresOptIn(
    message = "This API is experimental and may change without notice.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalXrplApi

// Usage
@ExperimentalXrplApi
public suspend fun simulate(tx: UnsignedTransaction): SimulateResult
```

### 7.6 Binary Compatibility Validator

```kotlin
// build.gradle.kts (root)
plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "<latest>"
}

// CI: ./gradlew apiCheck fails the build on breakage
// Intentional changes: ./gradlew apiDump then commit .api files
```

---

## 8. DSL & Builders

### 8.1 @DslMarker Required

```kotlin
@DslMarker
@Target(AnnotationTarget.CLASS)
public annotation class XrplDsl
```

**All DSL builder classes must be annotated with `@XrplDsl`.** Prevents scope leaks at compile time.

### 8.2 Builder Pattern

```kotlin
@XrplDsl
public class PaymentBuilder internal constructor() {
    // Required fields: lateinit
    public lateinit var account: Address
    public lateinit var destination: Address
    public lateinit var amount: CurrencyAmount

    // Optional fields: nullable with default
    public var destinationTag: UInt? = null
    public var invoiceId: String? = null
    public var memos: MutableList<Memo> = mutableListOf()

    // Nested builder
    public fun memo(block: MemoBuilder.() -> Unit) {
        memos.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): PaymentTransaction {
        require(::account.isInitialized) { "account is required" }
        require(::destination.isInitialized) { "destination is required" }
        require(::amount.isInitialized) { "amount is required" }
        return PaymentTransaction(account, destination, amount, destinationTag, invoiceId, memos.toList())
    }
}

// Top-level factory function
public fun payment(block: PaymentBuilder.() -> Unit): XrplTransaction.Unsigned {
    val payment = PaymentBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        account = payment.account,
        transactionType = TransactionType.PAYMENT,
        fields = payment.toFields(),
    )
}

// Usage
val tx = payment {
    account = wallet.address
    destination = "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe".toAddress()
    amount = 10.xrp
    memo {
        data = "Invoice #42"
        type = "text/plain"
    }
}
```

### 8.3 Builder Rules

| Rule | Description |
|------|-------------|
| **Builder constructor is `internal`** | Prevent direct `PaymentBuilder()` calls. Enter only through factory functions. |
| **`build()` is `internal`** | No external direct calls. Factory function calls it. |
| **Missing required field = immediate failure** | `lateinit` + `require(::field.isInitialized)` or required constructor params. |
| **Builder properties = `var`** | Mutable only inside the builder. `build()` result is immutable. |
| **Return type is immutable** | Collections in `build()` result use `toList()`, `toMap()`, etc. for defensive copies. |

### 8.4 Config DSL

```kotlin
// Ktor-style client configuration
public fun XrplClient(block: XrplClientConfig.() -> Unit = {}): XrplClient {
    val config = XrplClientConfig().apply(block)
    return XrplClientImpl(config.freeze())
}

@XrplDsl
public class XrplClientConfig {
    public var network: Network = Network.Mainnet
    public var timeout: Duration = 30.seconds
    public var maxRetries: Int = 3
    public var feeCushion: Double = 1.2
    public var maxFeeXrp: Double = 2.0

    // Nested configuration blocks
    public fun logging(block: LoggingConfig.() -> Unit) { ... }
    public fun retry(block: RetryConfig.() -> Unit) { ... }

    internal fun freeze(): FrozenClientConfig = FrozenClientConfig(
        network, timeout, maxRetries, feeCushion, maxFeeXrp,
    )
}
```

---

## 9. Coroutines

### 9.1 suspend vs Flow vs Synchronous

```
Single request/response    → suspend fun
Continuous stream           → fun (): Flow<T>
Pure computation (CPU)      → regular fun (no suspend needed)
```

```kotlin
// Single — suspend
public suspend fun accountInfo(address: Address): AccountInfo

// Stream — returns Flow (function itself is not suspend)
public fun subscribeToLedger(): Flow<LedgerEvent>

// Pure computation — synchronous
public fun encodeTransaction(tx: FilledTransaction): ByteArray
public fun deriveAddress(publicKey: PublicKey): Address
```

### 9.2 Dispatcher Rules

```kotlin
// BAD — hardcoded
suspend fun fetchLedger() = withContext(Dispatchers.IO) { ... }

// GOOD — injected
public class XrplClient(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    public suspend fun fetchLedger(): Ledger = withContext(ioDispatcher) { ... }
}

// Test
val client = XrplClient(ioDispatcher = UnconfinedTestDispatcher())
```

**Rules:**
- The only place `Dispatchers.*` is referenced directly is **constructor default arguments**.
- Direct `Dispatchers.*` usage in function bodies is forbidden.

### 9.3 Structured Concurrency

```kotlin
// GOOD — bound child coroutines with coroutineScope
public suspend fun fetchLedgerWithTransactions(seq: UInt): LedgerWithTxns = coroutineScope {
    val ledger = async { getLedger(seq) }
    val txns = async { getTransactions(seq) }
    LedgerWithTxns(ledger.await(), txns.await())
}

// BAD — GlobalScope is forbidden
GlobalScope.launch { ... }  // ✗ leak risk

// BAD — don't expose Deferred in public API
public fun fetchAsync(): Deferred<Ledger>  // ✗ caller bears cancellation burden
```

### 9.4 CancellationException

```kotlin
// BAD — swallows CancellationException
try {
    doWork()
} catch (e: Exception) {
    logger.error("Failed", e)  // Also catches CancellationException!
}

// GOOD — always rethrow
try {
    doWork()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    logger.error("Failed", e)
}

// BETTER — use ensureActive()
suspend fun longRunningWork() {
    items.forEach { item ->
        ensureActive()  // Check for cancellation
        process(item)
    }
}
```

### 9.5 Flow Rules

```kotlin
// Set dispatcher on the emit side with flowOn (not the collect side)
public fun subscribeToLedger(): Flow<LedgerEvent> = channelFlow {
    webSocket.incoming.consumeEach { frame ->
        send(parseLedgerEvent(frame))
    }
}.flowOn(ioDispatcher)

// Consumer doesn't need to switch dispatchers
client.subscribeToLedger().collect { event -> updateUI(event) }
```

| Rule | Description |
|------|-------------|
| **Flow is cold** | Nothing happens without a collector. Side effects begin after `collect` starts. |
| **`flowOn` on emit side** | Set dispatcher with `flowOn` inside the Flow-returning function. |
| **SharedFlow/StateFlow as properties** | Expose as properties, not functions: `val connectionState: StateFlow<ConnectionState>` |
| **`catch` just before `collect`** | The final consumer decides the error strategy. Don't swallow in intermediate operators. |

---

## 10. Extension Functions

### 10.1 Placement Rules

| Kind | File Location | Example |
|------|---------------|---------|
| **Type conversion** | Target type file or `{Type}Extensions.kt` | `String.toAddress()` |
| **DSL builder entry point** | Builder class file | `fun payment { }` (top-level) |
| **Literal extensions** | `Literals.kt` (single file) | `10.xrp`, `12L.drops` |
| **Companion factory** | Target class file | `Wallet.generate()` |
| **Client sugar** | `XrplClientExtensions.kt` | `XrplClient.getXrpBalance()` |

### 10.2 Extension Rules

```kotlin
// GOOD — extension function for natural call chains
val address = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh".toAddress()
val balance = client.getXrpBalance(address)
val amount = 10.xrp

// BAD — polluting unrelated types with extensions
fun String.submitToXrpl()  // ✗ String is not a transaction
fun Int.toLedgerIndex()    // ✗ Not every Int is a ledger index
```

| Rule | Description |
|------|-------------|
| **Receiver must semantically be that type** | `String.toAddress()` ✓ (string is an address representation), `String.submit()` ✗ |
| **Check namespace** | Minimize extensions on broad types like `Any`, `String`, `Int` |
| **Internal extensions are fine** | Module-internal utilities as `internal fun Type.helper()` |

### 10.3 Amount Literals

```kotlin
// Literals.kt — all literal extensions in a single file
public val Long.drops: XrpAmount get() = XrpAmount.ofDrops(this)
public val Int.xrp: XrpAmount get() = XrpAmount.ofXrp(this.toLong())
public val Double.xrp: XrpAmount get() = XrpAmount.ofXrp(this)

// Usage
val fee = 12L.drops
val payment = 25.xrp
```

---

## 11. Serialization

### 11.1 Json Instance (Project-Wide)

```kotlin
// Single instance per module, exposed as internal
internal val XrplJson: Json = Json {
    ignoreUnknownKeys = true      // Safe against server field additions
    isLenient = true              // Tolerate minor format differences
    encodeDefaults = false        // Don't serialize default-value fields
    explicitNulls = false         // Omit null fields
    coerceInputValues = true      // Invalid enum → default value
    classDiscriminator = "TransactionType"  // XRPL protocol field name
}
```

### 11.2 Serializable Rules

```kotlin
// Internal transport models — data class + @Serializable is OK
@Serializable
internal data class AccountInfoRequest(
    val account: String,
    val ledger_index: String = "validated",
)

// When XRPL JSON field names differ from Kotlin conventions — use @SerialName
@Serializable
internal data class AccountInfoResponse(
    @SerialName("account_data") val accountData: AccountData,
    @SerialName("ledger_index") val ledgerIndex: UInt,
)
```

| Rule | Description |
|------|-------------|
| **`@Serializable` on internal models** | Don't couple public API models to the serialization framework. |
| **Separate public models from serialization models** | Write mapping functions from internal `*Response` → public domain models. |
| **`@SerialName` for field mapping** | Kotlin camelCase ↔ XRPL snake_case/PascalCase. |
| **Value classes auto-flatten** | Use `@Serializable @JvmInline value class`. |
| **Minimize custom serializers** | Only for complex polymorphism (Amount, Transaction). |

### 11.3 Polymorphic Serialization

```kotlin
// Per-transaction-type deserialization — content-based polymorphism
internal object TransactionSerializer :
    JsonContentPolymorphicSerializer<TransactionModel>(TransactionModel::class) {

    override fun selectDeserializer(element: JsonElement) =
        when (val type = element.jsonObject["TransactionType"]?.jsonPrimitive?.content) {
            "Payment" -> PaymentModel.serializer()
            "OfferCreate" -> OfferCreateModel.serializer()
            "TrustSet" -> TrustSetModel.serializer()
            // ...
            else -> UnknownTransactionModel.serializer()  // Unknown types don't crash
        }
}
```

**The `else` branch returns `UnknownTransactionModel`** so the SDK doesn't crash when new transaction types are added to the protocol.

---

## 12. Error Handling

### 12.1 Error Strategy: Sealed Result

**Exceptions are for programming errors only. Expected failures are returned as sealed class values.**

```kotlin
public sealed class XrplResult<out T> {
    public data class Success<T>(val data: T) : XrplResult<T>()

    public sealed class Failure : XrplResult<Nothing>() {
        public data class RpcError(
            val code: String,
            val message: String,
            val rawResponse: String,
        ) : Failure()

        public data class NetworkError(val cause: Throwable) : Failure()
        public data class ValidationError(val field: String, val reason: String) : Failure()
        public data class TecError(val tecCode: String, val feeConsumed: XrpDrops) : Failure()
        public data object NotFound : Failure()
    }
}
```

### 12.2 When to Use Exceptions

| Scenario | Handling |
|----------|----------|
| **Programming error** (bad arguments, precondition violation) | `require()`, `check()`, `error()` |
| **Impossible state** | `error("Unreachable: ...")` |
| **External library exceptions** | Convert to `XrplResult.Failure` immediately at the boundary |

```kotlin
// require — caller's mistake (IllegalArgumentException)
public fun payment(block: PaymentBuilder.() -> Unit): XrplTransaction.Unsigned {
    val builder = PaymentBuilder().apply(block)
    require(builder.amount > 0.xrp) { "Payment amount must be positive" }
    return builder.build()
}

// check — internal invariant violation (IllegalStateException)
internal fun ensureConnected() {
    check(connectionState.value is ConnectionState.Connected) {
        "Client is not connected. Call connect() first."
    }
}
```

### 12.3 Error Message Format

```
[What went wrong]. [Actual value]. [How to fix it].
```

```kotlin
// GOOD
"Address must start with 'r' or 'X', got: '${value.take(5)}...'. Use Address(\"r...\") format."
"Fee exceeds maxFeeXrp (${config.maxFeeXrp} XRP). Set maxFeeXrp in client config to override."

// BAD
"Invalid input"
"Error occurred"
```

### 12.4 Result Convenience Extensions

```kotlin
// Convenience extensions on XrplResult
public fun <T> XrplResult<T>.getOrNull(): T? = when (this) {
    is XrplResult.Success -> data
    is XrplResult.Failure -> null
}

public fun <T> XrplResult<T>.getOrThrow(): T = when (this) {
    is XrplResult.Success -> data
    is XrplResult.Failure -> throw XrplException(this)
}

public inline fun <T, R> XrplResult<T>.map(transform: (T) -> R): XrplResult<R> = when (this) {
    is XrplResult.Success -> XrplResult.Success(transform(data))
    is XrplResult.Failure -> this
}

public inline fun <T> XrplResult<T>.onSuccess(action: (T) -> Unit): XrplResult<T> = also {
    if (it is XrplResult.Success) action(it.data)
}

public inline fun <T> XrplResult<T>.onFailure(action: (XrplResult.Failure) -> Unit): XrplResult<T> = also {
    if (it is XrplResult.Failure) action(it)
}
```

---

## 13. Testing

### 13.1 Frameworks

| Purpose | Tool |
|---------|------|
| Test framework | **Kotest** (FunSpec, StringSpec) |
| Property-based testing | **Kotest Property Testing** (Arb, checkAll) |
| Assertions | **Kotest Assertions** (shouldBe, shouldThrow) |
| Coroutine testing | **kotlinx-coroutines-test** (runTest, TestScope) |
| HTTP mocking | **Ktor MockEngine** (built-in test double) |
| Golden file testing | Project-internal utility |

### 13.2 Test Naming

```kotlin
class PaymentTransactionTest : FunSpec({

    // Pattern: "given + action + expected result"
    test("payment with valid amount should create unsigned transaction") { ... }
    test("payment with zero amount should fail validation") { ... }
    test("payment without destination should throw IllegalArgumentException") { ... }

    // Context grouping
    context("autofill") {
        test("should set fee from server response") { ... }
        test("should set lastLedgerSequence to current + 20") { ... }
    }
})
```

### 13.3 Test Structure

```kotlin
class XrplClientTest : FunSpec({

    // 1. Fixture setup — top of file
    val testAddress = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

    val mockEngine = MockEngine { request ->
        respond(
            content = loadFixture("account_info_response.json"),
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }

    val client = XrplClient {
        network = Network.Custom("http://mock", "")
        httpEngine = mockEngine
    }

    // 2. Test cases
    test("accountInfo should return balance for valid address") {
        // Given — already set up in fixtures

        // When
        val result = client.accountInfo(testAddress)

        // Then
        result.shouldBeInstanceOf<XrplResult.Success<AccountInfo>>()
        result.data.balance shouldBe XrpDrops(1_000_000)
    }
})
```

### 13.4 Test Categories

| Category | Location | Execution Condition |
|----------|----------|---------------------|
| **Unit tests** | `src/commonTest/` | Always (CI required) |
| **Property-based** | `src/commonTest/` | Always (CI required) |
| **Golden file** | `src/commonTest/resources/fixtures/` | Always (CI required) |
| **Integration tests** | `src/commonTest/` + `@Tag("integration")` | When Testnet is available (`-Pintegration`) |

### 13.5 Property-Based Testing

```kotlin
class BinaryCodecPropertyTest : FunSpec({

    test("encode then decode should be identity for XRP amounts") {
        checkAll(Arb.long(0L..100_000_000_000L)) { drops ->
            val amount = XrpAmount.ofDrops(drops)
            val encoded = BinaryCodec.encodeAmount(amount)
            val decoded = BinaryCodec.decodeAmount(encoded)
            decoded shouldBe amount
        }
    }

    test("address encode then decode should be identity") {
        checkAll(addressArb()) { address ->
            val encoded = AddressCodec.encode(address.publicKey)
            val decoded = AddressCodec.decode(encoded)
            decoded shouldBe address
        }
    }
})
```

### 13.6 Golden File Testing

```kotlin
// Fixed input → fixed binary output (regression prevention)
test("Payment serialization matches golden file") {
    val tx = payment {
        account = Address("rN7n3473SaZBCG4dFL83w7p1W9cgZw6GFN")
        destination = Address("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy")
        amount = 1_000_000L.drops
    }

    val encoded = BinaryCodec.encode(tx)
    val expected = loadFixture("golden/payment_basic.hex")
    encoded.toHexString() shouldBe expected.trim()
}
```

**Golden files go in `src/commonTest/resources/fixtures/golden/`.**
Changes to golden files must include rationale in the PR description.

### 13.7 Testing Anti-patterns

| Forbidden | Reason |
|-----------|--------|
| `Thread.sleep()` | Use `delay()` or `advanceTimeBy()` |
| `runBlocking` in tests | Use `runTest` |
| Unit tests depending on external services | Use `MockEngine` or fixtures |
| `@Ignore` without issue link | Ignored tests must reference an issue tracker link |
| Merging flaky tests | Fix flaky tests before merging |

---

## 14. Documentation

### 14.1 KDoc Required Targets

| Target | KDoc Required? |
|--------|----------------|
| `public class` / `interface` | **Required** |
| `public fun` | **Required** |
| `public val` / `var` | Required if name is not self-explanatory |
| `internal` | Recommended for complex logic |
| `private` | Not required (code should be self-documenting) |

### 14.2 KDoc Format

```kotlin
/**
 * Submits a signed transaction to the XRP Ledger and waits for validation.
 *
 * Internally calls [autofill] to populate fee, sequence, and lastLedgerSequence,
 * then [TransactionSigner.sign] to sign, and [submit] to submit.
 * Watches ledger close events via WebSocket until validation result is available.
 *
 * @param tx The unsigned transaction to submit. Create with [payment], [trustSet], etc.
 * @param wallet The wallet to sign with.
 * @param timeout Maximum time to wait for validation. Defaults to [DEFAULT_SUBMIT_TIMEOUT].
 * @return [XrplResult.Success] with [ValidatedTransaction], or an [XrplResult.Failure] subtype.
 * @throws IllegalStateException if the client is not connected.
 * @sample org.xrpl.sdk.samples.SubmitPaymentSample.submitAndWait
 * @see submit for fire-and-forget submission without waiting.
 */
public suspend fun submitAndWait(
    tx: XrplTransaction.Unsigned,
    wallet: Wallet,
    timeout: Duration = DEFAULT_SUBMIT_TIMEOUT,
): XrplResult<ValidatedTransaction>
```

### 14.3 KDoc Rules

| Rule | Description |
|------|-------------|
| **First sentence = summary** | One sentence ending with a period. Used by Dokka as summary. |
| **Write `@param`, `@return`** | Describe all parameters and return values. |
| **Declare `@throws`** | List throwable exceptions. (With sealed results, only programming errors throw.) |
| **Use `@sample`** | Reference compilable examples in `samples/` directory. |
| **Use links** | Link to related functions/classes with `[className]`, `[functionName]`. |
| **Exclude implementation details** | Say "sends via HTTP" not "internally uses OkHttp". |

### 14.4 `@sample` Rules

```
samples/
└── org/xrpl/sdk/samples/
    ├── ClientSetupSample.kt    # Compilable example code
    ├── SubmitPaymentSample.kt
    └── SubscriptionSample.kt
```

Sample files must be **compilable** and reflect real usage patterns.

---

## 15. Dependency Management

### 15.1 Dependency Principles

| Principle | Description |
|-----------|-------------|
| **Minimal dependencies** | `xrpl-core` depends only on `kotlinx-serialization-json` and stdlib. |
| **API vs Implementation** | Types exposed to users → `api()`. Internal only → `implementation()`. |
| **KMP compatible** | `commonMain` dependencies must be KMP libraries only. JVM-only deps go in `jvmMain`. |
| **Version catalog** | All versions managed in a single `libs.versions.toml`. |

### 15.2 Version Catalog

```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.1.x"
ktor = "3.x.x"
kotlinx-serialization = "1.8.x"
kotlinx-coroutines = "1.10.x"
kotlinx-datetime = "0.6.x"
kotest = "6.x.x"

[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-websockets = { module = "io.ktor:ktor-client-websockets", version.ref = "ktor" }
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
kotest-runner-junit5 = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions-core = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
kotest-property = { module = "io.kotest:kotest-property", version.ref = "kotest" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
binary-compatibility-validator = { id = "org.jetbrains.kotlinx.binary-compatibility-validator", version = "x.x.x" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version = "x.x.x" }
maven-publish = { id = "com.vanniktech.maven.publish", version = "x.x.x" }
```

### 15.3 Module Dependency Direction

```
xrpl-core (stdlib, kotlinx-serialization-json, kotlinx-datetime)
    ↑
xrpl-binary-codec (xrpl-core)
    ↑
xrpl-crypto (xrpl-core, cryptography-kotlin)
    ↑
xrpl-client (xrpl-core, xrpl-binary-codec, xrpl-crypto, ktor-client, kotlinx-coroutines)
    ↑
xrpl-bom (unified version for all modules, no code)
```

`xrpl-test-fixtures` depends on all modules via `api()`. Users consume it as `testImplementation` only.

---

## 16. Git & Commits

### 16.1 Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Release-ready stable code |
| `develop` | Next release integration |
| `feature/{issue}-{description}` | Feature development |
| `fix/{issue}-{description}` | Bug fixes |
| `release/{version}` | Release preparation |

### 16.2 Commit Messages

[Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <description>

[optional body]
[optional footer]
```

| Type | Purpose |
|------|---------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change with no behavior change |
| `test` | Adding/modifying tests |
| `docs` | Documentation changes |
| `chore` | Build, CI, dependencies |
| `perf` | Performance improvement |
| `breaking` | Breaking change (include `BREAKING CHANGE:` in footer) |

```
feat(client): add submitAndWait sugar function

Autofills, signs, submits, and waits for validation in a single call.
Polls ledger close events via WebSocket for finality detection.

Closes #42
```

**Scopes**: `core`, `codec`, `crypto`, `client`, `test`, `build`, `ci`

### 16.3 PR Rules

| Rule | Description |
|------|-------------|
| **1 PR = 1 concern** | Separate features from refactoring. |
| **CI must pass** | ktlint, apiCheck, and all tests must pass. |
| **Include `.api` files on API changes** | Commit `./gradlew apiDump` output. |
| **Explain golden file changes** | State rationale in PR description. |

---

## 17. KMP (Kotlin Multiplatform)

### 17.1 Targets

| Target | Priority | Purpose |
|--------|----------|---------|
| **JVM** | P0 (required) | Server, Android, Desktop |
| **JS** | P1 | Browser, Node.js |
| **iOS (Native)** | P1 | iOS/macOS apps |
| **Linux/Windows (Native)** | P2 | CLI tools, servers |

### 17.2 Source Set Structure

```
src/
├── commonMain/       # All public API, business logic
├── commonTest/       # Shared tests
├── jvmMain/          # JVM-specific (Java crypto, BouncyCastle)
├── jvmTest/
├── jsMain/           # JS-specific (Web Crypto API)
├── jsTest/
├── nativeMain/       # Native-specific (CryptoKit, OpenSSL)
├── nativeTest/
├── iosMain/          # iOS-specific (@ObjCName, etc.)
└── iosTest/
```

### 17.3 expect/actual Pattern

```kotlin
// GOOD — expect fun + interface (flexible)
// commonMain
public interface CryptoProvider {
    public fun sha256(data: ByteArray): ByteArray
    public fun sha512Half(data: ByteArray): ByteArray
    public fun ripemd160(data: ByteArray): ByteArray
}

public expect fun platformCryptoProvider(): CryptoProvider

// jvmMain
public actual fun platformCryptoProvider(): CryptoProvider = JvmCryptoProvider()

private class JvmCryptoProvider : CryptoProvider {
    override fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)
    // ...
}
```

```kotlin
// BAD — expect class (restrictive)
expect class CryptoProvider {
    fun sha256(data: ByteArray): ByteArray
}
// → Can't provide interface implementations per-platform, can't inject test doubles
```

### 17.4 KMP Rules

| Rule | Description |
|------|-------------|
| **Maximize `commonMain`** | 100% of public API in `commonMain`. `expect/actual` only for implementation details. |
| **`expect fun` > `expect class`** | Return interfaces for test double injection. |
| **Apply `@ObjCName`** | Ensure natural API names on iOS. |
| **Platform deps stay in their source set** | BouncyCastle in `jvmMain` must not leak into `commonMain`. |

---

## 18. Security

### 18.1 Secret Data Handling

```kotlin
// Zero out private keys after use
public class Wallet internal constructor(
    public val address: Address,
    public val publicKey: PublicKey,
    private val privateKey: ByteArray,
) : AutoCloseable {

    public fun sign(message: ByteArray): ByteArray {
        // ... signing logic
    }

    override fun close() {
        privateKey.fill(0)  // Wipe from memory
    }
}

// Usage
Wallet.fromSeed(seed).use { wallet ->
    val signed = wallet.sign(txBytes)
}
```

### 18.2 Security Rules

| Rule | Description |
|------|-------------|
| **Exclude private keys from `toString()`** | Never include private keys, seeds, or secrets in `toString()`. |
| **No secrets in logs** | Never log private keys, seeds, or secrets. |
| **`equals()` must be constant-time** | Prevent timing attacks on secret comparisons. Use `MessageDigest.isEqual()` or equivalent. |
| **Cryptographic RNG only** | `SecureRandom` (JVM), `crypto.getRandomValues()` (JS), `SecRandomCopyBytes` (iOS). |
| **Dependency vulnerability scanning** | Include Dependabot or `./gradlew dependencyCheckAnalyze` in CI. |

### 18.3 Input Validation

```kotlin
// Validate at module boundaries; trust internally
// Value class init blocks serve as the boundary

@JvmInline
value class Address(val value: String) {
    init {
        require(value.length in 25..50) { "Address length must be 25-50, got: ${value.length}" }
        require(value[0] in setOf('r', 'X')) { "Address must start with 'r' or 'X', got: '${value[0]}'" }
    }
}

// Internal functions receiving Address type know it's already validated
internal fun lookupAccount(address: Address): AccountData {
    // address.value is already valid — no re-validation needed
}
```

---

## Appendix A. Quick Reference — DO / DON'T

```kotlin
// ✓ DO
val list: List<String>                     // Immutable collection
public fun calculate(): Int                // Explicit return type
value class UserId(val value: String)      // Domain primitive wrapper
sealed interface Result                    // Closed result type
suspend fun fetch(): Data                  // Coroutine-native
fun observe(): Flow<Data>                  // Stream via Flow
payment { amount = 10.xrp }               // DSL builder
require(x > 0) { "x must be positive" }   // Precondition

// ✗ DON'T
var list: MutableList<String>              // Exposed mutable collection
fun calculate() = complexLogic()           // Inferred type
data class Account(...)                    // Public data class
fun fetchAsync(): Deferred<Data>           // Exposed Deferred
GlobalScope.launch { }                     // Unstructured concurrency
try { } catch (e: Exception) { }          // Swallowing CancellationException
Thread.sleep(1000)                         // Blocking sleep
```

---

## Appendix B. Convention Checklist (for PR Review)

- [ ] All `public` declarations have explicit visibility modifiers
- [ ] All `public` functions/properties have explicit return types
- [ ] No `data class` in public API (except inside sealed types)
- [ ] New `public` API has KDoc
- [ ] Domain primitives wrapped in value classes
- [ ] `@XrplDsl` applied (if it's a DSL builder)
- [ ] Correct choice of `suspend` / `Flow`
- [ ] No direct `Dispatchers.*` references (except constructor default args)
- [ ] `CancellationException` is not swallowed
- [ ] Trailing commas used
- [ ] Error messages include actual value + how to fix
- [ ] `./gradlew ktlintCheck apiCheck` passes
- [ ] Golden file changes include rationale
- [ ] Secrets excluded from `toString()` / logs
