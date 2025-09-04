package com.turbo.consistenthashing

import com.turbo.consistenthashing.BasicConsistentHashRing
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("Task 1: Basic Consistent Hash Ring")
class BasicConsistentHashRingTest {

    private lateinit var ring: BasicConsistentHashRing

    @BeforeEach
    fun setup() {
        ring = BasicConsistentHashRing()
    }

    @Nested
    @DisplayName("Adding servers")
    inner class AddServerTests {

        @Test
        @DisplayName("Should add a single server")
        fun shouldAddSingleServer() {
            // Given & When
            ring.addServer("server1")

            // Then
            assertEquals(1, ring.getServersCount(), "There should be 1 server")
            assertTrue(ring.getAllServers().contains("server1"), "Should contain server1")
        }

        @Test
        @DisplayName("It should add multiple servers.")
        fun shouldAddMultipleServers() {
            // Given & When
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")

            // Then
            assertEquals(3, ring.getServersCount(), "There should be 3 servers")
            val allServers = ring.getAllServers()
            assertContains(allServers, "server1")
            assertContains(allServers, "server2")
            assertContains(allServers, "server3")
        }

        @Test
        @DisplayName("Should not add duplicate server")
        fun shouldNotAddDuplicateServer() {
            // Given
            ring.addServer("server1")
            val initialCount = ring.getServersCount()

            // When
            ring.addServer("server1") // Add the same server again

            // Then
            assertEquals(
                initialCount, ring.getServersCount(),
                "The number of servers should not increase with duplication."
            )
        }
    }

    @Nested
    @DisplayName("Znajdowanie serwera dla klucza")
    inner class GetServerTests {

        @Test
        @DisplayName("Should return null for an empty ring")
        fun shouldReturnNullForEmptyRing() {
            // When
            val server = ring.getServerForKey("testKey")

            // Then
            assertNull(server, "An empty ring should return null")
        }

        @Test
        @DisplayName("It should find the server when the ring is not empty")
        fun shouldFindServerWhenRingNotEmpty() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")

            // When
            val server = ring.getServerForKey("testKey")

            // Then
            assertNotNull(server, "Should find a server")
            assertTrue(
                ring.getAllServers().contains(server),
                "The returned server should be in the ring"
            )
        }

        @Test
        @DisplayName("The same key should always go to the same server")
        fun shouldBeConsistentForSameKey() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")
            val testKey = "consistencyTestKey"

            // When
            val server1 = ring.getServerForKey(testKey)
            val server2 = ring.getServerForKey(testKey)
            val server3 = ring.getServerForKey(testKey)

            // Then
            assertEquals(server1, server2, "Subsequent calls should return the same server")
            assertEquals(server2, server3, "Subsequent calls should return the same server")
        }

        @Test
        @DisplayName("Different keys may go to different servers")
        fun shouldDistributeKeysAcrossServers() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")

            // When - we test many different keys
            val assignments = (1..100).map { "key$it" }
                .associateWith { ring.getServerForKey(it) }

            val uniqueServers = assignments.values.toSet()

            // Then - we should use more than one server
            assertTrue(
                uniqueServers.size > 1,
                "The keys should be distributed among different servers, but only the following were used: $uniqueServers"
            )
        }
    }

    @Nested
    @DisplayName("Removing Servers")
    inner class RemoveServerTests {

        @Test
        @DisplayName("Should remove existing server.")
        fun shouldRemoveExistingServer() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")

            // When
            ring.removeServer("server2")

            // Then
            assertEquals(2, ring.getServersCount(), "There should be 2 servers left.")
            assertFalse(
                ring.getAllServers().contains("server2"),
                "server2 should no longer exist"
            )
            assertTrue(
                ring.getAllServers().contains("server1"),
                "server1 should still exist"
            )
            assertTrue(
                ring.getAllServers().contains("server3"),
                "server3 should still exist"
            )
        }

        @Test
        @DisplayName("Deleting a non-existent server should not cause an error")
        fun shouldHandleRemovalOfNonExistentServer() {
            // Given
            ring.addServer("server1")
            val initialCount = ring.getServersCount()

            ring.removeServer("nonExistentServer")

            // Then
            assertEquals(
                initialCount, ring.getServersCount(),
                "Number of servers should not change"
            )
        }

        @Test
        @DisplayName("It should still find servers after removing")
        fun shouldStillFindServersAfterRemoval() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")

            // When
            ring.removeServer("server2")
            val server = ring.getServerForKey("testKey")

            // Then
            assertNotNull(server, "It should still find the server")
            assertTrue(
                server in setOf("server1", "server3"),
                "The server should be one of the others"
            )
        }

        @Test
        @DisplayName("Removal of all servers should result in an empty ring")
        fun shouldResultInEmptyRingAfterRemovingAllServers() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")

            // When
            ring.removeServer("server1")
            ring.removeServer("server2")

            // Then
            assertEquals(0, ring.getServersCount(), "The ring should be empty")
            assertNull(
                ring.getServerForKey("testKey"),
                "An empty ring should return null"
            )
        }
    }

    @Nested
    @DisplayName("Edge cases and boundary scenarios")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty and null keys")
        fun shouldHandleEmptyAndSpecialKeys() {
            // Given
            ring.addServer("server1")

            // When & Then
            assertNotNull(ring.getServerForKey(""), "Empty string should be handled")
            assertNotNull(ring.getServerForKey(" "), "Space should be handled")
            assertNotNull(ring.getServerForKey("null"), "String 'null' should be handled")
        }

        @Test
        @DisplayName("Should support long keys")
        fun shouldHandleLongKeys() {
            // Given
            ring.addServer("server1")
            val longKey = "a".repeat(1000)

            // When
            val server = ring.getServerForKey(longKey)

            // Then
            assertNotNull(server, "The long key should be serviced.")
        }

        @Test
        @DisplayName("Should support keys with different characters")
        fun shouldHandleKeysWithSpecialCharacters() {
            // Given
            ring.addServer("server1")
            val specialKeys = listOf(
                "key-with-dashes", "key_with_underscores",
                "key with spaces", "key123", "klucz-żółć", "🔑emoji-key"
            )

            // When & Then
            specialKeys.forEach { key ->
                assertNotNull(
                    ring.getServerForKey(key),
                    "Key '$key' should be handled"
                )
            }
        }
    }
}