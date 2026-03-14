package org.xrpl.sdk.client.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class MetadataUtilsTest : FunSpec({

    test("extracts XChainClaimID from successful metadata") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tesSUCCESS")
                putJsonArray("AffectedNodes") {
                    add(
                        buildJsonObject {
                            putJsonObject("CreatedNode") {
                                put("LedgerEntryType", "XChainOwnedClaimID")
                                putJsonObject("NewFields") {
                                    put("XChainClaimID", "1")
                                }
                            }
                        },
                    )
                }
            }

        getXChainClaimId(metadata) shouldBe "1"
    }

    test("returns null when TransactionResult is not tesSUCCESS") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tecNO_ENTRY")
                putJsonArray("AffectedNodes") {
                    add(
                        buildJsonObject {
                            putJsonObject("CreatedNode") {
                                put("LedgerEntryType", "XChainOwnedClaimID")
                                putJsonObject("NewFields") {
                                    put("XChainClaimID", "1")
                                }
                            }
                        },
                    )
                }
            }

        getXChainClaimId(metadata) shouldBe null
    }

    test("throws IllegalArgumentException when AffectedNodes is missing") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tesSUCCESS")
            }

        shouldThrow<IllegalArgumentException> {
            getXChainClaimId(metadata)
        }
    }

    test("throws IllegalArgumentException when TransactionResult is missing") {
        val metadata =
            buildJsonObject {
                putJsonArray("AffectedNodes") {}
            }

        shouldThrow<IllegalArgumentException> {
            getXChainClaimId(metadata)
        }
    }

    test("returns null when successful but no XChainOwnedClaimID node exists") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tesSUCCESS")
                putJsonArray("AffectedNodes") {
                    add(
                        buildJsonObject {
                            putJsonObject("ModifiedNode") {
                                put("LedgerEntryType", "AccountRoot")
                            }
                        },
                    )
                }
            }

        getXChainClaimId(metadata) shouldBe null
    }

    test("returns first XChainClaimID when multiple CreatedNodes exist") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tesSUCCESS")
                putJsonArray("AffectedNodes") {
                    add(
                        buildJsonObject {
                            putJsonObject("CreatedNode") {
                                put("LedgerEntryType", "XChainOwnedClaimID")
                                putJsonObject("NewFields") {
                                    put("XChainClaimID", "1")
                                }
                            }
                        },
                    )
                    add(
                        buildJsonObject {
                            putJsonObject("CreatedNode") {
                                put("LedgerEntryType", "XChainOwnedClaimID")
                                putJsonObject("NewFields") {
                                    put("XChainClaimID", "2")
                                }
                            }
                        },
                    )
                }
            }

        getXChainClaimId(metadata) shouldBe "1"
    }

    test("returns null when tesSUCCESS but AffectedNodes is empty array") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tesSUCCESS")
                putJsonArray("AffectedNodes") {}
            }

        getXChainClaimId(metadata) shouldBe null
    }

    test("returns null when XChainClaimID is missing from NewFields") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tesSUCCESS")
                putJsonArray("AffectedNodes") {
                    add(
                        buildJsonObject {
                            putJsonObject("CreatedNode") {
                                put("LedgerEntryType", "XChainOwnedClaimID")
                                putJsonObject("NewFields") {
                                    put("Account", "rSomeAccount")
                                }
                            }
                        },
                    )
                }
            }

        getXChainClaimId(metadata) shouldBe null
    }

    test("ignores ModifiedNode with XChainOwnedClaimID (only CreatedNode matters)") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tesSUCCESS")
                putJsonArray("AffectedNodes") {
                    add(
                        buildJsonObject {
                            putJsonObject("ModifiedNode") {
                                put("LedgerEntryType", "XChainOwnedClaimID")
                                putJsonObject("FinalFields") {
                                    put("XChainClaimID", "99")
                                }
                            }
                        },
                    )
                }
            }

        getXChainClaimId(metadata) shouldBe null
    }

    test("returns null for tecCLAIM_NO_BACKER even with valid CreatedNode") {
        val metadata =
            buildJsonObject {
                put("TransactionResult", "tecCLAIM_NO_BACKER")
                putJsonArray("AffectedNodes") {
                    add(
                        buildJsonObject {
                            putJsonObject("CreatedNode") {
                                put("LedgerEntryType", "XChainOwnedClaimID")
                                putJsonObject("NewFields") {
                                    put("XChainClaimID", "1")
                                }
                            }
                        },
                    )
                }
            }

        getXChainClaimId(metadata) shouldBe null
    }
})
