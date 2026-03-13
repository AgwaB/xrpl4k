# Samples

Runnable examples demonstrating every major feature of the xrpl4k SDK.

Each sample is a standalone Kotlin file (or Java file) with a `main()` function.
They use placeholder addresses and seeds — replace them with real testnet
credentials before running.

> **Tip**: Get free testnet credentials at https://xrpl.org/xrp-testnet-faucet.html

## Getting Started

### Wallet & Keys

| Sample | What it demonstrates |
|--------|---------------------|
| [WalletSample.kt](WalletSample.kt) | `Wallet.generate()` for new wallets, `Wallet.fromSeed()` for restoration, `Wallet.fromEntropy()` for raw key material, Ed25519 vs secp256k1 algorithm selection, and `AutoCloseable` lifecycle for zeroing private keys |

### Client Setup

| Sample | What it demonstrates |
|--------|---------------------|
| [ClientSetupSample.kt](ClientSetupSample.kt) | Network configuration (Mainnet, Testnet, Devnet, custom URL), retry policies, WebSocket setup, request timeouts, and proper client lifecycle with `.use {}` |

### Account Queries

| Sample | What it demonstrates |
|--------|---------------------|
| [QueryAccountSample.kt](QueryAccountSample.kt) | `accountInfo` for sequence/balance/flags, `accountLines` for trust lines, `getXrpBalance` convenience method, `getBalances` for all currencies, and `XrplResult` pattern matching |

### Transactions

| Sample | What it demonstrates |
|--------|---------------------|
| [SendXrpSample.kt](SendXrpSample.kt) | Simplest possible XRP transfer: check balance, build payment, `submitAndWait`, verify — the "hello world" of XRPL |
| [SubmitPaymentSample.kt](SubmitPaymentSample.kt) | Payment DSL builder, `autofill()` for fee/sequence/lastLedger, `signTransaction()`, `submit()` for fire-and-forget, and `submitAndWait()` for validated confirmation |
| [TrustLineAndIouSample.kt](TrustLineAndIouSample.kt) | `trustSet` to create trust lines, `IssuedAmount` for IOU payments (USD, EUR), issuer → holder → holder flow, and `accountLines` verification |
| [MultisignSample.kt](MultisignSample.kt) | M-of-N multi-signature workflow: `multiSignTransaction()` per signer, `combineSignatures()` to merge (auto-sorts by account ID), and `submitMultisigned()` |
| [EscrowSample.kt](EscrowSample.kt) | Time-based escrow: `escrowCreate` with finishAfter/cancelAfter, `escrowFinish` to release funds, `escrowCancel` to reclaim after timeout |
| [ValidationSample.kt](ValidationSample.kt) | `TransactionValidator` for client-side validation — catches self-payments, zero amounts, excessive fees, and type-specific rules before any network call |

### Subscriptions & Streaming

| Sample | What it demonstrates |
|--------|---------------------|
| [SubscriptionSample.kt](SubscriptionSample.kt) | WebSocket `Flow`-based subscriptions: `subscribeToLedger()` for ledger close events, `subscribeToTransactions()` for all validated transactions, and `subscribeToAccount()` for activity on a specific address |
| [PaginationSample.kt](PaginationSample.kt) | Auto-paginating `Flow` helpers: `allAccountLines()` walks trust line pages, `accountTransactions()` walks transaction history. Both support `take(N)` for early termination and `catch {}` for error handling |

### DEX & AMM

| Sample | What it demonstrates |
|--------|---------------------|
| [DexTradingSample.kt](DexTradingSample.kt) | `offerCreate` to place limit orders (sell XRP for USD, buy XRP with USD), `offerCancel` to remove orders, `accountOffers` to view open orders, and DEX flag options (passive, IOC, FOK, sell) |
| [OrderBookSample.kt](OrderBookSample.kt) | `bookOffers` for current open orders on a currency pair (both sides), `bookChanges` for OHLCV-style data showing all order book movements in a single ledger |
| [AmmSample.kt](AmmSample.kt) | `ammInfo` to query Automated Market Maker pool state — reserves, LP token supply, trading fee. Shows how to build currency specifiers with `buildJsonObject` |
| [PathFindSample.kt](PathFindSample.kt) | `ripplePathFind` for cross-currency payment path discovery — XRP delivery, IOU delivery, and source currency restrictions |

### NFTs (XLS-20)

| Sample | What it demonstrates |
|--------|---------------------|
| [NftQuerySample.kt](NftQuerySample.kt) | `nftBuyOffers` and `nftSellOffers` for marketplace queries, `nftInfo` and `nftHistory` for on-ledger state and full lifecycle (requires Clio server) |
| [NftTransactionSample.kt](NftTransactionSample.kt) | Full NFT lifecycle: `nfTokenMint` (with taxon, transfer fee, URI), `nfTokenCreateOffer` (sell flag), `nfTokenAcceptOffer` (buyer side), `nfTokenBurn` |

### Utilities

| Sample | What it demonstrates |
|--------|---------------------|
| [TransactionUtilsSample.kt](TransactionUtilsSample.kt) | Offline utilities: `hashSignedTx` recomputes tx hash from blob, `verifyTransaction` checks Ed25519/secp256k1 signatures, `parseBalanceChanges` extracts per-account deltas from metadata, `getNFTokenID` finds minted token ID |

### Java Interop

| Sample | What it demonstrates |
|--------|---------------------|
| [JavaInteropSample.java](JavaInteropSample.java) | `XrplClientJava` wrapper: `CompletableFuture`-based API, `try-with-resources` lifecycle, `accountInfo` / `getXrpBalance` / `fee` / `serverInfo` queries, and `submitAndWait` from Java |

## API Patterns Used

These samples demonstrate several patterns that appear throughout the SDK:

### XrplResult Pattern Matching

```kotlin
when (val result = client.accountInfo(address)) {
    is XrplResult.Success -> println("Balance: ${result.value.balance}")
    is XrplResult.Failure -> println("Error: ${result.error}")
}

// Or use getOrThrow() for the happy path
val info = client.accountInfo(address).getOrThrow()
```

### Client Lifecycle

```kotlin
// Always close the client to release resources
XrplClient { network = Network.Testnet }.use { client ->
    // ... use client here
}
```

### Transaction DSL

```kotlin
val tx = payment {
    account     = wallet.address
    destination = Address("rDestination...")
    amount      = 10.xrp
}
```
