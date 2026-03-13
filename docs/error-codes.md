# XRPL Kotlin SDK — Error Code Reference

## Overview

All XRPL SDK operations that can fail return `XrplResult<T>` rather than throwing. A result is
either a `XrplResult.Success<T>` carrying the value, or a `XrplResult.Failure` carrying an
`XrplFailure` that describes what went wrong.

```
XrplResult<T>
├── Success<T>        – operation succeeded; holds .value: T
└── Failure           – operation failed; holds .error: XrplFailure
```

Use the convenience extensions to work with results:

```kotlin
// Pattern-match
result.fold(
    onSuccess = { value -> /* use value */ },
    onFailure = { error -> /* handle error */ },
)

// Unwrap or throw
val value = result.getOrThrow()   // throws XrplException on failure
val value = result.getOrNull()    // returns null on failure

// Transform
val mapped = result.map { it.balance }
val chained = result.flatMap { doAnotherCall(it) }

// Side-effects
result
    .onSuccess { println("Account balance: ${it.balance}") }
    .onFailure { println("Error: $it") }
```

---

## XrplFailure Subtypes

| Subtype | Properties | When raised |
|---|---|---|
| `XrplFailure.RpcError` | `errorCode: Int`, `errorMessage: String` | The XRPL node returned a JSON-RPC error response (status `"error"`). |
| `XrplFailure.NetworkError` | `message: String`, `cause: Throwable?` | A transport-level failure: connection refused, timeout, TLS error, or response deserialization failure. |
| `XrplFailure.ValidationError` | `message: String` | A locally-detected invalid input (e.g., malformed transaction fields) before the request is sent. |
| `XrplFailure.TecError` | `code: Int`, `message: String` | The transaction was included in a ledger but failed with a `tec`-class engine result code (codes 100–199). |
| `XrplFailure.NotFound` | — | The requested resource does not exist on the ledger (account, transaction, or ledger not found). |

### RpcError — known numeric codes

The SDK maps the XRPL node's string error names to integer codes for uniform handling. Common values:

| Code | String Name | Meaning |
|---|---|---|
| 1 | `amendmentBlocked` | Node is amendment-blocked; cannot process transactions. |
| 14 | `tooBusy` | Node is overloaded; retry after a short delay. |
| 17 | `noNetwork` | Node is not connected to the XRPL network. |
| 19 | `actNotFound` | Account does not exist on the ledger → reclassified as `NotFound`. |
| 21 | `lgrNotFound` | Requested ledger not found → reclassified as `NotFound`. |
| 23 | `noCurrent` | Node has no current ledger. |
| 29 | `txnNotFound` | Transaction not found → reclassified as `NotFound`. |
| 35 | `actMalformed` | Malformed account field in the request. |
| 56 | `slowDown` | Node requests the client to slow down; transient — eligible for retry. |

Codes 19, 21, and 29 are automatically reclassified by the SDK into `XrplFailure.NotFound`.
Codes 14 and 56 are flagged as transient and retried according to `RetryConfig`.

---

## XRPL Engine Result Codes

Engine result codes appear in submitted transactions and are included in ledger metadata.
The SDK surfaces `tec`-class codes as `XrplFailure.TecError`; all other classes indicate
the transaction was not included in any ledger.

### Result Code Classes

| Prefix | Range | Meaning |
|---|---|---|
| `tes` | 0 | Success — transaction was applied to the ledger. |
| `tef` | -199 to -100 | Failure — transaction is invalid and was not applied. |
| `ter` | -99 to -1 | Retry — transaction could not be applied right now; try again. |
| `tec` | 100–199 | Claimed cost — transaction consumed a fee but was not applied. |

### tesSUCCESS

| Code | Value | Meaning |
|---|---|---|
| `tesSUCCESS` | 0 | Transaction was successfully applied to the ledger. |

### tef — Fatal failures (not applied, fee not charged)

| Code | Meaning |
|---|---|
| `tefPAST_SEQ` | Sequence number is in the past; the account has already processed a transaction with this sequence. |
| `tefMAX_LEDGER` | Transaction's `LastLedgerSequence` has already passed. |
| `tefBAD_AUTH` | The signing key is not authorized for this account. |
| `tefINTERNAL` | Internal error in the transaction engine. |
| `tefNO_AUTH_REQUIRED` | `SetRegularKey` attempted when no authorization is required. |
| `tefBAD_QUORUM` | Multi-sig: signer weights do not meet the quorum. |
| `tefNOT_MULTI_SIGNING` | Multi-sig: transaction is not a multi-signed transaction. |
| `tefBAD_SIGNATURE` | Multi-sig: a signer's signature is invalid. |
| `tefBAD_AUTH_MASTER` | Master key is disabled and the signing key is not the regular key. |
| `tefWRONG_PRIOR` | `AccountTxnID` does not match the last transaction for the account. |
| `tefFAILURE` | Unspecified failure. |

### ter — Retryable errors (not applied)

| Code | Meaning |
|---|---|
| `terRETRY` | Transaction should be resubmitted; transient node-level issue. |
| `terQUEUED` | Transaction has been queued and will be applied in a future ledger. |
| `terPRE_SEQ` | Sequence number is too high; a previous transaction must be applied first. |
| `terNO_ACCOUNT` | Account does not exist; the sender needs to be funded. |
| `terINSUF_FEE_B` | Insufficient balance to pay the fee after reserving the account reserve. |
| `terLAST_LEDGER` | `LastLedgerSequence` is in the past; transaction cannot be included. |

### tec — Claimed-cost failures (fee charged, not applied)

These become `XrplFailure.TecError` in the SDK.

| Code | Value | Meaning |
|---|---|---|
| `tecPATH_DRY` | 128 | Payment path could not deliver the requested amount; insufficient liquidity. |
| `tecUNFUNDED` | 129 | Insufficient XRP to cover the transaction cost. |
| `tecUNFUNDED_PAYMENT` | 104 | Sender does not have enough XRP to make the payment. |
| `tecUNFUNDED_OFFER` | 103 | Offer's TakerPays cannot be funded from the account's balance. |
| `tecNO_DST` | 124 | Destination account does not exist; it must be funded first. |
| `tecNO_DST_INSUF_XRP` | 125 | Destination account does not exist and the payment is insufficient to create it. |
| `tecNO_LINE_INSUF_RESERVE` | 126 | Not enough XRP to create a new trust line (reserve requirement). |
| `tecNO_LINE_REDUNDANT` | 127 | Trust line already exists with the same limit as the request. |
| `tecINSUF_RESERVE_LINE` | 122 | Insufficient reserve to add a new trust line. |
| `tecINSUF_RESERVE_OFFER` | 123 | Insufficient reserve to create an offer. |
| `tecINSUFFICIENT_RESERVE` | 141 | Insufficient reserve for the requested operation. |
| `tecOWNERS` | 132 | Account still has objects it owns; cannot be deleted. |
| `tecNO_PERMISSION` | 139 | Account does not have permission for the operation (e.g., DepositAuth). |
| `tecINTERNAL` | 144 | Internal error in the transaction engine. |
| `tecOVERSIZE` | 145 | Transaction or ledger object exceeds size limits. |
| `tecKILLED` | 146 | An `OfferCreate` with `tfFillOrKill` could not be filled. |
| `tecEXPIRED` | 148 | Object (offer, escrow) has expired. |
| `tecFROZEN` | 137 | Involves a frozen trust line. |
| `tecNO_TARGET` | 133 | Target account for the operation does not exist. |
| `tecNO_ENTRY` | 140 | Ledger entry referenced does not exist. |
| `tecFAILED_PROCESSING` | 105 | General processing failure for this transaction type. |
| `tecCLAIM` | 100 | Unspecified tec failure (fee claimed). |

---

## RPC Error Name to Failure Type Mapping

| RPC Error Name | Failure Type | Notes |
|---|---|---|
| `actNotFound` | `XrplFailure.NotFound` | Account not found on ledger. |
| `txnNotFound` | `XrplFailure.NotFound` | Transaction hash not in ledger history. |
| `lgrNotFound` | `XrplFailure.NotFound` | Ledger sequence or hash not available on node. |
| `actMalformed` | `XrplFailure.RpcError(35, …)` | Invalid account field in request. |
| `tooBusy` | `XrplFailure.RpcError(14, …)` | Transient; SDK retries automatically. |
| `slowDown` | `XrplFailure.RpcError(56, …)` | Transient; SDK retries automatically. |
| `noNetwork` | `XrplFailure.RpcError(17, …)` | Node is disconnected. |
| `noCurrent` | `XrplFailure.RpcError(23, …)` | Node has no current open ledger. |
| `amendmentBlocked` | `XrplFailure.RpcError(1, …)` | Node cannot process transactions. |
| `tec*` engine codes | `XrplFailure.TecError` | Reclassified from `RpcError` when code is 100–199. |
| Network/timeout | `XrplFailure.NetworkError` | Transport-level failures from Ktor. |
| Local validation | `XrplFailure.ValidationError` | Detected before sending the request. |

---

## Error Handling Examples

### Basic pattern match

```kotlin
when (val result = client.accountInfo(address)) {
    is XrplResult.Success -> println("Balance: ${result.value.accountData.balance}")
    is XrplResult.Failure -> when (val error = result.error) {
        is XrplFailure.NotFound -> println("Account $address does not exist yet.")
        is XrplFailure.NetworkError -> println("Network problem: ${error.message}")
        is XrplFailure.RpcError -> println("RPC error ${error.errorCode}: ${error.errorMessage}")
        is XrplFailure.TecError -> println("Transaction engine error ${error.code}: ${error.message}")
        is XrplFailure.ValidationError -> println("Invalid request: ${error.message}")
    }
}
```

### Retry on transient errors

```kotlin
// Configure automatic retries in the client (handles slowDown / tooBusy automatically)
val client = XrplClient {
    network = Network.Mainnet
    retry {
        maxAttempts = 5
        initialDelay = 2.seconds
        maxDelay = 30.seconds
    }
}
```

### Unwrap or throw

```kotlin
// Throws XrplException if the result is a failure
val info = client.accountInfo(address).getOrThrow()
```

### Handle tec errors after submission

```kotlin
val submitResult = client.submitTransaction(signedTx)
submitResult.onFailure { error ->
    if (error is XrplFailure.TecError) {
        when (error.code) {
            128 -> println("Path dry — no liquidity for payment.")
            124 -> println("Destination account does not exist.")
            132 -> println("Cannot delete account: still has owned objects.")
            else -> println("Transaction failed with tec code ${error.code}: ${error.message}")
        }
    }
}
```

### fold for transforming both branches

```kotlin
val message = client.getTransaction(txHash).fold(
    onSuccess = { "Transaction status: ${it.meta?.transactionResult}" },
    onFailure = { error ->
        if (error is XrplFailure.NotFound) "Transaction not yet validated"
        else "Lookup failed: $error"
    },
)
```

---

## See Also

- `XrplResult` — `/xrpl-core/src/commonMain/kotlin/org/xrpl/sdk/core/result/XrplResult.kt`
- `XrplException` — `/xrpl-core/src/commonMain/kotlin/org/xrpl/sdk/core/result/XrplException.kt`
- [XRPL Transaction Results](https://xrpl.org/transaction-results.html) — official result code reference
- [XRPL Error Formats](https://xrpl.org/error-formatting.html) — JSON-RPC error format specification
