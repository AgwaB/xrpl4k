package org.xrpl.sdk.client.sugar

import kotlinx.coroutines.delay
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.model.ValidatedTransaction
import org.xrpl.sdk.client.rpc.submit
import org.xrpl.sdk.client.rpc.tx
import org.xrpl.sdk.client.signing.signTransaction
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrNull
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.crypto.Wallet

/**
 * Full transaction lifecycle: autofill -> sign -> submit -> poll for validation.
 *
 * Polls the network approximately every 4 seconds (one ledger close interval)
 * until the transaction is validated or expires.
 *
 * **Sequence number race**: See [autofill] KDoc for guidance on `tefPAST_SEQ` when multiple
 * clients share the same XRPL account.
 *
 * @param tx the unsigned transaction.
 * @param wallet the wallet to sign with.
 * @return [XrplResult] containing the validated transaction result or a failure.
 */
public suspend fun XrplClient.submitAndWait(
    tx: XrplTransaction.Unsigned,
    wallet: Wallet,
): XrplResult<ValidatedTransaction> {
    // 1. Autofill
    val fillResult = autofill(tx)
    val filled =
        fillResult.getOrNull()
            ?: return XrplResult.Failure((fillResult as XrplResult.Failure).error)

    // 2. Sign
    val signed = wallet.signTransaction(filled)

    // 3. Submit
    val submitResult = submit(signed)
    val submitInfo =
        submitResult.getOrNull()
            ?: return XrplResult.Failure((submitResult as XrplResult.Failure).error)

    // Check preliminary rejection
    val preliminaryResult = submitInfo.engineResult
    if (preliminaryResult.startsWith("tem") ||
        preliminaryResult.startsWith("tef") ||
        preliminaryResult.startsWith("tel")
    ) {
        return XrplResult.Failure(
            XrplFailure.RpcError(
                errorCode = submitInfo.engineResultCode,
                errorMessage = "Transaction rejected: $preliminaryResult - ${submitInfo.engineResultMessage}",
            ),
        )
    }

    // 4. Poll for validation
    val txHash = signed.hash
    val lastLedgerSeq = filled.lastLedgerSequence

    while (true) {
        delay(4000) // ~1 ledger close interval

        val txResult = tx(txHash)
        val txInfo = txResult.getOrNull()

        if (txInfo != null && txInfo.validated) {
            val engineResult = txInfo.engineResult ?: "tesSUCCESS"
            return if (engineResult.startsWith("tec")) {
                val code = txInfo.engineResultCode ?: -1
                XrplResult.Failure(XrplFailure.TecError(code = code, message = engineResult))
            } else {
                XrplResult.Success(
                    ValidatedTransaction(
                        hash = txHash,
                        ledgerIndex = txInfo.ledgerIndex ?: LedgerIndex(0u),
                        engineResult = engineResult,
                        engineResultCode = txInfo.engineResultCode ?: 0,
                        meta = txInfo.meta,
                    ),
                )
            }
        }

        // Check if transaction has expired
        if (txInfo != null && txInfo.ledgerIndex != null) {
            if (txInfo.ledgerIndex.value > lastLedgerSeq) {
                return XrplResult.Failure(
                    XrplFailure.ValidationError(
                        "Transaction expired: ledger ${txInfo.ledgerIndex.value} > LastLedgerSequence $lastLedgerSeq",
                    ),
                )
            }
        }
    }
}
