package com.architect.atlastestclient

import com.architect.atlas.container.dsl.AtlasDI
import platform.UIKit.UIDevice

import platform.UIKit.UIFont
class IOSPlatform: Platform {

    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

class TestIOS{
    companion object{
        fun registerServices(): String{
            //AtlasDI.injectContainer(com.architect.atlas.container.AtlasContainer)
            AtlasDI.registerSingleton(ReviewProcess())

            AtlasDI.registerInterfaceToInstance(TestProcess::class, ReviewApps())

            val test = AtlasDI.resolveServiceNullableByName("com.architect.atlastestclient.TestProcess") as? TestProcess
            return test?.test() ?: ""
        }
    }
}
