package com.turbo.hashing

import java.security.MessageDigest
import kotlin.math.abs

/**
 * Contract for hashing strategies. Provides a default MD5->Long hash
 * for apples-to-apples comparison across implementations.
 */
interface HashFunction {
    /** Compute a 64-bit value from MD5 (first 8 bytes). */
    fun md5ToLong(input: String): Long {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        var result = 0L
        for (i in 0..7) {
            result = (result shl 8) or (digest[i].toLong() and 0xFFL)
        }
        return result
    }

    /** Make sure the value is non-negative (handles Long.MIN_VALUE as well). */
    fun toNonNegative(value: Long): Long =
        if (value == Long.MIN_VALUE) 0L else abs(value)
}
