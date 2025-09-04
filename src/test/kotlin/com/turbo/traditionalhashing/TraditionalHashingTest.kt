package com.turbo.traditionalhashing

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.*

@DisplayName("Task 0: Traditional Hashing (Modulo)")
class TraditionalHashingTest {

    private lateinit var ring: TraditionalHashing

    @BeforeEach
    fun setup() {
        ring = TraditionalHashing()
    }

    @Nested
    @DisplayName("Adding servers")
    inner class AddServerTests {
        @Test
        fun shouldAddSingleServer() {
            ring.addServer("server1")
            assertEquals(1, ring.getServersCount())
            assertTrue(ring.getAllServers().contains("server1"))
        }

        @Test
        fun shouldAddMultipleServers() {
            listOf("server1","server2","server3").forEach(ring::addServer)
            assertEquals(3, ring.getServersCount())
            assertTrue(ring.getAllServers().containsAll(setOf("server1","server2","server3")))
        }

        @Test
        fun shouldNotAddDuplicateServer() {
            ring.addServer("server1")
            val initial = ring.getServersCount()
            ring.addServer("server1")
            assertEquals(initial, ring.getServersCount())
        }
    }

    @Nested
    @DisplayName("Get server for key")
    inner class GetServerTests {
        @Test
        fun shouldReturnNullForEmpty() {
            assertNull(ring.getServerForKey("k"))
        }

        @Test
        fun shouldBeConsistentForSameKey() {
            listOf("s1","s2","s3").forEach(ring::addServer)
            val key = "consistency"
            val a = ring.getServerForKey(key)
            val b = ring.getServerForKey(key)
            val c = ring.getServerForKey(key)
            assertEquals(a,b); assertEquals(b,c)
        }

        @Test
        fun shouldDistributeKeysAcrossServers() {
            listOf("s1","s2","s3").forEach(ring::addServer)
            val assignments = (1..300).associate { it.toString() to ring.getServerForKey(it.toString()) }
            val used = assignments.values.filterNotNull().toSet()
            assertTrue(used.size > 1, "Expected >1 servers to be used, got: $used")
        }
    }

    @Nested
    @DisplayName("Removing servers")
    inner class RemoveServerTests {
        @Test
        fun shouldRemoveExisting() {
            listOf("s1","s2","s3").forEach(ring::addServer)
            ring.removeServer("s2")
            assertEquals(2, ring.getServersCount())
            assertFalse(ring.getAllServers().contains("s2"))
        }

        @Test
        fun removingNonExistentIsNoop() {
            ring.addServer("s1")
            val before = ring.getServersCount()
            ring.removeServer("unknown")
            assertEquals(before, ring.getServersCount())
        }
    }
}
