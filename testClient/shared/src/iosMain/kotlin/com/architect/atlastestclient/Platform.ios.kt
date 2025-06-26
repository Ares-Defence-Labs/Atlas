package com.architect.atlastestclient

import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.kmpessentials.logging.KmpLogging
import platform.UIKit.UIDevice

class IOSPlatform: Platform {

    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

class TestIOS{
    companion object{
        fun registerServices(atlasNavigationService: AtlasNavigationService): String{
            KmpLogging.writeInfo("INJECTING CONTAINER", "SAMPLE CONTAINER")
            AtlasDI.injectContainer(AtlasContainer)

            AtlasDI.registerInstance(atlasNavigationService)
         //   AtlasDI.injectContainer(com.architect.atlas.container.AtlasContainer)
//            AtlasDI.registerSingleton(ReviewProcess())
//
//            AtlasDI.registerInterfaceToInstance(TestProcess::class, ReviewApps())
//
//            val test = AtlasDI.resolveServiceNullableByName("com.architect.atlastestclient.TestProcess") as? TestProcess
            return ""
        }
    }
}
