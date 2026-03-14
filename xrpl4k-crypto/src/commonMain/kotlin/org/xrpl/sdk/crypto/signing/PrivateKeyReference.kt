package org.xrpl.sdk.crypto.signing

/**
 * A reference to a private key stored externally (HSM, KMS).
 * Marker interface for Phase 6 HSM support.
 */
public interface PrivateKeyReference : PrivateKeyable
