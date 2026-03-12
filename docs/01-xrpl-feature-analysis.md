# XRPL Kotlin SDK — Feature Analysis Report

> Date: 2026-03-12
> Purpose: Required features based on official XRPL documentation, full xrpl.js feature inventory, and comparative analysis with xrpl4j

---

## 1. Required Features Based on Official XRPL Documentation

### 1.1 Client API Methods

#### Account Methods (10)
| Method | Description |
|--------|-------------|
| `account_info` | Account data, XRP balance, flags, queue data |
| `account_lines` | Trust lines and IOU balances |
| `account_objects` | All ledger objects owned by an account (filterable by type) |
| `account_offers` | DEX orders created by an account |
| `account_tx` | Account transaction history (pagination, ledger range, type filter) |
| `account_channels` | List of Payment Channels where the account is the source |
| `account_currencies` | List of currencies the account can send or receive |
| `account_nfts` | List of NFTs owned by an account |
| `gateway_balances` | Total issued obligations and assets of a gateway |
| `noripple_check` | Check recommendations for DefaultRipple/NoRipple settings |

#### Ledger Methods (5)
| Method | Description |
|--------|-------------|
| `ledger` | Full data for a specific ledger version |
| `ledger_closed` | Hash/index of the most recently closed (unvalidated) ledger |
| `ledger_current` | Index of the currently in-progress ledger |
| `ledger_data` | All entries in a specific ledger (pagination, type filter) |
| `ledger_entry` | Retrieve a single ledger entry by type and identifier |

#### Transaction Methods (5)
| Method | Description |
|--------|-------------|
| `submit` | Submit a signed transaction blob |
| `submit_multisigned` | Submit a multi-signed transaction |
| `tx` | Retrieve a transaction by hash (full ledger history) |
| `transaction_entry` | Retrieve a transaction from a specific ledger version |
| `simulate` | Transaction dry-run — metadata preview (rippled 2.3+) |

#### Path / Order Book Methods (7)
| Method | Description |
|--------|-------------|
| `book_offers` | Current offers in a specific order book |
| `book_changes` | Summary of order book changes in a single ledger |
| `path_find` | WebSocket only: continuous payment path search |
| `ripple_path_find` | Single-request payment path search (HTTP capable) |
| `nft_buy_offers` | Buy offers for a specific NFToken |
| `nft_sell_offers` | Sell offers for a specific NFToken |
| `amm_info` | AMM instance state (assets, LP tokens, fees, auction slot) |

#### Additional Methods
| Method | Description |
|--------|-------------|
| `deposit_authorized` | Check whether one account can send payments to another |
| `get_aggregate_price` | Calculate average/median price for an asset pair from Oracle objects |
| `channel_verify` | Verify a Payment Channel claim signature |

#### Server Info Methods (7)
| Method | Description |
|--------|-------------|
| `server_info` | Human-readable server status |
| `server_state` | Machine-readable server status |
| `server_definitions` | Serialization definitions (field types, transaction types) |
| `fee` | Current transaction cost estimate |
| `manifest` | Validator manifest |
| `feature` | Amendment status (active, pending, majority) |
| `version` | rippled/Clio server version |

#### Subscription Methods (2, WebSocket only)
| Method | Description |
|--------|-------------|
| `subscribe` | Subscribe to an event stream |
| `unsubscribe` | Cancel a subscription |

**Subscribable streams:**
- `ledger` — new validated ledger events
- `transactions` — all transactions in closed ledgers
- `transactions_proposed` — unconfirmed + confirmed transactions
- `accounts` — transactions affecting specific accounts
- `accounts_proposed` — unconfirmed transactions for specific accounts
- `books` — order book changes for a specific trading pair
- `book_changes` — per-ledger OHLC order book summary
- `validations` — validator messages
- `consensus` — consensus phase changes
- `server` — server status / network connectivity
- `manifests` — validator ephemeral key updates

#### Utility Methods
| Method | Description |
|--------|-------------|
| `ping` | Test server connectivity |
| `random` | Generate a 256-bit random value |

#### Clio-Specific Methods
| Method | Description |
|--------|-------------|
| `nft_info` | Detailed information for a single NFT |
| `nft_history` | NFT transaction history |
| `nfts_by_issuer` | List of NFTs by issuer |
| `mpt_holders` | All holders of an MPT issuance |
| `vault_info` | Vault ledger entry state |

---

### 1.2 Transaction Types (68 + 3 pseudo-transactions)

#### Account Management
| Transaction | Key Fields |
|-------------|------------|
| `AccountSet` | ClearFlag, SetFlag, Domain, EmailHash, NFTokenMinter, TransferRate, TickSize |
| `AccountDelete` | Destination, DestinationTag |
| `SetRegularKey` | RegularKey (set or remove) |
| `SignerListSet` | SignerEntries (up to 32), SignerQuorum |
| `TicketCreate` | TicketCount (1–250) |
| `DelegateSet` | Authorize, Permissions (up to 10) |

#### Payment
| Transaction | Key Fields |
|-------------|------------|
| `Payment` | DeliverMax/Amount, Destination, SendMax, DeliverMin, Paths, CredentialIDs, DomainID |

#### Trust Lines / Tokens
| Transaction | Key Fields |
|-------------|------------|
| `TrustSet` | LimitAmount, QualityIn/Out; includes Deep Freeze flags |
| `Clawback` | Amount (reclaim issued tokens) |
| `DepositPreauth` | Authorize/Unauthorize, credential-based authorization |

#### DEX
| Transaction | Key Fields |
|-------------|------------|
| `OfferCreate` | TakerGets, TakerPays, Expiration, DomainID; Passive/IOC/FOK/Sell/Hybrid flags |
| `OfferCancel` | OfferSequence |

#### AMM (7)
| Transaction | Description |
|-------------|-------------|
| `AMMCreate` | Create a pool (2 assets + TradingFee) |
| `AMMDeposit` | Provide liquidity (multiple modes) |
| `AMMWithdraw` | Withdraw liquidity |
| `AMMBid` | Bid on an auction slot |
| `AMMVote` | Vote on trading fee |
| `AMMDelete` | Delete an empty AMM |
| `AMMClawback` | Clawback tokens from AMM |

#### MPT — Multi-Purpose Token (4)
| Transaction | Description |
|-------------|-------------|
| `MPTokenIssuanceCreate` | Create an MPT issuance (MaxAmount, Scale, TransferFee, metadata) |
| `MPTokenIssuanceDestroy` | Destroy an MPT issuance |
| `MPTokenIssuanceSet` | Modify MPT issuance settings (global lock/unlock) |
| `MPTokenAuthorize` | Holder opt-in/out, issuer authorization |

#### NFT (6)
| Transaction | Description |
|-------------|-------------|
| `NFTokenMint` | Mint an NFT (Taxon, TransferFee, URI, Mutable flag) |
| `NFTokenBurn` | Burn an NFT |
| `NFTokenModify` | Modify an NFT (change URI, requires lsfMutable) |
| `NFTokenCreateOffer` | Create an NFT buy/sell offer |
| `NFTokenCancelOffer` | Cancel an NFT offer |
| `NFTokenAcceptOffer` | Accept an NFT offer (including brokered mode) |

#### Escrow (3)
| Transaction | Description |
|-------------|-------------|
| `EscrowCreate` | Create a time- or condition-locked XRP escrow |
| `EscrowFinish` | Complete an escrow (provide Fulfillment) |
| `EscrowCancel` | Cancel an escrow |

#### Payment Channel (3)
| Transaction | Description |
|-------------|-------------|
| `PaymentChannelCreate` | Create a channel (Amount, SettleDelay, PublicKey) |
| `PaymentChannelFund` | Add funds to a channel |
| `PaymentChannelClaim` | Claim from or close a channel |

#### Check (3)
| Transaction | Description |
|-------------|-------------|
| `CheckCreate` | Create a deferred payment check |
| `CheckCash` | Cash a check (full or partial) |
| `CheckCancel` | Cancel a check |

#### DID (2)
| Transaction | Description |
|-------------|-------------|
| `DIDSet` | Set a DID document (Data, DIDDocument, URI) |
| `DIDDelete` | Delete a DID ledger entry |

#### Price Oracle (2)
| Transaction | Description |
|-------------|-------------|
| `OracleSet` | Set Oracle data (up to 10 asset pairs) |
| `OracleDelete` | Delete an Oracle |

#### Credential (3)
| Transaction | Description |
|-------------|-------------|
| `CredentialCreate` | Issue a credential (Subject, Type, Expiration) |
| `CredentialAccept` | Accept a credential |
| `CredentialDelete` | Delete a credential |

#### Permissioned Domain (2)
| Transaction | Description |
|-------------|-------------|
| `PermissionedDomainSet` | Create or modify a domain (AcceptedCredentials 1–10) |
| `PermissionedDomainDelete` | Delete a domain |

#### Vault (6)
| Transaction | Description |
|-------------|-------------|
| `VaultCreate` | Create an ERC-4626-style vault |
| `VaultSet` | Modify vault settings |
| `VaultDeposit` | Deposit into a vault |
| `VaultWithdraw` | Withdraw from a vault |
| `VaultClawback` | Clawback from a vault |
| `VaultDelete` | Delete a vault |

#### Loan (9)
| Transaction | Description |
|-------------|-------------|
| `LoanSet` | Create or modify a loan |
| `LoanDelete` | Delete a loan |
| `LoanManage` | Manage a loan |
| `LoanPay` | Repay a loan |
| `LoanBrokerSet` | Set up a loan broker |
| `LoanBrokerDelete` | Delete a loan broker |
| `LoanBrokerCoverDeposit` | Deposit broker cover |
| `LoanBrokerCoverWithdraw` | Withdraw broker cover |
| `LoanBrokerCoverClawback` | Clawback broker cover |

#### Cross-Chain Bridge (8)
| Transaction | Description |
|-------------|-------------|
| `XChainCreateBridge` | Create a cross-chain bridge |
| `XChainModifyBridge` | Modify a bridge |
| `XChainCreateClaimID` | Create a claim ID |
| `XChainCommit` | Commit assets to a bridge |
| `XChainClaim` | Claim bridged assets |
| `XChainAccountCreateCommit` | Commit to create an account |
| `XChainAddClaimAttestation` | Add a claim attestation |
| `XChainAddAccountCreateAttestation` | Add an account-creation attestation |

#### Batch (1)
| Transaction | Description |
|-------------|-------------|
| `Batch` | Atomic execution of 2–8 inner transactions (AllOrNothing, OnlyOne, UntilFailure, Independent) |

#### Pseudo-Transactions (validator-only, 3)
- `EnableAmendment`, `SetFee`, `UNLModify`

---

### 1.3 Ledger Objects (30)

| Type | Shorthand | Description |
|------|-----------|-------------|
| `AccountRoot` | `account` | Core account record |
| `Amendments` | `amendments` | Active amendments and pending votes |
| `AMM` | `amm` | AMM pool state |
| `Bridge` | `bridge` | Cross-chain bridge configuration |
| `Check` | `check` | Deferred payment check |
| `Credential` | `credential` | Issued/accepted credential |
| `Delegate` | `delegate` | Permission delegation |
| `DepositPreauth` | `deposit_preauth` | Deposit pre-authorization |
| `DID` | `did` | Decentralized identifier document |
| `DirectoryNode` | `directory` | Internal linked list |
| `Escrow` | `escrow` | Time- or condition-locked XRP |
| `FeeSettings` | `fee` | Network base fee and reserve |
| `LedgerHashes` | `hashes` | Recent ledger hashes |
| `Loan` | `loan` | Active loan |
| `LoanBroker` | `loan_broker` | Loan broker configuration |
| `MPToken` | `mptoken` | MPT balance for an individual account |
| `MPTokenIssuance` | `mpt_issuance` | MPT issuance definition |
| `NegativeUNL` | `nunl` | Validators temporarily removed from the UNL |
| `NFTokenOffer` | `nft_offer` | NFT buy/sell offer |
| `NFTokenPage` | `nft_page` | NFT storage page (up to 32 per account) |
| `Offer` | `offer` | DEX order book offer |
| `Oracle` | `oracle` | Price oracle data |
| `PayChannel` | `payment_channel` | Payment channel |
| `PermissionedDomain` | `permissioned_domain` | Credential-based access control domain |
| `RippleState` | `state` | Trust line between two accounts |
| `SignerList` | `signer_list` | Multi-signature signer list |
| `Ticket` | `ticket` | Pre-allocated sequence number |
| `Vault` | `vault` | Single-asset vault |
| `XChainOwnedClaimID` | `xchain_owned_claim_id` | Cross-chain claim ID |
| `XChainOwnedCreateAccountClaimID` | `xchain_owned_create_account_claim_id` | Cross-chain account-creation claim ID |

---

### 1.4 Cryptography Requirements

#### Key Algorithms
- **secp256k1** (default): ECDSA, 33-byte compressed public key
- **Ed25519**: EdDSA, 32-byte key with `0xED` prefix → 33 bytes

#### Key Derivation
- **Ed25519**: SHA-512Half(seed) → 32-byte private key → standard public key derivation
- **secp256k1**: 3-step derivation (root → intermediate → master), including group order validation

#### Address Derivation
1. SHA-256(compressed public key) → intermediate hash
2. RIPEMD-160(intermediate hash) → 20-byte Account ID
3. Base58Check(`0x00` prefix + Account ID) → classic address starting with `r`

#### Base58 Encoding
- XRP-specific alphabet: `rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz`
- Checksum: double SHA-256, first 4 bytes

| Data Type | Prefix | Leading Character |
|-----------|--------|-------------------|
| Account address | `0x00` | `r` |
| Account public key | `0x23` | `a` |
| Seed (private key) | `0x21` | `s` |
| Node public key | `0x1C` | `n` |

#### X-Address
- Classic address + optional destination tag → unified single string
- Separate prefixes for mainnet and testnet

#### Signing
- **Single signing**: serialize → prepend `0x53545800` → SHA-512Half → sign
- **Multi-signing**: serialize → prepend `0x534D5400` + signer Account ID → sign; `Signers` array sorted by Account ID ascending
- **Multi-signing cost**: fee = (N+1) × base fee

---

### 1.5 Binary Codec (Serialization)

#### Type System (19+ primitive types)

| Type | Code | Description |
|------|------|-------------|
| UInt8 | 16 | 8-bit big-endian |
| UInt16 | 1 | 16-bit big-endian |
| UInt32 | 2 | 32-bit big-endian |
| UInt64 | 3 | 64-bit big-endian |
| UInt128 | 4 | 128-bit |
| UInt160 | 17 | 160-bit |
| UInt192 | 21 | 192-bit |
| UInt256 | 5 | 256-bit |
| UInt512 | 23 | 512-bit |
| Amount | 6 | XRP (64-bit), IOU (384-bit), MPT (264-bit) |
| Blob | 7 | Length-prefixed binary |
| AccountID | 8 | Length-prefixed 160-bit address |
| Array | 15 | Variable length; terminated with `0xF1` |
| Object | 14 | Canonical-order fields; terminated with `0xE1` |
| PathSet | 18 | 1–6 paths × 1–8 steps; `0xFF` separator, `0x00` terminator |
| Vector256 | 19 | Length-prefixed array of 256-bit values |
| Currency | 26 | 160-bit currency code |
| Issue | 24 | 160-bit (XRP) or 320-bit (token + issuer) |
| XChainBridge | 25 | 656–976-bit |

#### Field Serialization Rules
- Field ID: 1–3 bytes (type_code in upper nibble + field_code in lower nibble)
- Canonical field order: sort by type_code first, then field_code
- Length prefixing: 0–192 bytes (1 byte), 193–12,480 (2 bytes), 12,481–918,744 (3 bytes)

---

### 1.6 Network Features

#### Protocols
- **JSON-RPC over HTTP**: stateless request-response, suitable for one-shot queries
- **WebSocket**: persistent connection, server push, required for subscriptions and streaming

#### Request/Response Conventions
- WebSocket: `command` + `id` → response echoes `id`, `type: "response"`, `status`
- JSON-RPC: `method` + `params: [{}]` → `status` inside `result`
- Pagination: `marker` + `limit` pattern
- Ledger specification: `ledger_index` (integer, `"current"`, `"closed"`, `"validated"`) or `ledger_hash`
- `validated: true` = finality guaranteed

#### Error Code System
| Prefix | Meaning |
|--------|---------|
| `tes*` | Success |
| `tec*` | Fee charged only (fee burned, operation failed) |
| `tef*` | Failure (cannot be applied now or in the future) |
| `tel*` | Local error (server-specific; retry on another server) |
| `tem*` | Malformed transaction format |
| `ter*` | Retry (may succeed in a future ledger) |

#### Reliable Transaction Submission
- `LastLedgerSequence` = validated_ledger_index + 4 (recommended)
- Track by `Sequence` number; if not included, resubmit with the same Sequence
- Confirm finality only from responses with `validated: true`

---

### 1.7 Recent Features (2024–2025 Amendments)

| Feature | Status | Description |
|---------|--------|-------------|
| AMM | Mainnet 2024 | Constant-product AMM, CLOB+AMM hybrid routing |
| MPT | Mainnet 2024/2025 | More efficient tokens than trust lines, integer balances |
| DID | Mainnet 2024 | W3C DID standard on-chain |
| Price Oracle | Mainnet 2024 | Aggregate up to 200 Oracles |
| Credential | In development 2024–2025 | Two-step issuance + acceptance, DepositPreauth integration |
| Permissioned Domain | In development 2025 | Credential-based access control domain |
| Batch | In development 2025 | Atomic execution of 2–8 transactions, 4 modes |
| Delegation | In development 2025 | Permission delegation per transaction type |
| Vault | In development 2025 | ERC-4626-style, DomainID integration |
| Loan | In development 2025 | Loan protocol + broker system |
| Deep Freeze | 2024/2025 | Extended token freeze (blocks transfers even between non-issuers) |
| NFTokenModify | 2024/2025 | Dynamic NFTs (change URI) |
| Simulate | rippled 2.3+ | Transaction dry-run |

---

## 2. Full xrpl.js Feature Inventory

> Source: `/Users/toby/xrpl/xrpl.js` (v4.6.0)

### 2.1 Package Structure

| Package | Role |
|---------|------|
| `xrpl` | Main SDK — Client, Wallet, Models, Sugar, Utils, Validation |
| `ripple-binary-codec` | Binary serialization/deserialization (19 types) |
| `ripple-address-codec` | Address encoding (classic, X-address) |
| `ripple-keypairs` | Key generation, signing, verification (Ed25519, secp256k1) |
| `secret-numbers` | Alternative secret format (8 × 6-digit numbers) |
| `@xrplf/isomorphic` | Platform compatibility layer (SHA-256/512, RIPEMD-160, WebSocket, random) |

### 2.2 Supported RPC Methods (45)

Account (10), Ledger (5), Transaction (5), Path/OrderBook (4), PayChannel (1), Subscription (2), Server (6), Utility (2), NFT (2), Clio (3), AMM (1), Oracle (1), Vault (1), Other (2)

### 2.3 Supported Transactions (68 + 3 pseudo-transactions)

See §1.2 for the full list. xrpl.js supports all transaction types currently proposed for XRPL.

### 2.4 Sugar / Helper Functions

| Function | Description |
|----------|-------------|
| `client.autofill(tx)` | Auto-fill Sequence, Fee, LastLedgerSequence, NetworkID; convert X-addresses |
| `client.submit(tx, opts?)` | autofill + sign + submit |
| `client.submitAndWait(tx, opts?)` | autofill + sign + submit + wait for validation |
| `client.simulate(tx, opts?)` | Simulate a transaction |
| `client.getXrpBalance(address)` | Retrieve XRP balance |
| `client.getBalances(address)` | Retrieve full XRP + token balances |
| `client.getOrderbook(c1, c2)` | Retrieve a two-sided order book |
| `client.getLedgerIndex()` | Latest validated ledger index |
| `client.fundWallet(wallet?)` | Faucet funding on testnet/devnet |
| `client.requestNextPage(req, resp)` | Fetch the next page using a marker |
| `client.requestAll(req)` | Automatically collect all pages |

#### Fee Calculation Special Cases
- EscrowFinish + Fulfillment: `baseFee * (33 + fulfillmentBytes / 16)`
- AccountDelete, AMMCreate, VaultCreate: fee = owner reserve
- Batch: `2 * baseFee + sum(inner tx fees)`
- Multisigned: `baseFee * (1 + signerCount)`

### 2.5 Wallet / Cryptography

#### Wallet Creation Methods (6)
| Method | Description |
|--------|-------------|
| `Wallet.generate(algorithm?)` | Generate a random wallet |
| `Wallet.fromSeed(seed)` | Derive from a Base58 seed |
| `Wallet.fromSecret(secret)` | Alias for fromSeed |
| `Wallet.fromEntropy(entropy)` | Derive from raw entropy |
| `Wallet.fromMnemonic(mnemonic)` | BIP-39 or RFC1751 mnemonic (deprecated) |
| `new Wallet(publicKey, privateKey)` | Direct construction |

#### Signing Functions
| Function | Description |
|----------|-------------|
| `wallet.sign(tx, multisign?)` | Sign a transaction → `{ tx_blob, hash }` |
| `wallet.verifyTransaction(signedTx)` | Verify a signature |
| `multisign(transactions)` | Combine multiple single signatures into a multi-signature |
| `signMultiBatch(wallet, tx)` | Multi-account signing for Batch |
| `combineBatchSigners(txs)` | Combine Batch signers |
| `authorizeChannel(wallet, channelId, amount)` | Sign a Payment Channel claim |
| `signLoanSetByCounterparty(...)` | Counterparty signature for LoanSet |

### 2.6 Binary Codec Public API

| Function | Description |
|----------|-------------|
| `encode(json)` | JSON → binary hex |
| `decode(binary)` | Binary hex → JSON |
| `encodeForSigning(json)` | Encode for single signing |
| `encodeForMultisigning(json, signer)` | Encode for multi-signing |
| `encodeForSigningBatch(json)` | Encode for Batch signing |
| `encodeForSigningClaim(json)` | Encode for Payment Channel claim signing |
| `encodeQuality(value)` / `decodeQuality(value)` | Encode/decode offer quality values |
| `decodeLedgerData(binary)` | Decode ledger data |

### 2.7 Address Codec

| Function | Description |
|----------|-------------|
| `encodeAccountID` / `decodeAccountID` | 20-byte ↔ r-address |
| `classicAddressToXAddress` / `xAddressToClassicAddress` | Convert between classic and X-address |
| `encodeSeed` / `decodeSeed` | Entropy ↔ seed |
| `encodeNodePublic` / `decodeNodePublic` | Node public key encoding |
| `isValidClassicAddress` / `isValidXAddress` | Validate addresses |

### 2.8 Client Features

#### Connection Management
- WebSocket only (`ws://`, `wss://`, `wss+unix://`)
- Automatic reconnection with exponential backoff (`ExponentialBackoff`)
- Connection timeout (default 5s), request timeout (default 20s)
- Proxy, basic auth, and custom header support
- Trace/debug logging

#### Event System
`connected`, `disconnected`, `error`, `ledgerClosed`, `transaction`, `validationReceived`, `manifestReceived`, `peerStatusChange`, `consensusPhase`, `path_find`

#### Client Options
| Option | Default | Description |
|--------|---------|-------------|
| `feeCushion` | 1.2 | Fee multiplier |
| `maxFeeXRP` | "2" | Maximum fee cap |
| `timeout` | 20000ms | Request timeout |
| `connectionTimeout` | 5000ms | Connection timeout |

### 2.9 Utilities

| Category | Functions |
|----------|-----------|
| **XRP conversion** | `xrpToDrops`, `dropsToXrp` |
| **Time conversion** | `rippleTimeToISOTime`, `isoTimeToRippleTime`, `rippleTimeToUnixTime`, `unixTimeToRippleTime` |
| **Quality/Rate** | `percentToTransferRate`, `transferRateToDecimal`, `percentToQuality`, `qualityToDecimal` |
| **String** | `convertStringToHex`, `convertHexToString` |
| **Hashing** | `hashSignedTx`, `hashAccountRoot`, `hashSignerListId`, `hashOfferId`, `hashTrustline`, `hashEscrow`, `hashPaymentChannel`, `hashVault`, `hashLoan`, `hashLedger`, and 13+ more |
| **NFT** | `getNFTokenID`, `parseNFTokenID` (decompose flags, transferFee, issuer, taxon, sequence) |
| **XChain** | `getXChainClaimID` |
| **Balance** | `getBalanceChanges` (compute balance changes from transaction metadata) |

### 2.10 Client-Side Validation

Provides `validate<Type>(tx)` functions for all 68 submittable transaction types:
1. `validateBaseTransaction(tx)` — validate common fields
2. Type-specific validation — verify required and optional fields for each transaction type
3. Check that issued currency code "XRP" is not used
4. Convert flag interfaces to numeric values

### 2.11 Error Classes

| Error | Purpose |
|-------|---------|
| `XrplError` | Base error |
| `RippledError` | Server response error |
| `ConnectionError` | Connectivity problem |
| `NotConnectedError` | Not connected |
| `TimeoutError` | Request timed out |
| `ValidationError` | Client-side validation failure |
| `NotFoundError` | Resource not found |
| `XRPLFaucetError` | Faucet error |

---

## 3. xrpl4j Analysis and Differences from xrpl.js

> Source: `/Users/toby/xrpl/xrpl4j` (Java SDK)

### 3.1 Module Structure

| Module | Role |
|--------|------|
| `xrpl4j-core` | Models, Binary Codec, Address Codec, cryptography, signing |
| `xrpl4j-client` | HTTP JSON-RPC client (Feign), faucet client, admin client |
| `xrpl4j-bom` | Maven BOM (dependency management) |
| `xrpl4j-integration-tests` | E2E tests against live/local rippled |

### 3.2 Core Architectural Differences

```
┌─────────────────────────────────────────────────────────────────┐
│                    Transport Layer                              │
├─────────────────────────────┬───────────────────────────────────┤
│        xrpl4j               │           xrpl.js                 │
│   HTTP JSON-RPC (Feign)     │    WebSocket (ws/browser)         │
│   Synchronous request-      │    Async event-streaming          │
│   response                  │                                   │
│   No subscriptions          │    subscribe/unsubscribe          │
│   Stateless                 │    Stateful (persistent conn.)    │
└─────────────────────────────┴───────────────────────────────────┘
```

### 3.3 Design Pattern Comparison

| Pattern | xrpl4j | xrpl.js |
|---------|--------|---------|
| **Immutability** | `@Value.Immutable` + Builder | Plain JS objects (mutable) |
| **Type wrappers** | Dedicated types: `Address`, `Hash256`, `XrpCurrencyAmount`, etc. | String/number based |
| **Polymorphic deserialization** | `Transaction.typeMap` BiMap + Jackson | `TransactionType` string switch |
| **Signing abstraction** | `TransactionSigner<P>` → swappable HSM | Integrated into `Wallet` class |
| **Singleton services** | `getInstance()` pattern | Function exports |
| **Unstable feature marking** | `@Beta` annotation | None (all treated equally) |
| **Error handling** | Exception-based | Exception-based |
| **Event system** | None | EventEmitter-based |
| **Validation** | Immutables `@Value.Check` (at build time) | `validate()` functions (at runtime) |

### 3.4 Detailed Feature Comparison

#### Transaction Types
- **Common**: 49 (all types supported by xrpl4j are also supported by xrpl.js)
- **xrpl.js only**: 14 — Vault (6), Loan (4), LoanBroker (5), DelegateSet, NFTokenModify
- **xrpl4j only**: none

#### Sugar / Helpers
| Feature | xrpl4j | xrpl.js |
|---------|--------|---------|
| autofill (Fee, Sequence, LastLedgerSequence) | **No** (manual) | **Yes** |
| submitAndWait | **No** (manual polling) | **Yes** |
| isFinal (6 finality states) | **Yes** | **No** (replaced by submitAndWait) |
| getBalances / getOrderbook | **No** | **Yes** |
| fundWallet (faucet) | **Yes** (FaucetClient) | **Yes** |
| Partial payment detection | **No** | **Yes** (automatic warning) |

#### Cryptography / Signing
| Feature | xrpl4j | xrpl.js |
|---------|--------|---------|
| Ed25519 / secp256k1 | Both | Both |
| HSM / external key support | **Yes** (`PrivateKeyReference`) | **No** |
| Destroyable keys | **Yes** (`javax.security.auth.Destroyable`) | **No** |
| Wallet abstraction | **No** (Seed + SignatureService separated) | **Yes** (unified Wallet) |
| Secret Numbers | **No** | **Yes** |
| RFC 1751 mnemonic | **No** | **Yes** |
| Attestation signing | **Yes** (dedicated methods) | **No** (not explicit) |

#### Utilities
| Feature | xrpl4j | xrpl.js |
|---------|--------|---------|
| XRP/drops conversion | **Yes** (`ofXrp/ofDrops/toXrp`) | **Yes** |
| Amount arithmetic (+, -, ×) | **Yes** | **No** |
| Time conversion | **No** | **Yes** |
| Balance changes | **No** | **Yes** |
| NFToken ID parsing | **No** | **Yes** |
| Ledger/transaction hashing | Partial | **Yes** |
| Quality/Rate conversion | **No** | **Yes** |

#### RPC Methods
- **xrpl4j**: ~26 client method implementations
- **xrpl.js**: 45 fully implemented + subscribe/unsubscribe

Methods missing client implementations in xrpl4j: `subscribe`, `unsubscribe`, `ledger_closed`, `ledger_current`, `ledger_data`, `server_state`, `server_definitions`, `path_find`, `noripple_check`, `feature`, `manifest`, `random`, `simulate`, `vault_info`, `nft_history`, `nfts_by_issuer`, `transaction_entry`

### 3.5 Features Unique to xrpl4j (Recommended for Kotlin SDK)

| Feature | Description | Value for Kotlin SDK |
|---------|-------------|----------------------|
| **HSM / external key abstraction** | `PrivateKeyReference` interface enables KMS/HSM integration | **Very high** |
| **Destroyable keys** | Safe in-memory key deletion | High |
| **isFinal() with 6 states** | `VALIDATED_SUCCESS`, `VALIDATED_FAILURE`, `EXPIRED`, `EXPIRED_WITH_SPENT_ACCOUNT_SEQUENCE`, `NOT_FINAL`, `VALIDATED_UNKNOWN` | High |
| **Amount arithmetic** | `XrpCurrencyAmount.plus/minus/times` with overflow checks | Medium |
| **Complete metadata model** | Transaction metadata modeled for 42+ types | High |
| **Attestation-specific signing** | Dedicated signing for XChain bridge attestations | Medium |
| **Java Keystore loader** | Load keys from JKS files | Low |
| **`@Beta` annotation** | Explicit marking of unstable features | Medium |

### 3.6 Conclusions for the Kotlin SDK

```
Integrate the best of both:

  Take from xrpl4j:
  ✓ HSM / external key abstraction (SignatureService interface)
  ✓ isFinal() precise finality determination
  ✓ Type-safe wrappers (Address, Hash256, etc.) → Kotlin value class
  ✓ Immutable models → Kotlin data class
  ✓ Amount arithmetic
  ✓ Complete metadata model
  ✓ @Beta → replace with @RequiresOptIn

  Take from xrpl.js:
  ✓ WebSocket + event subscriptions → Kotlin Flow
  ✓ autofill() + submitAndWait()
  ✓ All 68+ transaction types (including latest: Vault, Loan, etc.)
  ✓ All 45 RPC methods
  ✓ Rich utilities (time, hashing, NFT parsing, balance changes)
  ✓ Client-side validation
  ✓ Partial payment detection

  New additions in Kotlin:
  ✓ Dual transport: HTTP + WebSocket
  ✓ Coroutines (suspend fun + Flow)
  ✓ DSL builders
  ✓ Sealed class error/result hierarchy
  ✓ KMP (JVM / Android / iOS / JS)
  ✓ value class type wrappers (type safety with zero overhead)
```
