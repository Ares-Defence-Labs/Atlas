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

    @get:Input
    abstract var iOSOutputFiles: List<File>


    @get:Input
    abstract var projectCoreName: String

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

        // ios components
        val iOSViewModelToScreen =
            scanViewModelSwiftAnnotations()

        generateIOSNavigation(iOSViewModelToScreen.map { ScreenMetadata(it.first, it.second, it.fourth) })
        generateIOSSwiftBridge()
    }

    private fun scanViewModelAnnotations(): List<Quad<String, String, String, Boolean>> {
        val results = mutableListOf<Quad<String, String, String, Boolean>>()

        outputFiles.forEach { subProject ->
            subProject.walkTopDown().forEach { file ->
                if (!file.isFile || !(file.extension.equals(
                        "kt",
                        true
                    ) || file.extension.equals("swift", true))
                ) return@forEach

                val lines = file.readLines()
                var viewModelName: String? = null
                var screenName: String? = null
                var isInitial = false

                for ((index, line) in lines.withIndex()) {
                    if (line.contains("@AtlasScreen")) {
                        val kotlinRegex =
                            """@AtlasScreen\s*\(\s*([\w.<>]+)::class(?:\s*,\s*initial\s*=\s*(true|false))?""".toRegex()
                        val swiftRegex =
                            """@AtlasScreen\s*\(\s*([\w.<>]+)\.self(?:\s*,\s*initial\s*=\s*(true|false))?""".toRegex()
                        val match = kotlinRegex.find(line) ?: swiftRegex.find(line)
                        viewModelName = match?.groupValues?.get(1)
                        isInitial =
                            match?.groupValues?.getOrNull(2)?.toBooleanStrictOrNull() ?: false

                        val nextLines = lines.drop(index).take(3)
                        val funcRegex = """fun\s+(\w+)""".toRegex()
                        val funcMatch = nextLines.firstNotNullOfOrNull { funcRegex.find(it) }
                        screenName = funcMatch?.groupValues?.get(1)

                        if (viewModelName != null && screenName != null) {
                            results.add(
                                Quad(
                                    viewModelName,
                                    screenName,
                                    file.absolutePath,
                                    isInitial
                                )
                            )
                        }
                    }
                }
            }
        }

        return results
    }

    private fun generateAndroidNavigation(screens: List<Pair<String, String>>) {
        val viewModelImports =
            screens.mapNotNull { (viewModel, _) -> findViewModelImport(viewModel) }.distinct()

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
            appendLine(
                """
                        if(!navigationStack.any { q -> q == viewModelToRouteMap.keys.first() }){
                            navigationStack.add(viewModelToRouteMap.keys.first())
                        }
            """.trimIndent()
            )

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
            appendLine(
                """
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
            """.trimIndent()
            )
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

        val androidOut = outputAndroidDir.get().asFile
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

        val file = File(outputAndroidDir.get().asFile, "AtlasNavGraph.kt")
        file.parentFile.mkdirs()
        file.writeText(navGraph)
    }


    // IOS Specific Generators
    private fun scanViewModelSwiftAnnotations(): List<Quad<String, String, String, Boolean>> {
        val results = mutableListOf<Quad<String, String, String, Boolean>>()
        val swiftRegex = """^\s*//\s*@?AtlasScreen\s*\(\s*viewModel\s*:\s*([A-Za-z0-9_]+)\.self\s*(?:,\s*initial\s*:\s*(true|false))?\s*\)""".toRegex()

        iOSOutputFiles.forEach { subProject ->
            subProject.walkTopDown().forEach { file ->
                if (!file.isFile || !file.extension.equals("swift", true)) return@forEach

                val lines = file.readLines()
                var pendingAnnotation: MatchResult? = null

                for ((index, line) in lines.withIndex()) {
                    println("📄 [${file.name}:${index + 1}] $line")

                    val rawLine = line // keep it untrimmed for context

                    // Step 1: Look for the annotation
                    if (rawLine.contains("@AtlasScreen")) {
                        val match = swiftRegex.find(rawLine)

                        println("🔍 Attempting regex match on line: '$rawLine'")
                        println("🔣 Unicode points: ${rawLine.map { it.code }.joinToString(", ") { "U+%04X".format(it) }}")
                        println("✅ Matched: ${match != null}")
                        println("📦 Groups: ${match?.groupValues}")

                        if (match != null) {
                            println("🟨 Found @AtlasScreen annotation in ${file.name} line ${index + 1}")
                            pendingAnnotation = match
                        } else {
                            println("❌ No match found for: '$rawLine'")
                        }

                        continue
                    }

                    // Step 2: If we have a pending annotation, check for class/struct declaration
                    if (pendingAnnotation != null &&
                        (rawLine.trim().startsWith("struct ") || rawLine.trim().startsWith("class "))
                    ) {
                        val currentClass = rawLine.trim().split("\\s+".toRegex()).getOrNull(1)?.trim()
                        if (currentClass != null) {
                            val viewModelName = pendingAnnotation.groupValues[1]
                            val isInitial = pendingAnnotation.groupValues.getOrNull(2)?.toBooleanStrictOrNull() ?: false

                            println("✅ Matched @AtlasScreen to class $currentClass (vm=$viewModelName, initial=$isInitial)")

                            results.add(
                                Quad(
                                    viewModelName,
                                    currentClass,
                                    file.absolutePath,
                                    isInitial
                                )
                            )
                        } else {
                            error("Could not resolve class/struct for @AtlasScreen in ${file.name} line ${index + 1}")
                        }

                        pendingAnnotation = null
                    }
                }
            }
        }

        return results
    }

    private fun generateIOSNavigation(screens: List<ScreenMetadata>) {
        val iosImpl = buildString {
            appendLine("import SwiftUI")
            appendLine("import UIKit")
            appendLine("import $projectCoreName")
            appendLine()
            appendLine("@MainActor")
            appendLine("public class NavigationEngine: NSObject {")
            appendLine("    static let shared = NavigationEngine()")
            appendLine("    var stack: [ViewModel] = []")
            appendLine("    @MainActor public func routeWithParams(viewModelType: String, params: Any? = nil, isModal: Bool = false) {")
            appendLine("        let nav = UIApplication.globalRootNav")
            appendLine("        switch viewModelType {")

            for ((vm, screen, _) in screens) {
                val screenName = screen.replace(":", "")
                appendLine("        case \"$vm\":")
                appendLine("            let resolved = AtlasDI.companion.resolveServiceNullableByName(clazz: SwiftClassGenerator.companion.getClazz(type: $vm.self)) as! $vm")
                appendLine("            if let pc = params {")
                appendLine("                resolved.tryHandlePush(params: pc)")
                appendLine("            }")
                appendLine("            let view = $screenName(vm: resolved)")
                appendLine("            let controller = UIHostingController(rootView: view)")
                appendLine("            if isModal {")
                appendLine("                nav?.present(controller, animated: true)")
                appendLine("            } else {")
                appendLine("                nav?.pushViewController(controller, animated: true)")
                appendLine("            }")
                appendLine("            let vm = resolved; stack.append(vm)")
            }

            appendLine("        default: break")
            appendLine("        }")
            appendLine("    }")

            appendLine("   func setNavigationStack(stack: [String], params: Any?) { /* Not used directly */ }")
            appendLine("   func getNavigationStack() -> [String] { return stack.map { String(describing: $0) } }")

            appendLine("    func popToRoot(animate: Bool = true, params: Any? = nil) {")
            appendLine("        if let prev = stack.first as? Poppable {")
            appendLine("            if let pc = params {")
            appendLine("                prev.onPopParams(params: pc)")
            appendLine("            }")
            appendLine("        }")
            appendLine("        stack.removeAll()")
            appendLine("        UIApplication.shared.rootNav?.popToRootViewController(animated: animate)")
            appendLine("    }")

            appendLine("     func popPage(animate: Bool = true, params: Any? = nil) {")
            appendLine("        if stack.count >= 2 {")
            appendLine("            let prev = stack[stack.count - 2] as? Poppable")
            appendLine("            if let pc = params {")
            appendLine("                prev?.onPopParams(params: pc)")
            appendLine("            }")
            appendLine("        }")
            appendLine("        if !stack.isEmpty { stack.removeLast() }")
            appendLine("        UIApplication.shared.rootNav?.popViewController(animated: animate)")
            appendLine("    }")

            appendLine("     func popPagesWithCount(count: Int, animate: Bool = true, params: Any? = nil) {")
            appendLine("        guard let nav = UIApplication.shared.rootNav else { return }")
            appendLine("        let targetIndex = max(nav.viewControllers.count - count, 1)")
            appendLine("        let target = nav.viewControllers[targetIndex - 1]")
            appendLine("        if stack.count > count {")
            appendLine("            let prev = stack[stack.count - count - 1] as? Poppable")
            appendLine("            if let pc = params {")
            appendLine("                prev?.onPopParams(params: pc)")
            appendLine("            }")
            appendLine("        }")
            appendLine("        stack.removeLast(min(count, stack.count))")
            appendLine("        nav.popToViewController(target, animated: animate)")
            appendLine("    }")

            appendLine("     func popToPage(route: String, params: Any?) { /* Not implemented */ }")
            appendLine("     func dismissModal(animate: Bool = true, params: Any? = nil) {")
            appendLine("        UIApplication.shared.rootNav?.presentedViewController?.dismiss(animated: animate)")
            appendLine("    }")
            appendLine("}")

            // Determine the initial screen
            val initial = screens.firstOrNull { it.isInitial }
            val initialViewModel = initial?.viewModel ?: "/* MissingInitialVM */"
            val initialScreen = initial?.screen?.replace(":", "") ?: "/*MissingInitialScreen */"

            appendLine(
                """
            struct UIKitNavWrapperView: UIViewControllerRepresentable {
                func makeUIViewController(context: Context) -> UIViewController {
                    let root = $initialScreen(
                        vm: AtlasDI.companion.resolveServiceNullableByName(
                            clazz: SwiftClassGenerator.companion.getClazz(type: $initialViewModel.self)
                        ) as? $initialViewModel
                    )
                    let hostingController = UIHostingController(rootView: root)
                    let navController = UINavigationController(rootViewController: hostingController)
                    UIApplication.globalRootNav = navController
                    return navController
                }

                func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
            }

            extension UIApplication {
                static var globalRootNav: UINavigationController?
                var rootNav: UINavigationController? {
                    return (self.connectedScenes.first as? UIWindowScene)?
                        .windows
                        .first(where: { $0.isKeyWindow })?
                        .rootViewController as? UINavigationController
                }
            }
            """.trimIndent()
            )
        }

        val iosOut = outputIosDir.get().asFile
        iosOut.mkdirs()
        File(iosOut, "NavigationEngine.swift").writeText(iosImpl)
    }



    private fun generateIOSSwiftBridge() {
        val swiftBridge = buildString {
            appendLine("import Foundation")
            appendLine("import UIKit")
            appendLine("import $projectCoreName")
            appendLine()

            appendLine("""class IOSAtlasNavigationService: NSObject, @preconcurrency AtlasNavigationService {
    @MainActor
    func setNavigationStack(stack: [ViewModel], params: Any?) {
        DispatchQueue.main.async {
              NavigationEngine.shared.setNavigationStack(stack: stack.map { "\(${'$'}0)" }, params: params)
        }
    }
    
    func getNavigationStack() -> [ViewModel] {
        return []
    }
    
    @MainActor
    func navigateToPage(viewModelClass: any KotlinKClass, params: Any?) {
            DispatchQueue.main.async {
                NavigationEngine.shared.routeWithParams(viewModelType: viewModelClass.simpleName!, params: params)
            }
    }
    
    @MainActor
    func navigateToPageModal(viewModelClass: any KotlinKClass, params: Any?) {
            DispatchQueue.main.async {
                NavigationEngine.shared.routeWithParams(viewModelType: viewModelClass.simpleName!, params: params, isModal: true)
            }
    }
    
    @MainActor
    func popPagesWithCount(countOfPages: Int32, animate: Bool, params: Any?) {
        DispatchQueue.main.async {
             NavigationEngine.shared.popPagesWithCount(count: Int(countOfPages), animate: animate, params: params)
        }
    }

    @MainActor
    func popToRoot(animate: Bool = true, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popToRoot(animate: animate, params: params)
        }
    }

    @MainActor
    func popPage(animate: Bool = true, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popPage(animate: animate, params: params)
        }
    }

    @MainActor
    func popPagesWithCount(count: Int, animate: Bool = true, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popPagesWithCount(count: count, animate: animate, params: params)
        }
    }

    @MainActor
    func popToPage(route: String, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popToPage(route: route, params: params)
        }
    }

    @MainActor
    func dismissModal(animate: Bool = true, params: Any? = nil) {
        DispatchQueue.main.async {
           NavigationEngine.shared.dismissModal(animate: animate, params: params)
        }
    }
}

            """.trimIndent())
        }

        val iosOut = outputIosDir.get().asFile
        iosOut.mkdirs()
        File(iosOut, "IOSAtlasNavigationService.swift").writeText(swiftBridge)
    }
}

data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class ScreenMetadata(
    val viewModel: String,
    val screen: String,
    val isInitial: Boolean
)

fun String.normalizeToAscii(): String =
    this.map { if (it.code in 32..126) it else ' ' }.joinToString("")