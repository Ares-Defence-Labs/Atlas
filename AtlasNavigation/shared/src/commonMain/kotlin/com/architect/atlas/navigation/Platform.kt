package com.architect.atlas.navigation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform