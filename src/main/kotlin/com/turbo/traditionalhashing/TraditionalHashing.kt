package com.turbo.traditionalhashing

import com.turbo.common.BaseHashing
import com.turbo.hashing.HashFunction
import com.turbo.math.MathOperations

/**
 * Task 0 – Traditional (modulo-based) hashing
 *
 * GOAL: Implement a naive sharder that assigns a key to a server using `hash(key) % N`
 * where N is the number of servers. This approach is simple but highly unstable when N changes.
 *
 * TODOs for the student (keep English comments for consistency across tasks):
 * 1) Implement [addServer] so it adds a new unique server ID and keeps the list sorted (for tests).
 * 2) Implement [removeServer] so it removes the server ID if present.
 * 3) Implement [getServerForKey] using [md5ToLong] + modulo. Return null when no servers.
 */
open class TraditionalHashing :
    BaseHashing,
    HashFunction,   // brings md5ToLong(), toNonNegative()
    MathOperations  // brings gcd(), stdDevPercent()
{
    // Keep servers sorted for deterministic behaviour in tests & logs
    private val servers = mutableListOf<String>()

    override fun addServer(serverId: String) {
        TODO("Not yet implemented")

    }

    override fun removeServer(serverId: String) {
        TODO("Not yet implemented")

    }

    override fun getServerForKey(key: String): String? {
        TODO("Not yet implemented")
    }

    override fun getServersCount(): Int = servers.size
    override fun getAllServers(): Set<String> = servers.toSet()
}
