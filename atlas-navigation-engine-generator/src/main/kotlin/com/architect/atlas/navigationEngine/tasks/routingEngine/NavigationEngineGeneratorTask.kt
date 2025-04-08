package com.architect.atlas.navigationEngine.tasks.routingEngine

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class NavigationEngineGeneratorTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputAndroidDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var outputFiles: List<File>

    init {
        group = "AtlasNavigation"
        description = "Generates the platform-specific navigation engine implementations."
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateNavigatorClass() {
        val ants = scanViewModelAnnotations()
        val viewModelToScreen =
            ants.map { it.first to it.second } // Drop path, keep (viewModel, screen)

        generateAndroidNavigation(viewModelToScreen)
        generateAndroidNavGraph(ants)
        generateIOSNavigation(viewModelToScreen)
        generateIOSKotlinBridge(viewModelToScreen)
    }

    private fun scanViewModelAnnotations(): List<Quad<String, String, String, Boolean>> {
        val results = mutableListOf<Quad<String, String, String, Boolean>>()

        outputFiles.forEach { subProject ->
            subProject.walkTopDown().forEach { file ->
                if (!file.isFile || !(file.extension.equals("kt", true) || file.extension.equals("swift", true))) return@forEach

                val lines = file.readLines()
                var viewModelName: String? = null
                var screenName: String? = null
                var isInitial = false

                for ((index, line) in lines.withIndex()) {
                    if (line.contains("@AtlasScreen")) {
                        val kotlinRegex = """@AtlasScreen\s*\(\s*([\w.<>]+)::class(?:\s*,\s*initial\s*=\s*(true|false))?""".toRegex()
                        val swiftRegex = """@AtlasScreen\s*\(\s*([\w.<>]+)\.self(?:\s*,\s*initial\s*=\s*(true|false))?""".toRegex()
                        val match = kotlinRegex.find(line) ?: swiftRegex.find(line)
                        viewModelName = match?.groupValues?.get(1)
                        isInitial = match?.groupValues?.getOrNull(2)?.toBooleanStrictOrNull() ?: false

                        val nextLines = lines.drop(index).take(3)
                        val funcRegex = """fun\s+(\w+)""".toRegex()
                        val funcMatch = nextLines.firstNotNullOfOrNull { funcRegex.find(it) }
                        screenName = funcMatch?.groupValues?.get(1)

                        if (viewModelName != null && screenName != null) {
                            results.add(Quad(viewModelName, screenName, file.absolutePath, isInitial))
                        }
                    }
                }
            }
        }

        return results
    }

    private fun generateAndroidNavigation(screens: List<Pair<String, String>>) {
        val viewModelImports = screens.mapNotNull { (viewModel, _) -> findViewModelImport(viewModel) }.distinct()

        val androidImpl = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()
            viewModelImports.forEach { appendLine("import $it") }
            appendLine("import androidx.navigation.NavHostController")
            appendLine("import androidx.lifecycle.ViewModelStoreOwner")
            appendLine("import androidx.lifecycle.viewmodel.compose.viewModel")
            appendLine("import com.architect.atlas.architecture.navigation.AtlasNavigationService")
            appendLine("import com.architect.atlas.architecture.mvvm.ViewModel")
            appendLine("import com.architect.atlas.architecture.navigation.Poppable")
            appendLine("import kotlin.reflect.KClass")
            appendLine("import kotlinx.serialization.encodeToString")
            appendLine("import kotlinx.serialization.json.Json")
            appendLine()

            appendLine("object AtlasNavigation : AtlasNavigationService {")
            appendLine("    lateinit var navController: NavHostController")
            appendLine("    private val navigationStack = mutableListOf<KClass<out ViewModel>>()")
            appendLine("    private val viewModelToRouteMap: Map<KClass<out ViewModel>, String> = mapOf(")
            for ((viewModel, screenName) in screens) {
                appendLine("        $viewModel::class to \"$screenName\",")
            }
            appendLine("    )")
            appendLine("    private val routeToViewModelMap: Map<String, KClass<out ViewModel>> = viewModelToRouteMap.entries.associate { it.value to it.key }")

            appendLine("    override fun <T : ViewModel> navigateToPage(viewModelClass: KClass<T>, params: Any?) {")
            appendLine("        val route = viewModelToRouteMap[viewModelClass] ?: error(\"No screen registered for \$viewModelClass\")")
            appendLine("""
                        if(!navigationStack.any { q -> q == viewModelToRouteMap.keys.first() }){
                            navigationStack.add(viewModelToRouteMap.keys.first())
                        }
            """.trimIndent())

            appendLine("        navigationStack.add(viewModelClass)")
            appendLine("        val encoded = params?.let {")
            appendLine("            when (it) {")
            appendLine("                is String, is Number, is Boolean -> it.toString()")
            appendLine("                else -> Json.encodeToString(it)")
            appendLine("            }")
            appendLine("        } ?: \"\"")
            appendLine("        navController.navigate(\"\$route?pushParam=\$encoded\")")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> navigateToPageModal(viewModelClass: KClass<T>, params: Any?) {")
            appendLine("        navigateToPage(viewModelClass, params)")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> setNavigationStack(stack: List<T>, params: Any?) { }")
            appendLine("    override fun <T : ViewModel> getNavigationStack(): List<T> = emptyList()")

            appendLine("    override fun popToRoot(animate: Boolean, params: Any?) {")
            appendLine("        while (navigationStack.size > 1) {")
            appendLine("            navController.popBackStack()")
            appendLine("            handlePopParams(params)")
            appendLine("        }")
            appendLine("    }")

            appendLine("    override fun popPage(animate: Boolean, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")

            appendLine("    override fun popPagesWithCount(countOfPages: Int, animate: Boolean, params: Any?) {")
            appendLine("        repeat(countOfPages) {")
            appendLine("            handlePopParams(params)")
            appendLine("        }")
            appendLine("    }")

            appendLine("    override fun popToPage(route: String, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")

            appendLine("    override fun dismissModal(animate: Boolean, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")

            appendLine("    private fun handlePopParams(params: Any?) {")
            appendLine("        if (navigationStack.size < 2) return")
            appendLine("        navigationStack.removeLastOrNull()")
            appendLine("        val previousVmClass = navigationStack.lastOrNull() ?: return")
            appendLine("        val encoded = params?.let {")
            appendLine("            when (it) {")
            appendLine("                is String, is Number, is Boolean -> it.toString()")
            appendLine("                else -> Json.encodeToString(it)")
            appendLine("            }")
            appendLine("        } ?: return")
            appendLine("        navController.currentBackStackEntry?.let { backStackEntry ->")
            appendLine("""
                val decoded: Any = when {
            encoded.toIntOrNull() != null -> encoded.toInt()
            encoded.toDoubleOrNull() != null -> encoded.toDouble()
            encoded.equals("true", true) || encoded.equals("false", true) -> encoded.toBoolean()
            else -> try {
                Json.decodeFromString(encoded)
            } catch (_: Exception) {
                encoded
            }
        }
            """.trimIndent())
            appendLine("            val vm = androidx.lifecycle.ViewModelProvider(backStackEntry)[previousVmClass.java] ?: return@let")
            appendLine("            if (vm is Poppable<*>) {")
            appendLine("                @Suppress(\"UNCHECKED_CAST\")")
            appendLine("                (vm as Poppable<Any>).onPopParams(decoded)")
            appendLine("            }")
            appendLine("navController.popBackStack()")
            appendLine("        }")
            appendLine("    }")


            appendLine("}")
        }

        val androidOut = File(outputAndroidDir.get().asFile, "navigation")
        androidOut.mkdirs()
        File(androidOut, "AtlasNavigation.kt").writeText(androidImpl)
    }


    private fun findViewModelImport(viewModelName: String): String? {
        outputFiles.forEach { root ->
            root.walkTopDown().forEach { file ->
                if (!file.isFile || !file.extension.equals("kt", true)) return@forEach
                val lines = file.readLines()

                // Check if class or object with this ViewModel name is defined
                if (lines.any { it.contains("class $viewModelName") || it.contains("object $viewModelName") }) {
                    val packageLine = lines.firstOrNull { it.trim().startsWith("package ") }
                        ?.removePrefix("package ")
                        ?.trim()
                    return packageLine?.let { "$it.$viewModelName" }
                }
            }
        }
        return null
    }

    private fun generateAndroidNavGraph(screens: List<Quad<String, String, String, Boolean>>) {
        val navGraph = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()

            val functionImports = screens.mapNotNull { (_, screenName, filePath, _) ->
                File(filePath).useLines { lines ->
                    val pkg = lines.firstOrNull { it.trim().startsWith("package ") }
                        ?.removePrefix("package ")?.trim()
                    pkg?.let { "$it.$screenName" }
                }
            }

            val viewModelImports = screens.mapNotNull { (viewModelName, _, _, _) ->
                findViewModelImport(viewModelName)
            }

            functionImports.forEach { appendLine("import $it") }
            viewModelImports.forEach { appendLine("import $it") }

            appendLine("import com.architect.atlas.navigation.AtlasNavigation")
            appendLine("import androidx.compose.runtime.Composable")
            appendLine("import androidx.navigation.NavHostController")
            appendLine("import androidx.navigation.compose.NavHost")
            appendLine("import androidx.lifecycle.viewmodel.compose.viewModel")
            appendLine("import androidx.navigation.compose.composable")
            appendLine("import androidx.navigation.compose.rememberNavController")
            appendLine("import com.architect.atlas.architecture.navigation.Poppable")
            appendLine()

            appendLine("@Composable")
            appendLine("fun AtlasNavGraph() {")
            appendLine("    val navController = rememberNavController()")
            appendLine("    AtlasNavigation.navController = navController")

            val start = screens.firstOrNull { it.fourth }?.second ?: "MissingStart"
            appendLine("    NavHost(navController = navController, startDestination = \"$start\") {")
            for ((viewModel, screenComposable, _, _) in screens) {
                appendLine("        composable(\"$screenComposable?pushParam={pushParam}\") { backStackEntry ->")
                appendLine("            val vm = viewModel(modelClass = $viewModel::class.java, viewModelStoreOwner = backStackEntry)")
                appendLine("            val rawParam = backStackEntry.arguments?.getString(\"pushParam\")")
                appendLine("            rawParam?.let {")
                appendLine("                val param: Any = when {")
                appendLine("                    rawParam == \"null\" -> return@let")
                appendLine("                    rawParam.toIntOrNull() != null -> rawParam.toInt()")
                appendLine("                    rawParam.toDoubleOrNull() != null -> rawParam.toDouble()")
                appendLine("                    rawParam.equals(\"true\", true) || rawParam.equals(\"false\", true) -> rawParam.toBoolean()")
                appendLine("                    else -> try { kotlinx.serialization.json.Json.decodeFromString(rawParam) } catch (_: Exception) { rawParam }")
                appendLine("                }")
                appendLine("                    @Suppress(\"UNCHECKED_CAST\")")
                appendLine("                    (vm as? com.architect.atlas.architecture.navigation.Pushable<Any>)?.onPushParams(param)")
                appendLine("            }")
                appendLine("            $screenComposable(vm)")
                appendLine("        }")
            }
            appendLine("    }")
            appendLine("}")
        }

        val file = File(outputAndroidDir.get().asFile, "navigation/AtlasNavGraph.kt")
        file.parentFile.mkdirs()
        file.writeText(navGraph)
    }


    private fun generateIOSNavigation(screens: List<Pair<String, String>>) {
        val iosImpl = buildString {
            appendLine("import SwiftUI")
            appendLine("import UIKit")
            appendLine()
            appendLine("class NavigationEngine: NSObject {")
            appendLine("    static let shared = NavigationEngine()")
            appendLine("    private var stack: [String] = []")

            appendLine("    func routeWithParams(viewModelType: String, params: Any? = nil, isModal: Bool = false) {")
            appendLine("        let nav = UIApplication.shared.rootNav")
            appendLine("        switch viewModelType {")
            for ((vm, screen) in screens) {
                appendLine("        case \"$vm\":")
                appendLine("            let view = $screen(vm: AtlasDI.resolveService())")
                appendLine("            let controller = UIHostingController(rootView: view)")
                appendLine("            if isModal {")
                appendLine("                nav?.present(controller, animated: true)")
                appendLine("            } else {")
                appendLine("                nav?.pushViewController(controller, animated: true)")
                appendLine("            }")
            }
            appendLine("        default: break")
            appendLine("        }")
            appendLine("    }")

            appendLine("    func setNavigationStack(stack: [String], params: Any?) {")
            appendLine("        self.stack = stack")
            appendLine("    }")

            appendLine("    func getNavigationStack() -> [String] {")
            appendLine("        return stack")
            appendLine("    }")

            appendLine("    func popToRoot(animate: Bool = true, params: Any? = nil) {")
            appendLine("        UIApplication.shared.rootNav?.popToRootViewController(animated: animate)")
            appendLine("    }")

            appendLine("    func popPage(animate: Bool = true, params: Any? = nil) {")
            appendLine("        UIApplication.shared.rootNav?.popViewController(animated: animate)")
            appendLine("    }")

            appendLine("    func popPagesWithCount(count: Int, animate: Bool = true, params: Any? = nil) {")
            appendLine("        guard let nav = UIApplication.shared.rootNav else { return }")
            appendLine("        let targetIndex = max(nav.viewControllers.count - count, 1)")
            appendLine("        let target = nav.viewControllers[targetIndex - 1]")
            appendLine("        nav.popToViewController(target, animated: animate)")
            appendLine("    }")

            appendLine("    func popToPage(route: String, params: Any?) {")
            appendLine("        // Not implemented yet")
            appendLine("    }")

            appendLine("    func dismissModal(animate: Bool = true, params: Any? = nil) {")
            appendLine("        UIApplication.shared.rootNav?.presentedViewController?.dismiss(animated: animate)")
            appendLine("    }")
            appendLine("}")

            appendLine(
                """
                import UIKit
                    extension UIApplication {
                        var rootNav: UINavigationController? {
                        return (self.connectedScenes.first as? UIWindowScene)?
                        .windows
                        .first(where: { ${'$'}0.isKeyWindow })?
                        .rootViewController as? UINavigationController
                    }
            }
                
                
            """.trimIndent()
            )
        }

        val iosOut = File(outputIosDir.get().asFile, "navigation")
        iosOut.mkdirs()
        File(iosOut, "NavigationEngine.swift").writeText(iosImpl)
    }

    private fun generateIOSKotlinBridge(screens: List<Pair<String, String>>) {
        val kotlinBridge = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()
            appendLine("import com.architect.atlas.architecture.navigation.AtlasNavigationService")
            appendLine("import com.architect.atlas.architecture.viewmodel.ViewModel")
            appendLine("import platform.Foundation.*")
            appendLine("import kotlinx.cinterop.*")
            appendLine("import platform.UIKit.*")
            appendLine()
            appendLine("class IOSAtlasNavigationService : AtlasNavigationService {")

            appendLine("    override fun <T : ViewModel> navigateToPage(params: Any?) {")
            appendLine("        val viewModelName = T::class.simpleName ?: return")
            appendLine("        NavigationEngine.shared.routeWithParams(viewModelType = viewModelName, params = params)")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> navigateToPageModal(params: Any?) {")
            appendLine("        val viewModelName = T::class.simpleName ?: return")
            appendLine("        NavigationEngine.shared.routeWithParams(viewModelType = viewModelName, params = params, isModal = true)")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> setNavigationStack(stack: List<T>, params: Any?) {")
            appendLine("        val names = stack.mapNotNull { it::class.simpleName }")
            appendLine("        NavigationEngine.shared.setNavigationStack(stack = names, params = params)")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> getNavigationStack(): List<T> = emptyList()")
            appendLine("    override fun popToRoot(animate: Boolean, params: Any?) {")
            appendLine("        NavigationEngine.shared.popToRoot(animate = animate, params = params)")
            appendLine("    }")
            appendLine("    override fun popPage(animate: Boolean, params: Any?) {")
            appendLine("        NavigationEngine.shared.popPage(animate = animate, params = params)")
            appendLine("    }")
            appendLine("    override fun popPagesWithCount(countOfPages: Int, animate: Boolean, params: Any?) {")
            appendLine("        NavigationEngine.shared.popPagesWithCount(count = countOfPages, animate = animate, params = params)")
            appendLine("    }")
            appendLine("    override fun popToPage(route: String, params: Any?) {")
            appendLine("        NavigationEngine.shared.popToPage(route = route, params = params)")
            appendLine("    }")
            appendLine("    override fun dismissModal(animate: Boolean, params: Any?) {")
            appendLine("        NavigationEngine.shared.dismissModal(animate = animate, params = params)")
            appendLine("    }")
            appendLine("}")
        }

        val iosOut = File(outputIosDir.get().asFile, "navigation")
        iosOut.mkdirs()
        File(iosOut, "IOSAtlasNavigationService.kt").writeText(kotlinBridge)
    }
}

data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
