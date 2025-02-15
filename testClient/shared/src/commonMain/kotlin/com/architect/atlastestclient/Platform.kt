package com.architect.atlastestclient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform