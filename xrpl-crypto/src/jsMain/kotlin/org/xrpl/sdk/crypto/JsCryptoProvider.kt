package org.xrpl.sdk.crypto

public actual fun platformCryptoProvider(): CryptoProvider =
    error("JS CryptoProvider not yet implemented. JVM-only in Phase 2.")
