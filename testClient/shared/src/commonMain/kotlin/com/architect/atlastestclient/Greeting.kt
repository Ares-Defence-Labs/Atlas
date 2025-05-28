package com.architect.atlastestclient

import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.asCFlow
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.software.TestSingle

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "";
    }
}

class CompTestStandard{
    companion object{
        var test = MutableAtlasFlowState("")
        fun getTestSingle(name: String) : TestSingle?{
            ///AtlasDI.injectContainer(AtlasContainer)
//            test.
//            test.asStateFlow().asCFlow().observe {
//
//            }
//
//            test.asStateFlow().asCFlow().observeMain {
//
//            }

            return AtlasDI.resolveServiceNullableByName(name)
        }
    }
}


@Singleton
class TestSampleData{

}