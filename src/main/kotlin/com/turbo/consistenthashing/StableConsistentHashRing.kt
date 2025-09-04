package com.turbo.consistenthashing

// =============================================================================
// TASK 2: Stability during server changes 🟢
// =============================================================================

/**
 * DESCRIPTION:
 * Extend BasicConsistentHashRing to monitor key assignment changes.
 * Measure how many keys need to be moved when adding/removing a server.
 *
 * REQUIREMENTS:
 * - A method to check the assignment for a list of keys
 * - Method for counting changes after server modification
 * - Demonstration that changes only affect part of the data
 */
open class StableConsistentHashRing : BasicConsistentHashRing() {

    // Return the key -> server map for a list of keys
    fun getKeyAssignments(keys: List<String>): Map<String, String?> {
        return keys.associateWith { key -> getServerForKey(key) }
    }

    fun countChangedAssignments(
        keys: List<String>,
        beforeAssignments: Map<String, String?>,
        afterAssignments: Map<String, String?>
    ): Int {
        return keys.count { key -> beforeAssignments[key] != afterAssignments[key] }
    }

    fun measureServerAdditionImpact(keys: List<String>, newServerId: String): Double {
        if (keys.isEmpty()) return 0.0
        val beforeAssignments = getKeyAssignments(keys)
        addServer(newServerId)
        val afterAssignments = getKeyAssignments(keys)
        val changedCount = countChangedAssignments(keys, beforeAssignments, afterAssignments)
        return changedCount.toDouble() / keys.size
    }

    fun measureServerRemovalImpact(keys: List<String>, serverId: String): Double {
        if (keys.isEmpty()) return 0.0
        val beforeAssignments = getKeyAssignments(keys)
        removeServer(serverId)
        val afterAssignments = getKeyAssignments(keys)
        val changedCount = countChangedAssignments(keys, beforeAssignments, afterAssignments)
        return changedCount.toDouble() / keys.size
    }
}