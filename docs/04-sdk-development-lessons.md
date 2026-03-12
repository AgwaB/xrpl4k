# XRPL Kotlin SDK — SDK Development Lessons

> Last updated: 2026-03-12
> Purpose: What to know, what to do, and what NOT to do when building a Kotlin SDK.
> Synthesized from real-world post-mortems, major SDK teams (Stripe, AWS, Twilio, Firebase), and Kotlin-specific research.

---

## Table of Contents

1. [SDK Design Anti-Patterns](#1-sdk-design-anti-patterns)
2. [Kotlin-Specific Pitfalls](#2-kotlin-specific-pitfalls)
3. [Developer Experience (DX)](#3-developer-experience-dx)
4. [Release & Distribution](#4-release--distribution)
5. [Checklist: Before You Ship](#5-checklist-before-you-ship)

---

## 1. SDK Design Anti-Patterns

### 1.1 Architectural Mistakes

#### God Client — Monolithic Class

**Mistake**: Cramming all functionality into a single massive client class. IDE autocompletion returns hundreds of unrelated methods.

**Impact**: Hard to navigate, test, and extend. Adding functionality means modifying one ever-growing file.

**Do instead**: Use resource namespacing. Group methods into logical sub-clients.

```kotlin
// BAD — god client
client.accountInfo(...)
client.submitTransaction(...)
client.subscribeToLedger(...)
client.generateWallet(...)
client.encodeTransaction(...)

// GOOD — namespaced sub-clients
client.accounts.info(address)
client.transactions.submit(tx)
client.subscriptions.ledger()
// or: keep flat but split across extension files
```

**Reference**: Stripe and Twilio SDKs use nested singletons for logical grouping.

---

#### Global Mutable State / Singleton Init

**Mistake**: Requiring `SomeSDK.configure(apiKey: "...")` as a global singleton that affects the entire process.

**Impact**:
- Impossible to unit test (can't reset state between tests)
- Breaks multi-tenancy (can't have two clients with different credentials)
- Thread safety becomes a nightmare

**Do instead**: Accept configuration in the constructor. Return a client instance. Let users create multiple instances.

```kotlin
// BAD — global state
XrplSDK.initialize(network = Network.Testnet)
val info = XrplSDK.accountInfo(address)  // which client? which config?

// GOOD — instance-based
val client = XrplClient { network = Network.Testnet }
val info = client.accountInfo(address)

// Multiple clients with different configs — trivial
val mainnet = XrplClient { network = Network.Mainnet }
val testnet = XrplClient { network = Network.Testnet }
```

---

#### Leaky Abstractions

**Mistake**: Exposing internal implementation details in the public API — database column names, internal service names, legacy format constraints.

**Impact**: Consumers learn two mental models (their domain + your internals). Any internal refactoring becomes a breaking change.

**Do instead**: Design from the consumer's perspective. Never auto-generate public APIs from internal schemas without a transformation layer.

```kotlin
// BAD — internal structure leaks
data class AccountResponse(
    val acct_root: JsonObject,      // internal DB field name
    val ledger_hash_hex: String,    // encoding detail in name
    val _meta_internal_flags: Int,  // internal flags exposed
)

// GOOD — consumer-facing domain model
class AccountInfo(
    val account: Address,
    val balance: XrpDrops,
    val sequence: UInt,
    val ownerCount: UInt,
)
```

---

#### Building on a Too-Narrow Foundation

**Mistake**: Designing around the simplest use case, then retrofitting complex cases onto that foundation.

**Case study — Stripe**: Originally designed around credit cards (simple charge/response). When redirect-based payment methods appeared (iDEAL, SEPA, Klarna), they tried extending the `Charge` resource. The `Charge` object grew from 11 to 36 properties. Eventually locked five engineers in a room for three months to redesign from scratch with `PaymentIntents`.

**Lesson for XRPL SDK**: Design the transaction model to handle ALL transaction types from the start — not just Payment. The sealed class hierarchy (`Unsigned → Filled → Signed`) must accommodate 68+ transaction types, including future ones.

---

### 1.2 API Surface Anti-Patterns

#### Exceptions for Control Flow

**Mistake**: Throwing exceptions for non-exceptional conditions (resource not found, validation failure, rate limiting).

**Impact**: Consumer code becomes littered with try/catch blocks. Static analysis misses control paths. Expensive stack unwinding for expected conditions.

**Do instead**: Return sealed result types for expected failures. Reserve exceptions for programming errors only.

```kotlin
// BAD — exception for expected condition
fun getAccount(address: Address): AccountInfo  // throws NotFoundException

// GOOD — sealed result
fun getAccount(address: Address): XrplResult<AccountInfo>
// XrplResult.Failure.NotFound is just a data object, not an exception
```

---

#### Flat Exception Hierarchy

**Mistake**: All SDK errors thrown as a single `SDKException` type.

**Impact**: Consumers parse error message strings to differentiate errors — which Hyrum's Law then locks in as contracts.

**Do instead**: Build a typed error hierarchy (or sealed class in Kotlin).

```kotlin
// BAD — all errors are the same type
throw XrplException("Rate limit exceeded")
throw XrplException("Account not found")
throw XrplException("Network timeout")
// Consumer: if (e.message.contains("Rate limit")) — fragile!

// GOOD — typed hierarchy (already in our conventions)
sealed class Failure {
    data class RpcError(val code: String, val message: String, val rawResponse: String)
    data class NetworkError(val cause: Throwable)
    data class TecError(val tecCode: String, val feeConsumed: XrpDrops)
    data object NotFound
}
```

---

#### No Built-In Pagination

**Mistake**: Requiring consumers to manually implement loop-and-fetch for paginated endpoints.

**Impact**: Every consumer reinvents the same boilerplate. Rate limits get hit. Partial state corruption from network errors mid-sequence.

**Do instead**: Provide auto-pagination via Flow (already in our conventions).

```kotlin
// Consumers just collect — pagination is transparent
client.accountTransactions(address)
    .take(50)
    .filter { it.type == "Payment" }
    .collect { println(it.hash) }
```

---

#### No Retry Logic

**Mistake**: Making exactly one attempt per operation, leaving retry logic to consumers.

**Impact**: Network transience causes unnecessary failures. Every consumer reinvents exponential backoff, often incorrectly.

**Do instead**: Build in configurable retry with exponential backoff + jitter for idempotent operations.

```kotlin
val client = XrplClient {
    retry {
        maxAttempts = 3
        initialDelay = 1.seconds
        maxDelay = 30.seconds
        retryOn(NetworkError::class)
        retryOn(RpcError::class) { it.code == "slowDown" }
    }
}
```

---

### 1.3 Backward Compatibility

#### Hyrum's Law

> "With a sufficient number of users, all observable behaviors of your system will be depended on by somebody."

**Non-obvious breaking changes that have happened in real SDKs:**

| Change | Why It Broke Consumers |
|--------|----------------------|
| Error message wording change | Consumers parsing error strings |
| JSON key order change | Consumers using fixed-offset extraction |
| Performance degradation | Downstream timeout cascading |
| Callback timing (sync→async) | Consumers depending on synchronous execution ordering |
| `error.code` → `error.name` rename (AWS SDK v3) | Every error handling block broke |

**Mitigation for XRPL SDK**:
- Use sealed result types (not string parsing)
- Use Binary Compatibility Validator in CI
- Treat error message format changes as breaking
- Document all intended behaviors explicitly

---

#### The Migration Burden

**Case study — AWS SDK JS v2→v3**: Required restructuring all method calls (command pattern), import system changes (CJS→ESM), S3 upload moved to a separate module, error property renaming. Multiple weeks of migration per service. Documentation was "effectively a type reference, no more."

**Lesson**: When making breaking changes:
1. Provide automated migration tooling
2. Create compatibility shims for the transition period
3. Write the migration guide BEFORE releasing the new version
4. Support old + new versions in parallel during transition

---

### 1.4 Dependency Management

#### Exposing Transitive Dependencies

**Mistake**: Letting types from your dependencies appear in public method signatures.

```kotlin
// BAD — OkHttp type in public API
fun getHttpClient(): OkHttpClient  // consumer now depends on OkHttp

// GOOD — own interface
fun getHttpClient(): HttpClientEngine  // internal impl can be swapped
```

**Impact**: You're coupled to your dependency's API. Version conflicts become your users' problem.

---

#### Excessive Dependencies

**Mistake**: Pulling in heavy frameworks for convenience.

**Rule for XRPL SDK**:
- `xrpl-core`: stdlib + kotlinx-serialization only
- `xrpl-crypto`: + cryptography-kotlin only
- `xrpl-client`: + ktor-client + kotlinx-coroutines only
- Never depend on Spring, Guice, or other DI/lifecycle frameworks

---

### 1.5 Lessons from Major SDK Teams

| Team | Key Lesson |
|------|------------|
| **Stripe** | First-principles redesign beats incremental patching. Separating static data (PaymentMethods) from transaction data (PaymentIntents) created a clean state machine. |
| **Auth0** | SDKs are the first line of security guidance. Make the secure path the path of least resistance. Dogfood internally before external release. |
| **AWS JS SDK** | Never rename error properties in a minor version. Releasing with worse docs than the predecessor kills migration momentum. |
| **Twilio** | Completion rate of tutorials is 30% higher if the first code example is under 20 lines. Out-of-sync docs are perceived as "broken" even when code is correct. |
| **Google (Hyrum's Law)** | Even fixing bugs can break consumers. Use chaos mocks to randomize non-contractual behaviors so consumers build resilient integrations. |

---

## 2. Kotlin-Specific Pitfalls

### 2.1 KMP (Kotlin Multiplatform) Gotchas

#### expect/actual Pitfalls

```kotlin
// WRONG — expect classes are still Beta, require opt-in
expect class CryptoProvider { ... }

// CORRECT — use expect fun + interface
expect fun platformCryptoProvider(): CryptoProvider
```

**Extra enum constants in `actual` break exhaustive `when`:**

```kotlin
// commonMain
expect enum class Department { IT, HR, Sales }

// jvmMain — adds Legal
actual enum class Department { IT, HR, Sales, Legal }

// common code — COMPILE ERROR without else
when (dept) {
    Department.IT -> ...
    Department.HR -> ...
    Department.Sales -> ...
    // Legal is unhandled!
}
```

**Always add `else` in `when` over platform enums.**

---

#### Kotlin/Native Issues

| Issue | Impact | Mitigation |
|-------|--------|------------|
| GC stop-the-world pauses | Unpredictable pauses in background threads | Minimize allocations in hot paths |
| `Dispatchers.Main` not available in tests | Crash in unit tests | Mock with `StandardTestDispatcher()` |
| String encoding overhead (UTF-16) | Conversion cost at every cinterop boundary | Benchmark and document |
| Legacy `freeze()` in older libraries | Crashes with new memory model | Audit and update dependencies |

---

#### Kotlin/JS Limitations

| Limitation | Impact | Workaround |
|------------|--------|------------|
| `suspend fun` not callable from JS | JS consumers can't use coroutines | Wrap in `Promise`: `GlobalScope.promise { fetchData() }` |
| `CoroutineScope` not exportable | Can't pass scope to JS | Create adapter classes |
| `Long` becomes `BigInt` in JS | Numeric precision issues | Use `Double` or `String` in `@JsExport` APIs |
| `List` not exported | Collections unusable from JS | Use `Array` in export-facing APIs |
| Polymorphic serialization needs explicit serializer | `Json.encodeToString(data)` only works on JVM | Use `PolymorphicSerializer(...)` explicitly |

---

### 2.2 Coroutine Pitfalls for Library Authors

#### Dispatcher Hardcoding

```kotlin
// BAD — impossible to override in tests
class MyClient {
    suspend fun fetch() = withContext(Dispatchers.IO) { ... }
}

// GOOD — injected with sensible default
class MyClient(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun fetch() = withContext(ioDispatcher) { ... }
}
```

**Never hardcode `Dispatchers.Main`** — may not exist on all KMP targets.

---

#### Structured Concurrency Violations

```kotlin
// BAD — breaks parent-child cancellation chain
suspend fun fetchPosts() = withContext(Job()) { ... }

// GOOD
suspend fun fetchPosts() = coroutineScope { ... }
```

```kotlin
// BAD — GlobalScope leaks
GlobalScope.launch { ... }

// GOOD — accept injected scope
class SdkClient(private val scope: CoroutineScope) {
    fun startBackgroundWork() = scope.launch { ... }
}
```

---

#### Flow Mistakes

```kotlin
// BAD — suspend fun returning Flow (confusing execution timing)
suspend fun getEvents(): Flow<Event>  // suspension happens before Flow exists

// GOOD — regular fun returning Flow (cold by default)
fun getEvents(): Flow<Event>
```

```kotlin
// BAD — withContext inside flow builder
flow {
    withContext(Dispatchers.IO) { emit(fetchData()) }  // IllegalStateException!
}

// GOOD — flowOn at the end
flow {
    emit(fetchData())
}.flowOn(Dispatchers.IO)
```

---

#### Testing Coroutines

```kotlin
// BAD — runBlocking in tests
@Test fun test() = runBlocking { ... }  // blocks thread, no virtual time

// GOOD — runTest with virtual time
@Test fun test() = runTest {
    val client = XrplClient(ioDispatcher = UnconfinedTestDispatcher(testScheduler))
    // advanceTimeBy(), advanceUntilIdle() available
}
```

**All `TestDispatcher` instances in a test must share the same `TestCoroutineScheduler`.**

---

### 2.3 Java Interop

If the SDK needs to be called from Java, these issues must be addressed:

#### suspend fun from Java

```kotlin
// Kotlin
suspend fun fetchUser(id: String): User

// Java sees: Object fetchUser(String id, Continuation<? super User> $completion)
// Unusable from Java!

// Solution: provide Java-friendly bridge
@JvmStatic
fun fetchUserAsync(id: String): CompletableFuture<User> =
    GlobalScope.future { fetchUser(id) }
```

#### Value Class Name Mangling

```kotlin
@JvmInline value class UserId(val raw: String)

fun getUser(id: UserId): User
// Java sees: getUser-XXXXX(String) — mangled, uncallable!

// Solution (Kotlin 2.1+):
@JvmExposeBoxed
@JvmInline value class UserId(val raw: String)
// Java can now: new UserId("123")
```

#### `internal` Leaks to Java

Kotlin's `internal` compiles to JVM `public` with name mangling. Java can still call it.

```kotlin
// Hide from Java completely:
@JvmSynthetic
internal fun sensitiveHelper()
```

#### Other Interop Annotations

| Annotation | Purpose |
|------------|---------|
| `@JvmStatic` | Make companion object members callable as `MyClass.method()` from Java |
| `@JvmOverloads` | Generate Java overloads for default parameters |
| `@JvmName` | Rename a function for Java callers (resolve clashes) |
| `@Throws` | Declare checked exceptions for Java |
| `@JvmSuppressWildcards` | Remove `? extends` wildcards from Java signatures |

**Decision for XRPL SDK**: If Java interop is a non-goal (Kotlin-first SDK), these can be deferred. If Java callers are expected, add interop annotations from day one — retrofitting is painful.

---

### 2.4 kotlinx.serialization Edge Cases

| Issue | Impact | Mitigation |
|-------|--------|------------|
| Open class polymorphism requires manual `SerializersModule` registration | Missing subclass = runtime `SerializationException` | Use sealed classes (auto-registered) |
| Single `classDiscriminator` per `Json` instance | Can't handle mixed `"type"` / `"TransactionType"` discriminators | Use content-based polymorphic serializer |
| JS/Native need explicit serializer for polymorphism | `Json.encodeToString(data)` only works on JVM (reflection) | Always pass explicit serializer |
| ProGuard/R8 strips serializer companion | Runtime crash on Android release builds | Ship `consumer-rules.pro` |
| `@Required` + default value interaction | Deserialization throws even with default when field is missing | Avoid `@Required` in versioned protocols |
| `KClass.serializer()` allocates on every call | Performance trap in hot paths | Cache serializers |

**ProGuard rules to ship with the SDK:**

```proguard
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { static **$$serializer INSTANCE; }
-keep @kotlinx.serialization.Serializable class * { static ** Companion; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    static *** serializer(...);
}
```

---

### 2.5 Binary Compatibility Traps

| Change | Looks Safe? | Actually Breaks? |
|--------|-------------|-------------------|
| Adding default parameter to existing function | Yes (source-compatible) | **Yes** (JVM signature changes) |
| Narrowing return type (`Collection` → `List`) | Yes (more specific) | **Yes** (bytecode `invokevirtual` mismatch) |
| Adding property to public `data class` | Seems additive | **Yes** (`copy()`, `componentN()` change) |
| Changing `inline fun` body | Internal change | **Yes** (body copied into callers at compile time) |
| Changing `@PublishedApi internal` function | Marked internal | **Yes** (effectively public via inline) |
| Adding sealed class subclass | Additive | **Source-breaking** (exhaustive `when` without `else`) |

**Essential tooling:**

```kotlin
// Explicit API mode — forces explicit visibility and return types
kotlin { explicitApi() }

// Binary Compatibility Validator — catches ABI changes in CI
plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
}
```

---

## 3. Developer Experience (DX)

### 3.1 First-5-Minutes Experience

**Goal**: A developer should go from zero to a working API call in under 5 minutes.

**Principles**:
- Zero configuration beyond credentials for a basic call
- Copy-paste runnable quickstart examples
- One package import for the common case
- Show a working result, not just "you're configured"

**Target quickstart for XRPL SDK:**

```kotlin
// 1. Add dependency (1 line in build.gradle.kts)
// implementation("org.xrpl:xrpl-kotlin:1.0.0")

// 2. Create client (1 line)
val client = XrplClient { network = Network.Testnet }

// 3. Make first call (1 line)
val balance = client.getXrpBalance("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh".toAddress())
println("Balance: ${balance.toXrp()} XRP")

// Total: 3 lines of actual code. Under 5 minutes.
```

---

### 3.2 Error Message Design

**Format**: `[What went wrong]. [Actual value]. [How to fix it]. [Doc link].`

```kotlin
// BAD
"Invalid input"
"Error occurred"
"Authentication failed"

// GOOD
"Address must start with 'r' or 'X', got: 'abc...'. Use Address(\"r...\") format."
"Fee exceeds maxFeeXrp (2.0 XRP). Actual fee: 5.2 XRP. Set maxFeeXrp in client config."
"Connection to wss://s.altnet.rippletest.net:51233 timed out after 30s. Check network or increase timeout."
```

**Stripe's model**: Error responses include `type`, `code`, `message`, `param`, and `doc_url`. Every error links to a specific troubleshooting page.

---

### 3.3 Logging & Debugging

**Requirements**:

| Feature | Default | Configurable |
|---------|---------|--------------|
| Log level | ERROR only | Environment variable `XRPL_SDK_LOG_LEVEL=debug` |
| HTTP request/response logging | OFF | `trace = true` in config |
| Request IDs | Always attached | Surfaced in error objects |
| Retry logging | OFF | Enabled with debug logging |

```kotlin
val client = XrplClient {
    network = Network.Testnet
    trace = true  // logs all requests/responses
}
```

**Never log**: API keys, seeds, private keys, secret values at ANY level.

---

### 3.4 Configuration Design

**Three-tier cascade (lowest to highest priority)**:
1. SDK defaults (safe for production)
2. Programmatic config (DSL block)
3. Environment variables (always win)

**Sensible defaults out of the box**:

| Setting | Default | Rationale |
|---------|---------|-----------|
| Network | Mainnet | Production-safe default |
| Timeout | 30 seconds | Reasonable for most operations |
| Max retries | 3 | With exponential backoff + jitter |
| Fee cushion | 1.2 | 20% buffer on estimated fee |
| Max fee | 2.0 XRP | Safety cap on fees |

**Validate config at init time**, not at first API call:

```kotlin
// Fails immediately with clear message
val client = XrplClient {
    network = Network.Custom("not-a-url", "also-not-a-url")
}
// → ConfigurationError: 'rpcUrl' is not a valid URL. Got: "not-a-url" (missing scheme).
//   Try: "https://my-rippled-server:51234"
```

---

### 3.5 Documentation Strategy

**The Diátaxis Framework** — four distinct content types:

| Type | Purpose | Reader State | XRPL SDK Example |
|------|---------|-------------|-----------------|
| **Tutorial** | Learning experience | "I want to learn" | "Build your first payment" |
| **How-to Guide** | Solve a specific problem | "I need to do X" | "How to paginate account transactions" |
| **Reference** | Technical facts | "I need a detail" | API method signatures, parameters |
| **Explanation** | Conceptual background | "I want to understand" | "How XRPL transaction finality works" |

**Minimum documentation set**:
1. README with quickstart (under 20 lines of code)
2. Installation guide per platform (JVM, Android, iOS)
3. How-to guides for common tasks
4. API reference (auto-generated via Dokka)
5. Error code reference with causes and fixes
6. Changelog (Conventional Commits → auto-generated)
7. Migration guides for major versions

**Doc testing**: Run all code examples in CI. Outdated examples in docs are a leading cause of trust erosion.

---

### 3.6 Thread Safety

**Promise explicitly**: "All XrplClient instances are thread-safe and independent of each other."

This means:
- Internal state uses `AtomicReference`, `ConcurrentHashMap`, or `Mutex`
- Connection pools are thread-safe
- No mutable state is shared between client instances
- Document this guarantee in KDoc

---

## 4. Release & Distribution

### 4.1 Maven Central Publishing (Current — Central Portal)

**OSSRH was shut down June 30, 2025.** Use Sonatype Central Portal only.

**Recommended plugin**: `com.vanniktech.maven.publish` (v0.36.0+)

```kotlin
plugins {
    id("com.vanniktech.maven.publish") version "0.36.0"
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("org.xrpl", "xrpl-kotlin-core", "1.0.0")

    pom {
        name = "XRPL Kotlin SDK — Core"
        description = "Core types and models for XRPL Kotlin SDK"
        url = "https://github.com/..."
        licenses { license { name = "Apache-2.0" } }
        developers { developer { id = "..."; name = "..." } }
        scm { url = "https://github.com/..." }
    }
}
```

**CI secrets needed**:
```
ORG_GRADLE_PROJECT_mavenCentralUsername
ORG_GRADLE_PROJECT_mavenCentralPassword
ORG_GRADLE_PROJECT_signingInMemoryKey
ORG_GRADLE_PROJECT_signingInMemoryKeyId
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
```

**KMP caveat**: Apple targets (iOS, macOS) must compile on macOS. Use `macos-latest` runner for publish jobs.

---

### 4.2 CI/CD Pipeline

#### Per-PR (fast feedback)

```yaml
on: [pull_request]
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: gradle/actions/setup-gradle@v4
        with:
          cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}
      - run: ./gradlew check apiCheck ktlintCheck

  ios-test:
    runs-on: macos-latest
    if: contains(github.event.pull_request.labels.*.name, 'ios')
    steps:
      - run: ./gradlew iosSimulatorArm64Test
```

#### Release (tag-triggered)

```yaml
on:
  push:
    tags: ['v[0-9]+.[0-9]+.[0-9]+']
jobs:
  publish:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: zulu, java-version: 21 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
```

---

### 4.3 Gradle Build Optimization

```properties
# gradle.properties
org.gradle.configuration-cache=true
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
```

**Convention plugins over buildSrc**: Changes in `buildSrc` invalidate the entire build. Use `build-logic/` as a composite build instead.

```
build-logic/
├── settings.gradle.kts
└── convention/
    └── src/main/kotlin/
        ├── KmpLibraryPlugin.kt
        └── PublishingPlugin.kt
```

---

### 4.4 BOM (Bill of Materials)

```kotlin
// xrpl-bom/build.gradle.kts
plugins { `java-platform` }

dependencies {
    constraints {
        api("org.xrpl:xrpl-kotlin-core:${project.version}")
        api("org.xrpl:xrpl-kotlin-crypto:${project.version}")
        api("org.xrpl:xrpl-kotlin-client:${project.version}")
        api("org.xrpl:xrpl-kotlin-codec:${project.version}")
    }
}
```

Consumer usage:
```kotlin
dependencies {
    implementation(platform("org.xrpl:xrpl-kotlin-bom:1.0.0"))
    implementation("org.xrpl:xrpl-kotlin-core")      // no version
    implementation("org.xrpl:xrpl-kotlin-client")     // no version
}
```

---

### 4.5 Security Practices

| Practice | Tool / Method |
|----------|---------------|
| Gradle wrapper validation | `gradle/actions/setup-gradle@v4` (automatic) |
| Dependency verification | `./gradlew --write-verification-metadata sha256,pgp` |
| Dependency locking | `./gradlew dependencies --write-locks` |
| Vulnerability scanning | Dependabot or Renovate |
| SBOM generation | `org.cyclonedx.bom` Gradle plugin |
| Artifact signing | GPG via in-memory key in CI |
| ProGuard consumer rules | Ship `consumer-rules.pro` with Android artifacts |

---

### 4.6 Versioning & Changelog

**Version scheme**:
```
1.0.0-SNAPSHOT    ← continuous development
1.0.0-alpha01     ← feature-complete, rough
1.0.0-beta01      ← stabilizing
1.0.0-rc01        ← release candidate
1.0.0             ← GA release
```

**Changelog automation**: Conventional Commits → semantic-release or manual tag + auto-generated release notes.

**Deprecation lifecycle**:
1. `WARNING` — at least 1 minor release
2. `ERROR` — next minor release
3. `HIDDEN` / removed — major release only

**LTS policy** (document in `SUPPORT.md`):

| Version | Status | End of Support |
|---------|--------|----------------|
| 2.x | Active | TBD |
| 1.x | Maintenance (security only) | 2028-12-31 |

---

## 5. Checklist: Before You Ship

### Architecture

- [ ] No global mutable state — all config via constructor
- [ ] No God Client — logical grouping of methods
- [ ] Public API uses domain types, not implementation types
- [ ] Transaction model handles ALL 68+ types, not just Payment
- [ ] Dependency direction: core ← crypto ← client (no cycles)

### API Surface

- [ ] Sealed result types for expected failures (no exceptions for control flow)
- [ ] Built-in auto-pagination via Flow
- [ ] Built-in retry with configurable backoff
- [ ] Built-in fee estimation (autofill)
- [ ] Thread-safe client instances (documented guarantee)

### Kotlin Quality

- [ ] Explicit API mode enabled (`kotlin { explicitApi() }`)
- [ ] Binary Compatibility Validator in CI
- [ ] No hardcoded dispatchers
- [ ] No `GlobalScope` usage
- [ ] No swallowed `CancellationException`
- [ ] `Flow`-returning functions are NOT suspend
- [ ] `flowOn` used instead of `withContext` inside Flow builders
- [ ] ProGuard consumer rules shipped for Android

### DX

- [ ] Quickstart achieves working call in under 5 minutes / under 20 lines
- [ ] Error messages include: what, actual value, how to fix
- [ ] Debug logging via environment variable
- [ ] Config validation at init time (not first API call)
- [ ] `MockXrplClient` / `MockEngine` shipped for consumer testing

### Release

- [ ] Maven Central publishing via Central Portal
- [ ] GPG signing
- [ ] BOM published
- [ ] CHANGELOG.md auto-generated
- [ ] `.api` files committed and checked in CI
- [ ] macOS runner for Apple target publishing

### Documentation

- [ ] README with copy-paste quickstart
- [ ] Dokka API reference
- [ ] Error code reference
- [ ] Migration guide template ready

---

## References

### SDK Design
- [Stripe's Payments APIs: The First 10 Years](https://stripe.com/blog/payment-api-design)
- [Auth0: Guiding Principles for Building SDKs](https://auth0.com/blog/guiding-principles-for-building-sdks/)
- [Building Great SDKs — Pragmatic Engineer](https://newsletter.pragmaticengineer.com/p/building-great-sdks)
- [Hyrum's Law](https://www.hyrumslaw.com/)
- [Azure SDK General Guidelines](https://azure.github.io/azure-sdk/general_introduction.html)
- [Comprehensive Analysis of SDK Design Patterns — Vineeth](https://vineeth.io/posts/sdk-development)
- [Beyond API Compatibility — InfoQ](https://www.infoq.com/articles/breaking-changes-are-broken-semver/)

### Kotlin-Specific
- [Kotlin API Guidelines — Backward Compatibility](https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html)
- [KMP expect/actual Declarations](https://kotlinlang.org/docs/multiplatform/multiplatform-expect-actual.html)
- [Coroutines Best Practices — Android Developers](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Calling Kotlin from Java](https://kotlinlang.org/docs/java-to-kotlin-interop.html)
- [Binary Compatibility Validator](https://github.com/Kotlin/binary-compatibility-validator)
- [Mastering API Visibility in Kotlin — zsmb.co](https://zsmb.co/mastering-api-visibility-in-kotlin/)

### DX & Documentation
- [Diátaxis Documentation Framework](https://diataxis.fr/)
- [SDK Error Message Optimization — SDKs.io](https://sdks.io/docs/best-practices/build/optimize-error-messages/)
- [Stripe Developer Platform Insights](https://kenneth.io/post/insights-from-building-stripes-developer-platform-and-api-developer-experience-part-1)

### Release & Distribution
- [Maven Central Publishing in 2024+ — DeepMedia](https://blog.deepmedia.io/post/how-to-publish-to-maven-central-in-2024)
- [KMP Library Publishing — Kotlin Docs](https://kotlinlang.org/docs/multiplatform/multiplatform-publish-libraries.html)
- [vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
- [CI/CD for KMP in 2025 — KMPShip](https://www.kmpship.app/blog/ci-cd-kotlin-multiplatform-2025)
- [Gradle Dependency Verification](https://docs.gradle.org/current/userguide/dependency_verification.html)

### Error Handling & Testing
- [Kotlin Coroutines Best Practices — Kotlin Academy](https://kt.academy/article/cc-best-practices)
- [Polymorphism in kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md)
- [Codemods for API Refactoring — Martin Fowler](https://martinfowler.com/articles/codemods-api-refactoring.html)
