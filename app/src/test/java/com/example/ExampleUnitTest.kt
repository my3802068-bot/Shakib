package com.example

import com.example.data.model.DeviceBrand
import com.example.data.model.DeviceCatalog
import com.example.data.model.PerformanceProfile
import com.example.data.model.VirtualAppId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `device catalog provides infinix and realme android 14 profiles`() {
        val infinix = DeviceCatalog.forBrand(DeviceBrand.INFINIX)
        val realme = DeviceCatalog.forBrand(DeviceBrand.REALME)

        assertTrue(infinix.isNotEmpty())
        assertTrue(realme.isNotEmpty())
        assertEquals("Android 14", DeviceBrand.INFINIX.androidVersion)
        assertEquals("Android 14", DeviceBrand.REALME.androidVersion)
        assertEquals("XOS 14.5", DeviceBrand.INFINIX.osSkin)
        assertEquals("Realme UI 5.0", DeviceBrand.REALME.osSkin)
    }

    @Test
    fun `batch creation bounds and mini desktop limits are 1 to 5`() {
        assertEquals(1, DeviceCatalog.MIN_BATCH_COUNT)
        assertEquals(5, DeviceCatalog.MAX_BATCH_COUNT)
        assertEquals(5, DeviceCatalog.MAX_ACTIVE_MINI_DESKTOPS)
    }

    @Test
    fun `performance profile and virtual app lookups default safely`() {
        assertEquals(120, PerformanceProfile.fromId("SMOOTH_120").refreshRateHz)
        assertEquals(PerformanceProfile.SMOOTH_120, PerformanceProfile.fromId("unknown"))
        assertEquals(VirtualAppId.HOME, VirtualAppId.fromId("unknown"))
        assertEquals(VirtualAppId.PLAY_STORE, VirtualAppId.fromId("play_store"))
        assertTrue(DeviceCatalog.playStoreCatalog.isNotEmpty())
    }
}
