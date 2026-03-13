package org.xrpl.sdk.core.model.transaction

/**
 * Holds arbitrary fields for unknown transaction types received from the network.
 *
 * Ensures forward compatibility when the SDK encounters a [TransactionType] it does
 * not have a dedicated [TransactionFields] implementation for.
 *
 * @property fields The raw field map keyed by field name.
 */
public class UnknownTransactionFields(
    public val fields: Map<String, Any?>,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownTransactionFields) return false
        return fields == other.fields
    }

    override fun hashCode(): Int = fields.hashCode()

    override fun toString(): String = "UnknownTransactionFields(fields=$fields)"
}
