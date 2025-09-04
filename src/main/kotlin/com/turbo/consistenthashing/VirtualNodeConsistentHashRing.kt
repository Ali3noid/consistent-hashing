package com.turbo.consistenthashing

// =============================================================================
// TASK 4: Virtual Nodes Implementation
// =============================================================================

/**
 * DESCRIPTION:
 * Implement Virtual Nodes for better load distribution.
 * Each physical server should have multiple positions on the ring.
 *
 * REQUIREMENTS:
 * - Configurable number of virtual nodes per server
 * - Better distribution than the basic version
 * - Compatibility with previous interfaces
 */
open class VirtualNodeConsistentHashRing(
    private val virtualNodesPerServer: Int = 150
) : BasicConsistentHashRing() {

    protected val serverVirtualNodes = mutableMapOf<String, MutableSet<Long>>()

    // Add a server with multiple virtual nodes
    override fun addServer(serverId: String) {
        if (serverVirtualNodes.containsKey(serverId)) return
        val positions = mutableSetOf<Long>()
        for (i in 0 until virtualNodesPerServer) {
            val position = hash("$serverId:$i")
            val previous = ring.putIfAbsent(position, serverId)
            if (previous == null) {
                positions.add(position)
            }
        }
        serverVirtualNodes[serverId] = positions
    }

    // Remove a server and all its virtual nodes
    override fun removeServer(serverId: String) {
        val positions = serverVirtualNodes.remove(serverId) ?: return
        positions.forEach { pos -> ring.remove(pos) }
    }

    // Helper methods
    override fun getServersCount(): Int = serverVirtualNodes.keys.size
    fun getVirtualNodesCount(): Int = ring.size
    override fun getAllServers(): Set<String> = serverVirtualNodes.keys
}
