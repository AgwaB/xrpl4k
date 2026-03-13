import org.xrpl.sdk.client.java.XrplClientJava;
import org.xrpl.sdk.client.model.AccountInfo;
import org.xrpl.sdk.client.model.SubmitResult;
import org.xrpl.sdk.client.model.ValidatedTransaction;
import org.xrpl.sdk.core.Network;
import org.xrpl.sdk.core.model.transaction.XrplTransaction;
import org.xrpl.sdk.core.result.XrplResult;
import org.xrpl.sdk.core.type.Address;
import org.xrpl.sdk.crypto.Wallet;

import java.util.concurrent.CompletableFuture;

/**
 * Demonstrates the Java-friendly XrplClientJava wrapper.
 *
 * XrplClientJava exposes each operation as a CompletableFuture so Java callers
 * avoid dealing with Kotlin coroutines directly. Internally it runs on a
 * CoroutineScope backed by Dispatchers.Default.
 *
 * Implements AutoCloseable — always use try-with-resources or call close()
 * explicitly to release the underlying client and coroutine scope.
 *
 * The factory method XrplClientJava.create() accepts a Kotlin lambda, which in
 * Java requires returning Unit.INSTANCE at the end of the configuration block.
 */
public class JavaInteropSample {

    public static void main(String[] args) throws Exception {

        // --- 1. Create the client via try-with-resources ---
        try (XrplClientJava client = XrplClientJava.create(config -> {
            config.setNetwork(Network.Testnet);
            return kotlin.Unit.INSTANCE;
        })) {

            Address account = new Address("rSomeAccountAddressHere");

            // --- 2. accountInfo: fetch account sequence, balance, flags ---
            // All methods return CompletableFuture<XrplResult<T>>.
            // Call .get() to block until complete (use .thenApply() for async chains).
            XrplResult<AccountInfo> infoResult = client.accountInfo(account).get();

            if (infoResult instanceof XrplResult.Success) {
                AccountInfo info = ((XrplResult.Success<AccountInfo>) infoResult).getValue();
                System.out.println("Account:     " + info.getAccount());
                System.out.println("Sequence:    " + info.getSequence());
                System.out.println("Owner count: " + info.getOwnerCount());
                System.out.println("Flags:       " + info.getFlags());
            } else {
                XrplResult.Failure<AccountInfo> failure = (XrplResult.Failure<AccountInfo>) infoResult;
                System.out.println("accountInfo failed: " + failure.getError());
            }

            // --- 3. getXrpBalance ---
            client.getXrpBalance(account)
                .thenAccept(result -> {
                    if (result instanceof XrplResult.Success) {
                        var drops = ((XrplResult.Success<?>) result).getValue();
                        System.out.println("XRP balance: " + drops);
                    }
                })
                .get();  // wait for async chain

            // --- 4. serverInfo ---
            client.serverInfo()
                .thenAccept(result -> {
                    if (result instanceof XrplResult.Success) {
                        var info = ((XrplResult.Success<?>) result).getValue();
                        System.out.println("Server info: " + info);
                    }
                })
                .get();

            // --- 5. fee ---
            client.fee()
                .thenAccept(result -> {
                    if (result instanceof XrplResult.Success) {
                        var fee = ((XrplResult.Success<?>) result).getValue();
                        System.out.println("Fee schedule: " + fee);
                    }
                })
                .get();

            // --- 6. submitAndWait: full lifecycle from Java ---
            // Build and sign the transaction in Kotlin helpers, then hand off to Java.
            // Here we use a placeholder signed transaction; replace with real signing logic.
            // Wallet.fromSeed() and the transaction DSL are available from xrpl-crypto
            // and xrpl-core respectively.
            Wallet wallet = Wallet.fromSeed("sYourTestnetSeedHere");

            // Build an unsigned payment transaction using the Kotlin DSL from Java.
            // TransactionBuildersKt.payment() is the Kotlin top-level function exposed to Java.
            XrplTransaction.Unsigned tx = org.xrpl.sdk.core.model.transaction.TransactionBuildersKt.payment(builder -> {
                builder.setAccount(wallet.getAddress());
                builder.setDestination(new Address("rDestinationAddressHere"));
                builder.setAmount(org.xrpl.sdk.core.model.amount.LiteralsKt.getXrp(10));
                return kotlin.Unit.INSTANCE;
            });

            CompletableFuture<XrplResult<ValidatedTransaction>> submitFuture =
                client.submitAndWait(tx, wallet);

            XrplResult<ValidatedTransaction> submitResult = submitFuture.get();
            if (submitResult instanceof XrplResult.Success) {
                ValidatedTransaction validated = ((XrplResult.Success<ValidatedTransaction>) submitResult).getValue();
                System.out.println("Payment validated! hash=" + validated.getHash()
                    + " ledger=" + validated.getLedgerIndex());
            } else {
                System.out.println("Payment failed: " + ((XrplResult.Failure<?>) submitResult).getError());
            }
        }
        // XrplClientJava.close() is called automatically here.
    }
}
