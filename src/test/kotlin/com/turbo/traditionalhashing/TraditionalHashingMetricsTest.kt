package com.turbo.traditionalhashing

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@DisplayName("TraditionalHashingMetrics (analytical impact)")
@Tag("metrics")
class TraditionalHashingMetricsTest {

    private lateinit var ring: TraditionalHashing
    private lateinit var metrics: TraditionalHashingMetrics

    @BeforeEach
    fun setup() {
        ring = TraditionalHashing().also {
            listOf("server1","server2","server3").forEach(it::addServer)  // N = 3
        }
        metrics = TraditionalHashingMetrics(ring)
    }

    @Test
    @DisplayName("Addition impact for N=3 should be 3/4 = 0.75")
    fun additionImpactN3() {
        val expected = 3.0 / 4.0
        val actual = metrics.impactOfAddition("serverX")
        assertEquals(expected, actual, 1e-9)
    }

    @Test
    @DisplayName("Removal impact for N=3 should be 2/3")
    fun removalImpactN3() {
        val expected = 2.0 / 3.0
        val actual = metrics.impactOfRemoval("server2")
        assertEquals(expected, actual, 1e-9)
    }

    @Test
    @DisplayName("Generic op N=3 -> remove one add two = 3->4 should be 3/4")
    fun genericImpactN3To4() {
        val expected = 1.0 - (1.0 / 4.0) // 0.75
        val actual = metrics.impact { copy ->
            copy.removeServer("server2")
            copy.addServer("server4")
            copy.addServer("server5")
        }
        assertEquals(expected, actual, 1e-9)
    }

    @Test
    @DisplayName("Edge N=0 to M>0 should be 1.0")
    fun edgeEmptyToNonEmpty() {
        val empty = TraditionalHashing()
        val m = TraditionalHashingMetrics(empty)
        val impact = m.impact { it.addServer("only") }
        assertEquals(1.0, impact, 1e-9)
    }

    @Test
    @DisplayName("Edge N>0 to M=0 should be 1.0")
    fun edgeNonEmptyToEmpty() {
        val single = TraditionalHashing().also { it.addServer("solo") }
        val m = TraditionalHashingMetrics(single)
        val impact = m.impact { it.removeServer("solo") }
        assertEquals(1.0, impact, 1e-9)
    }

    @Test
    @DisplayName("No-op operation should yield zero impact")
    fun noOpImpact() {
        val impact = metrics.impact { /* do nothing */ }
        assertEquals(0.0, impact, 1e-9)
    }
}
