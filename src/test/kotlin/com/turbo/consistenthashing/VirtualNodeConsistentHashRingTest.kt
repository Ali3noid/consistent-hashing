import com.turbo.com.turbo.consistenthashing.VirtualNodeConsistentHashRing
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("Task 4: Virtual Nodes Implementation")
@Tag("intermediate")
class VirtualNodeConsistentHashRingTest {

    private lateinit var ring: VirtualNodeConsistentHashRing

    @BeforeEach
    fun setup() {
        ring = VirtualNodeConsistentHashRing(100)
    }

    @Nested
    @DisplayName("Basic Virtual Nodes functionality")
    inner class BasicVirtualNodeTests {

        @Test
        @DisplayName("Should create the correct number of virtual nodes")
        fun shouldCreateCorrectNumberOfVirtualNodes() {
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")

            assertEquals(3, ring.getServersCount())
            assertEquals(300, ring.getVirtualNodesCount())
        }

        @Test
        @DisplayName("Removing a server should remove all its virtual nodes")
        fun shouldRemoveAllVirtualNodesWhenRemovingServer() {
            ring.addServer("server1")
            ring.addServer("server2")
            val initialVirtualNodes = ring.getVirtualNodesCount()

            ring.removeServer("server1")

            assertEquals(1, ring.getServersCount())
            assertEquals(100, ring.getVirtualNodesCount())
            assertTrue(ring.getVirtualNodesCount() < initialVirtualNodes)
        }

        @Test
        @DisplayName("Virtual nodes for the same server should be unique")
        fun shouldCreateUniqueVirtualNodesForSameServer() {
            ring.addServer("server1")

            assertEquals(100, ring.getVirtualNodesCount())
        }

        @Test
        @DisplayName("Consistency: the same key always on the same server")
        fun shouldBeConsistentAcrossMultipleCalls() {
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")
            val testKey = "consistencyTest"

            val servers = (1..100).map { ring.getServerForKey(testKey) }

            val uniqueServers = servers.toSet()
            assertEquals(1, uniqueServers.size)
            assertNotNull(servers.first())
        }
    }

    @Nested
    @DisplayName("Load distribution with Virtual Nodes")
    inner class LoadDistributionTests {

        @Test
        @DisplayName("Virtual nodes should provide good distribution")
        fun shouldProvideGoodDistribution() {
            ring.addServer("server1")
            ring.addServer("server2")
            ring.addServer("server3")
            val testKeys = (1..9000).map { "key$it" }

            val assignments = testKeys.associateWith { ring.getServerForKey(it) }
            val distribution = assignments.values.groupingBy { it }.eachCount()

            assertEquals(3, distribution.size)

            println("Distribution with Virtual Nodes (${ring.getVirtualNodesCount()} total virtual nodes):")

            val percentages = mutableListOf<Double>()
            distribution.forEach { (server, count) ->
                val percentage = (count.toDouble() / testKeys.size) * 100
                percentages.add(percentage)
                println("   $server: $count keys (${"%.1f".format(percentage)}%)")

                assertTrue(percentage in 25.0..42.0)
            }

            val stdDev = calculateStandardDeviationPercentage(percentages)
            println("   Standard deviation: ${"%.2f".format(stdDev)}%")
            assertTrue(stdDev < 4.0)
        }

        @ParameterizedTest(name = "VirtualNodes={0} → expected deviation < {1}%")
        @CsvSource(
            "10,  15.0",   // Very few VN - loose limit
            "25,  12.0",   // Few VN
            "50,  8.0",    // Medium VN
            "100, 6.0",    // Good VN - looser limit
            "150, 5.0",    // Very good VN
            "200, 4.0",    // Excellent VN
            "300, 3.0"     // Extreme VN
        )
        @DisplayName("Virtual nodes should provide acceptable distribution")
        fun shouldProvideAcceptableDistributionWithVirtualNodes(virtualNodes: Int, maxAllowedDeviation: Double) {
            val testRing = VirtualNodeConsistentHashRing(virtualNodes)
            testRing.addServer("server1")
            testRing.addServer("server2")
            testRing.addServer("server3")
            val testKeys = (1..6000).map { "key$it" }

            val assignments = testKeys.associateWith { testRing.getServerForKey(it) }
            val distribution = assignments.values.groupingBy { it }.eachCount()

            val percentages = distribution.values.map { count ->
                (count.toDouble() / testKeys.size) * 100
            }
            val standardDeviationPercent = calculateStandardDeviationPercentage(percentages)

            println("Virtual nodes: $virtualNodes → deviation: ${"%.2f".format(standardDeviationPercent)}% (limit: $maxAllowedDeviation%)")

            distribution.forEach { (server, count) ->
                val percentage = (count.toDouble() / testKeys.size) * 100
                println("     $server: $count keys (${"%.1f".format(percentage)}%)")
            }

            assertTrue(standardDeviationPercent <= maxAllowedDeviation,
                "Deviation ${"%.2f".format(standardDeviationPercent)}% exceeds the limit $maxAllowedDeviation% for $virtualNodes VN")

            percentages.forEach { percentage ->
                assertTrue(percentage in 20.0..50.0,
                    "Distribution ${"%.1f".format(percentage)}% is out of reasonable range")
            }
        }

        @Test
        @DisplayName("Overall trend: more virtual nodes on AVERAGE give better distribution")
        fun shouldShowOverallImprovementTrend() {
            val testConfigurations = listOf(
                10, 30, 50, 100, 200, 400
            )
            val testKeys = (1..8000).map { "key$it" }
            val results = mutableListOf<Pair<Int, Double>>()

            testConfigurations.forEach { vnCount ->
                val testRing = VirtualNodeConsistentHashRing(vnCount)
                listOf("serverA", "serverB", "serverC").forEach { server ->
                    testRing.addServer(server)
                }

                val distribution = testKeys.associateWith { testRing.getServerForKey(it) }
                    .values.groupingBy { it }.eachCount()

                val percentages = distribution.values.map { (it.toDouble() / testKeys.size) * 100 }
                val stdDev = calculateStandardDeviationPercentage(percentages)

                results.add(vnCount to stdDev)
                println("$vnCount VN → deviation: ${"%.2f".format(stdDev)}%")
            }

            // Check overall trend - average of first 3 vs average of last 3
            val earlyResults = results.take(3).map { it.second }
            val lateResults = results.takeLast(3).map { it.second }

            val avgEarly = earlyResults.average()
            val avgLate = lateResults.average()

            println("\nTrend of improvement:")
            println("   Average of first 3: ${"%.2f".format(avgEarly)}%")
            println("   Average of last 3: ${"%.2f".format(avgLate)}%")
            println("   Improvement: ${"%.1f".format(((avgEarly - avgLate) / avgEarly) * 100)}%")

            assertTrue(avgLate <= avgEarly,
                "On average, more VN should give better distribution: ${"%.2f".format(avgLate)}% vs ${"%.2f".format(avgEarly)}%")

            // Check that the worst case is significantly worse than the best
            val worst = results.maxBy { it.second }
            val best = results.minBy { it.second }

            println("   Worst: ${worst.first} VN → ${"%.2f".format(worst.second)}%")
            println("   Best: ${best.first} VN → ${"%.2f".format(best.second)}%")

            assertTrue(worst.second > best.second * 1.3,
                "There should be a clear separation in distribution quality")
        }

        @Test
        @DisplayName("Direct comparison of extreme cases")
        fun shouldShowClearDifferenceBetweenExtremes() {
            val ringVeryFew = VirtualNodeConsistentHashRing(5)   // Very few VN
            val ringVeryMany = VirtualNodeConsistentHashRing(500) // Very many VN

            val servers = listOf("server1", "server2", "server3")
            servers.forEach { server ->
                ringVeryFew.addServer(server)
                ringVeryMany.addServer(server)
            }

            val testKeys = (1..12000).map { "key$it" }

            val distributionFew = testKeys.associateWith { ringVeryFew.getServerForKey(it) }
                .values.groupingBy { it }.eachCount()
            val distributionMany = testKeys.associateWith { ringVeryMany.getServerForKey(it) }
                .values.groupingBy { it }.eachCount()

            val percentagesFew = distributionFew.values.map { (it.toDouble() / testKeys.size) * 100 }
            val percentagesMany = distributionMany.values.map { (it.toDouble() / testKeys.size) * 100 }

            val deviationFew = calculateStandardDeviationPercentage(percentagesFew)
            val deviationMany = calculateStandardDeviationPercentage(percentagesMany)

            println("Comparison of extremes:")
            println("   5 virtual nodes:   deviation = ${"%.2f".format(deviationFew)}%")
            println("   500 virtual nodes: deviation = ${"%.2f".format(deviationMany)}%")

            println("\nDistribution (5 VN):")
            distributionFew.forEach { (server, count) ->
                val pct = (count.toDouble() / testKeys.size) * 100
                println("     $server: ${"%.1f".format(pct)}%")
            }

            println("\nDistribution (500 VN):")
            distributionMany.forEach { (server, count) ->
                val pct = (count.toDouble() / testKeys.size) * 100
                println("     $server: ${"%.1f".format(pct)}%")
            }

            // 500 VN should be better THAN 5 VN (but not necessarily better than all others)
            assertTrue(deviationMany < deviationFew,
                "500 VN (${"%.2f".format(deviationMany)}%) should be better than 5 VN (${"%.2f".format(deviationFew)}%)")
        }

        @Test
        @DisplayName("Statistical test: virtual nodes vs distribution quality")
        fun shouldShowStatisticalRelationshipBetweenVNAndDistribution() {
            val testConfigurations = listOf(5, 15, 30, 60, 150, 300, 600)
            val testKeys = (1..10000).map { "key$it" }
            val results = mutableListOf<Pair<Int, Double>>()

            println("Statistical analysis of virtual nodes:")

            testConfigurations.forEach { vnCount ->
                val testRing = VirtualNodeConsistentHashRing(vnCount)
                listOf("serverA", "serverB", "serverC").forEach { server ->
                    testRing.addServer(server)
                }

                val distribution = testKeys.associateWith { testRing.getServerForKey(it) }
                    .values.groupingBy { it }.eachCount()

                val percentages = distribution.values.map { (it.toDouble() / testKeys.size) * 100 }
                val stdDev = calculateStandardDeviationPercentage(percentages)

                results.add(vnCount to stdDev)
                println("   $vnCount VN → deviation: ${"%.2f".format(stdDev)}%")
            }

            // Check overall correlation - does more VN = better on average
            val lowVN = results.filter { it.first <= 30 }.map { it.second }.average()
            val highVN = results.filter { it.first >= 150 }.map { it.second }.average()

            println("\nGroup comparison:")
            println("   Few VN (≤30): average deviation = ${"%.2f".format(lowVN)}%")
            println("   Many VN (≥150): average deviation = ${"%.2f".format(highVN)}%")

            assertTrue(highVN <= lowVN + 1.0,
                "On average, many VN should be better or similar to few VN")

            // Check that all results are within reasonable limits
            results.forEach { (vn, deviation) ->
                assertTrue(deviation < 20.0,
                    "$vn VN: deviation ${"%.2f".format(deviation)}% is too high")
                assertTrue(deviation > 0.0,
                    "$vn VN: deviation cannot be 0%")
            }

            // Minimum and maximum should differ
            val minDeviation = results.minBy { it.second }
            val maxDeviation = results.maxBy { it.second }

            println("\nRange of results:")
            println("   Best: ${minDeviation.first} VN → ${"%.2f".format(minDeviation.second)}%")
            println("   Worst: ${maxDeviation.first} VN → ${"%.2f".format(maxDeviation.second)}%")

            assertTrue(maxDeviation.second > minDeviation.second * 1.5,
                "There should be a clear range of distribution quality")
        }

        private fun calculateStandardDeviationPercentage(percentages: Collection<Double>): Double {
            if (percentages.isEmpty()) return 0.0
            val mean = percentages.average()
            val variance = percentages.map { percentage ->
                val diff = percentage - mean
                diff * diff
            }.average()
            return kotlin.math.sqrt(variance)
        }
    }

    @Nested
    @DisplayName("Edge cases Virtual Nodes")
    inner class VirtualNodeEdgeCaseTests {

        @Test
        @DisplayName("Should handle very few virtual nodes")
        fun shouldHandleVeryFewVirtualNodes() {
            val ringFew = VirtualNodeConsistentHashRing(1)

            ringFew.addServer("server1")
            ringFew.addServer("server2")

            assertEquals(2, ringFew.getServersCount())
            assertEquals(2, ringFew.getVirtualNodesCount())

            val server = ringFew.getServerForKey("testKey")
            assertNotNull(server)
        }

        @Test
        @DisplayName("Should handle very many virtual nodes")
        fun shouldHandleVeryManyVirtualNodes() {
            val ringMany = VirtualNodeConsistentHashRing(1000)

            ringMany.addServer("server1")
            val server = ringMany.getServerForKey("testKey")

            assertEquals(1, ringMany.getServersCount())
            assertEquals(1000, ringMany.getVirtualNodesCount())
            assertNotNull(server)
            assertEquals("server1", server)
        }

        @Test
        @DisplayName("Zero virtual nodes should be handled gracefully")
        fun shouldHandleZeroVirtualNodes() {
            val ringZero = VirtualNodeConsistentHashRing(0)

            ringZero.addServer("server1")
            val server = ringZero.getServerForKey("testKey")

            assertEquals(1, ringZero.getServersCount())
            assertEquals(0, ringZero.getVirtualNodesCount())
        }
    }
}
