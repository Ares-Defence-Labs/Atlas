package com.architect.atlasResGen.tasks.platform

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class XcFontAssetsPackagingTask : DefaultTask() {
    @get:OutputFile
    abstract val copyScriptFile: RegularFileProperty

    @get:OutputFile
    abstract val injectPlistScriptFile: RegularFileProperty

    @get:Input
    abstract var fontDirectory: String


    @get:Input
    abstract var iOSProjectDirectory: String

    init {
        group = "AtlasFontsAssetPackaging"
        description = "Generates scripts that packages the font asset directories"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateMigrationScripts() {
        val updateScript = generateCopyFontsScript()
        val copyScript = generateInjectPlistScript()

        if (updateScript.exists() && copyScript.exists()) {
            runShellScript(updateScript)
            runShellScript(copyScript)
        }
    }

    private fun runShellScript(script: File) {
        logger.lifecycle("🚀 Executing script: ${script.name}")
        logger.lifecycle("📄 Script path: ${script.absolutePath}")

        if (!script.exists()) {
            logger.error("❌ Script does not exist: ${script.absolutePath}")
            return
        }

        logger.lifecycle("📜 Script content:\n${script.readText()}")

        val processBuilder = ProcessBuilder("sh", script.absolutePath)
            .directory(script.parentFile)
            .redirectErrorStream(true)

        try {
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            logger.lifecycle("📤 Script output:\n$output")
            logger.lifecycle("📦 Exit code: $exitCode")

            if (exitCode != 0) {
                logger.error("❌ Script failed with exit code $exitCode")
            } else {
                logger.lifecycle("✅ Script executed successfully")
            }

        } catch (e: Exception) {
            logger.error("❌ Exception while executing script: ${e.message}", e)
        }
    }

    private fun generateCopyFontsScript() : File{
        val output = copyScriptFile.get().asFile
        output.parentFile.mkdirs()

        val appName = File(iOSProjectDirectory).name
        output.writeText(
            """
            #!/bin/sh
            echo "\uD83D\uDCE6 Copying Atlas Fonts to Xcode..."

            SRC_DIR="$fontDirectory"
            DST_DIR="$iOSProjectDirectory/$appName/Resources/Fonts"

            rm -rf "${'$'}DST_DIR"
            mkdir -p "${'$'}DST_DIR"

            cp -R "${'$'}SRC_DIR"/* "${'$'}DST_DIR"

            echo "\u2705 Fonts copied to ${'$'}DST_DIR"
            """.trimIndent()
        )
        output.setExecutable(true)
        logger.lifecycle("✅ Generated font copy script at: ${output.absolutePath}")

        return output
    }

    private fun generateInjectPlistScript() : File{
        val scriptFile = injectPlistScriptFile.get().asFile
        scriptFile.parentFile.mkdirs()

        val scriptDirPath = scriptFile.parentFile.absolutePath
        val appName = File(iOSProjectDirectory).name

        val content = """
        #!/bin/sh
        echo "📦 Injecting fonts from mergedFonts.plist into Info.plist"

        SCRIPT_DIR="$scriptDirPath"
        MERGED_PLIST="${'$'}SCRIPT_DIR/../mergedFonts.plist"
        TARGET_PLIST="$iOSProjectDirectory/$appName/info.plist"

        if [ ! -f "${'$'}MERGED_PLIST" ]; then
          echo "❌ mergedFonts.plist not found at ${'$'}MERGED_PLIST"
          exit 1
        fi

        if [ ! -f "${'$'}TARGET_PLIST" ]; then
          echo "❌ Info.plist not found at ${'$'}TARGET_PLIST"
          exit 1
        fi

        /usr/libexec/PlistBuddy -c "Delete :UIAppFonts" "${'$'}TARGET_PLIST" 2>/dev/null
        /usr/libexec/PlistBuddy -c "Add :UIAppFonts array" "${'$'}TARGET_PLIST"

        while IFS= read -r line; do
          FONT_NAME=$(echo "${'$'}line" | sed -n 's/.*<string>Fonts\/\(.*\)<\/string>.*/\1/p')
          if [ ! -z "${'$'}FONT_NAME" ]; then
            /usr/libexec/PlistBuddy -c "Add :UIAppFonts: string ${'$'}FONT_NAME" "${'$'}TARGET_PLIST"
          fi
        done < "${'$'}MERGED_PLIST"

        echo "✅ Fonts injected into Info.plist"
    """.trimIndent()

        scriptFile.writeText(content)
        scriptFile.setExecutable(true)
        logger.lifecycle("✅ Font injection script written to: ${scriptFile.absolutePath}")

        return scriptFile
    }
}



