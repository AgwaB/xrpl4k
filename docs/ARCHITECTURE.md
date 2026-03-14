# Architecture

## Modules

| Module | Description |
|--------|-------------|
| [`xrpl-core`](../xrpl-core/) | Types, transaction DSL builders, validation, and `XrplResult<T>` |
| [`xrpl-binary-codec`](../xrpl-binary-codec/) | Canonical XRPL binary serialization and deserialization |
| [`xrpl-crypto`](../xrpl-crypto/) | Ed25519 / secp256k1 key generation, signing, and address encoding |
| [`xrpl-client`](../xrpl-client/) | HTTP JSON-RPC client, WebSocket subscriptions, autofill, and sugar functions |
| [`xrpl-bom`](../xrpl-bom/) | Maven BOM for consistent version alignment |

## Dependency Graph

```
xrpl-client
├── xrpl-core
├── xrpl-binary-codec
│   └── xrpl-core
└── xrpl-crypto
    └── xrpl-core
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
