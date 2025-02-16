package com.architect.atlastestclient

//import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.annotations.Factory
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModels

import com.architect.atlas.architecture.mvvm.ViewModel

class Greeting {
    private val platform: Platform = getPlatform()


    fun greet(): String {
//        AtlasContainer.resolve(Comps::class).test()
//        return "Hello, ${AtlasContainer.resolve(Comps::class).q}!"

        return ""
    }
}

@Factory
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


@Singleton
class TESTINGAGAIN{
    var q = "hello"
    fun test(){
        q = "testing49921814"
    }
}




@Singleton
class TESTCOMPLETEPOSTFUNCTION{
    var q = "hello"
    fun test(){
        q = "testing124"
    }
}



@ViewModels
class MainViewModel : ViewModel() {
    var q = "hello"
    fun test(){
        q = "testing124"
    }
}