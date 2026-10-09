package com.example

import com.example.update.AppUpdateManager
import com.example.update.UpdateInfo
import com.example.update.UpdateStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppUpdateLogicTest {

    @Test
    fun testCurrentAppVersionIsOnePointZero() {
        // User requirement: The current version of the app will be 1.0
        assertEquals("1.0", AppUpdateManager.CURRENT_VERSION_NAME)
        assertEquals(1, AppUpdateManager.CURRENT_VERSION_CODE)
    }

    @Test
    fun testUpdateInfoStructure() {
        val update = UpdateInfo(
            versionCode = 2,
            versionName = "1.1",
            downloadUrl = "https://github.com/prapanbiswas/podderpara-tshirt-orders/releases/download/v1.1/PodderparaDurgaMandir-v1.1.apk",
            releaseNotes = "New factory subtotal sheet and individual orders spreadsheet",
            fileSize = 15_420_000L
        )

        assertEquals("1.1", update.versionName)
        assertEquals(2, update.versionCode)
        assertTrue(update.downloadUrl.endsWith(".apk"))
        assertTrue(update.fileSize > 0)
    }

    @Test
    fun testVersionComparisonLogic() {
        val currentVer = "1.0"
        val newerVer = "1.1"
        val majorNewerVer = "2.0"
        val olderVer = "0.9"
        val sameVer = "1.0"

        fun isNewer(current: String, remote: String): Boolean {
            val cParts = current.split(".").map { it.toInt() }
            val rParts = remote.split(".").map { it.toInt() }
            for (i in 0 until maxOf(cParts.size, rParts.size)) {
                val c = cParts.getOrElse(i) { 0 }
                val r = rParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        }

        assertTrue(isNewer(currentVer, newerVer))
        assertTrue(isNewer(currentVer, majorNewerVer))
        assertFalse(isNewer(currentVer, sameVer))
        assertFalse(isNewer(currentVer, olderVer))
    }

    @Test
    fun testUpdateStatusTransitions() {
        AppUpdateManager.dismissUpdate()
        assertEquals(UpdateStatus.Idle, AppUpdateManager.updateStatus.value)

        val info = UpdateInfo(
            versionCode = 2,
            versionName = "1.1",
            downloadUrl = "https://example.com/app.apk",
            releaseNotes = "Release v1.1"
        )

        val available = UpdateStatus.Available(info, isDownloaded = false, localFile = null)
        assertFalse(available.isDownloaded)
        assertEquals("1.1", available.info.versionName)

        val tempApk = File.createTempFile("test_update", ".apk")
        tempApk.writeText("sample apk content")

        val ready = UpdateStatus.ReadyToInstall(info, tempApk)
        assertTrue(ready.file.exists())

        tempApk.delete()
    }
}
