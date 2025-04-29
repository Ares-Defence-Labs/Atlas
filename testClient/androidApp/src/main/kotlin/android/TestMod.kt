package com.architect.atlastestclient.android

import com.architect.atlas.container.annotations.Module
import com.architect.atlas.container.annotations.Provides

@Module
class TestMod{

    @Provides
    fun testSample() : Dep {
        return Dep()
    }
}


