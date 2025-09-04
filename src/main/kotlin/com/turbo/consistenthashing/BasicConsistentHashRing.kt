package com.turbo.consistenthashing

import java.security.MessageDigest
import java.util.*


// =============================================================================
// TASK 1: Basic Consistent Hash Ring
// =============================================================================

/**
 * DESCRIPTION:
 * Implement the basic structure of a Consistent Hash Ring.
 * The ring should map keys to servers using a hash function.
 *
 * REQUIREMENTS:
 * - Adding/removing servers
 * - Finding a server for a given key
 * - Using TreeMap for efficient searching
 *
 * HINT: Use TreeMap<Long, String> where Long is the hash and String is the server ID.
 */

open class BasicConsistentHashRing {
    protected val ring = TreeMap<Long, String>()

    // Implemented hash function using MD5 on UTF-8 bytes.
    // From the first 8 bytes, we create a Long value, which serves as a position on the ring.
    protected fun hash(input: String): Long {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        var result = 0L
        for (i in 0..7) {
            result = (result shl 8) or (digest[i].toLong() and 0xFFL)
        }
        return result
    }

    // Add the server to the ring at position hash(serverId).
    // Duplicates do not increase the number of entries.
    open fun addServer(serverId: String) {
        val position = hash(serverId)
        ring.putIfAbsent(position, serverId)
    }

    // Remove the server from the ring based on its hash(serverId) position.
    open fun removeServer(serverId: String) {
        val position = hash(serverId)
        ring.remove(position)
    }

    // Find the first server clockwise for a given key.
    open fun getServerForKey(key: String): String? {
        if (ring.isEmpty()) return null
        val position = hash(key)
        val entry = ring.ceilingEntry(position) ?: ring.firstEntry()
        return entry?.value
    }

    // Helper functions.
    open fun getServersCount(): Int = ring.size

    open fun getAllServers(): Set<String> = ring.values.toSet()
}