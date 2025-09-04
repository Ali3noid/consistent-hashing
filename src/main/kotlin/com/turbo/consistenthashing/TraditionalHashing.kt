package com.turbo.consistenthashing

import java.security.MessageDigest
import kotlin.math.abs

/**
 * Traditional (modulo-based) hashing sharder.
 * - Keeps a sorted list of server IDs.
 * - Maps key -> index by abs(hash(key)) % servers.size
 * - Provides measurement utilities to assess remapping on server add/remove.
 */
open class TraditionalHashing : BaseHashing {
    private val servers = mutableListOf<String>() // kept sorted

    // MD5 -> Long, same approach as in BasicConsistentHashRing for fair comparison
    protected fun hash(input: String): Long {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        var result = 0L
        for (i in 0..7) {
            result = (result shl 8) or (digest[i].toLong() and 0xFFL)
        }
        return result
    }

    private fun nonNegative(value: Long): Long {
        // handle Long.MIN_VALUE edge case
        return if (value == Long.MIN_VALUE) 0L else abs(value)
    }

    override fun addServer(serverId: String) {
        if (servers.contains(serverId)) return
        servers.add(serverId)
        servers.sort()
    }

    override fun removeServer(serverId: String) {
        servers.remove(serverId)
    }

    override fun getServerForKey(key: String): String? {
        if (servers.isEmpty()) return null
        val h = nonNegative(hash(key))
        val idx = (h % servers.size).toInt()
        return servers[idx]
    }

    override fun getServersCount(): Int = servers.size

    override fun getAllServers(): Set<String> = servers.toSet()

    // Measurement utilities (analogous to StableConsistentHashRing)
    fun getKeyAssignments(keys: List<String>): Map<String, String?> =
        keys.associateWith { k -> getServerForKey(k) }

    fun countChangedAssignments(
        keys: List<String>,
        beforeAssignments: Map<String, String?>,
        afterAssignments: Map<String, String?>
    ): Int = keys.count { k -> beforeAssignments[k] != afterAssignments[k] }

    fun measureServerAdditionImpact(keys: List<String>, newServerId: String): Double {
        if (keys.isEmpty()) return 0.0
        val before = getKeyAssignments(keys)
        addServer(newServerId)
        val after = getKeyAssignments(keys)
        val changed = countChangedAssignments(keys, before, after)
        return changed.toDouble() / keys.size
    }

    fun measureServerRemovalImpact(keys: List<String>, serverId: String): Double {
        if (keys.isEmpty()) return 0.0
        val before = getKeyAssignments(keys)
        removeServer(serverId)
        val after = getKeyAssignments(keys)
        val changed = countChangedAssignments(keys, before, after)
        return changed.toDouble() / keys.size
    }
}
