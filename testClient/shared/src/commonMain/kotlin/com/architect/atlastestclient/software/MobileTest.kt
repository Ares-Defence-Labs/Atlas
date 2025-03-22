package com.architect.atlastestclient.software

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.annotations.Factory
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModels

class MobileTest {
    fun helloThere(): String {
        return "Component Test Standard"
    }
}

@Singleton
class TestSingle {
    fun helloThere(): String {

        return "TestSingle Test Standard"
    }
}

@Factory
class TestHelloThere {
    fun helloThere(): String {
        return "TestHelloThereFactory Test Standard"
    }
}

@ViewModels
class DroidStandard : ViewModel() {
    init {
        var t = ""
    }
    var q = "";
    fun helloThere(): String {
        return q
    }

    fun setHelloTesting(s: String) {
        q = s
    }
}