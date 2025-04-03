package com.architect.atlasResGen.tasks.platform

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class XcAssetIDEMigrationTask: DefaultTask(){
    @get:OutputFile
    abstract val outputScriptFile: RegularFileProperty

    @get:InputDirectory
    abstract val xcassetsSourceDir: DirectoryProperty

    @TaskAction
    fun writeScript() {
        val output = outputScriptFile.get().asFile
        val sourcePath = xcassetsSourceDir.get().asFile.absolutePath

        output.parentFile.mkdirs()
        output.writeText(
            """
            #!/bin/sh
            echo "📦 Copying AtlasAssets.xcassets to Xcode project"

            SRC_DIR="$sourcePath"
            DST_DIR="${'$'}SRCROOT/YourApp/Assets.xcassets"

            if [ -d "${'$'}DST_DIR" ]; then
                rm -rf "${'$'}DST_DIR"
            fi

            mkdir -p "${'$'}DST_DIR"
            cp -R "${'$'}SRC_DIR/" "${'$'}DST_DIR"

            echo "✅ AtlasAssets.xcassets copied to ${'$'}DST_DIR"
            """.trimIndent()
        )
        output.setExecutable(true)
        logger.lifecycle("✅ Generated Xcode copy script at: ${output.absolutePath}")
    }
}