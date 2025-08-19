package com.architect.wearosapp

import com.architect.atlas.container.annotations.Module
import com.architect.atlas.container.annotations.Provides

@Module
class SampleTestModule{
    @Provides
    fun sampleTestService() : String {
        return ""
    }
}