package com.turbo.traditionalhashing

import com.turbo.math.MathOperations

/**
 * Measurement utilities for TraditionalHashing that DO NOT mutate the original sharder.
 * Analytical impact for modulo-based sharding:
 *   impact = 1 - gcd(N, M) / max(N, M)
 */
class TraditionalHashingMetrics(
    private val sharder: TraditionalHashing
) : MathOperations { // gain gcd() & stdDevPercent()

    /** Generic: apply [operation] on a COPY and compute impact N->M analytically. */
    fun impact(operation: (TraditionalHashing) -> Unit): Double {
        val beforeN = sharder.getServersCount()
        val tmp = TraditionalHashing().also { cloneFrom(sharder, it) }
        operation(tmp)
        val afterN = tmp.getServersCount()

        if (beforeN == afterN) return 0.0
        if (beforeN == 0 && afterN > 0) return 1.0
        if (afterN == 0 && beforeN > 0) return 1.0

        val g = gcd(beforeN, afterN)
        val unchanged = g.toDouble() / maxOf(beforeN, afterN).toDouble()
        return 1.0 - unchanged
    }

    fun impactOfAddition(newServerId: String): Double =
        impact { it.addServer(newServerId) }

    fun impactOfRemoval(serverId: String): Double =
        impact { it.removeServer(serverId) }

    /** Optional sampling for charts/sanity checks. */
    fun distribution(keys: List<String>): Map<String, Int> =
        keys.mapNotNull { sharder.getServerForKey(it) }
            .groupingBy { it }.eachCount()

    private fun cloneFrom(src: TraditionalHashing, dst: TraditionalHashing) {
        src.getAllServers().forEach { dst.addServer(it) }
    }
}
