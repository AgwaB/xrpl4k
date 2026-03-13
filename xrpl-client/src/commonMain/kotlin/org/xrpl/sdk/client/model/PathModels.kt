package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Address

public class PathAlternative(
    public val pathsComputed: List<List<JsonElement>>,
    public val sourceAmount: JsonElement?,
    public val destinationAmount: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathAlternative) return false
        return pathsComputed == other.pathsComputed &&
            sourceAmount == other.sourceAmount &&
            destinationAmount == other.destinationAmount
    }

    override fun hashCode(): Int {
        var result = pathsComputed.hashCode()
        result = 31 * result + (sourceAmount?.hashCode() ?: 0)
        result = 31 * result + (destinationAmount?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PathAlternative(" +
            "pathsComputed=$pathsComputed, " +
            "sourceAmount=$sourceAmount, " +
            "destinationAmount=$destinationAmount" +
            ")"
}

public class PathFindResult(
    public val alternatives: List<PathAlternative>,
    public val sourceAccount: Address?,
    public val destinationAccount: Address?,
    public val destinationAmount: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathFindResult) return false
        return alternatives == other.alternatives &&
            sourceAccount == other.sourceAccount &&
            destinationAccount == other.destinationAccount &&
            destinationAmount == other.destinationAmount
    }

    override fun hashCode(): Int {
        var result = alternatives.hashCode()
        result = 31 * result + (sourceAccount?.hashCode() ?: 0)
        result = 31 * result + (destinationAccount?.hashCode() ?: 0)
        result = 31 * result + (destinationAmount?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PathFindResult(" +
            "alternatives=$alternatives, " +
            "sourceAccount=$sourceAccount, " +
            "destinationAccount=$destinationAccount, " +
            "destinationAmount=$destinationAmount" +
            ")"
}
