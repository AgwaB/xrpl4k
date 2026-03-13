import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.crypto.Wallet

/**
 * Demonstrates wallet creation and restoration.
 *
 * A Wallet holds a key pair (public + private) and the derived XRPL classic address.
 * Three factory methods are available:
 *
 *   - Wallet.generate()    — create a brand-new random wallet
 *   - Wallet.fromSeed()    — restore from a Base58-encoded seed (starts with "s")
 *   - Wallet.fromEntropy() — derive from raw 16-byte entropy
 *
 * Wallet implements AutoCloseable: call close() or use `.use {}` to zero
 * private key bytes from memory when you're done.
 *
 * Two key algorithms are supported:
 *   - Ed25519 (default) — faster, deterministic signatures, 32-byte keys
 *   - Secp256k1         — Bitcoin-style, 33-byte compressed public keys
 */
fun main() {

    // --- 1. Generate a new wallet ---
    // Returns a GeneratedWallet containing the wallet and a seed string for backup.
    // The seed string should be shown to the user ONCE and not stored in code.
    val generated = Wallet.generate()
    generated.wallet.use { wallet ->
        println("=== New Wallet (Ed25519) ===")
        println("  Address:    ${wallet.address}")
        println("  Public key: ${wallet.publicKey}")
        println("  Algorithm:  ${wallet.algorithm}")
        println("  Seed:       ${generated.seedString}  <-- back this up!")
    }

    // --- 2. Generate with secp256k1 ---
    val secp = Wallet.generate(algorithm = KeyAlgorithm.Secp256k1)
    secp.wallet.use { wallet ->
        println("\n=== New Wallet (secp256k1) ===")
        println("  Address:    ${wallet.address}")
        println("  Public key: ${wallet.publicKey}")
        println("  Seed:       ${secp.seedString}")
    }

    // --- 3. Restore from seed ---
    // Use this to reload an existing wallet. The seed encodes both the
    // entropy and the algorithm, so no algorithm parameter is needed.
    Wallet.fromSeed("sYourSecretSeedHere").use { restored ->
        println("\n=== Restored Wallet ===")
        println("  Address:    ${restored.address}")
        println("  Algorithm:  ${restored.algorithm}")
    }

    // --- 4. Derive from raw entropy ---
    // Useful when you manage your own key material (e.g. from a hardware module).
    // Entropy must be exactly 16 bytes.
    val entropy = ByteArray(16) { it.toByte() }  // example only — use secure random!
    Wallet.fromEntropy(entropy, algorithm = KeyAlgorithm.Ed25519).use { derived ->
        println("\n=== Derived Wallet ===")
        println("  Address:    ${derived.address}")
    }
}
