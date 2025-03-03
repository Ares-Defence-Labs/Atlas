package com.architect.atlastestclient

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModels
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.dns.DNSTest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface TestProcess{
    fun test() : String
}

class ReviewApps : TestProcess{
    override fun test() : String{
        return "TESTING AGAIN"
    }
}

@Singleton
class ReviewProcessTester{

    init {
        var q = "hello"
    }
    var rs = AtlasDI.resolveLazyService<Comps>()
    fun test() : String{
        return "TESTING AGAIN"
    }
}

class ReviewProcess{

    var rs = AtlasDI.resolveLazyService<Comps>()
    fun test() : String{
        return "TESTING AGAIN"
    }
}

class ComponentTest : Hello{

}

interface Hello{

}
class Sample{
    fun test() : String{
        return "Provides Successful test"
    }
}

class DNSComps : DNSTest{

}

@ViewModels
class Comps : BaseComps(){

}

open class BaseComps: ViewModel(){

}

@ViewModels
class MainViewModel: BaseComps(){

    var q = ""
    fun test(){
        q = "Hello there"
    }
}

