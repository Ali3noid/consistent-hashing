package com.turbo.consistenthashing

import com.turbo.consistenthashing.StableConsistentHashRing
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("Task 2: Stability when changing servers")
@Tag("basic")
class StableConsistentHashRingTest {

    private lateinit var ring: StableConsistentHashRing

    @BeforeEach
    fun setup() {
        ring = StableConsistentHashRing()
        ring.addServer("server1")
        ring.addServer("server2")
        ring.addServer("server3")
    }

    @Nested
    @DisplayName("Key assignments")
    inner class KeyAssignmentTests {

        @Test
        @DisplayName("Should return assignments for a list of keys")
        fun shouldReturnKeyAssignments() {
            // Given
            val testKeys = listOf("key1", "key2", "key3", "key4", "key5")

            // When
            val assignments = ring.getKeyAssignments(testKeys)

            // Then
            assertEquals(testKeys.size, assignments.size, "There should be an assignment for each key")
            testKeys.forEach { key ->
                assertTrue(assignments.containsKey(key), "Should contain key: $key")
                assertNotNull(assignments[key], "Assignment for $key should not be null")
                assertTrue(ring.getAllServers().contains(assignments[key]),
                    "Assigned server should exist in the ring")
            }
        }

        @Test
        @DisplayName("Should be consistent - the same keys always on the same servers")
        fun shouldBeConsistent() {
            // Given
            val testKeys = listOf("key1", "key2", "key3")

            // When
            val assignments1 = ring.getKeyAssignments(testKeys)
            val assignments2 = ring.getKeyAssignments(testKeys)

            // Then
            assertEquals(assignments1, assignments2, "Assignments should be identical")
        }
    }

    @Nested
    @DisplayName("Impact measurement")
    inner class ImpactMeasurementTests {

        @Test
        @DisplayName("Should count changes in assignments")
        fun shouldCountChangedAssignments() {
            // Given
            val before = mapOf("key1" to "server1", "key2" to "server2", "key3" to "server1")
            val after = mapOf("key1" to "server1", "key2" to "server3", "key3" to "server2")
            val keys = listOf("key1", "key2", "key3")

            // When
            val changes = ring.countChangedAssignments(keys, before, after)

            // Then
            assertEquals(2, changes, "There should be 2 changes (key2 and key3)")
        }

        @Test
        @DisplayName("Impact of adding a server should be limited")
        fun shouldHaveLimitedImpactWhenAddingServer() {
            // Given
            val testKeys = (1..1000).map { "key$it" }
            val before = ring.getKeyAssignments(testKeys)

            // When
            val impact = ring.measureServerAdditionImpact(testKeys, "server4")
            val after = ring.getKeyAssignments(testKeys)
            val changedKeys = testKeys.filter { key -> before[key] != after[key] }

            // Then
            assertTrue(impact > 0, "There should be some impact")
            assertTrue(impact < 0.5, "Impact should be less than 50%")
            println("Impact of adding server: ${(impact * 100).toInt()}%")
            println("Changed keys (${changedKeys.size}): $changedKeys")

            // Show from-which-server to-which-server changes for each changed key
            val changeDetails = changedKeys.associateWith { key -> "${before[key]} -> ${after[key]}" }
            println("Change details (key: from -> to):")
            changeDetails.forEach { (key, change) -> println("$key: $change") }

            // Distribution of keys per server before and after
            val beforeDist = before.values.groupingBy { it }.eachCount()
            val afterDist = after.values.groupingBy { it }.eachCount()
            println("Distribution before: $beforeDist")
            println("Distribution after: $afterDist")
        }

        @Test
        @DisplayName("Impact of removing a server should be limited")
        fun shouldHaveLimitedImpactWhenRemovingServer() {
            // Given
            val testKeys = (1..1000).map { "key$it" }

            // When
            val impact = ring.measureServerRemovalImpact(testKeys, "server3")

            // Then
            assertTrue(impact > 0, "There should be some impact")
            assertTrue(impact < 0.5, "Impact should be less than 50%")
            println("Impact of removing server: ${(impact * 100).toInt()}%")
        }
    }
}
