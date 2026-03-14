# Architecture

## Modules

| Module | Description |
|--------|-------------|
| [`xrpl4k-core`](../xrpl4k-core/) | Types, transaction DSL builders, validation, and `XrplResult<T>` |
| [`xrpl4k-binary-codec`](../xrpl4k-binary-codec/) | Canonical XRPL binary serialization and deserialization |
| [`xrpl4k-crypto`](../xrpl4k-crypto/) | Ed25519 / secp256k1 key generation, signing, and address encoding |
| [`xrpl4k-client`](../xrpl4k-client/) | HTTP JSON-RPC client, WebSocket subscriptions, autofill, and sugar functions |
| [`xrpl4k-bom`](../xrpl4k-bom/) | Maven BOM for consistent version alignment |

## Dependency Graph

```
xrpl4k-client
├── xrpl4k-core
├── xrpl4k-binary-codec
│   └── xrpl4k-core
└── xrpl4k-crypto
    └── xrpl4k-core
```

## Project Structure

```
xrpl4k/
├── xrpl4k-core/              # Types, models, transaction DSL, validation
├── xrpl4k-binary-codec/      # Binary serialization (rippled-compatible)
├── xrpl4k-crypto/            # Key generation, signing, address encoding
├── xrpl4k-client/            # RPC client, WebSocket, autofill, sugar
├── xrpl4k-bom/               # Maven BOM
├── xrpl4k-test-fixtures/     # Shared test utilities
├── samples/                # Runnable examples
├── build-logic/            # Gradle convention plugins
└── docs/                   # Documentation
```

## RPC Methods

29 methods across 7 categories:

**Account** — `accountInfo`, `accountLines`, `accountCurrencies`, `accountNfts`, `accountObjects`, `accountOffers`, `accountTransactions`, `gatewayBalances`

**Ledger** — `ledger`, `ledgerCurrent`, `ledgerData`, `ledgerEntry`

**Transaction** — `submit`, `submitMultisigned`, `tx`, `transactionEntry`

**Server** — `serverInfo`, `serverState`, `fee`, `manifest`

**Order Book** — `bookOffers`, `bookChanges`

**Path** — `ripplePathFind`

**NFT** — `nftBuyOffers`, `nftSellOffers`, `nftInfo`, `nftHistory`

**AMM** — `ammInfo`
