package com.architect.atlas.navigationEngine.tasks.routingEngine

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class NavigationEngineGeneratorTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputAndroidDir: DirectoryProperty


    @get:OutputDirectory
    abstract val outputAndroidTabsDir: DirectoryProperty

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

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    @get:Input
    abstract var isIOSTarget: Boolean

    init {
        group = "AtlasNavigation"
        description = "Generates the platform-specific navigation engine implementations."
    }

    @TaskAction
    fun generateNavigatorClass() {
        // ios components
        if (isIOSTarget) {
            logger.lifecycle("WRITING NAVIGATION TO IOS")
            val iOSViewModelToScreen = scanViewModelSwiftAnnotations()
            generateIOSNavigation(iOSViewModelToScreen.map {
                ScreenMetadata(
                    it.first,
                    it.second,
                    it.fourth
                )
            })
            generateIOSSwiftBridge()
        } else {
            logger.lifecycle("WRITING NAVIGATION TO ANDROID")
            val ants = scanViewModelAnnotations()
            val viewModelToScreen =
                ants.map { it.first to it.second } // Drop path, keep (viewModel, screen)

            generateAndroidNavigation(viewModelToScreen)
            generateAndroidNavGraph(ants)
            generateTabNavigationServices(scanTabAnnotations())
        }
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

    private fun scanTabAnnotations(): Map<String, List<TabEntry>> {
        val tabGroups = mutableMapOf<String, MutableList<TabEntry>>()

        outputFiles.forEach { root ->
            root.walkTopDown().forEach { file ->
                if (!file.isFile || !file.extension.equals("kt", true)) return@forEach
                val lines = file.readLines()

                for ((index, line) in lines.withIndex()) {
                    if (line.contains("@AtlasTab")) {
                        val annotationBlock = lines.drop(index).take(5).joinToString(" ")
                        println("📌 Annotation Block: $annotationBlock")

                        val viewModelRegex =
                            """@AtlasTab\s*\(\s*([\w.]+)::class""".toRegex()  // positional
                        val namedViewModelRegex = """viewModel\s*=\s*([\w.]+)::class""".toRegex()

                        val positionRegex = """position\s*=\s*(\d+)""".toRegex()
                        val holderRegex = """holder\s*=\s*([\w.]+)::class""".toRegex()

                        val viewModel = viewModelRegex.find(annotationBlock)?.groupValues?.get(1)
                            ?: namedViewModelRegex.find(annotationBlock)?.groupValues?.get(1)
                        val position =
                            positionRegex.find(annotationBlock)?.groupValues?.get(1)?.toIntOrNull()
                        val holder = holderRegex.find(annotationBlock)?.groupValues?.get(1)

                        println("🧩 Parsed: viewModel=$viewModel, position=$position, holder=$holder")

                        if (viewModel == null || holder == null || position == null) {
                            logger.warn("⚠️ Could not parse @AtlasTab at ${file.name}:${index + 1}")
                            continue
                        }

                        // Find the next function name
                        val screenName = lines.drop(index + 1)
                            .take(10)
                            .firstOrNull { it.trim().startsWith("fun ") }
                            ?.let { """fun\s+(\w+)""".toRegex().find(it)?.groupValues?.get(1) }

                        if (screenName == null) {
                            logger.warn("⚠️ Could not extract function name for @AtlasTab in ${file.name} near line ${index + 1}")
                            continue
                        }

                        tabGroups.getOrPut(holder) { mutableListOf() }
                            .add(TabEntry(viewModel, screenName, holder, position))
                    }
                }
            }
        }

        return tabGroups
    }


    private fun generateTabNavigationServices(tabsByHolder: Map<String, List<TabEntry>>) {
        tabsByHolder.forEach { (holder, tabs) ->
            val sortedTabs = tabs.sortedBy { it.position }

            val outputFile = File(outputAndroidTabsDir.get().asFile, "${holder}TabsNavigation.kt")

            val code = buildString {
                appendLine("package com.architect.atlas.navigation")
                appendLine()

                val viewModelImports = sortedTabs.mapNotNull { findViewModelImport(it.viewModel) }
                viewModelImports.distinct().forEach { appendLine("import $it") }

                appendLine("import com.architect.atlas.architecture.navigation.AtlasTabNavigationService")
                appendLine("import kotlin.reflect.KClass")
                appendLine("import com.architect.atlas.architecture.mvvm.ViewModel")
                appendLine("import kotlinx.serialization.encodeToString")
                appendLine("import kotlinx.serialization.json.Json")
                appendLine("import androidx.navigation.NavHostController")
                appendLine()
                appendLine("object ${holder}TabsNavigation : AtlasTabNavigationService {")
                appendLine("    lateinit var navController: NavHostController")
                appendLine("    private var currentTab: KClass<out ViewModel>? = null")
                appendLine("    private val tabs: Map<KClass<out ViewModel>, String> = listOf(")
                sortedTabs.forEach {
                    appendLine("        ${it.viewModel}::class to \"${it.screen}\",")
                }
                appendLine("    ).toMap()")
                appendLine()
                appendLine("    override fun <T : ViewModel> navigateToTabIndex(viewModelClass: KClass<T>, params: Any?) {")
                appendLine("     android.os.Handler(android.os.Looper.getMainLooper()).post {")
                appendLine("        val route = tabs[viewModelClass] ?: error(\"Tab not found for \$viewModelClass\")")
                appendLine("        currentTab = viewModelClass")
                appendLine("        val encoded = params?.let {")
                appendLine("            when (it) {")
                appendLine("                is String, is Number, is Boolean -> it.toString()")
                appendLine("                else -> Json.encodeToString(it)")
                appendLine("            }")
                appendLine("        } ?: \"\"")
                appendLine("        navController.navigate(\"\$route?pushParam=\$encoded\") { launchSingleTop = true }")
                appendLine("        }")
                appendLine("    }")
                appendLine("    fun getCurrentTabViewModel(): KClass<out ViewModel>? = currentTab")
                appendLine("}")
            }

            outputFile.writeText(code)
            generateTabNavGraph(holder, sortedTabs)
        }
    }

    private fun generateTabNavGraph(holder: String, tabs: List<TabEntry>) {
        val androidOut = outputAndroidTabsDir.get().asFile
        androidOut.mkdirs()

        val sortedTabs = tabs.sortedBy { it.position }
        val file = File(outputAndroidTabsDir.get().asFile, "${holder}NavGraph.kt")

        val code = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()

            val screenImports = sortedTabs.mapNotNull { findFunctionImport(it.screen) }
            val viewModelImports = sortedTabs.mapNotNull { findViewModelImport(it.viewModel) }
            (screenImports + viewModelImports).distinct().forEach { appendLine("import $it") }

            appendLine("import androidx.navigation.compose.composable")
            appendLine("import androidx.compose.runtime.Composable")
            appendLine("import androidx.navigation.NavHostController")
            appendLine("import androidx.lifecycle.viewmodel.compose.viewModel")
            appendLine("import com.architect.atlas.architecture.navigation.Poppable")
            appendLine("import com.architect.atlas.architecture.mvvm.ViewModel")
            appendLine("import kotlinx.serialization.json.Json")
            appendLine("import androidx.navigation.NavBackStackEntry")
            appendLine("import androidx.lifecycle.Lifecycle")
            appendLine("import androidx.lifecycle.LifecycleEventObserver")
            appendLine("import androidx.lifecycle.LifecycleOwner")
            appendLine("import androidx.compose.runtime.getValue")
            appendLine("import android.graphics.drawable.Drawable")
            appendLine("import androidx.compose.runtime.DisposableEffect")
            appendLine("import androidx.compose.runtime.rememberUpdatedState")
            appendLine("import androidx.lifecycle.compose.LocalLifecycleOwner")
            appendLine("import androidx.navigation.compose.rememberNavController")
            appendLine("import com.architect.atlas.navigation.${holder}TabsNavigation")
            appendLine("import androidx.compose.ui.graphics.vector.ImageVector")
            appendLine("import androidx.compose.animation.*")
            appendLine("import androidx.compose.animation.core.tween")
            appendLine("import com.google.accompanist.navigation.animation.AnimatedNavHost")
            appendLine("import kotlin.reflect.KClass")
            appendLine("import androidx.compose.runtime.mutableStateOf")
            appendLine("import androidx.compose.runtime.remember")
            appendLine("import androidx.compose.runtime.setValue")
            appendLine("import androidx.compose.runtime.SideEffect")
            appendLine()
            appendLine("data class AtlasTabItem(")
            appendLine("    val label: String,")
            appendLine("    val viewModel: KClass<out ViewModel>,")
            appendLine("    val icon: ImageVector? = null,")
            appendLine("    val iconDrawable: Drawable? = null")
            appendLine(")")
            appendLine()
            appendLine("@OptIn(ExperimentalAnimationApi::class)")
            appendLine("@Composable")
            appendLine("fun ${holder}NavGraph() {")
            appendLine("    val navControl = rememberNavController()")
            appendLine("    ${holder}TabsNavigation.navController = navControl")
            appendLine("    var previousTabIndex by remember { mutableStateOf(0) }")
            appendLine("    val currentTab = ${holder}TabsNavigation.getCurrentTabViewModel()")
            appendLine("    val currentTabIndex = listOf(${sortedTabs.joinToString { it.viewModel + "::class" }}).indexOf(currentTab)")
            appendLine("    val isForward = currentTabIndex >= previousTabIndex")
            appendLine("    SideEffect { previousTabIndex = currentTabIndex }")
            appendLine()
            appendLine("    fun tabIndex(route: String?): Int {")
            appendLine("        return when (route?.substringBefore(\"?\")) {")
            sortedTabs.forEach { tab ->
                appendLine("            \"${tab.screen}\" -> ${tab.position}")
            }
            appendLine("            else -> -1")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    AnimatedNavHost(")
            appendLine("        navController = navControl,")
            appendLine("        startDestination = \"${sortedTabs.first().screen}\",")
            appendLine("        enterTransition = {")
            appendLine("            val from = tabIndex(initialState.destination.route)")
            appendLine("            val to = tabIndex(targetState.destination.route)")
            appendLine("            if (to > from)")
            appendLine("                slideInHorizontally { it } + fadeIn(tween(300))")
            appendLine("            else")
            appendLine("                slideInHorizontally { -it } + fadeIn(tween(300))")
            appendLine("        },")
            appendLine("        exitTransition = {")
            appendLine("            val from = tabIndex(initialState.destination.route)")
            appendLine("            val to = tabIndex(targetState.destination.route)")
            appendLine("            if (to > from)")
            appendLine("                slideOutHorizontally { -it } + fadeOut(tween(300))")
            appendLine("            else")
            appendLine("                slideOutHorizontally { it } + fadeOut(tween(300))")
            appendLine("        },")
            appendLine("        popEnterTransition = {")
            appendLine("            val from = tabIndex(initialState.destination.route)")
            appendLine("            val to = tabIndex(targetState.destination.route)")
            appendLine("            if (to > from)")
            appendLine("                slideInHorizontally { it } + fadeIn(tween(300))")
            appendLine("            else")
            appendLine("                slideInHorizontally { -it } + fadeIn(tween(300))")
            appendLine("        },")
            appendLine("        popExitTransition = {")
            appendLine("            val from = tabIndex(initialState.destination.route)")
            appendLine("            val to = tabIndex(targetState.destination.route)")
            appendLine("            if (to > from)")
            appendLine("                slideOutHorizontally { -it } + fadeOut(tween(300))")
            appendLine("            else")
            appendLine("                slideOutHorizontally { it } + fadeOut(tween(300))")
            appendLine("        }")
            appendLine("    ) {")

            for (tab in sortedTabs) {
                appendLine("        composable(\"${tab.screen}?pushParam={pushParam}\") { backStackEntry ->")
                appendLine("            val vm = viewModel(modelClass = ${tab.viewModel}::class.java, viewModelStoreOwner = backStackEntry)")
                appendLine("            val rawParam = backStackEntry.arguments?.getString(\"pushParam\")")
                appendLine("            rawParam?.let {")
                appendLine("                val param: Any = when {")
                appendLine("                    rawParam == \"null\" -> return@let")
                appendLine("                    rawParam.toIntOrNull() != null -> rawParam.toInt()")
                appendLine("                    rawParam.toDoubleOrNull() != null -> rawParam.toDouble()")
                appendLine("                    rawParam.equals(\"true\", true) || rawParam.equals(\"false\", true) -> rawParam.toBoolean()")
                appendLine("                    else -> try { Json.decodeFromString(rawParam) } catch (_: Exception) { rawParam }")
                appendLine("                }")
                appendLine("                @Suppress(\"UNCHECKED_CAST\")")
                appendLine("                (vm as? com.architect.atlas.architecture.navigation.Pushable<Any>)?.onPushParams(param)")
                appendLine("            }")
                appendLine("            HandleLifecycle(vm) {")
                appendLine("                ${tab.screen}(vm)")
                appendLine("            }")
                appendLine("        }")
            }

            appendLine("    }")
            appendLine("}")
        }

        file.writeText(code)
    }

    private fun findFunctionImport(screenName: String): String? {
        outputFiles.forEach { root ->
            root.walkTopDown().forEach { file ->
                if (!file.isFile || !file.extension.equals("kt", true)) return@forEach
                val lines = file.readLines()
                if (lines.any { it.contains("fun $screenName") }) {
                    val pkg = lines.firstOrNull { it.trim().startsWith("package ") }
                        ?.removePrefix("package ")?.trim()
                    return pkg?.let { "$it.$screenName" }
                }
            }
        }
        return null
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
            appendLine("     android.os.Handler(android.os.Looper.getMainLooper()).post {")
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
            appendLine("        }")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> navigateToPagePushAndReplace(viewModelClass: KClass<T>, params: Any?) {")
            appendLine("     android.os.Handler(android.os.Looper.getMainLooper()).post {")
            appendLine("        val route = viewModelToRouteMap[viewModelClass] ?: error(\"No screen registered for \$viewModelClass\")")
            appendLine("        if (navigationStack.isNotEmpty()) {")
            appendLine("            navigationStack.removeLastOrNull()")
            appendLine("        }")
            appendLine("        navigationStack.add(viewModelClass)")
            appendLine("        val encoded = params?.let {")
            appendLine("            when (it) {")
            appendLine("                is String, is Number, is Boolean -> it.toString()")
            appendLine("                else -> Json.encodeToString(it)")
            appendLine("            }")
            appendLine("        } ?: \"\"")
            appendLine("        navController.navigate(\"\$route?pushParam=\$encoded\") {")
            appendLine("            popUpTo(0) { inclusive = true }")
            appendLine("            launchSingleTop = true")
            appendLine("        }")
            appendLine("        }")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> navigateToPageModal(viewModelClass: KClass<T>, params: Any?) {")
            appendLine("        navigateToPage(viewModelClass, params)")
            appendLine("    }")

            appendLine("    override fun <T : ViewModel> setNavigationStack(stack: List<T>, params: Any?) { }")
            appendLine("    override fun <T : ViewModel> getNavigationStack(): List<T> = emptyList()")

            appendLine("    override fun popToRoot(animate: Boolean, params: Any?) {")
            appendLine("""
               android.os.Handler(android.os.Looper.getMainLooper()).post {
        val popCount = navigationStack.size - 1
        if (popCount > 0) {
            popPagesWithCount(popCount, animate, params)
        } else {
            handlePopParams(params)
        }
    }
    }
            """.trimIndent())

            appendLine("    override fun popPage(animate: Boolean, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")


            appendLine("""
                override fun popPagesWithCount(countOfPages: Int, animate: Boolean, params: Any?) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        repeat(countOfPages) {
                            if (navigationStack.size <= 1) return@repeat // prevent popping root

                            navigationStack.removeLastOrNull()
                            navController.popBackStack()
                        }

                        // Deliver pop params to the now-top view model
                        val previousVmClass = navigationStack.lastOrNull()
                        if (previousVmClass != null && params != null) {
                            val encoded = when (params) {
                                is String, is Number, is Boolean -> params.toString()
                                else -> Json.encodeToString(params)
                            }

                            navController.currentBackStackEntry?.let { backStackEntry ->
                                val vm = androidx.lifecycle.ViewModelProvider(
                                    navController.getViewModelStoreOwner(navController.graph.id)
                                )[previousVmClass.java]

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

                                if (vm is Poppable<*>) {
                                    @Suppress("UNCHECKED_CAST")
                                    (vm as Poppable<Any>).onPopParams(decoded)
                                }
                            }
                        }
                    }
                }
            """.trimIndent())

            appendLine("    override fun popToPage(route: String, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")

            appendLine("    override fun dismissModal(animate: Boolean, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")

            appendLine("    private fun handlePopParams(params: Any?) {")
            appendLine("     android.os.Handler(android.os.Looper.getMainLooper()).post {")
            appendLine("        if (navigationStack.size > 2) {")
            appendLine("        navigationStack.removeLastOrNull()")
            appendLine("        val previousVmClass = navigationStack.lastOrNull()")
            appendLine("if(previousVmClass != null){")
            appendLine("        val encoded = params?.let {")
            appendLine("            when (it) {")
            appendLine("                is String, is Number, is Boolean -> it.toString()")
            appendLine("                else -> Json.encodeToString(it)")
            appendLine("            }")
            appendLine("        }")
            appendLine("        navController.currentBackStackEntry?.let { backStackEntry ->")
            appendLine(
                """
                 val vm = androidx.lifecycle.ViewModelProvider(navController.getViewModelStoreOwner(navController.graph.id))[previousVmClass.java]
                if(encoded != null){  
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
            if (vm is Poppable<*>) {
               @Suppress("UNCHECKED_CAST")
               (vm as Poppable<Any>).onPopParams(decoded)
            }
          }
          }
          }
          }
          
       
            """.trimIndent()
            )
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

                // Check only if ViewModel is declared here (not just used)
                val declarationRegex = """(class|object)\s+$viewModelName\b""".toRegex()
                if (lines.any { declarationRegex.containsMatchIn(it) }) {
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
            appendLine("import com.architect.atlas.architecture.mvvm.ViewModel")
            appendLine("import androidx.navigation.NavBackStackEntry")
            appendLine("import androidx.navigation.NavGraphBuilder")
            appendLine("import androidx.compose.animation.*")

            appendLine(
                """
                    import androidx.compose.animation.core.tween
                import androidx.compose.runtime.DisposableEffect
                import androidx.lifecycle.Lifecycle
                import androidx.lifecycle.LifecycleEventObserver
                import androidx.lifecycle.LifecycleOwner
                import androidx.compose.runtime.getValue
                import androidx.compose.runtime.rememberUpdatedState
                import androidx.lifecycle.compose.LocalLifecycleOwner
            """.trimIndent()
            )
            appendLine()

            appendLine(
                """
                @Composable
                fun HandleLifecycle(
                    viewModel: ViewModel,
                    content: @Composable () -> Unit
                ) {
                    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
                    val currentViewModel by rememberUpdatedState(viewModel)

                    DisposableEffect(lifecycleOwner) {
                       val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_DESTROY -> {
                                    currentViewModel.onDestroy()
                                    currentViewModel.onCleared()
                                }
                                Lifecycle.Event.ON_RESUME -> currentViewModel.onAppearing()
                                Lifecycle.Event.ON_PAUSE -> currentViewModel.onDisappearing()
                                else -> {}
                            }
                        }

                        lifecycleOwner.lifecycle.addObserver(observer)

                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    content()
                }
            """.trimIndent()
            )

            appendLine("@Composable")
            appendLine("fun AtlasNavGraph() {")
            appendLine("    val navController = rememberNavController()")
            appendLine("    AtlasNavigation.navController = navController")

            val start = screens.firstOrNull { it.fourth }?.second ?: "MissingStart"
            appendLine("    NavHost(navController = navController, startDestination = \"$start\") {")
            for ((viewModel, screenComposable, _, _) in screens) {
                appendLine("@OptIn(ExperimentalAnimationApi::class)")
                appendLine(
                    "        composable(\"$screenComposable?pushParam={pushParam}\", " +
                            """enterTransition = {
                return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250))
            }, exitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(250)
                )
            }, popEnterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(250)
                )
            }""" +


                            ") { backStackEntry ->"
                )
                appendLine("            val vm = viewModel(modelClass = $viewModel::class.java)")
                appendLine("            val rawParam = backStackEntry.arguments?.getString(\"pushParam\")")
                appendLine("            rawParam?.let {")
                appendLine("                val param: Any = when {")
                appendLine("                    rawParam == \"null\" -> return@let")
                appendLine("                    rawParam.toIntOrNull() != null -> rawParam.toInt()")
                appendLine("                    rawParam.toDoubleOrNull() != null -> rawParam.toDouble()")
                appendLine("                    rawParam.equals(\"true\", true) || rawParam.equals(\"false\", true) -> rawParam.toBoolean()")
                appendLine("                    else -> try { kotlinx.serialization.json.Json.decodeFromString(rawParam) } catch (_: Exception) { rawParam }")
                appendLine("                }")
                appendLine("                @Suppress(\"UNCHECKED_CAST\")")
                appendLine("                (vm as? com.architect.atlas.architecture.navigation.Pushable<Any>)?.onPushParams(param)")
                appendLine("            }")
                appendLine("            HandleLifecycle(vm) {")
                appendLine("                $screenComposable(vm)")
                appendLine("            }")
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
        val swiftRegex =
            """^\s*//\s*@?AtlasScreen\s*\(\s*viewModel\s*:\s*([A-Za-z0-9_]+)\.self\s*(?:,\s*initial\s*:\s*(true|false))?\s*\)""".toRegex()

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
                        println(
                            "🔣 Unicode points: ${
                                rawLine.map { it.code }.joinToString(", ") { "U+%04X".format(it) }
                            }"
                        )
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
                        (rawLine.trim().startsWith("struct ") || rawLine.trim()
                            .startsWith("class "))
                    ) {
                        val currentClass =
                            rawLine.trim().split("\\s+".toRegex()).getOrNull(1)?.trim()
                        if (currentClass != null) {
                            val viewModelName = pendingAnnotation.groupValues[1]
                            val isInitial =
                                pendingAnnotation.groupValues.getOrNull(2)?.toBooleanStrictOrNull()
                                    ?: false

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
                appendLine("            let controller = LifecycleAwareHostingController(rootView: view, viewModel: resolved)")
                appendLine("            controller.navigationController?.setNavigationBarHidden(true, animated: false)")
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
                    let resolved = AtlasDI.companion.resolveServiceNullableByName(
                            clazz: SwiftClassGenerator.companion.getClazz(type: $initialViewModel.self)
                        ) as! $initialViewModel
                    let root = $initialScreen(
                        vm: resolved
                    )
                    let hostingController = LifecycleAwareHostingController(rootView: root, viewModel: resolved)
                    let navController = UINavigationController(rootViewController: hostingController)
                    UIApplication.globalRootNav = navController
                    return navController
                }

                func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
            }
            
            class LifecycleAwareHostingController<Content: View>: UIHostingController<Content> {
               private let viewModel: ViewModel
               init(rootView: Content, viewModel: ViewModel) {
                   self.viewModel = viewModel
                   super.init(rootView: rootView)
               }
               
               @objc required dynamic init?(coder aDecoder: NSCoder) {
                   fatalError("init(coder:) has not been implemented")
               }
               
               override func viewWillAppear(_ animated: Bool) {
                   super.viewWillAppear(animated)
                   viewModel.onAppearing()
               }
               
               override func viewWillDisappear(_ animated: Bool) {
                   viewModel.onDisappearing()
               }
               
               override func willMove(toParent parent: UIViewController?) {
                   if parent == nil {
                       Task { @MainActor in
                           self.viewModel.onDestroy()
                           self.viewModel.onCleared()
                       }
                   }
                   super.willMove(toParent: parent)
               }
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

            appendLine(
                """class IOSAtlasNavigationService: NSObject, @preconcurrency AtlasNavigationService {
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
    func navigateToPagePushAndReplace(viewModelClass: any KotlinKClass, params: Any?) {
     DispatchQueue.main.async {
        guard let nav = UIApplication.shared.rootNav else { return }

        // Remove the top of the stack and pop the view controller
        if !NavigationEngine.shared.stack.isEmpty {
            NavigationEngine.shared.stack.removeLast()
            if nav.viewControllers.count > 1 {
                nav.popViewController(animated: false)
            } else {
                // Replacing root: manually reset the root
                nav.setViewControllers([], animated: false)
            }
        }

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

            """.trimIndent()
            )
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

data class TabEntry(
    val viewModel: String,
    val screen: String,
    val holderViewModel: String,
    val position: Int
)

fun String.normalizeToAscii(): String =
    this.map { if (it.code in 32..126) it else ' ' }.joinToString("")