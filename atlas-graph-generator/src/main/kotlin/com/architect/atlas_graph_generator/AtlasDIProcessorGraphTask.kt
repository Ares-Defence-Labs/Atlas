package com.architect.atlasGraphGenerator

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class AtlasDIProcessorGraphTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    init {
        group = "Atlas"
        description = "Generates a dependency graph for the project"
        outputs.upToDateWhen { false }  // ✅ Always force execution
    }

    @TaskAction
    fun generateGraph() {
        project.logger.lifecycle("🚀 Generating the Atlas Dependency Graph")

        val atlasContainerDir = outputDir.get().asFile
        val androidContainerDir = androidOutputDir.get().asFile

        atlasContainerDir.mkdirs()
        androidContainerDir.mkdirs()

        val outputFile = File(atlasContainerDir, "AtlasContainer.kt")
        val androidOutputFile = File(androidContainerDir, "ViewModelExtensions.kt")

        // ✅ Scan for annotated classes
        val classToPackage = mutableMapOf<String, String>()
        val singletons = mutableSetOf<String>()
        val factories = mutableSetOf<String>()
        val scopedInstances = mutableSetOf<String>()
        val viewModels = mutableSetOf<String>()
        val modules = mutableSetOf<String>()

        val annotationMappings = mapOf(
            "com.architect.atlas.container.annotations.Singleton" to singletons,
            "com.architect.atlas.container.annotations.Factory" to factories,
            "com.architect.atlas.container.annotations.Scoped" to scopedInstances,
            "com.architect.atlas.container.annotations.ViewModel" to viewModels,
            "com.architect.atlas.container.annotations.Module" to modules
        )

        project.logger.lifecycle("🔍 Scanning project source files for annotations...")

        project.rootDir.walkTopDown()
            .filter { it.isDirectory && it.path.contains("src") && it.path.contains("kotlin") }
            .forEach { sourceDir ->
                sourceDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val content = file.readText()

                    // ✅ Extract package name from file
                    val packageMatch = Regex("""^package\s+([\w.]+)""").find(content)
                    val packageName = packageMatch?.groupValues?.get(1) ?: ""

                    annotationMappings.forEach { (annotation, collection) ->
                        val annotationSimpleName = annotation.substringAfterLast(".")
                        val annotationRegex = "@$annotationSimpleName\\s+class\\s+(\\w+)".toRegex()

                        annotationRegex.findAll(content).forEach { match ->
                            val className = match.groupValues[1]
                            if (content.contains("import $annotation")) {
                                collection.add(className)
                                classToPackage[className] = packageName
                                project.logger.lifecycle("✅ Found $annotation in ${file.name}: $className (package: $packageName)")
                            }
                        }
                    }
                }
            }

        // ✅ Generate `AtlasContainer.kt`
        outputFile.writeText(
            generateAtlasContainer(
                classToPackage,
                singletons,
                factories,
                scopedInstances,
                viewModels,
                modules
            )
        )
        project.logger.lifecycle("✅ Generated AtlasContainer.kt at: ${outputFile.absolutePath}")

        // ✅ Generate Android-specific ViewModel Delegate Extensions
        androidOutputFile.writeText(generateAndroidExtensions())
        project.logger.lifecycle("✅ Generated ViewModelExtensions.kt at: ${androidOutputFile.absolutePath}")
    }

    private fun generateAtlasContainer(
        classToPackage: Map<String, String>,
        singletons: Set<String>,
        factories: Set<String>,
        scopedInstances: Set<String>,
        viewModels: Set<String>,
        modules: Set<String>
    ): String {
        val imports = classToPackage.entries.joinToString("\n") { "import ${it.value}.${it.key}" }

        val formattedSingletons =
            singletons.joinToString("\n        ") { "singletons[${it}::class] = ${it}()" }
        val formattedFactories =
            factories.joinToString("\n        ") { "factories[${it}::class] = { ${it}() }" }
        val formattedScopedInstances =
            scopedInstances.joinToString("\n        ") { "scoped[${it}::class] = mutableMapOf()" }
        val formattedViewModels =
            viewModels.joinToString("\n        ") { "viewModels[${it}::class] = lazy { ${it}() }" }

        return """
            package com.architect.atlas.container

            $imports

            import kotlin.reflect.KClass

            object AtlasContainer {
                private val singletons: MutableMap<KClass<*>, Any> = mutableMapOf()
                private val factories: MutableMap<KClass<*>, () -> Any> = mutableMapOf()
                private val scoped: MutableMap<KClass<*>, MutableMap<String, Any>> = mutableMapOf()
                private val viewModels: MutableMap<KClass<*>, Lazy<Any>> = mutableMapOf()

                init {
                    ${if (singletons.isNotEmpty()) "        $formattedSingletons" else "        // No singletons registered"}
                    ${if (factories.isNotEmpty()) "        $formattedFactories" else "        // No factories registered"}
                    ${if (scopedInstances.isNotEmpty()) "        $formattedScopedInstances" else "        // No scoped instances registered"}
                    ${if (viewModels.isNotEmpty()) "        $formattedViewModels" else "        // No ViewModels registered"}
                }

                fun <T : Any> resolve(clazz: KClass<T>): T {
                    return (singletons[clazz]
                        ?: factories[clazz]?.invoke()
                        ?: viewModels[clazz]?.value) as? T
                        ?: throw IllegalArgumentException("No provider found for " + clazz.simpleName)
                }
            }
        """.trimIndent()
    }

    private fun generateAndroidExtensions(): String {
        return """
            package com.architect.atlas.container.android

            import com.architect.atlas.container.AtlasContainer
            import androidx.activity.ComponentActivity
            import androidx.fragment.app.Fragment
            import androidx.appcompat.app.AppCompatActivity
            import kotlin.properties.ReadOnlyProperty

            inline fun <reified T : Any> Fragment.viewModels(): ReadOnlyProperty<Fragment, T> {
                return ReadOnlyProperty { _, _ -> AtlasContainer.resolve(T::class) }
            }

            inline fun <reified T : Any> AppCompatActivity.viewModels(): ReadOnlyProperty<AppCompatActivity, T> {
                return ReadOnlyProperty { _, _ -> AtlasContainer.resolve(T::class) }
            }

            inline fun <reified T : Any> ComponentActivity.viewModels(): ReadOnlyProperty<ComponentActivity, T> {
                return ReadOnlyProperty { _, _ -> AtlasContainer.resolve(T::class) }
            }
        """.trimIndent()
    }
}