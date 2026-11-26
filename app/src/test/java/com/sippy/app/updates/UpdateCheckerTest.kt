package com.sippy.app.updates

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {
    private fun info(code: Int) = UpdateInfo(
        versionCode = code,
        versionName = "1.3.0",
        apkUrl = "https://github.com/Cooreo/Sippy/releases/download/1.3.0/sippy-debug.apk",
        sha256 = "a".repeat(64),
        size = 1000L,
        notes = ""
    )

    @Test
    fun versionCodeDrivesUpdateAvailability() {
        assertTrue(UpdateChecker.isNewer(info(5), 4))
        assertFalse(UpdateChecker.isNewer(info(5), 5))
        assertFalse(UpdateChecker.isNewer(info(4), 5))
        assertFalse(UpdateChecker.isNewer(info(0), 4))
    }

    @Test
    fun legacyStringComparisonStillHandlesTwoDigitMinorVersions() {
        assertTrue(UpdateChecker.isNewer("1.10.0", "1.9.0"))
        assertFalse(UpdateChecker.isNewer("1.3.0", "1.3.0"))
        assertFalse(UpdateChecker.isNewer("1.2.9", "1.3.0"))
    }

    @Test
    fun checksumIsRecognisedWhenWellFormed() {
        assertTrue(info(5).hasChecksum)
        assertFalse(info(5).copy(sha256 = "short").hasChecksum)
    }
}
