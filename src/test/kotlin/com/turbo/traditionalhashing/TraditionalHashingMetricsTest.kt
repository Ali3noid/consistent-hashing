package com.turbo.traditionalhashing

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("TraditionalHashingMetrics (empirical impact over sample keys)")
@Tag("metrics")
class TraditionalHashingMetricsTest {

    private lateinit var metrics: TraditionalHashingMetrics
    private lateinit var keys: List<String>

    private val EPS = 0.02

    private fun sampleKeys(n: Int = 100_000): List<String> =
        (0 until n).map { "key-$it" }

    @BeforeEach
    fun setup() {
        metrics = TraditionalHashingMetrics().also {
            listOf("server1", "server2", "server3").forEach(it::addServer)
        }
        keys = sampleKeys()
    }

    @Test
    @DisplayName("Addition impact for N=3 should be ~3/4 = 0.75")
    fun additionImpactN3() {
        val expected = 3.0 / 4.0
        val actual = metrics.impactOfAddition(keys, "serverX")
        assertEquals(expected, actual, EPS)
    }

    @Test
    @DisplayName("Removal impact for N=3 should be ~2/3 ≈ 0.6667")
    fun removalImpactN3() {
        val expected = 2.0 / 3.0
        val actual = metrics.impactOfRemoval(keys, "server2")
        assertEquals(expected, actual, EPS)
    }

    @Test
    @DisplayName("Generic op: N=3 -> remove one, add two (M=4) ≈ 0.833")
    fun genericImpactN3To4() {
        val expected = impactAnalyticalWithLabels(
            listOf("server1", "server2", "server3"),
            listOf("server1", "server3", "server4", "server5")
        )
        val actual = metrics.impact(keys) { copy ->
            copy.removeServer("server2")
            copy.addServer("server4")
            copy.addServer("server5")
        }
        assertEquals(expected, actual, EPS)
    }

    private fun impactAnalyticalWithLabels(before: List<String>, after: List<String>): Double {
        val beforeSize = before.size
        val afterSize = after.size
        if (beforeSize == afterSize && before == after) return 0.0
        if (beforeSize == 0 || afterSize == 0) return if (beforeSize == afterSize) 0.0 else 1.0

        tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) kotlin.math.abs(a) else gcd(b, a % b)

        val g = gcd(beforeSize, afterSize)
        val lcm = (beforeSize / g) * afterSize

        val idxBefore = before.withIndex().groupBy({ it.value }, { it.index })
        val idxAfter = after.withIndex().groupBy({ it.value }, { it.index })

        var pairs = 0L
        for ((label, ib) in idxBefore) {
            val ja = idxAfter[label] ?: continue
            for (i in ib) for (j in ja) {
                if (((i - j) % g + g) % g == 0) pairs++
            }
        }

        val unchanged = pairs.toDouble() / lcm.toDouble()
        return 1.0 - unchanged
    }

    @Test
    @DisplayName("Edge N=0 to M>0 should be 1.0")
    fun edgeEmptyToNonEmpty() {
        val empty = TraditionalHashingMetrics()
        val keysLocal = sampleKeys(10_000)
        val impact = empty.impact(keysLocal) { it.addServer("only") }
        assertEquals(1.0, impact, 1e-9)
    }

    @Test
    @DisplayName("Edge N>0 to M=0 should be 1.0")
    fun edgeNonEmptyToEmpty() {
        val single = TraditionalHashingMetrics().also { it.addServer("solo") }
        val keysLocal = sampleKeys(10_000)
        val impact = single.impact(keysLocal) { it.removeServer("solo") }
        assertEquals(1.0, impact, 1e-9)
    }

    @Test
    @DisplayName("No-op operation should yield zero impact")
    fun noOpImpact() {
        val actual = metrics.impact(keys) { /* no-op */ }
        assertEquals(0.0, actual, 1e-9)
    }
}
