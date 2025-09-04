package com.turbo.consistenthashing

import com.turbo.consistenthashing.ReplicatedConsistentHashRing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

@DisplayName("Task 5: Data Replication")
@Tag("intermediate")
class ReplicatedConsistentHashRingTest {

    private lateinit var ring: ReplicatedConsistentHashRing

    @BeforeEach
    fun setup() {
        ring = ReplicatedConsistentHashRing(virtualNodesPerServer = 100, replicationFactor = 3)
    }

    @Nested
    @DisplayName("Basic replication")
    inner class BasicReplicationTests {

        @Test
        @DisplayName("Should return the primary server for a key")
        fun shouldReturnPrimaryServer() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")
            ring.addServer("server4")

            // When
            val primary = ring.getPrimaryServerForKey("testKey")

            // Then
            Assertions.assertNotNull(primary, "Should return a primary server")
            Assertions.assertTrue(
                ring.getAllServers().contains(primary),
                "The primary server should exist in the ring"
            )
        }

        @Test
        @DisplayName("Should return the correct number of servers for replication")
        fun shouldReturnCorrectNumberOfServersForReplication() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")
            ring.addServer("server4")

            // When
            val allServers = ring.getAllServersForKey("testKey")

            // Then
            Assertions.assertEquals(3, allServers.size, "There should be 3 servers (replication factor)")
            Assertions.assertEquals(allServers.toSet().size, allServers.size, "Servers should be unique")

            val primary = ring.getPrimaryServerForKey("testKey")
            Assertions.assertEquals(
                primary, allServers.first(),
                "The first server should be the primary"
            )
        }

        @Test
        @DisplayName("Should handle the case when servers are fewer than the replication factor")
        fun shouldHandleFewerServersThanReplicationFactor() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2") // Only 2 servers, replication factor = 3

            // When
            val allServers = ring.getAllServersForKey("testKey")
            val hasFullReplication = ring.hasFullReplication("testKey")

            // Then
            Assertions.assertEquals(2, allServers.size, "Should return all available servers")
            Assertions.assertFalse(hasFullReplication, "Should not have full replication")
        }
    }

    @Nested
    @DisplayName("Full replication")
    inner class FullReplicationTests {

        @Test
        @DisplayName("Should recognize full replication")
        fun shouldRecognizeFullReplication() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")
            ring.addServer("server4")

            // When
            val hasFullReplication = ring.hasFullReplication("testKey")

            // Then
            Assertions.assertTrue(hasFullReplication, "Should have full replication")
        }

        @Test
        @DisplayName("Different keys may have different replica servers")
        fun shouldHaveDifferentReplicasForDifferentKeys() {
            // Given
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")
            ring.addServer("server4")

            // When
            val replicas1 = ring.getAllServersForKey("key1")
            val replicas2 = ring.getAllServersForKey("key2")

            // Then
            Assertions.assertNotEquals(replicas1, replicas2, "Different keys may have different replicas")
        }

        @ParameterizedTest(name = "Replication test with {0} servers")
        @ValueSource(ints = [1, 2, 3, 4, 5, 10])
        @DisplayName("Replication should work with different numbers of servers")
        fun shouldWorkWithDifferentNumberOfServers(serverCount: Int) {
            // Given
            repeat(serverCount) { i ->
                ring.addServer("server$i")
            }

            // When
            val allServers = ring.getAllServersForKey("testKey")
            val hasFullReplication = ring.hasFullReplication("testKey")

            // Then
            val expectedReplicas = minOf(serverCount, ring.getReplicationFactor())
            Assertions.assertEquals(
                expectedReplicas, allServers.size,
                "There should be $expectedReplicas replicas"
            )

            if (serverCount >= ring.getReplicationFactor()) {
                Assertions.assertTrue(hasFullReplication, "Should have full replication")
            } else {
                Assertions.assertFalse(hasFullReplication, "Should not have full replication")
            }
        }
    }
}