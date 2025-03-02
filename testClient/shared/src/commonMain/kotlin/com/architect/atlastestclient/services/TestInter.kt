package com.architect.atlastestclient.services

import com.architect.atlas.container.annotations.Module
import com.architect.atlas.container.annotations.Provides
import com.architect.atlastestclient.ComponentTest
import com.architect.atlastestclient.DNSComps
import com.architect.atlastestclient.Hello
import com.architect.atlastestclient.Sample
import com.architect.atlastestclient.dns.DNSTest
import com.architect.atlastestclient.software.MobileTest

class TestInter {
}


@Module
class TestMod{

    @Provides
    fun process() : Sample {
        return Sample()
    }

    @Provides
    fun getComponent(): Hello {
        return ComponentTest()
    }

    @Provides
    fun getComponentDNS(): DNSTest {
        return DNSComps()
    }

    @Provides
    fun getComponentMobileTest(): MobileTest {
        return MobileTest()
    }


}