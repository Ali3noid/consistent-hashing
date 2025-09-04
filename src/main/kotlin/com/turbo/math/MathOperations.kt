package com.turbo.math

/**
 * Math helpers shared across implementations (with default bodies).
 */
interface MathOperations {

    /** Euclidean algorithm. */
    fun gcd(a: Int, b: Int): Int {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y != 0) {
            val t = x % y
            x = y
            y = t
        }
        return x
    }

    /** Standard deviation for percentages (or any Double collection). */
    fun stdDevPercent(values: Collection<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
}
