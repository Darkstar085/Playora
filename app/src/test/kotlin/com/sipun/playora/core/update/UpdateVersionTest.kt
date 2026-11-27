package com.sipun.playora.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVersionTest {
    @Test
    fun comparesNumericVersionParts() {
        assertTrue(UpdateManager.isNewerVersion("0.9.9", "0.10.0"))
        assertTrue(UpdateManager.isNewerVersion("1.0", "1.0.1"))
        assertFalse(UpdateManager.isNewerVersion("1.2.0", "1.2"))
        assertFalse(UpdateManager.isNewerVersion("2.0.0", "1.99.99"))
    }

    @Test
    fun handlesVPrefixAndEqualVersions() {
        // v prefix handling
        assertTrue(UpdateManager.isNewerVersion("v1.0.0", "v1.0.1"))
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "v1.1.0"))
        assertTrue(UpdateManager.isNewerVersion("v0.5.0", "0.6.0"))

        // Equal versions should be false
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "v1.0.0"))
    }

    @Test
    fun handlesMultiSegmentAndSuffixes() {
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "1.0.0.1"))
        assertFalse(UpdateManager.isNewerVersion("1.0.0.1", "1.0.0"))
        assertTrue(UpdateManager.isNewerVersion("1.0.0-beta1", "1.0.1"))
    }

    @Test
    fun parsesAppReleaseManifestAndDetectsUpToDate() {
        val manifestJson = """
            {
              "version": "1.2.0",
              "tag": "v1.2.0",
              "name": "Playora 1.2.0",
              "description": "## What's new\n\n- Improved [player](https://example.com) seekbar\n- Fixed crashes\n\n**Full Changelog:** https://github.com/Darkstar085/Playora/compare/v1.1.0...v1.2.0\n",
              "download_url": "https://github.com/Darkstar085/Playora/releases/download/v1.2.0/Playora_v1.2.0.apk",
              "size": 34701134,
              "sha256": "f3b69a4ea5aae98b94640b7bd7d7ad412b045c4d81d40dfb436dcbd78b0004a7",
              "published_at": "2026-11-24T02:25:50Z"
            }
        """.trimIndent()

        val result = UpdateManager.parseManifestLookup(manifestJson, "1.1.0")
        assertTrue(result is UpdateManager.ManifestLookupResult.Success)
        val update = (result as UpdateManager.ManifestLookupResult.Success).update
        assertNotNull(update)
        assertEquals("1.2.0", update!!.version)
        assertEquals("Playora 1.2.0", update.name)
        assertEquals("Playora_v1.2.0.apk", update.fileName)
        assertEquals(
            "sha256:f3b69a4ea5aae98b94640b7bd7d7ad412b045c4d81d40dfb436dcbd78b0004a7",
            update.digest,
        )
        assertEquals("Improved player seekbar\nFixed crashes", update.notes)

        val upToDate = UpdateManager.parseManifestLookup(manifestJson, "1.2.0")
        assertTrue(upToDate is UpdateManager.ManifestLookupResult.Success)
        assertNull((upToDate as UpdateManager.ManifestLookupResult.Success).update)
    }

    @Test
    fun invalidManifestTriggersFallback() {
        val brokenManifest = """{"version": "1.2.0", "sha256": "short"}"""
        val result = UpdateManager.parseManifestLookup(brokenManifest, "1.1.0")
        assertTrue(result is UpdateManager.ManifestLookupResult.Unavailable)
    }

    @Test
    fun parsesGithubReleaseFallback() {
        val releaseJson = """
            {
              "tag_name": "v1.2.0",
              "name": "Playora v1.2.0",
              "body": "## What's new\n\n- Fallback release notes\n",
              "published_at": "2026-11-24T02:25:54Z",
              "draft": false,
              "prerelease": false,
              "assets": [
                {
                  "url": "https://api.github.com/repos/Darkstar085/Playora/releases/assets/123",
                  "name": "Playora_v1.2.0.apk",
                  "browser_download_url": "https://github.com/Darkstar085/Playora/releases/download/v1.2.0/Playora_v1.2.0.apk",
                  "content_type": "application/vnd.android.package-archive",
                  "size": 34701134,
                  "digest": "sha256:f3b69a4ea5aae98b94640b7bd7d7ad412b045c4d81d40dfb436dcbd78b0004a7"
                }
              ]
            }
        """.trimIndent()

        val update = UpdateManager.parseRelease(releaseJson, "1.1.0")
        assertNotNull(update)
        assertEquals("1.2.0", update!!.version)
        assertEquals("Playora_v1.2.0.apk", update.fileName)
        assertEquals("Fallback release notes", update.notes)
    }

    @Test
    fun parsesManifestWithBlankDateAndNormalizedUppercaseDigest() {
        val manifestJson = """
            {
              "version": "v1.3.0",
              "download_url": "https://github.com/Darkstar085/playora/releases/download/v1.3.0/app-release.apk",
              "sha256": "SHA256:F3B69A4EA5AAE98B94640B7BD7D7AD412B045C4D81D40DFB436DCBD78B0004A7"
            }
        """.trimIndent()

        val result = UpdateManager.parseManifestLookup(manifestJson, "1.1.0")
        assertTrue(result is UpdateManager.ManifestLookupResult.Success)
        val update = (result as UpdateManager.ManifestLookupResult.Success).update
        assertNotNull(update)
        assertEquals("1.3.0", update!!.version)
        assertEquals("Playora 1.3.0", update.name)
        assertEquals("Recently", update.releaseDate)
        assertEquals(
            "sha256:f3b69a4ea5aae98b94640b7bd7d7ad412b045c4d81d40dfb436dcbd78b0004a7",
            update.digest,
        )
    }

    @Test
    fun manifestMissingVersionOrDownloadUrlTriggersFallback() {
        val noVersion = """
            {
              "version": " ",
              "download_url": "https://github.com/Darkstar085/playora/releases/download/v1.2.0/app.apk",
              "sha256": "f3b69a4ea5aae98b94640b7bd7d7ad412b045c4d81d40dfb436dcbd78b0004a7"
            }
        """.trimIndent()
        val noUrl = """
            {
              "version": "1.2.0",
              "download_url": "",
              "sha256": "f3b69a4ea5aae98b94640b7bd7d7ad412b045c4d81d40dfb436dcbd78b0004a7"
            }
        """.trimIndent()

        assertTrue(UpdateManager.parseManifestLookup(noVersion, "1.1.0") is UpdateManager.ManifestLookupResult.Unavailable)
        assertTrue(UpdateManager.parseManifestLookup(noUrl, "1.1.0") is UpdateManager.ManifestLookupResult.Unavailable)
    }

    @Test
    fun draftAndPrereleaseGithubReleasesAreIgnored() {
        val draftJson = """
            {
              "tag_name": "v2.0.0",
              "draft": true,
              "prerelease": false,
              "assets": []
            }
        """.trimIndent()
        val prereleaseJson = """
            {
              "tag_name": "v2.0.0",
              "draft": false,
              "prerelease": true,
              "assets": []
            }
        """.trimIndent()

        assertNull(UpdateManager.parseRelease(draftJson, "1.0.0"))
        assertNull(UpdateManager.parseRelease(prereleaseJson, "1.0.0"))
    }

    @Test(expected = IllegalStateException::class)
    fun releaseWithoutVerifiedApkAssetThrows() {
        val releaseWithoutDigest = """
            {
              "tag_name": "v2.0.0",
              "draft": false,
              "prerelease": false,
              "assets": [
                {
                  "name": "Playora_v2.0.0.apk",
                  "browser_download_url": "https://github.com/Darkstar085/playora/releases/download/v2.0.0/Playora_v2.0.0.apk",
                  "content_type": "application/vnd.android.package-archive",
                  "size": 1000
                }
              ]
            }
        """.trimIndent()

        UpdateManager.parseRelease(releaseWithoutDigest, "1.0.0")
    }

    @Test
    fun formatReleaseNotesCleansMarkdownAndLinks() {
        val notes = """
            # Release v1.2.0
            - Added [Liquid Glass](https://github.com/itshedimisawi/vitro) UI
            * Fixed [Crash](https://example.com) on launch
            • Improved seekbar precision
            **Full Changelog:** https://github.com/Darkstar085/playora/compare/v1.1.0...v1.2.0
        """.trimIndent()

        assertEquals(
            "Added Liquid Glass UI\nFixed Crash on launch\nImproved seekbar precision",
            UpdateManager.formatReleaseNotes(notes),
        )

        val proseOnly = """
            ## Summary
            Updated [player](https://example.com) engine
            **Full Changelog:** https://example.com/diff
        """.trimIndent()
        assertEquals("Updated player engine", UpdateManager.formatReleaseNotes(proseOnly))
    }

    @Test
    fun testAppUpdateModel() {
        val update = AppUpdate(
            version = "1.0.1",
            name = "Playora 1.0.1",
            notes = "• Bug fixes\n• Performance",
            downloadUrl = "https://github.com/Darkstar085/playora/releases/download/v1.0.1/app-release.apk",
            fileName = "playora-1.0.1.apk",
            size = 35_000_000L,
            digest = "sha256:abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234",
            releaseDate = "Nov 23, 2026",
            isPrerelease = false,
        )

        assertEquals("1.0.1", update.version)
        assertEquals("Playora 1.0.1", update.name)
        assertFalse(update.isPrerelease)
        assertEquals(35_000_000L, update.size)
        assertTrue(update.downloadUrl.startsWith("https://github.com"))
    }
}
