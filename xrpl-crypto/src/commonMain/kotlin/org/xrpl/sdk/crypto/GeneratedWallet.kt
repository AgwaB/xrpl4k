package org.xrpl.sdk.crypto

/**
 * Result of generating a new wallet, including the seed string for backup.
 * The seed string should be shown to the user once and not stored in memory.
 *
 * @property wallet The generated wallet.
 * @property seedString Base58-encoded seed string for user backup only.
 */
public class GeneratedWallet(
    public val wallet: Wallet,
    public val seedString: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeneratedWallet) return false
        return wallet == other.wallet && seedString == other.seedString
    }

    override fun hashCode(): Int = 31 * wallet.hashCode() + seedString.hashCode()

    override fun toString(): String = "GeneratedWallet(wallet=$wallet, seedString=***)"
}
