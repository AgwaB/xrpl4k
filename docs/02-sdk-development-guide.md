# XRPL Kotlin SDK — SDK Development Guide

> Date: 2026-03-12 (incorporating in-depth research)
> Purpose: Kotlin SDK design principles, real SDK references, blockchain SDK patterns, high-DX design patterns

---

## 1. General SDK Design Principles

### 1.1 Core 5 Principles (Azure SDK Guidelines)

| Principle | Definition |
|-----------|------------|
| **Idiomatic** | Must feel natural by following the conventions of the target language |
| **Consistent** | Logging, error handling, and naming are uniform across all modules |
| **Approachable** | Predictable defaults; core use cases discoverable without deep knowledge |
| **Diagnosable** | Error messages are concise, contextually linked, and actionable |
| **Dependable** | Stability takes priority over compatibility breaks; breaking changes only in major releases |

### 1.2 JetBrains Official Kotlin API Guidelines

#### Declare Explicit Return Types on Public APIs

```kotlin
// BAD — relies on type inference; binary compatibility broken when implementation changes
fun Int.defaultDeserializer() = JsonDeserializer { ... }

// GOOD — return type is the contract
fun Int.defaultDeserializer(): JsonDeserializer<Int> = JsonDeserializer { ... }
```

#### Avoid data class on Public APIs

```kotlin
// BAD — adding a field changes copy(), componentN(), and constructor signatures
data class User(val name: String, val email: String)
// Adding active in v2 → all copy() call sites break

// GOOD — regular class; direct control over the API surface
class User(val name: String, val email: String) {
    fun copy(name: String = this.name, email: String = this.email) = User(name, email)
}
```

#### Do Not Add Default Arguments to Existing Functions

```kotlin
// v1.0
fun fib() = 0

// v1.1 — binary compatibility broken! Compiled callers get NoSuchMethodError
fun fib(input: Int = 0) = fibonacci(input)

// CORRECT — manual overloads
fun fib() = 0
fun fib(input: Int) = fibonacci(input)
```

### 1.3 Backward Compatibility

**Binary Compatibility Validator** (built into Kotlin Gradle Plugin 2.2.0+):
```kotlin
plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.16.3"
}
// ./gradlew apiDump → generates .api files
// ./gradlew apiCheck → automatically detects compatibility breaks in CI
```

**Deprecation cycle:**
```kotlin
@Deprecated(
    message = "Use submitTransaction(). This function ignores timeout.",
    replaceWith = ReplaceWith("submitTransaction(timeout)"),
    level = DeprecationLevel.WARNING  // → ERROR → removed in next major
)
fun sendTransaction() { }
```

**Unstable API gating:**
```kotlin
@RequiresOptIn(
    message = "This API is experimental and may change without prior notice.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalXrplApi
```

### 1.4 Versioning Strategy

```
MAJOR.MINOR.BUGFIX (SemVer)
alpha (unstable API) → beta (feature complete, API frozen) → rc (ship-blocking only) → stable
```

Narrowing a return type (`Number` → `Int`) is also a binary compatibility break. Changing `internal` functions annotated with `@PublishedApi` also affects inline call sites.

---

## 2. Real Kotlin SDK Reference Analysis

### 2.1 Ktor Client (JetBrains) — HTTP/WebSocket Client

**The SDK with the most to learn from.** Architecture, DSL design, plugin system, engine abstraction, and testing are all highly worth referencing.

#### Architecture: 3-Layer Separation

```
ktor-client-core     ← engine-agnostic types, pipeline, plugin interfaces
ktor-client-{engine}  ← platform-specific transport (CIO, OkHttp, Darwin, JS)
ktor-client-{plugin}  ← optional features (ContentNegotiation, Logging, etc.)
```

#### DSL Design — Hierarchical Receiver-Lambda

```kotlin
val client = HttpClient(CIO) {
    expectSuccess = true                    // top-level config
    engine {                                // engine-specific config block
        threadsCount = 4
    }
    install(ContentNegotiation) {           // plugin config block
        json(Json { prettyPrint = true })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
        filter { request -> request.url.host.contains("xrpl") }
    }
}
```

**Key insight**: Each nested block has its own typed receiver, so autocomplete shows only the options for that level.

#### Custom Plugin System

```kotlin
class CustomHeaderConfig {
    var headerName: String = "X-Custom"
    var headerValue: String = "default"
}

val CustomHeaderPlugin = createClientPlugin("CustomHeader", ::CustomHeaderConfig) {
    val name = pluginConfig.headerName
    val value = pluginConfig.headerValue
    onRequest { request, _ ->
        request.headers.append(name, value)
    }
    onResponse { response ->
        println("Status: ${response.status}")
    }
}

// Installed the same way as built-in plugins
install(CustomHeaderPlugin) {
    headerName = "X-Request-Id"
    headerValue = UUID.randomUUID().toString()
}
```

#### Engine Abstraction — Swapped via Constructor Argument

```kotlin
val client = HttpClient()        // auto-selected from classpath
val client = HttpClient(OkHttp)  // explicit selection
val client = HttpClient(CIO)     // pure Kotlin, all platforms
```

| Engine | Platform | HTTP/2 | WebSocket |
|--------|----------|--------|-----------|
| CIO | JVM, Android, Native, JS | No | Yes |
| OkHttp | JVM, Android | Yes | Yes |
| Darwin | Apple native | Yes | Yes |
| Js | Browser/Node | Yes | Yes |

#### WebSocket → Coroutine Channels

```kotlin
client.webSocket(host = "xrplcluster.com", path = "/") {
    launch {
        for (frame in incoming) {
            if (frame is Frame.Text) println(frame.readText())
        }
    }
    send("""{"command":"subscribe","streams":["ledger"]}""")
}
```

`incoming`/`outgoing` are `Channel<Frame>` — integrates naturally with structured concurrency.

#### Testing — MockEngine Injection

```kotlin
class ApiClient(engine: HttpClientEngine) {
    private val client = HttpClient(engine) {
        install(ContentNegotiation) { json() }
    }
}

val mockEngine = MockEngine { request ->
    respond(
        content = """{"result":{"account_data":{"Balance":"1000000"}}}""",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}
val testClient = ApiClient(mockEngine)
```

**DX key**: Engine is a constructor parameter → testable without a mocking framework.

---

### 2.2 Apollo Kotlin — GraphQL SDK

#### Code Generation → Type-Safe Queries

```graphql
query HeroQuery($id: ID!) {
  hero(id: $id) { name, appearsIn }
}
```

Generated result:
```kotlin
class HeroQuery(val id: String) : Query<HeroQuery.Data> {
    data class Data(val hero: Hero?)
    data class Hero(val name: String, val appearsIn: List<Episode>)
}

// Usage — fully type-safe, no reflection
val response = apolloClient.query(HeroQuery(id = "1001")).execute()
println(response.data?.hero?.name)
```

#### Subscriptions → Flow

```kotlin
apolloClient
    .subscription(ReviewAddedSubscription(episode = Episode.JEDI))
    .toFlow()
    .collect { response ->
        println("New review: ${response.data?.reviewAdded?.commentary}")
    }
```

#### Interceptors: HTTP Level + GraphQL Level

```kotlin
// HTTP level (headers, auth, retry)
class AuthInterceptor(val token: String) : HttpInterceptor {
    override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain) =
        chain.proceed(request.newBuilder().addHeader("Authorization", "Bearer $token").build())
}

// GraphQL level (manipulate ApolloRequest/Response, multiple emits possible)
class LoggingInterceptor : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>, chain: ApolloInterceptorChain
    ): Flow<ApolloResponse<D>> = chain.proceed(request).onEach { response ->
        println("${request.operation.name()}: ${response.data}")
    }
}
```

**Key takeaway**: The pattern of exposing subscriptions (WebSocket) via `toFlow()`. The same pattern applies to XRPL's subscribe.

---

### 2.3 Firebase Kotlin SDK (GitLive) — KMP Reference

#### Wrapping Platform-Native SDKs with expect/actual

```kotlin
// commonMain
expect val Firebase.auth: FirebaseAuth
expect class FirebaseAuth {
    suspend fun signInWithCustomToken(token: String): AuthResult
    val currentUser: FirebaseUser?
}

// androidMain — wrapping Android Firebase SDK
actual val Firebase.auth: FirebaseAuth
    get() = FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance())

actual class FirebaseAuth(val android: com.google.firebase.auth.FirebaseAuth) {
    actual suspend fun signInWithCustomToken(token: String): AuthResult =
        android.signInWithCustomToken(token).await().let(::AuthResult)
}
```

#### Callback → suspend/Flow Conversion

```kotlin
// Single result → suspend
val result: AuthResult = Firebase.auth.signInWithCustomToken(token)

// Real-time updates → Flow (listener registration/deregistration managed automatically)
val snapshots: Flow<DocumentSnapshot> =
    db.collection("cities").document("LA").snapshots
snapshots.collect { snapshot -> println(snapshot.data<City>()) }
```

#### kotlinx.serialization Integration

```kotlin
@Serializable
data class City(val name: String, val population: Int)

// Write
db.collection("cities").document("LA").set(City("Los Angeles", 3_900_000))

// Read
val city: City = db.collection("cities").document("LA").get().data()
```

**Key takeaway**: The pattern of converting platform callbacks to `suspend`/`Flow`. The same approach applies to XRPL WebSocket listeners.

---

### 2.4 Koin — DI DSL Design

```kotlin
// Module definition — hierarchical DSL
val appModule = module {
    single<HttpClient> { buildHttpClient() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    factory<UserPresenter> { UserPresenter(get(), get()) }

    // Scope-based lifecycle
    scope<UserSession> {
        scoped<SessionCache>()
    }
}

// Module composition
val fullModule = module {
    includes(networkModule, databaseModule, appModule)
}

// Injection — property delegate
class MyActivity : KoinComponent {
    val repo: UserRepository by inject()   // lazy delegate
    val service: Service = get()           // eager
}
```

**Key takeaway**: The `by inject()` property delegate pattern. Applicable in the XRPL SDK for settings access like `var theme by settings.string()`.

---

### 2.5 kotlinx-datetime (JetBrains) — Minimal API Design

#### Intentional Omissions as Features

`LocalDateTime` arithmetic is absent — adding 1 hour during a DST "spring forward" produces a time that does not exist. The type system forces going through `Instant`:

```kotlin
// LocalDateTime + Duration → not possible! (intentional)
// Correct pattern:
val adjusted = local.toInstant(tz).plus(1, DateTimeUnit.HOUR).toLocalDateTime(tz)
```

#### Platform Bridging — Zero Overhead

```
commonMain/  ← all public APIs
jvmMain/     ← delegates to java.time (zero overhead)
jsMain/      ← js-joda + npm timezone data
nativeMain/  ← ThreeTen backport
```

```kotlin
// JVM-only extension functions (jvmMain)
val javaInstant: java.time.Instant = kotlinInstant.toJavaInstant()
val kotlinInstant: Instant = javaInstant.toKotlinInstant()
```

**Key takeaway**: "Operations with undefined behavior are excluded from the API." In the XRPL SDK as well, enforce safe explicit paths rather than dangerous convenience functions.

---

### 2.6 Multiplatform Settings — Interface + Platform Implementations

#### Settings Access via Property Delegates

```kotlin
// Property name becomes the storage key — prevents string-key errors
var count: Int by settings.int()
var username: String by settings.string(defaultValue = "Guest")
var userId: Long by settings.long(key = "user_id", defaultValue = -1L)
```

#### Testing — MapSettings (in-memory, commonMain)

```kotlin
class RepositoryTest {
    private val settings: Settings = MapSettings()  // no mocking framework needed
    private val repo = UserRepository(settings)

    @Test
    fun `stores user id`() {
        repo.saveUserId(42)
        assertEquals(42, settings.getInt("user_id", -1))
    }
}
```

**Key takeaway**: Test doubles are shipped with the library. The XRPL SDK should also include `MockXrplClient` in `commonMain`.

---

## 3. Blockchain Kotlin SDK Analysis

### 3.1 KEthereum — 70+ Modules, 100% Kotlin

#### Crypto Abstraction — Pluggable Implementations

```kotlin
// crypto_api — interface only
interface Signer {
    fun sign(transactionHash: ByteArray, privateKey: BigInteger, canonical: Boolean): ECDSASignature
    fun recover(recId: Int, sig: ECDSASignature, message: ByteArray?): BigInteger?
    fun publicFromPrivate(privateKey: BigInteger): BigInteger
}

// 3 implementation modules — selected via dependency
// crypto_impl_bouncycastle, crypto_impl_java_provider, crypto_impl_spongycastle
```

#### Transaction Signing via Extension Functions

```kotlin
// Signing = an operation on a transaction → receiver extension
fun Transaction.signViaEIP155(key: ECKeyPair, chainId: ChainId): SignatureData {
    val signatureData = key.signMessage(encodeLegacyTxRLP(SignatureData().apply { v = chainId.value }))
    return signatureData.copy(v = signatureData.v.plus(chainId.value.shl(1)).plus(valueOf(8)))
}

// Transaction analysis extensions
fun Transaction.isTokenTransfer() = input.toList().startsWith(tokenTransferSignature)
fun Transaction.getTokenTransferTo() = Address(input.toList().subList(...).toHexString())
fun Transaction.calculateHash() = encodeLegacyTxRLP().keccak()
```

#### Flow-Based Block/Transaction Streaming

```kotlin
fun getBlockFlow(rpc: EthereumRPC, delayMs: Long = 4200) = flow {
    var lastBlock: BigInteger? = null
    while (true) {
        val newBlock = rpc.blockNumber()
        if (newBlock != null && newBlock != lastBlock) {
            while (newBlock != lastBlock) {
                val block = if (lastBlock == null) newBlock else lastBlock + ONE
                rpc.getBlockByNumber(block)?.let {
                    emit(it)
                    lastBlock = block
                }
            }
        }
        delay(delayMs)
    }
}

fun getTransactionFlow(rpc: EthereumRPC) = flow {
    getBlockFlow(rpc).collect { block ->
        block.transactions.forEach { emit(it) }
    }
}
```

| What to adopt | What to avoid |
|---------------|---------------|
| Extension functions to express tx operations (`tx.sign(key)`) | `data class Transaction` with mutable `var` fields |
| 70+ independent modules — depend only on what you need | All transactions are mutable |
| `crypto_api` interface separation | Design compromises for Room DB compatibility leaking into the API |

---

### 3.2 sol4k — Solana JVM/Android

#### data class RpcException — Structured Errors

```kotlin
data class RpcException(
    val code: Int,
    override val message: String,
    val rawResponse: String,  // raw JSON included for debugging
) : RuntimeException(message)
```

`data class` exception → supports destructuring and pattern matching. `rawResponse` lets you immediately inspect the raw response on RPC failure.

#### Keypair — private constructor + companion factory

```kotlin
class Keypair private constructor(private val keypair: TweetNaclFast.Signature.KeyPair) {
    val secret: ByteArray get() = keypair.secretKey
    val publicKey: PublicKey get() = PublicKey(keypair.publicKey)

    fun sign(message: ByteArray): ByteArray =
        TweetNaclFast.Signature(ByteArray(0), secret).detached(message)

    companion object {
        @JvmStatic fun generate(): Keypair = Keypair(TweetNaclFast.Signature.keyPair())
        @JvmStatic fun fromSecretKey(secret: ByteArray): Keypair =
            Keypair(TweetNaclFast.Signature.keyPair_fromSeed(secret))
    }
}
```

| What to adopt | What to avoid |
|---------------|---------------|
| `data class RpcException(code, message, rawResponse)` | All Connection methods are synchronous (no suspend) |
| private constructor + companion factory | Risk of blocking the main thread on Android |

---

### 3.3 solana-kmp — Kotlin Multiplatform Native

#### Signer Interface — suspend + @ObjCName

```kotlin
@OptIn(ExperimentalObjCName::class)
@ObjCName("Signer")  // appears as "Signer" in Swift, not "MetaplexSigner"
interface Signer : CoreSigner {
    override val publicKey: PublicKey
    suspend fun signMessage(message: ByteArray): ByteArray  // coroutine-native
}
```

Compiles to async/await protocol in Swift, coroutine-native on JVM. True KMP.

#### Ed25519 — Delegating to diglol/crypto

```kotlin
object SolanaEddsa {
    suspend fun generateKeypair(): Keypair {
        val keypair = Ed25519.generateKeyPair()  // diglol/crypto — KMP
        return SolanaKeypair(SolanaPublicKey(keypair.publicKey), keypair.privateKey)
    }
}
```

| What to adopt | What to avoid |
|---------------|---------------|
| `@ObjCName` + `suspend fun` = true KMP interface | `TODO("Not yet implemented")` — runtime crash |
| Delegate to a proven KMP crypto library | Cannot detect unimplemented methods at compile time |

---

### 3.4 Tezos Kotlin SDK — Type-Safe State Machine

#### Enforcing Signing State with sealed interface

```kotlin
sealed interface Operation {
    val branch: BlockHash
    val contents: List<OperationContent>

    data class Unsigned(
        override val branch: BlockHash,
        override val contents: List<OperationContent>
    ) : Operation

    data class Signed(
        override val branch: BlockHash,
        override val contents: List<OperationContent>,
        val signature: Signature,
    ) : Operation {
        companion object {
            fun from(unsigned: Unsigned, signature: Signature): Signed =
                Signed(unsigned.branch, unsigned.contents, signature)
        }
    }
}
```

An unsigned transaction cannot be submitted — **guaranteed at compile time**.

```kotlin
// Signing = type transition from Unsigned → Signed
fun <T : Operation> T.signWith(key: SecretKey, tezos: Tezos = Tezos.Default): Operation.Signed
```

**This is the pattern most worth adopting in the XRPL Kotlin SDK.**

---

### 3.5 XRPL Kotlin Ecosystem Status

| Repo | Description | Status |
|------|-------------|--------|
| `XRPLF/xrpl4j` | Official Java SDK (usable from Kotlin) | Actively maintained |
| `girin-app/xrpl4j-android` | Attempted Android port of xrpl4j | Inactive |
| `nhartner/xrpl4j-android-demo` | Android demo | 2021 |

**No production-quality Kotlin/KMP XRPL SDK exists** — a clear market gap.

xrpl4j does not work out of the box on Android (BouncyCastle/Java crypto constraints).

---

### 3.6 Blockchain SDK DX Scorecard

| SDK | Best Patterns | What to Avoid |
|-----|---------------|---------------|
| **KEthereum** | Extension functions for tx operations | Mutable data class |
| **web3j** | Auto-generate type-safe wrappers from ABI | Direct use of Java `Function`/`FunctionEncoder` |
| **sol4k** | `data class RpcException` structured errors | Synchronous-only API |
| **solana-kmp** | `suspend fun` + `@ObjCName` KMP interface | `TODO()` unimplemented methods |
| **tezos-kotlin** | `sealed interface Operation { Unsigned; Signed }` | Internal DI registry leaking |
| **xrpl4j** | `@Value.Immutable` builder + type wrappers | Immutables-generated classes exposed in API |

---

## 4. High-DX Kotlin Design Patterns (Code Examples)

### 4.1 Preventing Scope Leaks with @DslMarker

#### Problem: Without @DslMarker

```kotlin
// Compiles but is semantically wrong
val tx = transaction {
    account = "rSender..."
    payment {
        amount = 1_000_000
        destination = "rReceiver..."
        account = "rOther..."  // BUG! Modifies account from the outer receiver
    }
}
```

#### Solution: Apply @DslMarker

```kotlin
@DslMarker
annotation class XrplDsl

@XrplDsl
class TransactionBuilder {
    var account: String = ""
    var fee: Long = 12
    fun payment(block: PaymentBuilder.() -> Unit) = PaymentBuilder().also(block)
}

@XrplDsl
class PaymentBuilder {
    var amount: Long = 0
    var destination: String = ""
}

// Now accessing account inside payment { } → compile error!
// Use this@transaction.account to explicitly escape if needed
```

**Ktor, Koin, and Compose all use this pattern.**

---

### 4.2 Modeling Protocol Results with Sealed Classes

#### Beyond Simple Success/Failure

```kotlin
sealed class XrplResult<out T> {
    data class Success<T>(val data: T) : XrplResult<T>()

    sealed class Failure : XrplResult<Nothing>() {
        data class RpcError(val code: String, val message: String, val rawResponse: String) : Failure()
        data class NetworkError(val cause: Throwable) : Failure()
        data class ValidationError(val field: String, val reason: String) : Failure()
        data object NotFound : Failure()
        data class TecError(val tecCode: String, val feeConsumed: XrpDrops) : Failure()
    }
}

// Compiler enforces handling all cases — no else needed
when (val result = client.submitTransaction(tx)) {
    is XrplResult.Success -> println("Hash: ${result.data}")
    is XrplResult.Failure.RpcError -> println("RPC ${result.code}: ${result.message}")
    is XrplResult.Failure.NetworkError -> retry()
    is XrplResult.Failure.ValidationError -> fixField(result.field)
    XrplResult.Failure.NotFound -> handleNotFound()
    is XrplResult.Failure.TecError -> println("Fee burned: ${result.feeConsumed}")
}
```

Adding a new subclass → all `when` sites become compile errors → **automated mistake prevention**.

#### Result vs Either vs Sealed Comparison

| | `kotlin.Result<T>` | `Arrow Either<E, T>` | Custom Sealed |
|---|---|---|---|
| Error type | `Throwable` only | Any `E` | Model directly |
| Number of cases | 2 | 2 | Unlimited |
| Loading state | No | No | Yes |
| Dependency | stdlib | Arrow library | None |
| Recommended for | Exception wrapping | Functional chaining | **Protocol modeling (recommended)** |

---

### 4.3 Value Class — Zero-Overhead Type Safety

```kotlin
@JvmInline value class Address(val value: String) {
    init { require(value.startsWith("r") && value.length in 25..34) }
}

@JvmInline value class TxHash(val value: String) {
    init { require(value.length == 64) { "TxHash must be 64 hex characters" } }
}

@JvmInline value class XrpDrops(val value: Long) {
    operator fun plus(other: XrpDrops) = XrpDrops(this.value + other.value)
    operator fun minus(other: XrpDrops) = XrpDrops(this.value - other.value)
    fun toXrp(): Double = value / 1_000_000.0
}
```

Unboxed to primitive types on JVM — no heap allocation; type safety enforced at compile time.

#### Integration with kotlinx.serialization

```kotlin
@JvmInline @Serializable
value class UserId(val value: String)

@Serializable
data class Account(val userId: UserId, val drops: XrpDrops)
// JSON: { "userId": "alice", "drops": 1000000 }  (serialized flat, without a wrapper)
```

#### Caveats
- `UserId?` (nullable) → boxing occurs
- `List<UserId>` (generic) → boxing occurs
- Fix: use sentinel values instead of nullable on hot paths

---

### 4.4 Designing the API Surface with Extension Functions

#### Scoped Extensions vs Global Extensions

```kotlin
// Scoped extension — accessible only within that builder (isolated)
fun HttpClientConfig<*>.configureLogging() {
    install(Logging) { level = LogLevel.INFO }
}
val client = HttpClient { configureLogging() }

// Global extension — accessible anywhere
fun String.toAddress(): Address = Address(this)
val addr = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh".toAddress()
```

#### Amount Literal Extensions

```kotlin
val Long.drops: XrpAmount get() = XrpAmount.ofDrops(this)
val Double.xrp: XrpAmount get() = XrpAmount.ofXrp(this)
val Int.xrp: XrpAmount get() = XrpAmount.ofXrp(this.toDouble())

// Usage
val fee = 12L.drops
val amount = 10.xrp
val total = 1_000_000L.drops + 500L.drops
```

#### Factory Functions via Companion Extensions

```kotlin
// kotlinx-datetime pattern
fun LocalDate.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDate = clock.now().toLocalDateTime(timeZone).date

// Applied to XRPL
fun Wallet.Companion.generate(algorithm: KeyAlgorithm = Ed25519): Wallet { ... }
fun Wallet.Companion.fromSeed(seed: String): Wallet { ... }

// Usage — reads like a constructor
val wallet = Wallet.generate()
val imported = Wallet.fromSeed("sEdV19BLFeQTBMASmmMZ6hCRQq5oGT2")
```

---

### 4.5 Event Streaming with Coroutine Flow

#### WebSocket → StateFlow + SharedFlow

```kotlin
class XrplWebSocketClient(private val url: String) {
    // Current state — StateFlow (retains last value, delivered immediately to new collectors)
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)
    val connectionState: StateFlow<ConnectionState> = _state.asStateFlow()

    // Event broadcast — SharedFlow (no replay, configurable buffer)
    private val _messages = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun connect(scope: CoroutineScope) = scope.launch {
        val client = HttpClient { install(WebSockets) }
        try {
            client.webSocket(url) {
                _state.value = ConnectionState.Connected
                for (frame in incoming) {
                    if (frame is Frame.Text) _messages.emit(frame.readText())
                }
            }
        } catch (e: Exception) {
            _state.value = ConnectionState.Failed(e)
        }
    }
}
```

#### Filtering Streams by Type + Auto-Reconnect

```kotlin
fun XrplWebSocketClient.ledgerStream(): Flow<LedgerEvent> =
    messages
        .filter { it.contains("\"type\":\"ledgerClosed\"") }
        .map { json.decodeFromString<LedgerEvent>(it) }
        .distinctUntilChanged { old, new -> old.ledgerIndex == new.ledgerIndex }
        .retryWhen { cause, attempt ->
            emit(LedgerEvent.Reconnecting(attempt))
            delay(minOf(1000L * (2.0.pow(attempt.toDouble())).toLong(), 30_000L))
            attempt < 5
        }
```

#### StateFlow vs SharedFlow Decision

```
StateFlow:   1 current value, delivered immediately to new collectors, requires initial value
             → Use for: connection state, auth state, configuration
SharedFlow:  N buffered events, configurable replay, no initial value required
             → Use for: incoming messages, ledger events, one-shot events
cold Flow:   lazy evaluation, executes per collector
             → Use for: paginated requests, file reads, one-off transforms
```

---

### 4.6 Pagination → Flow

#### Cursor-Based Automatic Pagination

```kotlin
fun <T, C> cursorFlow(
    initialCursor: C?,
    fetch: suspend (cursor: C?) -> Pair<List<T>, C?>
): Flow<T> = flow {
    var cursor = initialCursor
    do {
        val (items, nextCursor) = fetch(cursor)
        items.forEach { emit(it) }
        cursor = nextCursor
    } while (cursor != null)
}

// Applied to XRPL — automatic pagination for account_tx
fun XrplClient.accountTransactions(account: String): Flow<Transaction> =
    cursorFlow(initialCursor = null) { marker ->
        val response = request(AccountTxRequest(account = account, marker = marker))
        response.transactions to response.marker
    }

// Usage — cooperative cancellation with take()
client.accountTransactions("rAddress...")
    .take(50)          // fetch 50 and stop automatically
    .filter { it.type == "Payment" }
    .collect { println(it.hash) }
```

**Key point**: Without `take()`, all pages are fetched — must be documented clearly.

---

### 4.7 Configuration DSL Pattern

#### Ktor-Style Hierarchical Configuration

```kotlin
data class XrplClientConfig(
    var network: Network = Network.Mainnet,
    var timeout: Duration = 30.seconds,
    var maxRetries: Int = 3,
    var feeCushion: Double = 1.2,
    var maxFeeXrp: Double = 2.0,
    var trace: Boolean = false,
)

class XrplClient private constructor(val config: XrplClientConfig) {
    companion object {
        operator fun invoke(block: XrplClientConfig.() -> Unit = {}): XrplClient {
            val config = XrplClientConfig().apply(block)
            return XrplClient(config)
        }
    }
}

// Usage — reads like prose
val client = XrplClient {
    network = Network.Testnet
    timeout = 60.seconds
    feeCushion = 1.5
    trace = true
}
```

#### Mistake Prevention
- Do not put required parameters inside the DSL block → only discovered at runtime
- Provide `configure: Config.() -> Unit = {}` as a default → prevents forcing callers to pass an empty `{ }`
- Freeze the config object after `build()` (prevent mutable sharing)

---

### 4.8 KMP Factory Pattern

#### expect fun + Interface (Recommended)

```kotlin
// commonMain — contract only
interface CryptoProvider {
    fun sha256(data: ByteArray): ByteArray
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
}
expect fun createCryptoProvider(): CryptoProvider

// jvmMain
actual fun createCryptoProvider(): CryptoProvider = object : CryptoProvider {
    override fun sha256(data: ByteArray) = MessageDigest.getInstance("SHA-256").digest(data)
    override fun sign(message: ByteArray, privateKey: ByteArray) = bouncyCastleSign(message, privateKey)
}

// iosMain
actual fun createCryptoProvider(): CryptoProvider = object : CryptoProvider {
    override fun sha256(data: ByteArray) = CryptoKit.SHA256.hash(data)
    override fun sign(message: ByteArray, privateKey: ByteArray) = cryptoKitSign(message, privateKey)
}
```

Advantages over `expect class`:
- Multiple implementations per platform (test doubles)
- Interface can be injected via DI
- No need for `@OptIn(ExperimentalMultiplatform::class)`

#### Zero-Overhead Interop via typealias

```kotlin
// commonMain
expect class LocalDate { fun getYear(): Int; fun getMonth(): Int }

// jvmMain — used directly without a wrapper
actual typealias LocalDate = java.time.LocalDate
```

---

## 5. Rules for Writing Coroutine-Based SDKs

### 5.1 suspend vs Flow vs Deferred

```kotlin
// Single shot → suspend
suspend fun fetchUser(id: String): User

// Continuous stream → Flow (cold, structured)
fun observeUsers(): Flow<List<User>>

// Internal parallelism → async/Deferred (do not expose in public API)
private suspend fun fetchBoth(): Pair<A, B> = coroutineScope {
    val a = async { fetchA() }
    val b = async { fetchB() }
    a.await() to b.await()
}
```

### 5.2 Dispatcher Injection — Never Hardcode

```kotlin
class XrplClient(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend fun fetchLedger() = withContext(ioDispatcher) { ... }
    suspend fun decodeTx() = withContext(computeDispatcher) { ... }
}

// In tests:
val client = XrplClient(ioDispatcher = UnconfinedTestDispatcher())
```

### 5.3 Structured Concurrency

```kotlin
// Caller-bound (cancelled when the caller is cancelled)
suspend fun fetchLedgerWithTxns(seq: Long): LedgerWithTxns = coroutineScope {
    val ledger = async { api.getLedger(seq) }
    val txns = async { api.getTransactions(seq) }
    LedgerWithTxns(ledger.await(), txns.await())
}

// Work that must outlive the caller → inject an external scope
class AuditLog(private val externalScope: CoroutineScope) {
    suspend fun record(event: Event) {
        externalScope.launch { db.insert(event) }.join()
    }
}
```

### 5.4 Never Swallow CancellationException

```kotlin
// BAD
try { doWork() } catch (e: Exception) { log(e) }  // also swallows CancellationException!

// GOOD
try { doWork() }
catch (e: CancellationException) { throw e }  // always rethrow
catch (e: Exception) { log(e) }
```

---

## 6. kotlinx.serialization SDK Patterns

### 6.1 Base Configuration — Forward Compatibility

```kotlin
val sdkJson = Json {
    ignoreUnknownKeys = true     // does not break when server adds new fields
    isLenient = true             // tolerates minor format differences
    encodeDefaults = false       // fields with default values are not serialized
    explicitNulls = false        // null fields are omitted
    coerceInputValues = true     // invalid enum → converted to default value
}
```

### 6.2 Sealed Class Polymorphism

```kotlin
@Serializable
sealed class XrplTransaction {
    @Serializable @SerialName("Payment")
    data class Payment(val amount: Long, val destination: String) : XrplTransaction()

    @Serializable @SerialName("OfferCreate")
    data class OfferCreate(val takerPays: Long, val takerGets: Long) : XrplTransaction()
}
// → {"type":"Payment","amount":100,"destination":"rXXX"}
```

#### Content-Based Polymorphism Without a Discriminator

```kotlin
object TxSerializer : JsonContentPolymorphicSerializer<XrplTransaction>(XrplTransaction::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "Destination" in element.jsonObject -> XrplTransaction.Payment.serializer()
        "TakerPays" in element.jsonObject -> XrplTransaction.OfferCreate.serializer()
        else -> throw SerializationException("Unknown tx type")
    }
}
```

### 6.3 Surrogate Pattern (Serializing Third-Party Classes)

```kotlin
@Serializable @SerialName("XrpAmount")
private class XrpAmountSurrogate(val drops: Long)

object XrpAmountSerializer : KSerializer<XrpAmount> {
    override val descriptor = XrpAmountSurrogate.serializer().descriptor
    override fun serialize(encoder: Encoder, value: XrpAmount) =
        encoder.encodeSerializableValue(XrpAmountSurrogate.serializer(), XrpAmountSurrogate(value.drops))
    override fun deserialize(decoder: Decoder): XrpAmount =
        XrpAmount.ofDrops(decoder.decodeSerializableValue(XrpAmountSurrogate.serializer()).drops)
}
```

---

## 7. Blockchain SDK-Specific Requirements

### 7.1 Transaction Lifecycle — Enforced by Types

```kotlin
// Applying the tezos-kotlin-sdk pattern
sealed interface XrplTransaction {
    val account: Address
    val transactionType: TransactionType

    data class Unsigned(
        override val account: Address,
        override val transactionType: TransactionType,
        val fields: Map<String, Any>,
        val fee: XrpDrops? = null,          // subject to autofill
        val sequence: UInt? = null,          // subject to autofill
        val lastLedgerSequence: UInt? = null // subject to autofill
    ) : XrplTransaction

    data class Filled(
        override val account: Address,
        override val transactionType: TransactionType,
        val fields: Map<String, Any>,
        val fee: XrpDrops,
        val sequence: UInt,
        val lastLedgerSequence: UInt
    ) : XrplTransaction

    data class Signed(
        override val account: Address,
        override val transactionType: TransactionType,
        val txBlob: String,
        val hash: TxHash
    ) : XrplTransaction
}

// The type system enforces the workflow:
// Unsigned → (autofill) → Filled → (sign) → Signed → (submit)
suspend fun XrplClient.autofill(tx: XrplTransaction.Unsigned): XrplTransaction.Filled
suspend fun Wallet.sign(tx: XrplTransaction.Filled): XrplTransaction.Signed
suspend fun XrplClient.submit(tx: XrplTransaction.Signed): SubmitResult
```

### 7.2 Key Management — HSM Abstraction

```kotlin
// Adopting the SignatureService pattern from xrpl4j
interface TransactionSigner<K : PrivateKeyable> {
    fun sign(key: K, transaction: XrplTransaction.Filled): XrplTransaction.Signed
    fun multiSign(key: K, transaction: XrplTransaction.Filled): SingleSignature
}

// In-memory implementation (for development)
class InMemorySigner : TransactionSigner<PrivateKey> { ... }

// HSM implementation (for production)
class HsmSigner : TransactionSigner<PrivateKeyReference> { ... }
```

### 7.3 Network Abstraction

```kotlin
sealed class Network(val rpcUrl: String, val wsUrl: String) {
    object Mainnet : Network("https://xrplcluster.com", "wss://xrplcluster.com")
    object Testnet : Network("https://s.altnet.rippletest.net:51234", "wss://s.altnet.rippletest.net:51233")
    object Devnet : Network("https://s.devnet.rippletest.net:51234", "wss://s.devnet.rippletest.net:51233")
    data class Custom(val rpc: String, val ws: String) : Network(rpc, ws)
}
```

### 7.4 Dual Transport (HTTP + WebSocket)

```kotlin
// One-off queries → HTTP JSON-RPC
suspend fun accountInfo(address: Address): AccountInfo

// Subscriptions/streaming → WebSocket
fun subscribeToLedger(): Flow<LedgerEvent>

// Both from the same client:
val client = XrplClient {
    network = Network.Testnet
    transport = Transport.Both  // use both HTTP + WebSocket
    // transport = Transport.HttpOnly  // also works without WebSocket
}
```

---

## 8. Deployment / Testing Strategy

### 8.1 Maven Central Publishing

```kotlin
plugins {
    id("com.vanniktech.maven.publish") version "0.29.0"
}
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.yourorg", "xrpl-kotlin", "1.0.0")
}
```

### 8.2 GitHub Actions CI/CD

```yaml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: macos-latest  # Xcode required for iOS targets
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - uses: gradle/actions/setup-gradle@v3
      - run: ./gradlew check --no-configuration-cache
  publish:
    if: startsWith(github.ref, 'refs/tags/v')
    needs: test
    runs-on: macos-latest
    steps:
      - run: ./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
```

### 8.3 Testing Strategy

| Level | Tool | Target |
|-------|------|--------|
| **Unit** | Kotest FunSpec + runTest | Codecs, addresses, key derivation |
| **Property-based** | Kotest Arb + checkAll | Encode/decode round-trips, sign/verify round-trips |
| **Golden file** | Static hex file comparison | Binary codec regression |
| **Integration** | Testnet + CI environment variables | RPC calls, transaction submission |
| **Mocking** | Ktor MockEngine | Network-isolated tests |

```kotlin
// Property-based test example
class BinaryCodecPropertyTest : StringSpec({
    "encode → decode identity" {
        checkAll(Arb.long(1L..100_000_000_000L)) { drops ->
            val amount = XrpAmount.ofDrops(drops)
            val encoded = BinaryCodec.encode(amount)
            BinaryCodec.decode(encoded) shouldBe amount
        }
    }
})

// MockEngine test example
val mockEngine = MockEngine { request ->
    respond("""{"result":{"account_data":{"Balance":"1000000"}}}""")
}
val client = XrplClient(network = Network.Custom("http://mock", ""), httpEngine = mockEngine)
```

### 8.4 Documentation (Dokka)

```kotlin
/**
 * Submits a signed transaction to the XRP Ledger and waits for validation.
 *
 * @param tx An [XrplTransaction.Signed] produced by [TransactionSigner.sign].
 * @param timeout Maximum time to wait for validation. Default 30 seconds.
 * @return [XrplResult.Success] with the transaction hash, or an [XrplResult.Failure] subtype.
 * @throws IllegalStateException if the client is not connected.
 * @sample io.xrpl.samples.submitPayment
 */
suspend fun submitAndWait(
    tx: XrplTransaction.Signed,
    timeout: Duration = 30.seconds
): XrplResult<ValidatedTransaction>
```

---

## 9. Target DX — End-User Code Example

```kotlin
// 1. Configure client
val client = XrplClient {
    network = Network.Testnet
    feeCushion = 1.2
}

// 2. Generate/restore wallet
val wallet = Wallet.generate()
client.fundWallet(wallet)  // testnet faucet

// 3. Build transaction — DSL
val tx = payment {
    account = wallet.address
    destination = "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe".toAddress()
    amount = 10.xrp
    memo = "Invoice #42"
}

// 4. One-stop submission
when (val result = client.submitAndWait(tx, wallet)) {
    is XrplResult.Success -> println("Validated in ledger ${result.data.ledgerIndex}")
    is XrplResult.Failure.TecError -> println("Fee burned: ${result.feeConsumed}")
    is XrplResult.Failure.RpcError -> println("${result.code}: ${result.message}")
    is XrplResult.Failure.NetworkError -> retry()
    is XrplResult.Failure.ValidationError -> fixField(result.field)
    XrplResult.Failure.NotFound -> handleNotFound()
}

// 5. Real-time subscription — Flow
client.subscribeToLedger().collect { event ->
    println("New ledger: ${event.ledgerIndex}")
}

// 6. Automatic pagination — Flow
client.accountTransactions(wallet.address)
    .take(20)
    .filter { it.type == "Payment" }
    .collect { println(it.hash) }

// 7. Balance query
val balance = client.getXrpBalance(wallet.address)
println("Balance: ${balance.toXrp()} XRP")
```

---

## 10. Summary of Key Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | **Separation of 3 concerns**: signing / transaction / network | xrpl4j, Ktor architecture |
| 2 | **Model all results with sealed classes** | Compiler-enforced exhaustive handling |
| 3 | **Transaction state as types**: Unsigned → Filled → Signed | tezos-kotlin-sdk pattern |
| 4 | **KMP from day one** | solana-kmp, Firebase Kotlin SDK |
| 5 | **kotlinx.serialization exclusively** | Only JSON library compatible with KMP |
| 6 | **Ktor client as base** | HTTP + WebSocket, MockEngine, KMP |
| 7 | **KEthereum-style modularization** | BOM + depend only on needed modules |
| 8 | **@DslMarker builders** | Proven pattern from Ktor, Koin, Compose |
| 9 | **value class type wrappers** | Zero-overhead type safety |
| 10 | **Property-based testing + golden files** | Prevent codec regressions |
| 11 | **Binary Compatibility Validator** | Prevent accidental compatibility breaks |
| 12 | **HSM abstraction (SignatureService)** | Production-proven pattern from xrpl4j |

---

## References

### Kotlin Official
- [Kotlin API Guidelines](https://kotlinlang.org/docs/api-guidelines-introduction.html)
- [Type-Safe Builders / @DslMarker](https://kotlinlang.org/docs/type-safe-builders.html)
- [Coroutine Context and Dispatchers](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)
- [Expected and Actual Declarations](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-expect-actual.html)
- [Android Coroutines Best Practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)

### SDK References
- [Ktor Client](https://ktor.io/docs/client-create-and-configure.html) — DSL, plugins, engines
- [Apollo Kotlin](https://www.apollographql.com/docs/kotlin) — code generation, subscriptions, cache
- [Firebase Kotlin SDK](https://github.com/GitLive/firebase-kotlin-sdk) — KMP expect/actual
- [Koin DSL](https://insert-koin.io/docs/reference/koin-core/dsl) — module/scope definitions
- [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) — minimal API design
- [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings) — interface + platform implementations

### Blockchain SDKs
- [KEthereum](https://github.com/komputing/KEthereum) — 70+ modules, pluggable crypto
- [sol4k](https://github.com/sol4k/sol4k) — JVM/Android Solana
- [solana-kmp](https://github.com/metaplex-foundation/solana-kmp) — KMP Solana
- [tezos-kotlin-sdk](https://github.com/airgap-it/tezos-kotlin-sdk) — sealed Operation pattern
- [web3j](https://github.com/LFDT-web3j/web3j) — code generation
- [xrpl4j](https://github.com/XRPLF/xrpl4j) — official Java XRPL SDK

### Error Handling / Serialization
- [Arrow Typed Errors](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/)
- [kotlinx.serialization Serializers Guide](https://android.googlesource.com/platform/external/kotlinx.serialization/+/HEAD/docs/serializers.md)
- [Azure SDK Guidelines](https://azure.github.io/azure-sdk/general_introduction.html)

### Deployment / Testing
- [vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
- [Kotest Property-Based Testing](https://kotest.io/docs/proptest/property-based-testing.html)
- [Binary Compatibility Validator](https://github.com/Kotlin/binary-compatibility-validator)
- [Dokka](https://github.com/Kotlin/dokka)
- [cryptography-kotlin](https://github.com/whyoleg/cryptography-kotlin) — KMP crypto
