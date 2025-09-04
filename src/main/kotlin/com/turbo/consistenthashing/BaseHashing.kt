package com.turbo.consistenthashing

interface BaseHashing {
    fun addServer(serverId: String)
    fun removeServer(serverId: String)
    fun getServerForKey(key: String): String?
    fun getServersCount(): Int
    fun getAllServers(): Set<String>
}

