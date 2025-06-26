package com.architect.atlastestclient

import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.asCFlow
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModels
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.software.TestSingle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "";
    }
}

class CompTestStandard {
    companion object {

        val serviceHandle = AtlasDI.resolveLazyService<TestSingle>()
        fun getTestSingle(name: String): TestSingle? {
            GlobalScope.launch {
                repeat(1000) {
                    GlobalScope.launch {
                        serviceHandle.value.helloThere()
                    }
                }
            }

            return AtlasDI.resolveServiceNullableByName(name)
        }
    }
}