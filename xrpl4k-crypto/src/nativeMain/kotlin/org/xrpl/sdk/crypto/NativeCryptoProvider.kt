package org.xrpl.sdk.crypto

public actual fun platformCryptoProvider(): CryptoProvider =
    error("Native CryptoProvider not yet implemented. JVM-only in Phase 2.")
