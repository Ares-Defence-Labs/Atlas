package com.architect.atlastestclient

import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.annotations.Singleton

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        AtlasContainer.resolve(Comps::class).test()
        return "Hello, ${platform.name}!"
    }
}

@Singleton
class Comps{
    var q = "hello"
    fun test(){
        q = "mAHAHAH"
    }
}