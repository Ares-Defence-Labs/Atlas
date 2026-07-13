package com.architect.atlas.navigationEngine.tasks.routingEngine.android

import com.architect.atlas.navigationEngine.helpers.findFunctionImport
import com.architect.atlas.navigationEngine.helpers.findViewModelImport
import com.architect.atlas.navigationEngine.helpers.isUnderAny
import com.architect.atlas.navigationEngine.tasks.models.Quad
import com.architect.atlas.navigationEngine.tasks.models.TabEntry
import com.architect.atlas.navigationEngine.tasks.routingEngine.android.helpers.scanTabAnnotations
import com.architect.atlas.navigationEngine.tasks.routingEngine.android.helpers.scanViewModelAnnotationsCompose
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class ComposeNavigationEngineGeneratorTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputAndroidDir: DirectoryProperty

    @get:Optional
    @get:OutputDirectory
    abstract val wearOSDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputAndroidTabsDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val wearOSSourceFiles: ConfigurableFileCollection

    @get:Input
    abstract var outputFiles: List<File>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val androidSourceFiles: ConfigurableFileCollection

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    init {
        group = "AtlasNavigation"
        description =
            "Generates the platform-specific navigation engine implementations (Compose targets only)"

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
    }

    @TaskAction
    fun generateNavigatorClass() {
        logger.lifecycle("WRITING NAVIGATION TO ANDROID -- Compose")
        val ants = scanViewModelAnnotationsCompose(outputFiles, logger, androidSourceFiles)
        val droidSourceFiles = androidSourceFiles.files.toList()
        val droidAnts = if (droidSourceFiles.isNotEmpty()) {
            val filtered = ants.filter { (_, _, filePath, _) ->
                File(filePath).isUnderAny(droidSourceFiles)
            }.distinct()
            logger.lifecycle(
                "AtlasNav: droidSource roots = ${
                    droidSourceFiles.joinToString { it.path }
                }")
            logger.lifecycle("AtlasNav: droidAnts size    = ${filtered.size}")

            if (filtered.isEmpty() && ants.isNotEmpty()) {
                logger.warn(
                    "⚠️ AtlasNav: androidSourceFiles filter removed all screens. " +
                            "Falling back to unfiltered annotations for nav."
                )
                ants
            } else {
                filtered
            }
        } else {
            logger.warn(
                "⚠️ AtlasNav: No androidSourceFiles provided; using all annotations for nav. " +
                        "Configure 'androidSourceFiles' for more precise filtering."
            )
            ants
        }

        val wearViewModelToScreen = droidAnts.map { it.first to it.second }.distinct()

        logger.lifecycle(
            "AtlasNav: compose screens = " +
                    wearViewModelToScreen.joinToString { "${it.first} -> ${it.second}" }
        )

        generateAndroidNavigation(wearViewModelToScreen)
        generateAndroidNavGraph(droidAnts)
        generateTabNavigationServices(scanTabAnnotations(outputFiles, logger))

        val wearOSOut = wearOSDir.orNull?.asFile
        if (wearOSOut != null) {
            // Build a wear-only view of the parsed annotations
            val sourceFiles = wearOSSourceFiles.files.toList()
            val wearAnts = if (sourceFiles.isNotEmpty()) {
                ants.filter { (_, _, filePath, _) -> File(filePath).isUnderAny(sourceFiles) }
                    .distinct()
            } else {
                logger.warn("⚠️ No wearSourceRoots provided; Wear build will include ALL screens. Set 'wearSourceRoots' for correct filtering.")
                emptyList()
            }

            generateAndroidNavigation(wearAnts.map { it.first to it.second }, true)
            generateAndroidNavGraph(wearAnts, true)
        }
    }

    private fun generateAndroidNavigation(
        screens: List<Pair<String, String>>,
        isWearOS: Boolean = false
    ) {
        val viewModelImports =
            screens.mapNotNull { (viewModel, _) -> findViewModelImport(viewModel, outputFiles) }
                .distinct()

        val androidImpl = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()
            viewModelImports.forEach { appendLine("import $it") }
            appendLine(
                """
                    import androidx.compose.foundation.background
                    import androidx.compose.foundation.layout.Box
                    import androidx.compose.foundation.layout.fillMaxSize
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.draw.clipToBounds
                    import androidx.compose.material3.MaterialTheme
import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.architecture.navigation.Pushable
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

object AtlasNavigation : AtlasNavigationService {
    private const val PUSH_KEY = "atlas_push_param"
    private var navController: NavHostController? = null
    private var hostActivity: ComponentActivity? = null
    private val routeStack = mutableListOf<String>()
    private val navigationStack = ArrayDeque<KClass<out ViewModel>>()
    private var lastTeardownRoutes: List<String>? = null
    private val viewModelToRouteMap: Map<KClass<out ViewModel>, String> = mapOf(
            """.trimIndent()
            )
            for ((viewModel, screenName) in screens) {
                appendLine("        $viewModel::class to \"$screenName\",")
            }
            appendLine(
                """
    )
    private fun canonicalRoute(base: String): String = "${'$'}base?pushParam={pushParam}"
    private fun canonicalKey(route: String): String = canonicalRoute(route.substringBefore('?'))
    private val routeToViewModelMap: Map<String, KClass<out ViewModel>> = buildMap {
        for ((vm, base) in viewModelToRouteMap) {
            put(canonicalRoute(base), vm)
            put(base, vm)
        }
    }
    private val navScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val navMutex = Mutex()
    @Volatile private var replacingRoot = false
    @Volatile private var replacingCurrent = false
    private val entryObservers = mutableMapOf<String, DefaultLifecycleObserver>()
    private var graphObserver: DefaultLifecycleObserver? = null
    private var hostActivityCallbacks: Application.ActivityLifecycleCallbacks? = null
    @Volatile private var ready = CompletableDeferred<Unit>()
    private val pendingOps = ArrayDeque<(NavHostController) -> Unit>()
    private val pendingGlobalPush = mutableMapOf<String, String>()

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
                synchronized(pendingOps) { pendingOps.addFirst(op) }
                break
            }
        }
    }

    private fun detachEntryObserver(nav: NavHostController?, routePattern: String) {
        val obs = entryObservers.remove(routePattern) ?: return
        val base = routePattern.substringBefore('?')
        val entry = runCatching { nav?.getBackStackEntry(routePattern) }.getOrNull()
            ?: runCatching { nav?.getBackStackEntry(base) }.getOrNull()
        entry?.lifecycle?.removeObserver(obs)
    }

    private fun detachAllEntryObservers(nav: NavHostController?) {
        entryObservers.keys.toList().forEach { detachEntryObserver(nav, it) }
        entryObservers.clear()
    }

    private fun replaceRootMirror(newRoutePattern: String, newVm: KClass<out ViewModel>) {
        routeStack.forEach { r -> routeToViewModelMap[canonicalKey(r)]?.let(AtlasDI::resetViewModel) }
        routeStack.clear()
        routeStack += newRoutePattern
        navigationStack.clear()
        navigationStack.addLast(newVm)
    }

    private fun replaceTopMirror(newRoutePattern: String, newVm: KClass<out ViewModel>) {
        routeStack.lastOrNull()?.let { old -> routeToViewModelMap[canonicalKey(old)]?.let(AtlasDI::resetViewModel) }
        if (routeStack.isNotEmpty()) routeStack.removeAt(routeStack.lastIndex)
        routeStack += newRoutePattern
        if (navigationStack.isNotEmpty()) navigationStack.removeLast()
        navigationStack.addLast(newVm)
    }

    fun bindToNavController(controller: NavHostController) = bindToNavController(controller, null)

    fun bindToNavController(controller: NavHostController, activity: ComponentActivity?) {
        if (navController === controller) return
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
        if (!ready.isCompleted) ready.complete(Unit)
        ready = CompletableDeferred()
        attachNavLifecycleGuards(controller)
        attachActivityDestroyGuard(activity)
        routeStack.clear()
        navigationStack.clear()
        controller.currentBackStackEntry?.let { if (!ready.isCompleted) ready.complete(Unit) }
        controller.addOnDestinationChangedListener { _, destination, _ ->
            val newRoute = destination.route ?: return@addOnDestinationChangedListener
            if (!ready.isCompleted) {
                ready.complete(Unit)
                navScope.launch { flushPendingOps(controller) }
            }
            if (!routeStack.contains(newRoute) && lastTeardownRoutes?.contains(newRoute) == true) {
                val snapshot = lastTeardownRoutes!!
                var i = snapshot.lastIndex
                while (i >= 0 && snapshot[i] != newRoute) {
                    val removedRoute = snapshot[i]
                    routeToViewModelMap[canonicalKey(removedRoute)]?.let { AtlasDI.resetViewModel(it) }
                    i--
                }
                routeStack.clear()
                navigationStack.clear()
                for (j in 0..i) {
                    val r = snapshot[j]
                    routeStack.add(r)
                    routeToViewModelMap[canonicalKey(r)]?.let { vm ->
                        navigationStack.addLast(vm)
                        attachEntryObserver(controller, canonicalKey(r), vm)
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
            if (routeStack.isEmpty()) {
                routeStack.add(newRoute)
                routeToViewModelMap[canonicalKey(newRoute)]?.let { vm ->
                    navigationStack.addLast(vm)
                    attachEntryObserver(controller, canonicalKey(newRoute), vm)
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
                while (routeStack.isNotEmpty() && routeStack.last() != newRoute) {
                    val removedRoute = routeStack.removeAt(routeStack.lastIndex)
                    routeToViewModelMap[canonicalKey(removedRoute)]?.let { vm ->
                        if (navigationStack.isNotEmpty() && navigationStack.lastOrNull() == vm) {
                            navigationStack.removeLast()
                        } else {
                            navigationStack.remove(vm)
                        }
                        detachEntryObserver(controller, canonicalKey(removedRoute))
                        AtlasDI.resetViewModel(vm)
                    }
                }
            } else {
                routeStack.add(newRoute)
                routeToViewModelMap[canonicalKey(newRoute)]?.let { vm ->
                    navigationStack.addLast(vm)
                    attachEntryObserver(controller, canonicalKey(newRoute), vm)
                }
            }
        }
    }

    private fun attachNavLifecycleGuards(controller: NavHostController) {
        val graphOwner = controller.getBackStackEntry(controller.graph.id)
        graphObserver?.let { graphOwner.lifecycle.removeObserver(it) }
        graphObserver = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                if (routeStack.isNotEmpty()) {
                    lastTeardownRoutes = routeStack.toList()
                }
                detachAllEntryObservers(controller)
                routeStack.clear()
                navigationStack.clear()
                if (!ready.isCompleted) ready.complete(Unit)
                ready = CompletableDeferred()
            }
        }.also { graphOwner.lifecycle.addObserver(it) }
        for ((route, vm) in routeToViewModelMap) {
            attachEntryObserver(controller, canonicalKey(route), vm)
        }
        controller.currentBackStackEntry?.destination?.route?.let { r ->
            routeToViewModelMap[canonicalKey(r)]?.let { vm ->
                attachEntryObserver(controller, canonicalKey(r), vm)
            }
        }
        controller.previousBackStackEntry?.destination?.route?.let { r ->
            routeToViewModelMap[canonicalKey(r)]?.let { vm ->
                attachEntryObserver(controller, canonicalKey(r), vm)
            }
        }
    }

    private fun attachEntryObserver(
        nav: NavHostController,
        routePattern: String,
        vmKlass: KClass<out ViewModel>
    ) {
        if (entryObservers.containsKey(routePattern)) return
        val base = routePattern.substringBefore('?')
        val entry =
            runCatching { nav.getBackStackEntry(routePattern) }.getOrNull()
                ?: runCatching { nav.getBackStackEntry(base) }.getOrNull()
                ?: nav.currentBackStackEntry?.takeIf { it.destination.route?.substringBefore('?') == base }
                ?: nav.previousBackStackEntry?.takeIf { it.destination.route?.substringBefore('?') == base }
                ?: return
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                bootstrapOnceAtCreated(entry, vmKlass)
            }
            override fun onDestroy(owner: LifecycleOwner) {
                detachEntryObserver(nav, routePattern)
            }
        }
        if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            bootstrapOnceAtCreated(entry, vmKlass)
        }
        entry.lifecycle.addObserver(observer)
        entryObservers[routePattern] = observer
    }

    private fun bootstrapOnceAtCreated(
        entry: NavBackStackEntry,
        vmKlass: KClass<out ViewModel>
    ) {
        val handle = entry.savedStateHandle
        val didKey = "didBootstrap#" + vmKlass.qualifiedName
        val alreadyBooted = handle.get<Boolean>(didKey) == true
        val vm = resolveViewModel(vmKlass, entry)
        val fromGlobal = synchronized(pendingGlobalPush) { pendingGlobalPush.remove(vmKlass.qualifiedName) }
        val prevEntry = navController?.previousBackStackEntry
        val fromPrev = prevEntry?.savedStateHandle?.get<String>(PUSH_KEY)
        val fromArgs = entry.arguments?.getString("pushParam")
        val fromHandle = handle.get<String>("pushParam")
        val payload = fromGlobal ?: fromPrev ?: fromArgs ?: fromHandle
        if (payload != null && vm is Pushable<*>) {
            val decoded = decodeParam(payload) ?: payload
            @Suppress("UNCHECKED_CAST")
            (vm as Pushable<Any>).onPushParams(decoded)
        }
        if (fromPrev != null) prevEntry?.savedStateHandle?.set(PUSH_KEY, null)
        if (fromHandle != null) handle.remove<String>("pushParam")
        if (!alreadyBooted) {
            vm.bootstrapVmFromNavEngine()
            handle[didKey] = true
        }
    }

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

    private fun safeNavOp(op: (NavHostController) -> Unit) {
        val nav = navController
        if (nav == null) {
            synchronized(pendingOps) { pendingOps.addLast(op) }
            return
        }
        navScope.launch {
            ready.await()
            val entry = nav.currentBackStackEntry
                ?: runCatching { nav.getBackStackEntry(nav.graph.id) }.getOrNull()
            if (entry == null) {
                synchronized(pendingOps) { pendingOps.addLast(op) }
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
        viewModelClass: KClass<T>,
        params: Any?,
        popAll: Boolean = false,
        popCurrent: Boolean = false
    ) {
        val routeBase = viewModelToRouteMap[viewModelClass] ?: error("No screen registered for ${'$'}viewModelClass")
        val canonicalPattern = canonicalRoute(routeBase)
        val encodedParam: String? = params?.let { p ->
            when (p) {
                is String, is Number, is Boolean -> p.toString()
                else -> Json.encodeToString(p)
            }
        }
        when {
            popAll -> { replacingRoot = true; replaceRootMirror(canonicalPattern, viewModelClass) }
            popCurrent -> { replacingCurrent = true; replaceTopMirror(canonicalPattern, viewModelClass) }
        }
        safeNavOp { nav ->
            if (encodedParam != null) {
                synchronized(pendingGlobalPush) {
                    pendingGlobalPush[viewModelClass.qualifiedName ?: viewModelClass.toString()] = encodedParam
                }
                nav.currentBackStackEntry?.savedStateHandle?.set(PUSH_KEY, encodedParam)
            }
            nav.navigate(routeBase) {
                when {
                    popAll -> popUpTo(0)
                    popCurrent -> nav.currentDestination?.route?.let { popUpTo(it) { inclusive = true } }
                }
                launchSingleTop = true
                restoreState = false
            }
            attachEntryObserver(nav, canonicalPattern, viewModelClass)
        }
    }

    override fun popToRoot(animate: Boolean, params: Any?) {
        deliverPopParamsToPrevious(params)
        safeNavOp { nav ->
            while (nav.previousBackStackEntry != null) {
                nav.popBackStack()
            }
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
                nav.popBackStack(route = canonicalKey(target), inclusive = false)
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

    private fun deliverPopParamsToPrevious(params: Any?) {
        val nav = navController ?: return
        val prevEntry = nav.previousBackStackEntry ?: return
        val prevRoute = prevEntry.destination.route ?: return
        val prevVmClass = routeToViewModelMap[canonicalKey(prevRoute)] ?: return
        val encoded = encodeParam(params) ?: return
        decodeParam(encoded)?.let { decoded ->
            val vm = resolveViewModel(prevVmClass, owner = prevEntry)
            if (vm is Poppable<*>) {
                @Suppress("UNCHECKED_CAST")
                (vm as Poppable<Any>).onPopParams(decoded)
            }
        }
    }

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
                findViewModelImport(viewModelName, outputFiles)
            }

            functionImports.forEach { appendLine("import $it") }
            viewModelImports.forEach { appendLine("import $it") }

            val start = screens.firstOrNull { it.fourth }?.second ?: "MissingStart"

            appendLine(
                """
                    
                      import androidx.compose.foundation.background
                    import androidx.compose.foundation.layout.Box
                    import androidx.compose.foundation.layout.fillMaxSize
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.draw.clipToBounds
                    import androidx.compose.material3.MaterialTheme
                    
import com.architect.atlas.architecture.navigation.Pushable
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.navigation.AtlasNavigation
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.serialization.json.Json
import androidx.compose.runtime.compositionLocalOf
import java.lang.ref.WeakReference

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
    DisposableEffect(Unit) {
        AtlasNavigation.bindToNavController(navController)
        onDispose { AtlasNavigation.bindToNavController(controller = navController, activity = null) }
    }
    CompositionLocalProvider(LocalAtlasNavController provides navController) {
        NavHost(navController = navController, startDestination = "$start",
         modifier = Modifier
        .fillMaxSize()
        .clipToBounds()
        .background(MaterialTheme.colorScheme.background)) {
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
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(300)
    )
},
exitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(300)
    )
},
popEnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(300)
    )
},
popExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(300)
    )
}
    ) { backStackEntry ->
        val vm: VM = viewModel(
            modelClass = VM::class.java,
            viewModelStoreOwner = backStackEntry
        )
        HandleLifecycle<VM>(vm) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content(vm)
    }
}
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
            val wearFile = File(wearOSDir.get().asFile, "AtlasNavGraph.kt")
            wearFile.parentFile.mkdirs()
            wearFile.writeText(navGraph)
        }
    }

    // tab navigation

    private fun generateTabNavigationServices(tabsByHolder: Map<String, List<TabEntry>>) {
        tabsByHolder.forEach { (holder, tabs) ->
            val sortedTabs = tabs.sortedBy { it.position }
            val distinctTabs = sortedTabs.distinctBy { it.viewModel to it.screen }

            val outputFile = File(outputAndroidTabsDir.get().asFile, "${holder}TabsNavigation.kt")

            val code = buildString {
                appendLine("package com.architect.atlas.navigation")
                appendLine()

                val viewModelImports =
                    distinctTabs.mapNotNull { findViewModelImport(it.viewModel, outputFiles) }
                viewModelImports.distinct().forEach { appendLine("import $it") }

                appendLine("import com.architect.atlas.architecture.navigation.AtlasTabNavigationService")
                appendLine("import kotlin.reflect.KClass")
                appendLine("import com.architect.atlas.architecture.mvvm.ViewModel")
                appendLine("import kotlinx.serialization.encodeToString")
                appendLine("import kotlinx.serialization.json.Json")
                appendLine("import androidx.navigation.NavHostController")
                appendLine("import androidx.navigation.NavController")
                appendLine("import androidx.compose.runtime.compositionLocalOf")
                appendLine("import java.lang.ref.WeakReference")
                appendLine()

                appendLine("val TabLocalAtlasNavController = compositionLocalOf<NavHostController> {")
                appendLine("    error(\"NavController not provided\")")
                appendLine("}")
                appendLine()
                appendLine("object AtlasTabNavHolder {")
                appendLine("    private var navControllerRef: WeakReference<NavHostController>? = null")
                appendLine("    fun bind(navController: NavHostController) { navControllerRef = WeakReference(navController) }")
                appendLine("    fun get(): NavHostController? = navControllerRef?.get()")
                appendLine("}")
                appendLine()

                appendLine("object ${holder}TabsNavigation : AtlasTabNavigationService {")
                appendLine("    private var currentTab: KClass<out ViewModel>? = null")
                appendLine("    private val holderRoute = \"${holder}Root\"")
                appendLine("    private val tabs: Map<KClass<out ViewModel>, String> = listOf(")
                distinctTabs.forEach {
                    appendLine("        ${it.viewModel}::class to \"${it.screen}\",")
                }
                appendLine("    ).toMap()")
                appendLine()
                appendLine("    override fun <T : ViewModel> navigateToTabIndex(viewModelClass: KClass<T>, params: Any?) {")
                appendLine("        android.os.Handler(android.os.Looper.getMainLooper()).post {")
                appendLine("            val nav = AtlasTabNavHolder.get() ?: return@post")
                appendLine("            val baseRoute = tabs[viewModelClass] ?: error(\"Tab not found for \$viewModelClass\")")
                appendLine("            currentTab = viewModelClass")
                appendLine()
                appendLine("            val encoded: String? = params?.let { p ->")
                appendLine("                when (p) {")
                appendLine("                    is String, is Number, is Boolean -> p.toString()")
                appendLine("                    else -> Json.encodeToString(p)")
                appendLine("                }")
                appendLine("            }")
                appendLine()
                appendLine("            fun setParamOnTargetIfPossible() {")
                appendLine("                if (encoded == null) return")
                appendLine("                val entry = runCatching { nav.getBackStackEntry(baseRoute) }.getOrNull() ?: return")
                appendLine("                entry.savedStateHandle.set(\"tabPushParam\", encoded)")
                appendLine("            }")
                appendLine()
                appendLine("            val targetExists = runCatching { nav.getBackStackEntry(baseRoute) }.isSuccess")
                appendLine("            if (targetExists) {")
                appendLine("                setParamOnTargetIfPossible()")
                appendLine("            } else {")
                appendLine("                val listener = object : NavController.OnDestinationChangedListener {")
                appendLine("                    override fun onDestinationChanged(controller: NavController, destination: androidx.navigation.NavDestination, arguments: android.os.Bundle?) {")
                appendLine("                        if (destination.route == baseRoute) {")
                appendLine("                            setParamOnTargetIfPossible()")
                appendLine("                            controller.removeOnDestinationChangedListener(this)")
                appendLine("                        }")
                appendLine("                    }")
                appendLine("                }")
                appendLine("                nav.addOnDestinationChangedListener(listener)")
                appendLine("            }")
                appendLine()
                appendLine("            if (nav.currentDestination?.route != baseRoute) {")
                appendLine("                nav.navigate(baseRoute) {")
                appendLine("                    popUpTo(holderRoute) { saveState = true }")
                appendLine("                    launchSingleTop = true")
                appendLine("                    restoreState = true")
                appendLine("                }")
                appendLine("            }")
                appendLine("        }")
                appendLine("    }")
                appendLine()
                appendLine("    fun getCurrentTabViewModel(): KClass<out ViewModel>? = currentTab")
                appendLine("}")
            }

            outputFile.writeText(code)
            generateTabNavGraph(holder, distinctTabs)
        }
    }

    fun generateTabNavGraph(holder: String, tabs: List<TabEntry>) {
        val androidOut = outputAndroidTabsDir.get().asFile
        androidOut.mkdirs()

        val sortedTabs = tabs.sortedBy { it.position }
        val distinctTabs = sortedTabs.distinctBy { it.screen }

        val file = File(outputAndroidTabsDir.get().asFile, "${holder}NavGraph.kt")

        val code = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()

            val screenImports =
                distinctTabs.mapNotNull { findFunctionImport(it.screen, outputFiles) }
            val viewModelImports =
                distinctTabs.mapNotNull { findViewModelImport(it.viewModel, outputFiles) }
            (screenImports + viewModelImports).distinct().forEach { appendLine("import $it") }

            appendLine("import androidx.navigation.compose.composable")
            appendLine("import androidx.navigation.navigation")
            appendLine("import androidx.compose.runtime.*")
            appendLine("import androidx.navigation.compose.rememberNavController")
            appendLine("import androidx.navigation.compose.currentBackStackEntryAsState")
            appendLine("import com.google.accompanist.navigation.animation.AnimatedNavHost")
            appendLine("import androidx.compose.animation.*")
            appendLine("import androidx.compose.animation.core.tween")
            appendLine("import kotlinx.serialization.json.Json")
            appendLine("import androidx.compose.runtime.CompositionLocalProvider")
            appendLine("import androidx.compose.runtime.mutableStateOf")
            appendLine("import androidx.compose.runtime.remember")
            appendLine("import androidx.compose.runtime.setValue")
            appendLine("import androidx.compose.runtime.SideEffect")
            appendLine("import com.architect.atlas.architecture.mvvm.ViewModel")
            appendLine("import kotlin.reflect.KClass")
            appendLine("import android.graphics.drawable.Drawable")
            appendLine("import androidx.compose.ui.graphics.vector.ImageVector")
            appendLine("import com.architect.atlas.navigation.${holder}TabsNavigation")
            appendLine("import com.architect.atlas.navigation.TabLocalAtlasNavController")
            appendLine("import com.architect.atlas.navigation.AtlasTabNavHolder")
            appendLine("import androidx.lifecycle.Lifecycle")
            appendLine("import androidx.lifecycle.LifecycleEventObserver")
            appendLine("import kotlinx.coroutines.suspendCancellableCoroutine")
            appendLine("import kotlin.coroutines.resume")
            appendLine()

            appendLine("fun tabIndex(route: String?): Int = when (route) {")
            distinctTabs.forEach { tab -> appendLine("    \"${tab.screen}\" -> ${tab.position}") }
            appendLine("    else -> -1")
            appendLine("}")
            appendLine()

            appendLine("@Composable")
            appendLine("inline fun <reified VM : com.architect.atlas.architecture.mvvm.ViewModel> HandleLifecycle(")
            appendLine("    viewModel: VM,")
            appendLine("    isTabContent: Boolean = false,")
            appendLine("    crossinline content: @Composable () -> Unit")
            appendLine(") {")
            appendLine("    val lifecycleOwner: androidx.lifecycle.LifecycleOwner =")
            appendLine("        androidx.lifecycle.compose.LocalLifecycleOwner.current")
            appendLine("    val currentViewModel by rememberUpdatedState(viewModel)")
            appendLine()
            appendLine("    DisposableEffect(lifecycleOwner) {")
            appendLine("        val observer = LifecycleEventObserver { _, event ->")
            appendLine("            when (event) {")
            appendLine("                Lifecycle.Event.ON_RESUME -> currentViewModel.onAppearing()")
            appendLine("                Lifecycle.Event.ON_PAUSE  -> currentViewModel.onDisappearing()")
            appendLine("                Lifecycle.Event.ON_DESTROY -> {")
            appendLine("                    if (!isTabContent) {")
            appendLine("                        currentViewModel.onDestroy()")
            appendLine("                        currentViewModel.onCleared()")
            appendLine("                    }")
            appendLine("                }")
            appendLine("                else -> Unit")
            appendLine("            }")
            appendLine("        }")
            appendLine("        lifecycleOwner.lifecycle.addObserver(observer)")
            appendLine("        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }")
            appendLine("    }")
            appendLine("    content()")
            appendLine("}")
            appendLine()

            appendLine("@Composable")
            appendLine("private fun ConsumeTabParamOnce(entry: androidx.navigation.NavBackStackEntry, onParam: (Any) -> Unit) {")
            appendLine("    val handle = entry.savedStateHandle")
            appendLine("    val raw: String? = handle.get(\"tabPushParam\")")
            appendLine("    LaunchedEffect(raw) {")
            appendLine("        if (raw != null && raw != \"null\") {")
            appendLine("            val p: Any = when {")
            appendLine("                raw.toIntOrNull() != null -> raw.toInt()")
            appendLine("                raw.toDoubleOrNull() != null -> raw.toDouble()")
            appendLine("                raw.equals(\"true\", true) || raw.equals(\"false\", true) -> raw.toBoolean()")
            appendLine("                else -> runCatching { Json.decodeFromString<Any>(raw) }.getOrElse { raw }")
            appendLine("            }")
            appendLine("            onParam(p)")
            appendLine("            handle.remove<String>(\"tabPushParam\")")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()

            appendLine("@Composable")
            appendLine("private fun <VM : ViewModel> RunVmBootstrapExactlyAtCreatedOnce(")
            appendLine("    entry: androidx.navigation.NavBackStackEntry,")
            appendLine("    vm: VM")
            appendLine(") {")
            appendLine("    val handle = entry.savedStateHandle")
            appendLine("    val lifecycle = entry.lifecycle")
            appendLine("    val key = \"didBootstrap#\" + vm::class.qualifiedName")
            appendLine()
            appendLine("    LaunchedEffect(vm) {")
            appendLine("        if (handle.get<Boolean>(key) == true) return@LaunchedEffect")
            appendLine("        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {")
            appendLine("            suspendCancellableCoroutine<Unit> { cont ->")
            appendLine("                var obs: LifecycleEventObserver? = null")
            appendLine("                obs = LifecycleEventObserver { _, event ->")
            appendLine("                    if (event == Lifecycle.Event.ON_CREATE) {")
            appendLine("                        obs?.let { lifecycle.removeObserver(it) }")
            appendLine("                        if (cont.isActive) cont.resume(Unit)")
            appendLine("                    }")
            appendLine("                }")
            appendLine("                lifecycle.addObserver(obs)")
            appendLine("                cont.invokeOnCancellation { obs?.let { lifecycle.removeObserver(it) } }")
            appendLine("            }")
            appendLine("        }")
            appendLine("        if (handle.get<Boolean>(key) != true) {")
            appendLine("            vm.bootstrapVmFromNavEngine()")
            appendLine("            handle[key] = true")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
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
            appendLine("fun ${holder}NavGraph(onTabPositionChanged: (Int) -> Unit) {")
            appendLine("    val navControl = rememberNavController()")
            appendLine("    AtlasTabNavHolder.bind(navControl)")
            appendLine("    CompositionLocalProvider(TabLocalAtlasNavController provides navControl) {")
            appendLine("        val navBackStackEntry by navControl.currentBackStackEntryAsState()")
            appendLine("        val currentRoute = navBackStackEntry?.destination?.route")
            appendLine("        var previousTabIndex by remember { mutableStateOf(0) }")
            appendLine("        val newTabIndex = tabIndex(currentRoute)")
            appendLine("        val isForward = newTabIndex >= previousTabIndex")
            appendLine("        SideEffect { previousTabIndex = newTabIndex }")
            appendLine("        var lastCallbackTabIndex by remember { mutableStateOf(-1) }")
            appendLine("        LaunchedEffect(newTabIndex) {")
            appendLine("            if (newTabIndex != -1 && newTabIndex != lastCallbackTabIndex) {")
            appendLine("                lastCallbackTabIndex = newTabIndex")
            appendLine("                onTabPositionChanged(newTabIndex)")
            appendLine("            }")
            appendLine("        }")
            appendLine("        val holderRoute = \"${holder}Root\"")
            appendLine("        AnimatedNavHost(")
            appendLine("            navController = navControl,")
            appendLine("            startDestination = holderRoute,")
            appendLine("            enterTransition = {")
            appendLine("                val from = tabIndex(initialState.destination.route)")
            appendLine("                val to = tabIndex(targetState.destination.route)")
            appendLine("                if (to > from) slideInHorizontally { it } + fadeIn(tween(300))")
            appendLine("                else            slideInHorizontally { -it } + fadeIn(tween(300))")
            appendLine("            },")
            appendLine("            exitTransition = {")
            appendLine("                val from = tabIndex(initialState.destination.route)")
            appendLine("                val to = tabIndex(targetState.destination.route)")
            appendLine("                if (to > from) slideOutHorizontally { -it } + fadeOut(tween(300))")
            appendLine("                else            slideOutHorizontally {  it } + fadeOut(tween(300))")
            appendLine("            },")
            appendLine("            popEnterTransition = { fadeIn(tween(300)) },")
            appendLine("            popExitTransition = { fadeOut(tween(300)) }")
            appendLine("        ) {")
            appendLine("            navigation(startDestination = \"${distinctTabs.first().screen}\", route = holderRoute) {")
            distinctTabs.forEach { tab ->
                appendLine("                composable(\"${tab.screen}\") { entry ->")
                appendLine("                    val nav = TabLocalAtlasNavController.current")
                appendLine("                    val holderOwner = remember(entry) { nav.getBackStackEntry(holderRoute) }")
                appendLine("                    val vm = androidx.lifecycle.viewmodel.compose.viewModel(")
                appendLine("                        modelClass = ${tab.viewModel}::class.java,")
                appendLine("                        viewModelStoreOwner = holderOwner")
                appendLine("                    )")
                appendLine("                    ConsumeTabParamOnce(entry) { p ->")
                appendLine("                        @Suppress(\"UNCHECKED_CAST\")")
                appendLine("                        (vm as? com.architect.atlas.architecture.navigation.Pushable<Any>)?.onPushParams(p)")
                appendLine("                    }")
                appendLine("                    RunVmBootstrapExactlyAtCreatedOnce(entry, vm)")
                appendLine("                    HandleLifecycle(vm, isTabContent = true) { ${tab.screen}(vm) }")
                appendLine("                }")
            }
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
        }

        file.writeText(code)
    }

}
