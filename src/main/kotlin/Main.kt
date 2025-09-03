package com.turbo

import com.turbo.com.turbo.consistenthashing.StableConsistentHashRing
import com.turbo.com.turbo.consistenthashing.TraditionalHashing

fun main() {
    // Demo: Compare traditional modulo hashing vs consistent hashing on node add/remove impact
    val servers = listOf("server1", "server2", "server3", "server4", "server5", "server6", "server7")
    val newServer = "server41"
    val keys = (1..5000).map { "key$it" }

    // Traditional hashing
    val traditional = TraditionalHashing().apply { servers.forEach { addServer(it) } }
    val tradAddImpact = traditional.measureServerAdditionImpact(keys, newServer)
    // revert to initial state for fair removal test
    traditional.removeServer(newServer)
    val tradRemoveImpact = traditional.measureServerRemovalImpact(keys, "server3")

    // Consistent hashing (basic stable ring)
    val consistent = StableConsistentHashRing().apply { servers.forEach { addServer(it) } }
    val consAddImpact = consistent.measureServerAdditionImpact(keys, newServer)
    // recreate for removal test with initial servers
    val consistent2 = StableConsistentHashRing().apply { servers.forEach { addServer(it) } }
    val consRemoveImpact = consistent2.measureServerRemovalImpact(keys, "server3")

    println("=== Remapping impact when adding/removing a node ===")
    println("Traditional hashing (modulo): add=${"%d".format((tradAddImpact * 100).toInt())}% remove=${"%d".format((tradRemoveImpact * 100).toInt())}%")
    println("Consistent hashing:          add=${"%d".format((consAddImpact * 100).toInt())}% remove=${"%d".format((consRemoveImpact * 100).toInt())}%")
}