package com.architect.atlastestclient.software

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.architecture.navigation.Pushable
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.container.annotations.Factory
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModels
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.tabs.TabParentViewModel
import com.architect.kmpessentials.alerts.KmpAlert
import com.architect.kmpessentials.launcher.KmpLauncher
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

data class SampleProcess(val res: Int, var result: String, val listResult: List<Person>)

data class Person(val person: String, val rc: Int)

@ViewModels
class DroidStandard : ViewModel(), Pushable<String>, Poppable<Int> {
    init {
        var t = ""

    }

    val testText = MutableAtlasFlowState("Test String")
    val isShown = MutableAtlasFlowState(false)
    val resultsSamples = MutableAtlasFlowState(
        listOf(
            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),
            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),

            SampleProcess(0, "Sample 1", listOf(Person("Testing", 0))),
            SampleProcess(1, "Sample 2", listOf(Person("Testing 2", 2))),
            SampleProcess(2, "Sample 3", listOf(Person("Testing 3", 3))),
        )
    )

    var q = "";
    fun helloThere(): String {
        return q
    }

    fun setHelloTesting(s: String) {
        q = s
    }

    fun openSecondScreen() {
        KmpLogging.writeInfo("OPEN Second SCREEN", "RUNNING Second SCREEN")
        AtlasDI.resolveService<AtlasNavigationService>()
            .navigateToPage(DroidStandardSecond::class)
    }

    override fun onAppearing() {
        super.onAppearing()

        KmpLogging.writeInfo("APPEAR", "LAUNCH")
        KmpLauncher.startTimer(4.0) {
            //  testText.value = "Sample Int, THis works!"
            false
        }
    }

    override fun onDisappearing() {
        super.onDisappearing()
        KmpLogging.writeInfo("DISAPPEAR", "LAUNCH")

    }

    override suspend fun onInitialize() {
        super.onInitialize()

        KmpLogging.writeInfo("INIT FIRST SCREEN", "LAUNCH")

        KmpAlert.showAlert("", "INITIAZLIED FIRST SCREEN")
    }

    override fun onDestroy() {
        super.onDestroy()
        KmpLogging.writeInfo("DESTROY", "LAUNCH")
    }

    override fun onPushParams(params: String) {
        KmpLogging.writeInfo("PUSH", params)
    }

    override fun onPopParams(params: Int) {
        KmpLogging.writeInfo("POP", "$params")
    }

}


@ViewModels
class DroidStandardSecond : ViewModel(), Pushable<String>, Poppable<Int> {
    init {
        var t = ""
    }


    fun openThirdScreenPush() {
        KmpLogging.writeInfo("OPEN THIRD SCREEN", "RUNNING THIRD SCREEN")
        AtlasDI.resolveService<AtlasNavigationService>()
            .navigateToPage(TabParentViewModel::class)
    }

    fun openThirdScreenEntireStack() {
        KmpLogging.writeInfo("OPEN THIRD SCREEN", "RUNNING THIRD SCREEN")
        AtlasDI.resolveService<AtlasNavigationService>()
            .navigateToPagePushAndReplace(DroidStandardThird::class)
    }

    fun openThirdScreen() {
        KmpLogging.writeInfo("OPEN THIRD SCREEN", "RUNNING THIRD SCREEN")
        AtlasDI.resolveService<AtlasNavigationService>()
            .navigateToPagePushAndReplaceCurrentScreen(DroidStandardThird::class)
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
        KmpLogging.writeInfo("LOGIN", "APPEAR")
    }

    override fun onDisappearing() {
        super.onDisappearing()
        KmpLogging.writeInfo("LOGIN", "DISEAPPEARING")
    }

    override suspend fun onInitialize() {
        super.onInitialize()

        KmpLogging.writeInfo("SEDOND SCREEN", "INITIALIZE")
        KmpAlert.showAlert("", "INITIAZLIED SECOND SCREEN")
    }

    override fun onDestroy() {
        super.onDestroy()
        KmpLogging.writeInfo("SECOND PAGE", "DESTROY")
    }

    override fun onPushParams(params: String) {
        KmpLogging.writeInfo("PUSH", params)
    }

    override fun onPopParams(params: Int) {
        KmpLogging.writeInfo("POP", "$params")
    }
}


@ViewModels
class DroidStandardThird : ViewModel(), Pushable<String>, Poppable<Int> {
    init {
        var t = ""
    }

    fun replaceThirdScreenWithSecond() {
        KmpLogging.writeInfo("OPEN SECOND SCREEN CURRENT", "OPEN SECOND SCREEN CURRENT")
        AtlasDI.resolveService<AtlasNavigationService>()
            .navigateToPagePushAndReplaceCurrentScreen(DroidStandardSecond::class)
    }

    fun replaceEntireStackThirdScreenWithSecond() {
        KmpLogging.writeInfo("OPEN SECOND SCREEN CURRENT", "OPEN SECOND SCREEN CURRENT")
        AtlasDI.resolveService<AtlasNavigationService>()
            .navigateToPagePushAndReplace(DroidStandardSecond::class)
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
        KmpLogging.writeInfo("LOGIN", "APPEAR")
    }

    override fun onDisappearing() {
        super.onDisappearing()
        KmpLogging.writeInfo("LOGIN", "DISEAPPEARING")
    }

    override suspend fun onInitialize() {
        super.onInitialize()

        KmpLogging.writeInfo("LOGIN", "INITIALIZE")
        KmpAlert.showAlert("", "INITIAZLIED SECOND SCREEN")
    }

    override fun onDestroy() {
        super.onDestroy()
        KmpLogging.writeInfo("LOGIN", "DESTROY")
    }

    override fun onPushParams(params: String) {
        KmpLogging.writeInfo("PUSH", params)
    }

    override fun onPopParams(params: Int) {
        KmpLogging.writeInfo("POP", "$params")
    }
}