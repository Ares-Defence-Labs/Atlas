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

            // tab navigation
            scanIosTabAnnotationsFromSwiftFiles(iOSOutputFiles).forEach { (holder, tabs) ->
                generateIosTabNavigationServiceFile(holder, tabs)
            }
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

    private fun scanIosTabAnnotationsFromSwiftFiles(sourceDirs: List<File>): Map<String, List<TabEntrySwift>> {
        val tabEntries = mutableListOf<TabEntrySwift>()
        val swiftAnnotationRegex =
            Regex("""//@AtlasSwiftTab\(\s*(\w+)::class\s*,\s*position\s*=\s*(\d+)\s*,\s*holder\s*=\s*(\w+)::class(?:\s*,\s*initialSelected\s*=\s*(true|false))?\s*\)""")

        sourceDirs.forEach {
            it.walkTopDown().filter { it.isFile && it.extension == "swift" }.forEach { file ->
                val content = file.readText()

                swiftAnnotationRegex.findAll(content).forEach { match ->
                    val viewModel = match.groupValues[1]
                    val position = match.groupValues[2].toInt()
                    val holder = match.groupValues[3]
                    val screenName = file.name.removeSuffix(".swift")

                    val initialSelected =
                        match.groupValues.getOrNull(4)?.toBooleanStrictOrNull() ?: false
                    tabEntries.add(
                        TabEntrySwift(
                            viewModel = viewModel,
                            screen = screenName + ".swift",
                            position = position,
                            holder = holder,
                            initialSelected = initialSelected
                        )
                    )
                }
            }
        }

        return tabEntries.groupBy { it.holder }
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
                appendLine("import androidx.compose.runtime.compositionLocalOf")
                appendLine("import java.lang.ref.WeakReference")

                appendLine(
                    """
                val TabLocalAtlasNavController = compositionLocalOf<NavHostController> {
                    error("NavController not provided")
                }
            """.trimIndent()
                )

                appendLine(
                    """
                    
                    object AtlasTabNavHolder {
                        private var navControllerRef: WeakReference<NavHostController>? = null

                        fun bind(navController: NavHostController) {
                            navControllerRef = WeakReference(navController)
                        }

                        fun get(): NavHostController? = navControllerRef?.get()
                    }
                """.trimIndent()
                )
                appendLine()
                appendLine("object ${holder}TabsNavigation : AtlasTabNavigationService {")
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
                appendLine("        AtlasTabNavHolder.get()?.navigate(\"\$route?pushParam=\$encoded\") { launchSingleTop = true }")
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
            appendLine("import androidx.compose.runtime.CompositionLocalProvider")
            appendLine(
                """
                import com.architect.atlas.navigation.TabLocalAtlasNavController
                import com.architect.atlas.navigation.AtlasTabNavHolder
                import androidx.navigation.compose.currentBackStackEntryAsState
                import androidx.compose.runtime.LaunchedEffect
            """.trimIndent()
            )
            appendLine()

            appendLine("    fun tabIndex(route: String?): Int {")
            appendLine("        return when (route?.substringBefore(\"?\")) {")
            sortedTabs.forEach { tab ->
                appendLine("            \"${tab.screen}\" -> ${tab.position}")
            }
            appendLine("            else -> -1")
            appendLine("        }")
            appendLine("    }")

            appendLine("data class AtlasTabItem(")
            appendLine("    val label: String,")
            appendLine("    val viewModel: KClass<out ViewModel>,")
            appendLine("    val icon: ImageVector? = null,")
            appendLine("    val iconDrawable: Drawable? = null")
            appendLine(")")
            appendLine()
            appendLine("@OptIn(ExperimentalAnimationApi::class)")

            appendLine("@Composable")
            appendLine("fun ${holder}NavGraph(onTabPositionChanged: (Int) -> Unit) {")
            appendLine(
                """
    val navControl = rememberNavController()
    AtlasTabNavHolder.bind(navControl)
    CompositionLocalProvider(TabLocalAtlasNavController provides navControl) {
    """.trimIndent()
            )

            appendLine(
                """
    val navBackStackEntry by navControl.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
    
    """.trimIndent()
            )

// Animation direction logic
            appendLine("    var previousTabIndex by remember { mutableStateOf(0) }")
            appendLine("    val newTabIndex = tabIndex(currentRoute)")
            appendLine("    val isForward = newTabIndex >= previousTabIndex")
            appendLine("    SideEffect { previousTabIndex = newTabIndex }")

// Change callback tracking
            appendLine("    var lastCallbackTabIndex by remember { mutableStateOf(-1) }")
            appendLine(
                """
    LaunchedEffect(newTabIndex) {
        if (newTabIndex != -1 && newTabIndex != lastCallbackTabIndex) {
            lastCallbackTabIndex = newTabIndex
            onTabPositionChanged(newTabIndex)
        }
    }
    """.trimIndent()
            )

// tabIndex() function
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


            appendLine("}")
            appendLine("}")
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
            appendLine("import android.annotation.SuppressLint")
            appendLine("import androidx.navigation.NavHostController")
            appendLine("import androidx.lifecycle.ViewModelProvider")
            appendLine("import androidx.lifecycle.viewmodel.compose.viewModel")
            appendLine("import com.architect.atlas.architecture.navigation.AtlasNavigationService")
            appendLine("import com.architect.atlas.architecture.mvvm.ViewModel")
            appendLine("import com.architect.atlas.architecture.navigation.Poppable")
            appendLine("import kotlinx.serialization.encodeToString")
            appendLine("import kotlinx.serialization.decodeFromString")
            appendLine("import kotlinx.serialization.json.Json")
            appendLine("import kotlin.reflect.KClass")
            appendLine("import com.architect.atlas.container.dsl.AtlasDI")
            appendLine()

            appendLine(
                """
                class ObservableStack<T>(
                    private val onPop: (T) -> Unit
                ) : MutableList<T> by mutableListOf() {

                    fun removeLastSafely(): T? {
                        if (isNotEmpty()) {
                            val removed = removeLast()
                            onPop(removed)
                            return removed
                        }
                        return null
                    }

                    fun popMultiple(count: Int) {
                        repeat(count) {
                            if (isNotEmpty()) {
                                removeLastSafely()
                            }
                        }
                    }

                    fun clearStack(onEach: (T) -> Unit) {
                        for (item in this) onEach(item)
                        clear()
                    }
                }
            """.trimIndent()
            )
            appendLine("object AtlasNavigation : AtlasNavigationService {")
            appendLine(
                """
                private val navigationStack = ObservableStack<KClass<out ViewModel>> { poppedVm ->
                    AtlasDI.resetViewModel(poppedVm)
                }
            """.trimIndent()
            )
            appendLine("    private val viewModelToRouteMap: Map<KClass<out ViewModel>, String> = mapOf(")
            for ((viewModel, screenName) in screens) {
                appendLine("        $viewModel::class to \"$screenName\",")
            }
            appendLine("    )")
            appendLine()
            appendLine("    override fun <T : ViewModel> navigateToPage(viewModelClass: KClass<T>, params: Any?) {")
            appendLine("        pushToStack(viewModelClass)")
            appendLine("        navigateWithRoute(viewModelClass, params)")
            appendLine("    }")
            appendLine()
            appendLine("    override fun <T : ViewModel> navigateToPagePushAndReplace(viewModelClass: KClass<T>, params: Any?) {")
            appendLine("        replaceTopOfStack(viewModelClass)")
            appendLine("        navigateWithRoute(viewModelClass, params, popAll = true)")
            appendLine("    }")
            appendLine()
            appendLine("    override fun <T : ViewModel> navigateToPagePushAndReplaceCurrentScreen(viewModelClass: KClass<T>, params: Any?) {")
            appendLine("        replaceTopOfStack(viewModelClass)")
            appendLine("        navigateWithRoute(viewModelClass, params, popCurrent = true)")
            appendLine("    }")
            appendLine()
            appendLine("    override fun <T : ViewModel> navigateToPageModal(viewModelClass: KClass<T>, params: Any?) = navigateToPage(viewModelClass, params)")
            appendLine("    override fun <T : ViewModel> setNavigationStack(stack: List<T>, params: Any?) {}")
            appendLine("    override fun <T : ViewModel> getNavigationStack(): List<T> = emptyList()")
            appendLine()
            appendLine("    override fun popToRoot(animate: Boolean, params: Any?) {")
            appendLine("        postToMain {")
            appendLine("            val popCount = navigationStack.size - 1")
            appendLine("            if (popCount > 0) popPagesWithCount(popCount, animate, params) else handlePopParams(params)")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    override fun popPage(animate: Boolean, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")
            appendLine()
            appendLine("    override fun popPagesWithCount(countOfPages: Int, animate: Boolean, params: Any?) {")
            appendLine("        postToMain {")
            appendLine(
                """
                repeat(countOfPages) {
                    if (navigationStack.size > 1) {
                        navigationStack.removeLastSafely()
                        AtlasNavHolder.get()?.popBackStack()
                    }
                }
            """.trimIndent()
            )
            appendLine("            deliverPopParams(params)")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    override fun popToPage(route: String, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")
            appendLine()
            appendLine("    override fun dismissModal(animate: Boolean, params: Any?) {")
            appendLine("        handlePopParams(params)")
            appendLine("    }")
            appendLine()
            appendLine("    private fun <T : ViewModel> navigateWithRoute(viewModelClass: KClass<T>, params: Any?, popAll: Boolean = false, popCurrent: Boolean = false) {")
            appendLine("        val route = viewModelToRouteMap[viewModelClass] ?: error(\"No screen registered for \$viewModelClass\")")
            appendLine("        val encoded = encodeParam(params)")
            appendLine("        postToMain {")
            appendLine("            AtlasNavHolder.get()?.navigate(\"\$route?pushParam=\$encoded\") {")
            appendLine("                when {")
            appendLine("                    popAll -> popUpTo(0) { inclusive = true }")
            appendLine("                    popCurrent -> popUpTo(AtlasNavHolder.get()!!.currentDestination?.id ?: 0) { inclusive = true }")
            appendLine("                }")
            appendLine("                launchSingleTop = true")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    private fun handlePopParams(params: Any?) {")
            appendLine("        postToMain {")
            appendLine(
                """
                if (navigationStack.size > 1) {
                    navigationStack.removeLastSafely()
                    deliverPopParams(params)
                    AtlasNavHolder.get()?.popBackStack()
                }
            """.trimIndent()
            )
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    private fun deliverPopParams(params: Any?) {")
            appendLine("        val previousVmClass = navigationStack.lastOrNull() ?: return")
            appendLine("        val encoded = encodeParam(params) ?: return")
            appendLine("        AtlasNavHolder.get()?.currentBackStackEntry?.let {")
            appendLine("            val vm = resolveViewModel(previousVmClass)")
            appendLine("            decodeParam(encoded)?.let { decoded ->")
            appendLine("                if (vm is Poppable<*>) {")
            appendLine("                    @Suppress(\"UNCHECKED_CAST\")")
            appendLine("                    (vm as Poppable<Any>).onPopParams(decoded)")
            appendLine("                }")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    private fun resolveViewModel(vmClass: KClass<out ViewModel>): ViewModel {")
            appendLine("        return ViewModelProvider(AtlasNavHolder.get()!!.getViewModelStoreOwner(AtlasNavHolder.get()!!.graph.id))[vmClass.java]")
            appendLine("    }")
            appendLine()
            appendLine("    private fun pushToStack(viewModelClass: KClass<out ViewModel>) {")
            appendLine("        if (navigationStack.isEmpty()) navigationStack.add(viewModelToRouteMap.keys.first())")
            appendLine("        navigationStack.add(viewModelClass)")
            appendLine("    }")
            appendLine()
            appendLine("    private fun replaceTopOfStack(viewModelClass: KClass<out ViewModel>) {")
            appendLine("        if (navigationStack.isNotEmpty()) navigationStack.removeLastSafely()")
            appendLine("        navigationStack.add(viewModelClass)")
            appendLine("    }")
            appendLine()
            appendLine("    private fun encodeParam(param: Any?): String? {")
            appendLine("        return param?.let {")
            appendLine("            when (it) {")
            appendLine("                is String, is Number, is Boolean -> it.toString()")
            appendLine("                else -> Json.encodeToString(it)")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    private fun decodeParam(encoded: String): Any? {")
            appendLine("        return when {")
            appendLine("            encoded.toIntOrNull() != null -> encoded.toInt()")
            appendLine("            encoded.toDoubleOrNull() != null -> encoded.toDouble()")
            appendLine("            encoded.equals(\"true\", true) || encoded.equals(\"false\", true) -> encoded.toBoolean()")
            appendLine("            else -> runCatching { Json.decodeFromString<Any>(encoded) }.getOrNull() ?: encoded")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    private fun postToMain(block: () -> Unit) {")
            appendLine("        android.os.Handler(android.os.Looper.getMainLooper()).post(block)")
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

            // Imports
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

            val start = screens.firstOrNull { it.fourth }?.second ?: "MissingStart"

            appendLine(
                """
import com.architect.atlas.navigation.AtlasNavigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.architecture.navigation.Pushable
import com.architect.atlas.architecture.mvvm.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import kotlinx.serialization.json.Json
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.compositionLocalOf

import com.architect.atlas.container.dsl.AtlasDI
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.architect.atlas.container.AtlasContainer
import androidx.activity.ComponentActivity

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import java.lang.ref.WeakReference

            """.trimIndent()
            )
            appendLine()

            // Composable function
            appendLine(
                """
                    
                    val LocalAtlasNavController = compositionLocalOf<NavHostController> {
                        error("NavController not provided")
                    }

                    object AtlasNavHolder {
                        private var navControllerRef: WeakReference<NavHostController>? = null

                        fun bind(navController: NavHostController) {
                            navControllerRef = WeakReference(navController)
                        }

                        fun get(): NavHostController? = navControllerRef?.get()
                    }
                    
@Composable
fun AtlasNavGraph() {
    val navController = rememberNavController()
    AtlasNavHolder.bind(navController)
    CompositionLocalProvider(LocalAtlasNavController provides navController) {
    
    NavHost(navController = navController, startDestination = "$start") {
${
                    screens.joinToString("\n") { (viewModel, screen) ->
                        """        screen<$viewModel>("$screen") { $screen(it) }"""
                    }
                }
    }
    }
}
""".trimIndent()
            )

            // screen<> extension function
            appendLine(
                """
@OptIn(ExperimentalAnimationApi::class)
inline fun <reified VM : ViewModel> NavGraphBuilder.screen(
    route: String,
    noinline content: @Composable (VM) -> Unit
) {
    composable(
        route = "${'$'}route?pushParam={pushParam}",
        arguments = listOf(navArgument("pushParam") {
            nullable = true
            defaultValue = null
        }),
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
        },
    ) { backStackEntry ->
   val activity = LocalContext.current as ComponentActivity
val viewModelStoreOwner = activity.viewModelStore
val vm = remember(viewModelStoreOwner) {
    AtlasContainer.resolveViewModel(VM::class)
}!!
    
        backStackEntry.arguments?.getString("pushParam")?.let { raw ->
            decodeParam(raw)?.let { param ->
                if (vm is Pushable<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (vm as Pushable<Any>).onPushParams(param)
                }
            }
        }
        HandleLifecycle<VM>(vm) {
            content(vm)
        }
    }
}
""".trimIndent()
            )

            // decodeParam function
            appendLine(
                """
fun decodeParam(raw: String): Any? = when {
    raw == "null" -> null
    raw.toIntOrNull() != null -> raw.toInt()
    raw.toDoubleOrNull() != null -> raw.toDouble()
    raw.equals("true", true) || raw.equals("false", true) -> raw.toBoolean()
    else -> runCatching { Json.decodeFromString<Any>(raw) }.getOrNull() ?: raw
}
""".trimIndent()
            )

            // Lifecycle wrapper
            appendLine(
                """
@Composable
inline fun <reified VM : ViewModel> HandleLifecycle(
    viewModel: VM,
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
        }

        val file = File(outputAndroidDir.get().asFile, "AtlasNavGraph.kt")
        file.parentFile.mkdirs()
        file.writeText(navGraph)
    }

    // IOS Specific Generators
    private fun generateIosTabNavigationServiceFile(
        holder: String,
        tabs: List<TabEntrySwift>
    ) {
        val file = File(outputIosDir.get().asFile, "${holder}TabNavigationService.swift")
        file.parentFile.mkdirs()

        val importSection = """
    import UIKit
    import SwiftUI
    import $projectCoreName
    """.trimIndent()

        val tabIndicesEntries = tabs.joinToString(",\n") { tab ->
            val viewModelName = tab.viewModel.substringAfterLast(".")
            "\"$viewModelName\": ${tab.position}"
        }

        val tabMappingEntries = tabs.joinToString(",\n") { tab ->
            val viewModelName = tab.viewModel.substringAfterLast(".")
            val screenName = tab.screen.removeSuffix(".swift")
            val widgetName = screenName.replace("Screen", "Widget")

            """"$viewModelName": {
        let vm = AtlasDI.companion.resolveServiceNullableByName(
            clazz: SwiftClassGenerator.companion.getClazz(type: $viewModelName.self)
        ) as! $viewModelName
        return LifecycleAwareHostingController(rootView: $widgetName(vm: vm), viewModel: vm)
    }"""
        }

        val className = "${holder.removeSuffix("ViewModel")}TabsNavigation"

        val classCode = """
    @MainActor
    struct LifecycleTabAwareHostingView<Content: View, VM: ViewModel>: View {
        @StateObject private var viewModel: VM
        let content: (VM) -> Content

        init(viewModel: VM, @ViewBuilder content: @escaping (VM) -> Content) {
            _viewModel = StateObject(wrappedValue: viewModel)
            self.content = content
        }

        var body: some View {
            content(viewModel)
                .onAppear { viewModel.onAppearing() }
                .onDisappear { viewModel.onDisappearing() }
        }
    }

    protocol AtlasTabItemView {
        associatedtype Selected: View
        associatedtype Deselected: View

        @ViewBuilder func selectedTabItem() -> Selected
        @ViewBuilder func deselectedTabItem() -> Deselected
    }

    @MainActor
    @ViewBuilder
    func buildTab<T: ViewModel, Content: View, SelectedTabItem: View, DeselectedTabItem: View>(
        _ type: T.Type,
        selectedTabIndex: Binding<Int>,
        tabIndex: Int,
        selectedTabItemBuilder: () -> SelectedTabItem,
        deselectedTabItemBuilder: () -> DeselectedTabItem,
        screenBuilder: @escaping (T) -> Content
    ) -> some View {
        let vm = AtlasDI.companion.resolveServiceNullableByName(
            clazz: SwiftClassGenerator.companion.getClazz(type: type)
        ) as! T

        LifecycleTabAwareHostingView(viewModel: vm) { vm in
            screenBuilder(vm)
        }
        .tag(tabIndex)
        .tabItem {
            if selectedTabIndex.wrappedValue == tabIndex {
                selectedTabItemBuilder()
            } else {
                deselectedTabItemBuilder()
            }
        }
    }

    @MainActor
@ViewBuilder
func buildFloatingActionButton<T: ViewModel, Content: View, ItemView: AtlasTabItemView>(
    fabTabIndex: Int,
    selectedTabIndex: Binding<Int>,
    viewModelType: T.Type,
    itemView: ItemView,
    @ViewBuilder fabContainer: @escaping (T) -> Content
) -> some View {
    let vm = AtlasDI.companion.resolveServiceNullableByName(
        clazz: SwiftClassGenerator.companion.getClazz(type: viewModelType)
    ) as! T

    GeometryReader { geometry in
        VStack {
            Spacer()
            HStack {
                Spacer()
                
                Button(action: {
                    selectedTabIndex.wrappedValue = fabTabIndex
                }) {
                    ZStack {
                        fabContainer(vm)
                            .frame(width: 84, height: 84)
                            .clipShape(Circle())
                            .shadow(radius: 4)
                        
                        if selectedTabIndex.wrappedValue == fabTabIndex {
                            itemView.selectedTabItem()
                        } else {
                            itemView.deselectedTabItem()
                        }
                    }
                }
                .padding(.bottom, geometry.safeAreaInsets.bottom + 48) // lift above tab bar
                
                Spacer()
            }
        }
        .edgesIgnoringSafeArea(.bottom)
    }
}

    @MainActor
    class $className: NSObject, @preconcurrency AtlasTabNavigationService {
        static let shared = $className()

        @Published private(set) var selectedTabIndex: Int = 0

        private let tabIndices: [String: Int] = [
            $tabIndicesEntries
        ]

        private let tabMapping: [String: () -> UIViewController] = [
            $tabMappingEntries
        ]

        private var currentTab: ViewModel?

        @MainActor
        func navigateToTabIndex(viewModelClass: KotlinKClass, params: Any? = nil) {
            guard let viewModelName = viewModelClass.simpleName else {
                print("Invalid ViewModel class passed")
                return
            }

            if let index = tabIndices[viewModelName] {
                print("Navigating to tab index \\(index) for \\(viewModelName)")
                selectedTabIndex = index
            } else {
                print("Tab index for \\(viewModelName) not found")
            }
        }

        func getSelectedTabIndex() -> Int {
            return selectedTabIndex
        }

        func setSelectedTabIndex(_ index: Int) {
            selectedTabIndex = index
        }

        func routeToTab(viewModelType: String) -> UIViewController {
            currentTab = nil
            guard let builder = tabMapping[viewModelType] else {
                fatalError("No tab registered for \\(viewModelType)")
            }
            let vc = builder()
            currentTab = (vc as? LifecycleAwareHosting)?.viewModel
            return vc
        }

        func getCurrentTabViewModel() -> ViewModel? {
            return currentTab
        }
    }
    """.trimIndent()

        file.writeText(
            """
        $importSection

        $classCode
        """.trimIndent()
        )
    }
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
            appendLine("        UIApplication.globalRootNav?.popToRootViewController(animated: animate)")
            appendLine("    }")

            appendLine("     func popPage(animate: Bool = true, params: Any? = nil) {")
            appendLine("        if stack.count >= 2 {")
            appendLine("            let prev = stack[stack.count - 2] as? Poppable")
            appendLine("            if let pc = params {")
            appendLine("                prev?.onPopParams(params: pc)")
            appendLine("            }")
            appendLine("        }")
            appendLine("        if !stack.isEmpty { stack.removeLast() }")
            appendLine("        UIApplication.globalRootNav?.popViewController(animated: animate)")
            appendLine("    }")

            appendLine("     func popPagesWithCount(count: Int, animate: Bool = true, params: Any? = nil) {")
            appendLine("        guard let nav = UIApplication.globalRootNav else { return }")
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
            appendLine("        UIApplication.globalRootNav?.presentedViewController?.dismiss(animated: animate)")
            appendLine("    }")

            appendLine()
            appendLine("@MainActor")
            appendLine("func createViewController(viewModelType: String, viewModel: ViewModel) -> UIViewController? {")
            for ((vm, screen, _) in screens) {
                val vmSimpleName = vm.substringAfterLast(".")
                val screenName = screen.replace(":", "")

                appendLine("    if viewModelType.hasSuffix(\"$vmSimpleName\") {")
                appendLine("        let vm = viewModel as! $vm")
                appendLine("        let view = $screenName(vm: vm)")
                appendLine("        return LifecycleAwareHostingController(rootView: view, viewModel: vm)")
                appendLine("    }")
            }

            appendLine("    return nil")
            appendLine("    }")
            appendLine("    }")

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
            
            protocol LifecycleAwareHosting {
                var viewModel: ViewModel { get }
            }

            class LifecycleAwareHostingController<Content: View>: UIHostingController<Content>, @preconcurrency LifecycleAwareHosting {
               let viewModel: ViewModel
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
            appendLine("import SwiftUICore")
            appendLine("""
                import SwiftUI
                    import Combine
            """.trimIndent())
            appendLine()

            appendLine(
                """    
                    import Foundation
                    import SwiftUI
                    import Combine

                    
                    @MainActor
                    class AlertDialogManager: ObservableObject {
                        static let shared = AlertDialogManager()
                        
                        @Published var activeSheetID: String? = nil
                        @Published var params: [String: Any] = [:]
                        @Published var heightOffset: CGFloat = 0
                        @Published var isPresented: Bool = false
                        
                        init() {
                            NotificationCenter.default.addObserver(
                                self,
                                selector: #selector(handleOpenAlertDialog(_:)),
                                name: .openAlertDialog,
                                object: nil
                            )
                            
                            NotificationCenter.default.addObserver(
                                self,
                                selector: #selector(dismiss(_:)),
                                name: .dismissAlertDialog,
                                object: nil
                            )
                        }
                        
                        @objc private func handleOpenAlertDialog(_ notification: Notification) {
                            DispatchQueue.main.async { [weak self] in
                                guard let self = self,
                                      let userInfo = notification.userInfo,
                                      let id = userInfo["id"] as? String else { return }
                                
                                self.params = userInfo.reduce(into: [String: Any]()) { dict, pair in
                                    if let key = pair.key as? String {
                                        dict[key] = pair.value
                                    }
                                }
                                
                                self.heightOffset = userInfo["heightOffset"] as? CGFloat ?? 0
                                self.activeSheetID = id
                                self.isPresented = true
                            }
                        }
                        
                        @objc private func dismiss(_ notification: Notification) {
                            activeSheetID = nil
                            params = [:]
                            isPresented = false
                            print("Bottom sheet dismissed")
                        }
                    }


                    @MainActor
                    class BottomSheetManager: ObservableObject {
                        static let shared = BottomSheetManager()
                        
                        @Published var activeSheetID: String? = nil
                        @Published var params: [String: Any] = [:]
                        @Published var heightOffset: CGFloat = 0
                        @Published var isPresented: Bool = false
                        
                        init() {
                            NotificationCenter.default.addObserver(
                                self,
                                selector: #selector(handleOpenBottomSheet(_:)),
                                name: .openBottomSheet,
                                object: nil
                            )
                        }
                        
                        @objc private func handleOpenBottomSheet(_ notification: Notification) {
                            DispatchQueue.main.async { [weak self] in
                                guard let self = self,
                                      let userInfo = notification.userInfo,
                                      let id = userInfo["id"] as? String else { return }
                                
                                self.params = userInfo.reduce(into: [String: Any]()) { dict, pair in
                                    if let key = pair.key as? String {
                                        dict[key] = pair.value
                                    }
                                }
                                
                                self.heightOffset = userInfo["heightOffset"] as? CGFloat ?? 0
                                self.activeSheetID = id
                                self.isPresented = true
                                
                                print("Bottom sheet presented for ID: \(id)")
                            }
                        }
                        
                        func dismiss() {
                            activeSheetID = nil
                            params = [:]
                            isPresented = false
                            print("Bottom sheet dismissed")
                        }
                    }

                    extension Notification.Name {
                        static let openBottomSheet = Notification.Name("openBottomSheet")
                        static let openAlertDialog = Notification.Name("openAlertDialog")
                        static let dismissAlertDialog = Notification.Name("dismissAlertDialog")
                    }

                    extension View {
                        func bottomSheetRegistry(
                            id: String,
                            @ViewBuilder content: @escaping (_ params: [String: Any]) -> some View
                        ) -> some View {
                            modifier(BottomSheetModifier(sheetID: id, content: content))
                        }
                        
                        func alertDialogRegistry(
                            id: String,
                            @ViewBuilder content: @escaping (_ params: [String: Any]) -> some View
                        ) -> some View {
                            modifier(AlertModifier(sheetID: id, content: content))
                        }
                    }


                    struct AlertModifier: ViewModifier {
                        @ObservedObject var manager = AlertDialogManager.shared
                        let sheetID: String
                        let contentBuilder: ([String: Any]) -> AnyView
                        
                        init<T: View>(
                            sheetID: String,
                            @ViewBuilder content: @escaping ([String: Any]) -> T
                        ) {
                            self.sheetID = sheetID
                            self.contentBuilder = { AnyView(content(${'$'}0)) }
                        }
                        
                        @State private var contentHeight: CGFloat = 250
                        @State private var yOffset: CGFloat = UIScreen.main.bounds.height
                        
                        func body(content viewContent: Content) -> some View {
                            viewContent
                                .overlay(
                                    Group {
                                        Color.black.opacity(manager.isPresented && manager.activeSheetID == sheetID ? 0.3 : 0)
                                            .ignoresSafeArea()
                                            .onTapGesture {
                                                withAnimation {
                                                   // manager.dismiss()
                                                }
                                            }
                                        if(manager.isPresented){
                                        alertView()
                                            .allowsHitTesting(manager.isPresented && manager.activeSheetID == sheetID)
                                            }
                                    },
                                    alignment: .bottom
                                )
                        }
                        
                        @ViewBuilder
                        private func alertView() -> some View {
                            ZStack(alignment: .center) {
                                if manager.isPresented {
                                    self.contentBuilder(manager.params)
                                        .frame(maxWidth: .infinity, maxHeight: contentHeight + manager.heightOffset)
                                        .background(Color.white)
                                        .clipShape(RoundedCorner(radius: 26))
                                        .shadow(radius: 10)
                                        .transition(.opacity)
                                        .animation(.easeInOut(duration: 2.0), value: manager.isPresented)
                                }
                            }
                            .padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20))
                        }
                    }

                    struct BottomSheetModifier: ViewModifier {
                        @ObservedObject var manager = BottomSheetManager.shared
                        let sheetID: String
                        let contentBuilder: ([String: Any]) -> AnyView
                        
                        init<T: View>(
                            sheetID: String,
                            @ViewBuilder content: @escaping ([String: Any]) -> T
                        ) {
                            self.sheetID = sheetID
                            self.contentBuilder = { AnyView(content(${'$'}0)) }
                        }
                        
                        @State private var contentHeight: CGFloat = 300
                        @State private var yOffset: CGFloat = UIScreen.main.bounds.height
                        
                        func body(content viewContent: Content) -> some View {
                            viewContent
                                .overlay(
                                    Group {
                                        Color.black.opacity(manager.isPresented && manager.activeSheetID == sheetID ? 0.3 : 0)
                                            .ignoresSafeArea()
                                            .onTapGesture {
                                                withAnimation {
                                                    manager.dismiss()
                                                }
                                            }
                                        
                                        bottomSheetView()
                                            .allowsHitTesting(manager.isPresented && manager.activeSheetID == sheetID)
                                    },
                                    alignment: .bottom
                                )
                        }
                        @ViewBuilder
                        private func bottomSheetView() -> some View {
                            VStack(spacing: 0) {
                                Spacer()
                                
                                self.contentBuilder(manager.params)
                                    .frame(maxWidth: .infinity, maxHeight: contentHeight + manager.heightOffset)
                                    .background(Color.white)
                                    .clipShape(RoundedCorner(radius: 26, corners: [.topLeft, .topRight]))
                                    .shadow(radius: 10)
                                    .offset(y: manager.isPresented && manager.activeSheetID == sheetID ? 0 : UIScreen.main.bounds.height)
                                    .animation(.interactiveSpring(response: 0.4, dampingFraction: 0.85, blendDuration: 0.25),
                                               value: manager.isPresented
                                    )
                            }
                            .ignoresSafeArea(edges: .bottom)
                        }
                    }

                    struct RoundedCorner: Shape {
                        var radius: CGFloat = .infinity
                        var corners: UIRectCorner = .allCorners
                        
                        func path(in rect: CGRect) -> Path {
                            let path = UIBezierPath(
                                roundedRect: rect,
                                byRoundingCorners: corners,
                                cornerRadii: CGSize(width: radius, height: radius)
                            )
                            return Path(path.cgPath)
                        }
                    }

                    @MainActor
                    private struct HeightPreferenceKey: @preconcurrency PreferenceKey {
                        static var defaultValue: CGFloat = 0
                        static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
                            value = nextValue()
                        }
                    }

                    
                    class IOSAtlasNavigationService: NSObject, @preconcurrency AtlasNavigationService {
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
            guard let nav = UIApplication.globalRootNav else { return }

            guard let rootVC = nav.viewControllers.first as? LifecycleAwareHosting else { 
            print("CANNOT FIND ROOTVC \(nav.viewControllers.first as? LifecycleAwareHosting)")
            return }

            let className = try? SwiftClassGenerator.companion.getQualifiedNameOfInstance(instance: rootVC.viewModel)
            
            print("VM CLASS \(className)")
            
            let newRootVM = AtlasDI.companion.resolveServiceNullableByName(clazz: className!) as? ViewModel
            print("VM DATA FOUND \(newRootVM)")
            
            guard let rootViewController = NavigationEngine.shared.createViewController(viewModelType: className!, viewModel: newRootVM!)
            else {
            print("CANNOT FIND CLASS NAME (SOMETHING BROKE)")
            return }

            nav.setViewControllers([rootViewController], animated: false)
            NavigationEngine.shared.stack.removeAll()
            NavigationEngine.shared.stack.append(newRootVM!)

            NavigationEngine.shared.routeWithParams(viewModelType: viewModelClass.simpleName!, params: params)
        }
    }
    
    @MainActor
    func navigateToPagePushAndReplaceCurrentScreen(viewModelClass: any KotlinKClass, params: Any?) {
        DispatchQueue.main.async {
            guard let nav = UIApplication.globalRootNav else { return }

            if !NavigationEngine.shared.stack.isEmpty {
                NavigationEngine.shared.stack.removeLast()
            }

            if nav.viewControllers.count > 1 {
                nav.popViewController(animated: false)
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

data class TabEntrySwift(
    val viewModel: String,
    val screen: String,
    val position: Int,
    val holder: String,
    val initialSelected: Boolean
)

fun String.normalizeToAscii(): String =
    this.map { if (it.code in 32..126) it else ' ' }.joinToString("")