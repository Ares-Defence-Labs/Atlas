package com.architect.atlastestclient

import com.architect.atlas.container.AtlasContainer
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
        fun getTestSingle(name: String) : TestSingle?{
            AtlasDI.injectContainer(AtlasContainer)
            return AtlasDI.resolveServiceNullableByName(name)
        }
    }
}
