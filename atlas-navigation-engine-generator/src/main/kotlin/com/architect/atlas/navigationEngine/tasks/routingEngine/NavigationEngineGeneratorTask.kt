package com.architect.atlas.navigationEngine.tasks.routingEngine

import com.architect.atlas.navigationEngine.helpers.isUnderAny
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class NavigationEngineGeneratorTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputAndroidDir: DirectoryProperty

    @get:Optional
    @get:OutputDirectory
    abstract val wearOSDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputAndroidTabsDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var outputFiles: List<File>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val wearOSSourceFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val androidSourceFiles: ConfigurableFileCollection

    @get:Input
    abstract var iOSOutputFiles: List<File>

    @get:Input
    abstract var projectCoreName: String

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    @get:Input
    abstract var isIOSTarget: Boolean

    init {
        group = "AtlasNavigation"
        description = "Generates the platform-specific navigation engine implementations."

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
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
            val droidSourceFiles = androidSourceFiles.files.toList()
            val droidAnts = if (droidSourceFiles.isNotEmpty()) {
                ants.filter { (_, _, filePath, _) -> File(filePath).isUnderAny(droidSourceFiles) }
            } else {
                logger.warn("⚠️ No wearSourceRoots provided; Wear build will include ALL screens. Set 'wearSourceRoots' for correct filtering.")
                emptyList()
            }

            val wearViewModelToScreen = droidAnts.map { it.first to it.second }

            // requires filtering by android type
            generateAndroidNavigation(wearViewModelToScreen)
            generateAndroidNavGraph(droidAnts)
            generateTabNavigationServices(scanTabAnnotations())

            val wearOSOut = wearOSDir.orNull?.asFile
            if (wearOSOut != null) {
                // Build a wear-only view of the parsed annotations
                val sourceFiles = wearOSSourceFiles.files.toList()
                val wearAnts = if (sourceFiles.isNotEmpty()) {
                    ants.filter { (_, _, filePath, _) -> File(filePath).isUnderAny(sourceFiles) }
                } else {
                    logger.warn("⚠️ No wearSourceRoots provided; Wear build will include ALL screens. Set 'wearSourceRoots' for correct filtering.")
                    emptyList()
                }

                val wearViewModelToScreen = wearAnts.map { it.first to it.second }

                generateAndroidNavigation(wearViewModelToScreen, true)
                generateAndroidNavGraph(wearAnts, true)
            }
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



    private fun generateAndroidNavigation(
        screens: List<Pair<String, String>>,
        isWearOS: Boolean = false
    ) {
        val viewModelImports =
            screens.mapNotNull { (viewModel, _) -> findViewModelImport(viewModel) }.distinct()

        val androidImpl = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()
            viewModelImports.forEach { appendLine("import $it") }

            appendLine(
                """
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.container.dsl.AtlasDI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
            """.trimIndent()
            )
            appendLine()
            appendLine("""class ObservableStack<T> : MutableList<T> by mutableListOf()""")
            appendLine()
            appendLine("object AtlasNavigation : AtlasNavigationService {")
            appendLine(
                """
    // --- State & mirrors ---
    private var navController: NavHostController? = null
    private var hostActivity: ComponentActivity? = null

    // Live mirrors for the current NavController instance
    private val routeStack = mutableListOf<String>() // canonical routes: "Base?pushParam={pushParam}"
    private val navigationStack = ObservableStack<KClass<out ViewModel>>() // optional VM mirror

    // Shadow of the last known stack captured at teardown (Activity/graph destroy)
    private var lastTeardownRoutes: List<String>? = null

    // VM -> base route (without args placeholder)
    private val viewModelToRouteMap: Map<KClass<out ViewModel>, String> = mapOf(
            """.trimIndent()
            )
            for ((viewModel, screenName) in screens) {
                appendLine("        $viewModel::class to \"$screenName\",")
            }
            appendLine("    )")
            appendLine()
            appendLine(
                """
    private fun canonicalRoute(base: String): String = "${'$'}base?pushParam={pushParam}"
    private val routeToViewModelMap: Map<String, KClass<out ViewModel>> =
        viewModelToRouteMap.map { (vm, base) -> canonicalRoute(base) to vm }.toMap()

    // single-flight for nav ops
    private val navScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val navMutex = Mutex()

    @Volatile private var replacingRoot = false
    @Volatile private var replacingCurrent = false

    // lifecycle observers (bookkeeping only; NO resets inside per-entry)
    private val entryObservers = mutableMapOf<String, DefaultLifecycleObserver>()
    private var graphObserver: DefaultLifecycleObserver? = null
    private var hostActivityCallbacks: Application.ActivityLifecycleCallbacks? = null
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // --- Readiness gating for early navigation ops ---
    @Volatile private var ready = CompletableDeferred<Unit>()
    private val pendingOps = ArrayDeque<(NavHostController) -> Unit>()

    private suspend fun flushPendingOps(nav: NavHostController) {
        while (true) {
            val op = synchronized(pendingOps) { if (pendingOps.isEmpty()) null else pendingOps.removeFirst() } ?: break
            val entry = nav.currentBackStackEntry
                ?: runCatching { nav.getBackStackEntry(nav.graph.id) }.getOrNull()

            if (entry != null) {
                entry.lifecycle.whenStateAtLeast(Lifecycle.State.RESUMED) {
                    navMutex.withLock { op(nav) }
                }
            } else {
                // No entry yet; requeue and bail (will retry on next destination change)
                synchronized(pendingOps) { pendingOps.addFirst(op) }
                break
            }
        }
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // --- Helpers ---
    private fun detachEntryObserver(nav: NavHostController?, routePattern: String) {
        val obs = entryObservers.remove(routePattern) ?: return
        runCatching { nav?.getBackStackEntry(routePattern)?.lifecycle?.removeObserver(obs) }
    }

    private fun detachAllEntryObservers(nav: NavHostController?) {
        entryObservers.keys.toList().forEach { detachEntryObserver(nav, it) }
        entryObservers.clear()
    }

    private fun replaceRootMirror(newRoutePattern: String, newVm: KClass<out ViewModel>) {
        // Logical replace-all: treat dropped screens as popped -> reset
        routeStack.forEach { r -> routeToViewModelMap[r]?.let(AtlasDI::resetViewModel) }
        routeStack.clear()
        routeStack += newRoutePattern
        navigationStack.clear()
        navigationStack += newVm
    }

    private fun replaceTopMirror(newRoutePattern: String, newVm: KClass<out ViewModel>) {
        // Logical replace-top: treat old top as popped -> reset
        routeStack.lastOrNull()?.let { old -> routeToViewModelMap[old]?.let(AtlasDI::resetViewModel) }
        if (routeStack.isNotEmpty()) routeStack.removeLast()
        routeStack += newRoutePattern
        if (navigationStack.isNotEmpty()) navigationStack.removeAt(navigationStack.lastIndex)
        navigationStack += newVm
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // --- BIND / REBIND (public APIs only) ---
    fun bindToNavController(controller: NavHostController) = bindToNavController(controller, null)

    fun bindToNavController(controller: NavHostController, activity: ComponentActivity?) {
        if (navController === controller) return

        // Cleanup previous controller bindings
        teardownActivityCallbacks()
        val oldNav = navController
        navController = controller
        hostActivity = activity

        if (oldNav != null) {
            runCatching {
                val graphOwner = oldNav.getBackStackEntry(oldNav.graph.id)
                graphObserver?.let { ob -> graphOwner.lifecycle.removeObserver(ob) }
            }
            if (routeStack.isNotEmpty()) {
                lastTeardownRoutes = routeStack.toList()
            }
            detachAllEntryObservers(oldNav)
            routeStack.clear()
            navigationStack.clear()
        }

        // Reset readiness for this binding (and release any stale waiters)
        if (!ready.isCompleted) ready.complete(Unit)
        ready = CompletableDeferred()

        // Attach lifecycle guards before mirrors
        attachNavLifecycleGuards(controller)
        attachActivityDestroyGuard(activity)

        // Re-sync mirrors from scratch; listener will update thereafter
        routeStack.clear()
        navigationStack.clear()

        // If a destination is already attached, mark ready immediately
        controller.currentBackStackEntry?.let {
            if (!ready.isCompleted) ready.complete(Unit)
        }

        controller.addOnDestinationChangedListener { _, destination, _ ->
            val newRoute = destination.route ?: return@addOnDestinationChangedListener

            // First observed destination after bind -> mark ready & flush queue
            if (!ready.isCompleted) {
                ready.complete(Unit)
                navScope.launch { flushPendingOps(controller) }
            }

            // --- RECOVERY: treat first change after teardown as a POP if newRoute existed before ---
            if (!routeStack.contains(newRoute) && lastTeardownRoutes?.contains(newRoute) == true) {
                val snapshot = lastTeardownRoutes!!
                var i = snapshot.lastIndex
                while (i >= 0 && snapshot[i] != newRoute) {
                    val removedRoute = snapshot[i]
                    routeToViewModelMap[removedRoute]?.let { AtlasDI.resetViewModel(it) }
                    i--
                }
                routeStack.clear()
                navigationStack.clear()
                for (j in 0..i) {
                    val r = snapshot[j]
                    routeStack.add(r)
                    routeToViewModelMap[r]?.let { vm ->
                        navigationStack.add(vm)
                        attachEntryObserver(controller, r, vm)
                    }
                }
                lastTeardownRoutes = null
                replacingRoot = false
                replacingCurrent = false
                return@addOnDestinationChangedListener
            }

            if (lastTeardownRoutes != null && routeStack.isEmpty() && !lastTeardownRoutes!!.contains(newRoute)) {
                lastTeardownRoutes = null
            }

            // --- Normal flow (no recovery needed) ---
            if (routeStack.isEmpty()) {
                routeStack.add(newRoute)
                routeToViewModelMap[newRoute]?.let { vm ->
                    navigationStack.add(vm)
                    attachEntryObserver(controller, newRoute, vm)
                }
                return@addOnDestinationChangedListener
            }

            if (routeStack.lastOrNull() == newRoute) {
                replacingRoot = false
                replacingCurrent = false
                return@addOnDestinationChangedListener
            }

            val top = routeStack.last()
            if (newRoute == top) return@addOnDestinationChangedListener

            if (routeStack.contains(newRoute)) {
                // --- POP: remove everything above newRoute; RESET each removed VM ---
                while (routeStack.isNotEmpty() && routeStack.last() != newRoute) {
                    val removedRoute = routeStack.removeLast()
                    routeToViewModelMap[removedRoute]?.let { vm ->
                        if (navigationStack.isNotEmpty() && navigationStack.last() == vm) {
                            navigationStack.removeAt(navigationStack.lastIndex)
                        } else {
                            navigationStack.remove(vm)
                        }
                        detachEntryObserver(controller, removedRoute)
                        AtlasDI.resetViewModel(vm) // <-- reset on pop
                    }
                }
            } else {
                // --- FORWARD ---
                routeStack.add(newRoute)
                routeToViewModelMap[newRoute]?.let { vm ->
                    navigationStack.add(vm)
                    attachEntryObserver(controller, newRoute, vm)
                }
            }
        }
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // --- NAV CONTROLLER LIFECYCLE GUARDS (no resets here) ---
    private fun attachNavLifecycleGuards(controller: NavHostController) {
        val graphOwner = controller.getBackStackEntry(controller.graph.id)
        graphObserver?.let { graphOwner.lifecycle.removeObserver(it) }
        graphObserver = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                // Snapshot stack then clear mirrors/observers
                if (routeStack.isNotEmpty()) {
                    lastTeardownRoutes = routeStack.toList()
                }
                detachAllEntryObservers(controller)
                routeStack.clear()
                navigationStack.clear()
                // Prepare latch for next bind
                if (!ready.isCompleted) ready.complete(Unit)
                ready = CompletableDeferred()
            }
        }.also { graphOwner.lifecycle.addObserver(it) }

        // Best-effort: observe any pre-existing entries (public API probe)
        for (route in routeToViewModelMap.keys) {
            val vm = routeToViewModelMap[route] ?: continue
            val entry = runCatching { controller.getBackStackEntry(route) }.getOrNull()
            if (entry != null) attachEntryObserver(controller, route, vm)
        }

        controller.currentBackStackEntry?.destination?.route?.let { r ->
            routeToViewModelMap[r]?.let { attachEntryObserver(controller, r, it) }
        }
        controller.previousBackStackEntry?.destination?.route?.let { r ->
            routeToViewModelMap[r]?.let { attachEntryObserver(controller, r, it) }
        }
    }

    private fun attachEntryObserver(
        nav: NavHostController,
        routePattern: String,
        vmKlass: KClass<out ViewModel>
    ) {
        if (entryObservers.containsKey(routePattern)) return
        val entry = runCatching { nav.getBackStackEntry(routePattern) }.getOrNull() ?: return
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                // Do NOT reset here; teardown will snapshot & next POP will handle resets.
                detachEntryObserver(nav, routePattern)
            }
        }
        entry.lifecycle.addObserver(observer)
        entryObservers[routePattern] = observer
    }

    // --- Activity-level guard (no resets; just snapshot + clear mirrors) ---
    private fun attachActivityDestroyGuard(activity: ComponentActivity?) {
        val app = activity?.application ?: return
        val callbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityDestroyed(a: Activity) {
                if (a !== activity) return
                if (routeStack.isNotEmpty()) {
                    lastTeardownRoutes = routeStack.toList()
                }
                detachAllEntryObservers(navController)
                routeStack.clear()
                navigationStack.clear()
                // Prepare latch for next bind
                if (!ready.isCompleted) ready.complete(Unit)
                ready = CompletableDeferred()
            }
            override fun onActivityCreated(a: Activity, s: Bundle?) {}
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityResumed(a: Activity) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, outState: Bundle) {}
        }
        app.registerActivityLifecycleCallbacks(callbacks)
        hostActivityCallbacks = callbacks
    }

    private fun teardownActivityCallbacks() {
        val callbacks = hostActivityCallbacks ?: return
        hostActivity?.application?.unregisterActivityLifecycleCallbacks(callbacks)
        hostActivityCallbacks = null
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // -------- Safe nav helpers --------
    private fun safeNavOp(op: (NavHostController) -> Unit) {
        val nav = navController
        if (nav == null) {
            // Not bound yet — enqueue
            synchronized(pendingOps) { pendingOps.add(op) }
            return
        }

        navScope.launch {
            // Wait until first destination is attached (or immediate if already)
            ready.await()

            val entry = nav.currentBackStackEntry
                ?: runCatching { nav.getBackStackEntry(nav.graph.id) }.getOrNull()

            if (entry == null) {
                // Still no entry: enqueue and exit; it will run on next destination change
                synchronized(pendingOps) { pendingOps.add(op) }
                return@launch
            }

            entry.lifecycle.whenStateAtLeast(Lifecycle.State.RESUMED) {
                withContext(Dispatchers.Main.immediate) {
                    navMutex.withLock { op(nav) }
                }
            }
        }
    }

    private fun safeNavigate(
        route: String,
        builder: NavOptionsBuilder.() -> Unit = {}
    ) {
        safeNavOp { nav -> nav.navigate(route, builder) }
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // ---- API: pushes ----
    override fun <T : ViewModel> navigateToPage(viewModelClass: KClass<T>, params: Any?) {
        navigateWithRoute(viewModelClass, params)
    }

    override fun <T : ViewModel> navigateToPagePushAndReplace(viewModelClass: KClass<T>, params: Any?) {
        navigateWithRoute(viewModelClass, params, popAll = true)
    }

    override fun <T : ViewModel> navigateToPagePushAndReplaceCurrentScreen(viewModelClass: KClass<T>, params: Any?) {
        navigateWithRoute(viewModelClass, params, popCurrent = true)
    }

    override fun <T : ViewModel> navigateToPageModal(viewModelClass: KClass<T>, params: Any?) =
        navigateToPage(viewModelClass, params)

    override fun <T : ViewModel> setNavigationStack(stack: List<T>, params: Any?) {}
    override fun <T : ViewModel> getNavigationStack(): List<T> = emptyList()

    private fun <T : ViewModel> navigateWithRoute(
        viewModelClass: KClass<T>, params: Any?, popAll: Boolean = false, popCurrent: Boolean = false
    ) {
        val routeBase = viewModelToRouteMap[viewModelClass] ?: error("No screen registered for ${'$'}viewModelClass")
        val canonicalPattern = canonicalRoute(routeBase)
        val fullRoute = "${'$'}routeBase?pushParam=${'$'}{encodeParam(params)}"

        when {
            popAll -> { replacingRoot = true;  replaceRootMirror(canonicalPattern, viewModelClass) }
            popCurrent -> { replacingCurrent = true; replaceTopMirror(canonicalPattern, viewModelClass) }
            else -> { /* forward push: listener will append */ }
        }

        safeNavigate(fullRoute) {
            if (popAll) {
                popUpTo(0)
            } else if (popCurrent) {
                val curr = navController?.currentDestination?.route
                if (curr != null) popUpTo(curr) { inclusive = true }
            }
            launchSingleTop = true
            restoreState = false
        }
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // ---- POPS ----
    override fun popToRoot(animate: Boolean, params: Any?) {
        deliverPopParamsToPrevious(params)
        safeNavOp { nav ->
            val targetRoute = routeStack.firstOrNull() ?: return@safeNavOp
            if (nav.currentBackStackEntry?.destination?.route == targetRoute) return@safeNavOp
            nav.popBackStack(route = targetRoute, inclusive = false)
        }
    }

    override fun popPage(animate: Boolean, params: Any?) {
        deliverPopParamsToPrevious(params)
        safeNavOp { it.popBackStack() }
    }

    override fun popPagesWithCount(countOfPages: Int, animate: Boolean, params: Any?) {
        safeNavOp { nav ->
            val targetIdx = (routeStack.size - 1 - countOfPages).coerceAtLeast(0)
            val target = routeStack.getOrNull(targetIdx)
            if (target != null) {
                deliverPopParamsToPrevious(params)
                nav.popBackStack(route = target, inclusive = false)
            } else {
                repeat(countOfPages) {
                    deliverPopParamsToPrevious(params)
                    if (!nav.popBackStack()) return@safeNavOp
                }
            }
        }
    }

    override fun popToPage(route: String, params: Any?) {
        deliverPopParamsToPrevious(params)
        val target = canonicalRoute(route)
        safeNavOp { nav -> nav.popBackStack(route = target, inclusive = false) }
    }

    override fun dismissModal(animate: Boolean, params: Any?) = popPage(animate, params)
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // ---- Pop params delivery (uses previous entry owner) ----
    private fun deliverPopParamsToPrevious(params: Any?) {
        val nav = navController ?: return
        val prevEntry = nav.previousBackStackEntry ?: return
        val prevRoute = prevEntry.destination.route ?: return
        val prevVmClass = routeToViewModelMap[prevRoute] ?: return

        val encoded = encodeParam(params) ?: return
        decodeParam(encoded)?.let { decoded ->
            val vm = resolveViewModel(prevVmClass, owner = prevEntry)
            if (vm is Poppable<*>) {
                @Suppress("UNCHECKED_CAST")
                (vm as Poppable<Any>).onPopParams(decoded)
            }
        }
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // ---- VM resolution ----
    private fun resolveViewModel(
        vmClass: KClass<out com.architect.atlas.architecture.mvvm.ViewModel>,
        owner: ViewModelStoreOwner? = null
    ): com.architect.atlas.architecture.mvvm.ViewModel {
        val nav = requireNotNull(navController) { "NavController not bound. Call AtlasNavigation.bindToNavController(navController) first." }
        val vmOwner: ViewModelStoreOwner =
            owner ?: nav.currentBackStackEntry
            ?: error("No current back stack entry available for ViewModel resolution")

        @Suppress("UNCHECKED_CAST")
        val androidVmClass = vmClass.java as Class<androidx.lifecycle.ViewModel>
        val vm = ViewModelProvider(vmOwner)[androidVmClass]
        @Suppress("UNCHECKED_CAST")
        return vm as com.architect.atlas.architecture.mvvm.ViewModel
    }
            """.trimIndent()
            )
            appendLine()
            appendLine(
                """
    // ---- Encoding helpers ----
    private fun encodeParam(param: Any?): String? =
        param?.let { if (it is String || it is Number || it is Boolean) it.toString() else Json.encodeToString(it) }

    private fun decodeParam(encoded: String): Any? =
        encoded.toIntOrNull() ?: encoded.toDoubleOrNull()
        ?: (if (encoded.equals("true", true) || encoded.equals("false", true)) encoded.toBoolean() else runCatching { Json.decodeFromString<Any>(encoded) }.getOrNull() ?: encoded)

    private fun postToMain(block: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post(block)
    }

    suspend fun awaitNavigationReady() = ready.await()
    fun isNavigationReady(): Boolean = ready.isCompleted
}
            """.trimIndent()
            )
        }

        if (!isWearOS) {
            val androidOut = outputAndroidDir.get().asFile
            androidOut.mkdirs()
            File(androidOut, "AtlasNavigation.kt").writeText(androidImpl)
        } else {
            val wearOSOut = wearOSDir.orNull?.asFile
            if (wearOSOut != null) {
                wearOSOut.mkdirs()
                File(wearOSOut, "AtlasNavigation.kt").writeText(androidImpl)
            }
        }
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

    private fun generateAndroidNavGraph(
        screens: List<Quad<String, String, String, Boolean>>,
        isWearOS: Boolean = false
    ) {
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

            val start = screens.firstOrNull { it.fourth }?.second ?: "MissingStart"

            appendLine(
                """
import com.architect.atlas.navigation.AtlasNavigation
import com.architect.atlas.architecture.navigation.Pushable
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.AtlasContainer

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.serialization.json.Json
import androidx.compose.runtime.compositionLocalOf
import java.lang.ref.WeakReference
            """.trimIndent()
            )
            appendLine()

            appendLine(
                """
val LocalAtlasNavController = compositionLocalOf<NavHostController> {
    error("NavController not provided")
}

object AtlasNavHolder {
    private var navControllerRef: WeakReference<NavHostController>? = null
    fun bind(navController: NavHostController) { navControllerRef = WeakReference(navController) }
    fun get(): NavHostController? = navControllerRef?.get()
}
                    
@Composable
fun AtlasNavGraph() {
    val navController = rememberNavController()
    AtlasNavHolder.bind(navController)

    // Bind immediately on enter; unbind on dispose.
    DisposableEffect(Unit) {
        AtlasNavigation.bindToNavController(navController)
        onDispose { AtlasNavigation.bindToNavController(controller = navController, activity = null) /* no-op but keeps symmetry */ }
    }

    CompositionLocalProvider(LocalAtlasNavController provides navController) {
        NavHost(navController = navController, startDestination = "$start") {
${
                    screens.joinToString("\n") { (viewModel, screen) ->
                        """            screen<$viewModel>("$screen") { $screen(it) }"""
                    }
                }
        }
    }
}
""".trimIndent()
            )

            appendLine(
                """
@OptIn(ExperimentalAnimationApi::class)
inline fun <reified VM : ViewModel> NavGraphBuilder.screen(
    route: String,
    noinline content: @Composable (VM) -> Unit
) {
    composable(
        route = "${'$'}route?pushParam={pushParam}",
        arguments = listOf(navArgument("pushParam") { nullable = true; defaultValue = null }),
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250))
        },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250))
        },
    ) { backStackEntry ->
        // Wear-safe: DO NOT cast context to ComponentActivity.
        // Scope VM to this backStackEntry so each destination has its own instance.
        val vm = remember(backStackEntry) { AtlasContainer.resolveViewModel(VM::class) }!!

        backStackEntry.arguments?.getString("pushParam")?.let { raw ->
            decodeParam(raw)?.let { param ->
                if (vm is Pushable<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (vm as Pushable<Any>).onPushParams(param)
                }
            }
        }

        HandleLifecycle<VM>(vm) { content(vm) }
    }
}
""".trimIndent()
            )

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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    content()
}
""".trimIndent()
            )
        }

        if (!isWearOS) {
            val file = File(outputAndroidDir.get().asFile, "AtlasNavGraph.kt")
            file.parentFile.mkdirs()
            file.writeText(navGraph)
        } else {
            val wearOSOut = wearOSDir.orNull?.asFile
            if (wearOSOut != null) {
                val wearFile = File(wearOSDir.get().asFile, "AtlasNavGraph.kt")
                wearFile.parentFile.mkdirs()
                wearFile.writeText(navGraph)
            }
        }
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
                let vmName = SwiftClassGenerator.companion.getClazz(type: $viewModelName.self)
                let vm = AtlasDI.companion.resolveServiceNullableByName(
                    clazz: vmName
                ) as! $viewModelName
                return LifecycleAwareHostingController(rootView: $widgetName(vm: vm), viewModel: vm, viewModelName: vmName)
            }"""
        }

        val className = "${holder.removeSuffix("ViewModel")}TabsNavigation"

        val classCode = """
            extension AnyTransition {
                static var slideFromRight: AnyTransition {
                    .asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .leading))
                }

                static var slideFromLeft: AnyTransition {
                    .asymmetric(insertion: .move(edge: .leading), removal: .move(edge: .trailing))
                }
            }
            
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
    func buildTabScreen<T: ViewModel, Content: View>(
        _ type: T.Type,
        tag: Int,
        screenBuilder: @escaping (T) -> Content
    ) -> some View {
        let vm = AtlasDI.companion.resolveServiceNullableByName(
            clazz: SwiftClassGenerator.companion.getClazz(type: type)
        ) as! T

        LifecycleTabAwareHostingView(viewModel: vm) {
            screenBuilder(${'$'}0)
        }
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
    class $className: NSObject, ObservableObject, @preconcurrency AtlasTabNavigationService {
        static func shared() -> $className{
            AtlasDI.companion.resolveServiceNullableByName(clazz: SwiftClassGenerator.companion.getClazz(type: AtlasTabNavigationService.self)) as! $className
        }

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
                appendLine("            let vmName = SwiftClassGenerator.companion.getClazz(type: $vm.self)")
                appendLine("            let view = $screenName(vm: resolved)")
                appendLine("            let controller = LifecycleAwareHostingController(rootView: view, viewModel: resolved, viewModelName: vmName)")
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
            appendLine("        if stack.count > 1 { stack.removeSubrange(1..<stack.count) }")
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
                appendLine("        return LifecycleAwareHostingController(rootView: view, viewModel: vm, viewModelName: \"$vmSimpleName\")")
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
                    let vmName = SwiftClassGenerator.companion.getClazz(type: $initialViewModel.self)
                    let resolved = AtlasDI.companion.resolveServiceNullableByName(
                            clazz: vmName
                        ) as! $initialViewModel
                    let root = $initialScreen(
                        vm: resolved
                    )
                    
                    let hostingController = LifecycleAwareHostingController(rootView: root, viewModel: resolved, viewModelName: vmName)
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
               let viewModelName: String
               init(rootView: Content, viewModel: ViewModel, viewModelName: String) {
                   self.viewModel = viewModel
                   self.viewModelName = viewModelName
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
                           
                           AtlasDI.companion.resetViewModelByName(clazz: viewModelName)
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
            appendLine(
                """
                import SwiftUI
                    import Combine
            """.trimIndent()
            )
            appendLine()

            appendLine(
                """    
                    import Foundation
                    import SwiftUI
                    import UIKit
                    import Combine

                    extension UIApplication {
                        static var topViewController: UIViewController? {
                            guard let keyWindow = shared.connectedScenes
                                .compactMap({ ${'$'}0 as? UIWindowScene })
                                .flatMap({ ${'$'}0.windows })
                                .first(where: { ${'$'}0.isKeyWindow }) else {
                                    return nil
                            }
                            return topViewController(base: keyWindow.rootViewController)
                        }

                        private static func topViewController(base: UIViewController?) -> UIViewController? {
                            if let nav = base as? UINavigationController {
                                return topViewController(base: nav.visibleViewController)
                            }
                            if let tab = base as? UITabBarController {
                                return topViewController(base: tab.selectedViewController)
                            }
                            if let presented = base?.presentedViewController {
                                return topViewController(base: presented)
                            }
                            return base
                        }
                    }
                    
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
    ZStack {
        if manager.isPresented && manager.activeSheetID == sheetID {
            Color.black.opacity(0.3)
                .ignoresSafeArea()
                .onTapGesture {
                    withAnimation {
                    }
                }

            alertView()
                .allowsHitTesting(true)
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
                                        if manager.isPresented && manager.activeSheetID == sheetID {
                                            Color.black.opacity(0.3)
                                                .ignoresSafeArea()
                                                .onTapGesture {
                                                    withAnimation {
                                                        manager.dismiss()
                                                    }
                                                }
                                        }
                                    }
                                )
                                .overlay(
                                    Group {
                                            
                                            bottomSheetView()
                                    },
                                    alignment: .bottom
                                )
                        }
                        @ViewBuilder
                        private func bottomSheetView() -> some View {       
                                Group {
            if manager.isPresented && manager.activeSheetID == sheetID {
                contentBuilder(manager.params)
            } else {
                EmptyView() 
            }
        }
                                    .frame(maxWidth: .infinity, maxHeight: contentHeight + manager.heightOffset)
                                    .background(Color.white)
                                    .clipShape(RoundedCorner(radius: 26, corners: [.topLeft, .topRight]))
                                    .shadow(radius: 10)
                                    .offset(y: manager.isPresented && manager.activeSheetID == sheetID ? 0 : UIScreen.main.bounds.height)
                                    .animation(.interactiveSpring(response: 0.4, dampingFraction: 0.85, blendDuration: 0.25),
                                               value: manager.isPresented
                                    ).ignoresSafeArea()
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
    func setNavigationStack(stack: [ViewModel], params: Any?) {
        DispatchQueue.main.async {
              NavigationEngine.shared.setNavigationStack(stack: stack.map { "\(${'$'}0)" }, params: params)
        }
    }
    
    func getNavigationStack() -> [ViewModel] {
        return []
    }
    
    func navigateToPage(viewModelClass: any KotlinKClass, params: Any?) {
            DispatchQueue.main.async {
                NavigationEngine.shared.routeWithParams(viewModelType: viewModelClass.simpleName!, params: params)
            }
    }
    
    func navigateToPagePushAndReplace(viewModelClass: any KotlinKClass, params: Any?) {
    DispatchQueue.main.async {
        guard let nav = UIApplication.globalRootNav else { return }

        // Resolve class name and view model instance
        guard let className = viewModelClass.qualifiedName,
              let newViewModel = AtlasDI.companion.resolveServiceNullableByName(clazz: className) as? ViewModel,
              let controller = NavigationEngine.shared.createViewController(viewModelType: className, viewModel: newViewModel)
        else {
            return
        }

        // Set new stack with just the new controller
        nav.setViewControllers([controller], animated: true)

        // Clear and set the navigation stack in your engine
        NavigationEngine.shared.stack.removeAll()
        NavigationEngine.shared.stack.append(newViewModel)

        // Optionally handle push params directly
        if let pushable = newViewModel as? Pushable, let p = params {
            pushable.onPushParams(params: p)
        }
    }
}
    
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
    
    func navigateToPageModal(viewModelClass: any KotlinKClass, params: Any?) {
            DispatchQueue.main.async {
                NavigationEngine.shared.routeWithParams(viewModelType: viewModelClass.simpleName!, params: params, isModal: true)
            }
    }
    
    func popPagesWithCount(countOfPages: Int32, animate: Bool, params: Any?) {
        DispatchQueue.main.async {
             NavigationEngine.shared.popPagesWithCount(count: Int(countOfPages), animate: animate, params: params)
        }
    }

    func popToRoot(animate: Bool = true, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popToRoot(animate: animate, params: params)
        }
    }

    func popPage(animate: Bool = true, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popPage(animate: animate, params: params)
        }
    }

    func popPagesWithCount(count: Int, animate: Bool = true, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popPagesWithCount(count: count, animate: animate, params: params)
        }
    }

    func popToPage(route: String, params: Any? = nil) {
        DispatchQueue.main.async {
            NavigationEngine.shared.popToPage(route: route, params: params)
        }
    }

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