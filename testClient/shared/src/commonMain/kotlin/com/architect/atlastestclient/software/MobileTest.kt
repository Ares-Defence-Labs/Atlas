package com.architect.atlastestclient.software

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.architecture.navigation.Pushable
import com.architect.atlas.container.annotations.Factory
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModels
import com.architect.kmpessentials.alerts.KmpAlert
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

    override fun onAppearing() {
        super.onAppearing()
        KmpLogging.writeInfo("APPEAR","LAUNCH")
    }

    override fun onDisappearing() {
        super.onDisappearing()
        KmpLogging.writeInfo("DISAPPEAR","LAUNCH")
    }

    override fun onInitialize() {
        super.onInitialize()

        KmpLogging.writeInfo("INIT","LAUNCH")
    }

    override fun onDestroy() {
        super.onDestroy()
        KmpLogging.writeInfo("DESTROY","LAUNCH")
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


    override fun onAppearing() {
        super.onAppearing()
        KmpLogging.writeInfo("LOGIN","APPEAR")
    }

    override fun onDisappearing() {
        super.onDisappearing()
        KmpLogging.writeInfo("LOGIN","DISEAPPEARING")
    }

    override fun onInitialize() {
        super.onInitialize()

        KmpLogging.writeInfo("LOGIN","INITIALIZE")
    }

    override fun onDestroy() {
        super.onDestroy()
        KmpLogging.writeInfo("LOGIN","DESTROY")
    }

    override fun onPushParams(params: String) {
        KmpAlert.showAlert("","")
        KmpLogging.writeInfo("PUSH", params)
    }

    override fun onPopParams(params: Int) {
        KmpLogging.writeInfo("POP", "$params")
    }
}