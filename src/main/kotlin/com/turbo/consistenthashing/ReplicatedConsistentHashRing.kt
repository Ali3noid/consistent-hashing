package com.turbo.com.turbo.consistenthashing

// =============================================================================
// TASK 5: Data Replication
// =============================================================================

/**
 * DESCRIPTION:
 * Add replication support — each key should be assigned to N servers
 * (primary server + N-1 replicas) to increase reliability.
 *
 * REQUIREMENTS:
 * - Configurable number of replicas
 * - A method returning all servers for a key (primary + replicas)
 * - Replicas should be on consecutive servers in the clockwise direction
 */
open class ReplicatedConsistentHashRing(
    virtualNodesPerServer: Int = 150,
    private val replicationFactor: Int = 3
) : VirtualNodeConsistentHashRing(virtualNodesPerServer) {

    // Return the primary server for the key (delegates to base ring logic)
    open fun getPrimaryServerForKey(key: String): String? = getServerForKey(key)

    fun getAllServersForKey(key: String): List<String> {
        if (getServersCount() == 0) return emptyList()
        val distinctServersAvailable = getAllServers().size
        if (distinctServersAvailable == 0) return emptyList()
        val needed = minOf(replicationFactor, distinctServersAvailable)

        // Start from the primary position and walk clockwise collecting unique servers
        val position = hash(key)
        var entry = ring.ceilingEntry(position) ?: ring.firstEntry()
        val result = ArrayList<String>(needed)
        val seen = HashSet<String>()

        val startKey = entry.key
        while (result.size < needed) {
            val serverId = entry.value
            if (seen.add(serverId)) {
                result.add(serverId)
            }
            if (result.size >= needed) break
            val next = ring.higherEntry(entry.key) ?: ring.firstEntry()
            if (next.key == startKey) break
            entry = next
        }
        return result
    }

    // Check whether the key has full replication
    fun hasFullReplication(key: String): Boolean {
        if (getAllServers().size < replicationFactor) return false
        return getAllServersForKey(key).size >= replicationFactor
    }

    fun getReplicationFactor(): Int = replicationFactor
}
