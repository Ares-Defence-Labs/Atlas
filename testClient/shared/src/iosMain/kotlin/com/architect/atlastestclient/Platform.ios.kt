package com.architect.atlastestclient

import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlas.container.dsl.AtlasDI.container
import com.architect.atlastestclient.software.TestHelloThere
import com.architect.atlastestclient.software.TestSingle
import com.architect.kmpessentials.logging.KmpLogging
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

class TesterShare{
    companion object{
        fun processTest(){
            AtlasDI.injectContainer(AtlasContainer)
            AtlasDI.registerSingleton(ReviewProcess())
            //AtlasDI.registerSingleton(TestProcess::class,  ReviewApps());

            container?.register(TestProcess::class, ReviewApps(), null, null, false)

            var ts = AtlasDI.resolveService<TestSingle>()
            var tsc = AtlasDI.resolveService<TestHelloThere>()

            KmpLogging.writeInfo("RES", "TEST RESULT ${ts.helloThere()}")
            KmpLogging.writeInfo("RES2", "TEST RESULT ${tsc.helloThere()}")

        }
    }
}