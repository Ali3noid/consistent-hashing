package com.turbo.traditionalhashing

/**
 * Measurement utilities for TraditionalHashing that DO NOT mutate the original sharder.
 * Analytical impact for modulo-based sharding:
 *   impact = 1 - gcd(N, M) / max(N, M)
 */
class TraditionalHashingMetrics : TraditionalHashing() {

    // Return the key -> server map for a list of keys
    fun getKeyAssignments(keys: List<String>): Map<String, String?> {
        return keys.associateWith { key -> getServerForKey(key) }
    }

    /** Generic: apply [operation] on a COPY and compute impact. */
    fun impact(keys: List<String>, operation: (TraditionalHashing) -> Unit): Double {
        if (keys.isEmpty()) return 0.0

        val before = getKeyAssignments(keys)
        val tmp = TraditionalHashing().apply {
            this@TraditionalHashingMetrics.getAllServers().forEach { addServer(it) }
        }
        operation(tmp)
        val after = keys.associateWith { key -> tmp.getServerForKey(key) }

        val changed = keys.count { key -> (before[key] != after[key]) }

        return changed.toDouble() / keys.size.toDouble()
    }

    fun impactOfAddition(keys: List<String>, newServerId: String): Double =
        impact(keys) { it.addServer(newServerId) }

    fun impactOfRemoval(keys: List<String>, serverId: String): Double =
        impact(keys) { it.removeServer(serverId) }

    /** Optional sampling for charts/sanity checks. */
    fun distribution(keys: List<String>): Map<String, Int> =
        keys.mapNotNull { getServerForKey(it) }
            .groupingBy { it }.eachCount()
}
