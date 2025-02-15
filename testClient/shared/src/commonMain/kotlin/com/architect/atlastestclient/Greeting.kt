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
        q = "testing"
    }
}


@Singleton
class CompsTestStatus{
    var q = "hello"
    fun test(){
        q = "testing124"
    }
}



@Singleton
class HELLOTHERE{
    var q = "hello"
    fun test(){
        q = "testing12452356"
    }
}


@Singleton
class CompsTestNUMBERONEStatus{
    var q = "hello"
    fun test(){
        q = "testing49921814"
    }
}