package com.architect.atlas.common.helpers

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.text.Normalizer
import kotlin.math.abs

object AppleProjectFinder {
    enum class AppleTargetKind { IOS_APP, WATCH_APP_CONTAINER, WATCH_APP_EXTENSION, OTHER }

    data class XcodeTarget(
        val kind: AppleTargetKind,
        val name: String,
        /** Parent directory of the .xcodeproj (e.g., iosApp/). */
        val containerDir: File,
        /** Target-specific directory (usually Info.plist's parent). */
        val targetDir: File,
        /** Resolved Info.plist if available. */
        val infoPlist: File?
    )

    // ---------- Public helpers ----------

    fun Project.isWatchBuildNow(): Boolean {
        val env = System.getenv()
        val plat = env["PLATFORM_NAME"].orEmpty()
        val sdk = env["SDK_NAME"].orEmpty()
        if (plat.startsWith("watch") || sdk.startsWith("watch")) return true
        if (gradle.startParameter.taskNames.any {
                it.contains(
                    "watchos",
                    ignoreCase = true
                )
            }) return true
        return runCatching {
            gradle.taskGraph.allTasks.any {
                it.name.contains(
                    "watchos",
                    ignoreCase = true
                )
            }
        }
            .getOrDefault(false)
    }

    fun Project.isIPhoneBuildNow(): Boolean {
        val env = System.getenv()
        val plat = env["PLATFORM_NAME"].orEmpty()
        val sdk = env["SDK_NAME"].orEmpty()
        if (plat.startsWith("iphone") || sdk.startsWith("iphone")) return true
        if (gradle.startParameter.taskNames.any {
                it.contains(
                    "ios",
                    ignoreCase = true
                ) && !it.contains("watch", true)
            }) return true
        return runCatching {
            gradle.taskGraph.allTasks.any {
                it.name.contains(
                    "ios",
                    ignoreCase = true
                ) && !it.name.contains("watch", true)
            }
        }
            .getOrDefault(false)
    }

    fun findAllXcodeTargets(root: File, logger: Logger): List<XcodeTarget> {
        return root.walkTopDown()
            .filter { it.isDirectory && it.extension == "xcodeproj" }
            .flatMap { xproj ->
                val pbx = xproj.projectPbxproj()
                if (pbx.isFile) parseXcodeTargetsFrom(pbx, logger) else emptyList()
            }
            .toList()
    }

    // ---------- PBX parsing ----------

    fun File.projectPbxproj(): File =
        resolve("project.pbxproj").takeIf { it.isFile } ?: this

    fun parseXcodeTargetsFrom(projectFile: File, logger: Logger): List<XcodeTarget> {
        val text = projectFile.readText()

        val nativeTargetBlockRegex = Regex(
            """([A-F0-9]{24}) /\* .*? \*/ = \{\s*[^}]*?isa = PBXNativeTarget;([\s\S]*?)\};""",
            setOf(RegexOption.DOT_MATCHES_ALL)
        )
        val nameRegex = Regex("""\bname\s*=\s*("?)([^";]+)\1\s*;""")
        val productTypeRegex = Regex("""\bproductType\s*=\s*"([^"]+)"\s*;""")
        val buildCfgListIdRegex = Regex("""\bbuildConfigurationList\s*=\s*([A-F0-9]{24})\s*;""")

        val cfgListRegex =
            Regex("""([A-F0-9]{24}) /\* .*? \*/ = \{\s*[^}]*?isa = XCConfigurationList;[\s\S]*?buildConfigurations = \(\s*([\s\S]*?)\);""")
        val cfgIdInListRegex = Regex("""([A-F0-9]{24}) /\* .*? \*/""")

        val buildCfgRegex =
            Regex("""([A-F0-9]{24}) /\* .*? \*/ = \{\s*[^}]*?isa = XCBuildConfiguration;[\s\S]*?buildSettings = \{([\s\S]*?)\};""")

        fun extractSetting(block: String, key: String): String? {
            val r = Regex("""\b$key\b\s*=\s*(?:"([^"]+)"|([^;]+));""")
            val m = r.find(block) ?: return null
            return m.groups[1]?.value ?: m.groups[2]?.value?.trim()
        }

        val cfgIdToSettings =
            buildCfgRegex.findAll(text).associate { it.groupValues[1] to it.groupValues[2] }
        val cfgListToCfgIds = cfgListRegex.findAll(text).associate { m ->
            val listId = m.groupValues[1]
            val ids = cfgIdInListRegex.findAll(m.groupValues[2]).map { it.groupValues[1] }.toList()
            listId to ids
        }

        val containerDir = projectFile.parentFile.parentFile ?: projectFile.parentFile
        val results = mutableListOf<XcodeTarget>()

        nativeTargetBlockRegex.findAll(text).forEach { mt ->
            val targetId = mt.groupValues[1]
            val blockBody = mt.groupValues[2]

            val name = nameRegex.find(blockBody)?.groupValues?.get(2)?.trim()?.trim('"').orEmpty()
            val productTypeRaw =
                productTypeRegex.find(blockBody)?.groupValues?.get(1)?.trim().orEmpty()
            val productType = productTypeRaw.lowercase()
            val cfgListId = buildCfgListIdRegex.find(blockBody)?.groupValues?.get(1)

            val cfgIds = cfgListId?.let { cfgListToCfgIds[it] }.orEmpty()
            val preferredCfgId = sequenceOf(
                cfgIds.firstOrNull { text.contains("$it /* Release */") },
                cfgIds.firstOrNull { text.contains("$it /* Debug */") }
            ).filterNotNull().firstOrNull() ?: cfgIds.firstOrNull()

            val settingsBlock = preferredCfgId?.let { cfgIdToSettings[it] } ?: ""

            val sdkRoot = extractSetting(settingsBlock, "SDKROOT")?.lowercase()
            val supportedPlatforms =
                extractSetting(settingsBlock, "SUPPORTED_PLATFORMS")?.lowercase()
            val targetedDeviceFamily =
                extractSetting(settingsBlock, "TARGETED_DEVICE_FAMILY")?.lowercase()
            val infoPlistRel = extractSetting(settingsBlock, "INFOPLIST_FILE")
            val productName = extractSetting(settingsBlock, "PRODUCT_NAME") ?: name

            val infoPlist = resolveInfoPlist(
                containerDir,
                infoPlistRel,
                targetName = name,
                productName = productName
            )
            val targetDir =
                infoPlist?.parentFile ?: guessTargetDir(containerDir, name) ?: containerDir

            // Plist signals
            val plistText = runCatching { infoPlist?.readText() }.getOrNull().orEmpty()
            val looksLikeWatchExtensionPlist =
                plistText.contains("NSExtensionPointIdentifier") && plistText.contains("com.apple.watchkit")
            val looksLikeWatchAppPlist =
                Regex("""<key>\s*WKWatchKitApp\s*</key>\s*<true\s*/>""").containsMatchIn(plistText)

            // ---- Classification ----
            val isWatchContainerByProductType =
                productType.contains("application.watchapp2-container")
            val isWatchExtensionByProductType =
                productType.contains("watchkit2-extension")

            val isWatchExtensionByName =
                name.contains("watch app", ignoreCase = true) && productType.contains("application")

            val looksLikeWatchContainerByName =
                name.equals("watchapp", ignoreCase = true)

            val kind = when {
                // Extension first
                isWatchExtensionByProductType || isWatchExtensionByName || looksLikeWatchExtensionPlist ->
                    AppleTargetKind.WATCH_APP_EXTENSION

                // Container bundle
                isWatchContainerByProductType || looksLikeWatchAppPlist || looksLikeWatchContainerByName ->
                    AppleTargetKind.WATCH_APP_CONTAINER

                // iOS app
                productType.contains("application") &&
                        (sdkRoot == null ||
                                sdkRoot == "iphoneos" || sdkRoot == "iphonesimulator" ||
                                supportedPlatforms?.contains("iphone") == true) ->
                    AppleTargetKind.IOS_APP

                else -> AppleTargetKind.OTHER
            }

            logger.lifecycle(
                "XcodeTarget: name=$name, kind=$kind, productType=$productTypeRaw, " +
                        "SDKROOT=$sdkRoot, SUPPORTED_PLATFORMS=$supportedPlatforms, " +
                        "TARGETED_DEVICE_FAMILY=$targetedDeviceFamily, " +
                        "plist=${infoPlist?.relativeToOrSelf(containerDir)?.path ?: "—"}"
            )

            results += XcodeTarget(
                kind = kind,
                name = name.ifBlank { targetId },
                containerDir = containerDir,
                targetDir = targetDir,
                infoPlist = infoPlist
            )
        }

        if (results.isEmpty()) {
            logger.warn("No PBXNativeTarget blocks parsed from: ${projectFile.absolutePath}")
        } else {
            logger.lifecycle("Finished parsing ${results.size} targets from ${projectFile.parentFile.name}")
        }
        return results
    }

    // ---------- internals ----------

    private fun resolveInfoPlist(
        containerDir: File,
        plistValue: String?,
        targetName: String,
        productName: String
    ): File? {
        if (plistValue.isNullOrBlank()) return null
        val expanded = plistValue
            .replace("\$(SRCROOT)", containerDir.absolutePath)
            .replace("\$SRCROOT", containerDir.absolutePath)
            .replace("\$(PROJECT_DIR)", containerDir.absolutePath)
            .replace("\$PROJECT_DIR", containerDir.absolutePath)
            .replace("\$(TARGET_NAME)", targetName)
            .replace("\$TARGET_NAME", targetName)
            .replace("\$(PRODUCT_NAME)", productName)
            .replace("\$PRODUCT_NAME", productName)

        val f = File(expanded)
        val abs = if (f.isAbsolute) f else File(containerDir, expanded)
        return abs.normalize().takeIf { it.isFile }
    }

    private fun guessTargetDir(containerDir: File, targetName: String): File? {
        val normTarget = normalizeName(targetName)
        var best: Pair<File, Int>? = null

        containerDir.walk()
            .maxDepth(2)
            .filter { it.isDirectory && it != containerDir }
            .forEach { dir ->
                val score = nameDistance(normTarget, normalizeName(dir.name))
                val candidate = best
                if (candidate == null || score < candidate.second) {
                    best = dir to score
                }
            }

        return best?.takeIf { it.second <= 3 }?.first
    }

    private fun normalizeName(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFKD)
        val sb = StringBuilder()
        for (ch in n.lowercase()) {
            if (ch.isLetterOrDigit()) sb.append(ch)
            else if (ch.isWhitespace()) sb.append(' ')
        }
        return sb.toString().trim().replace(Regex("\\s+"), " ")
    }

    private fun nameDistance(a: String, b: String): Int {
        val min = minOf(a.length, b.length)
        var prefix = 0
        while (prefix < min && a[prefix] == b[prefix]) prefix++
        return abs(a.length - b.length) + (min - prefix)
    }


    // Convenience filters
    fun List<XcodeTarget>.watchContainers(): List<XcodeTarget> =
        filter { it.kind == AppleTargetKind.WATCH_APP_CONTAINER }

    fun List<XcodeTarget>.watchExtensions(): List<XcodeTarget> =
        filter { it.kind == AppleTargetKind.WATCH_APP_EXTENSION }

    fun List<XcodeTarget>.iosApps(): List<XcodeTarget> =
        filter { it.kind == AppleTargetKind.IOS_APP }
}