package com.architect.atlastestclient.software

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.architecture.navigation.Pushable
import com.architect.atlas.container.annotations.Factory
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModels
import com.architect.kmpessentials.logging.KmpLogging

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
class DroidStandard : ViewModel(), Pushable<String>, Poppable<Int> {
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

    override fun onPushParams(params: String) {
        KmpLogging.writeInfo("PUSH", params)
    }

    override fun onPopParams(params: Int) {
        KmpLogging.writeInfo("POP", "$params")
    }
}


@ViewModels
class DroidStandardSecond : ViewModel(), Pushable<String>, Poppable<Int>{
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

    override fun onPushParams(params: String) {
        KmpLogging.writeInfo("PUSH", params)
    }

    override fun onPopParams(params: Int) {
        TODO("Not yet implemented")
    }
}