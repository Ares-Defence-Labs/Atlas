package com.architect.atlas.navigationEngine.tasks.routingEngine.android

import com.architect.atlas.navigationEngine.helpers.findScreenImport
import com.architect.atlas.navigationEngine.helpers.findViewModelImport
import com.architect.atlas.navigationEngine.helpers.isUnderAny
import com.architect.atlas.navigationEngine.tasks.routingEngine.android.helpers.scanViewModelAnnotations
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
abstract class ClassicalNavigationEngineGeneratorTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputAndroidDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val androidSourceFiles: ConfigurableFileCollection

    @get:Input
    abstract var outputFiles: List<File>

    @get:Input
    abstract var androidBasePackageRef: String

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
        logger.lifecycle("WRITING NAVIGATION TO ANDROID")
        val ants = scanViewModelAnnotations(outputFiles, logger, androidSourceFiles)
        val droidSourceFiles = androidSourceFiles.files.toList()
        val droidAnts = if (droidSourceFiles.isNotEmpty()) {
            val filtered = ants.filter { (_, _, filePath, _) ->
                File(filePath).isUnderAny(droidSourceFiles)
            }
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

        generateAndroidClassicalNavigation(droidAnts.map { it.first to it.second }.distinct())
    }

    private fun generateAndroidClassicalNavigation(
        screens: List<Pair<String, String>>,
    ) {
        val viewModelImports =
            screens.mapNotNull { (viewModel, _) -> findViewModelImport(viewModel, outputFiles) }
                .distinct()

        val screenImports =
            screens.mapNotNull { (_, screenClass) -> findScreenImport(screenClass, outputFiles) }
                .distinct()

        val androidImpl = buildString {
            appendLine("package com.architect.atlas.navigation")
            appendLine()

            viewModelImports.forEach { appendLine("import $it") }
            screenImports.forEach { appendLine("import $it") }

            appendLine(
                """
import $androidBasePackageRef.R                    
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.architecture.navigation.Pushable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.reflect.KClass

/**
 * Classical (Fragment-based) navigation implementation for Atlas.
 *
 * This is generated from @AtlasScreen annotations on platform classes
 * (e.g. Fragments) and only cares about the annotation – it does NOT
 * require the class to extend Fragment at codegen time.
 *
 * At runtime, if a non-Fragment is mapped, an error will be thrown when
 * trying to navigate to that screen.
 */
object AtlasFragmentNavigation : AtlasNavigationService {

    private const val PUSH_KEY = "atlas_push_param"

    private var hostActivity: FragmentActivity? = null
    private var containerId: Int = android.R.id.content

    // ViewModel -> Screen class (Fragment or any annotated type)
    private val viewModelToScreenMap: Map<KClass<out ViewModel>, KClass<*>> = mapOf(
            """.trimIndent()
            )

            // e.g. DroidStandard::class to FirstTestFragment::class
            for ((viewModel, screenClass) in screens) {
                appendLine("        $viewModel::class to $screenClass::class,")
            }

            appendLine(
                """
    )

    /**
     * Bind the classical navigation engine to a host activity and container.
     *
     * Example:
     *  class TestActivity : FragmentActivity() {
     *      override fun onCreate(savedInstanceState: Bundle?) {
     *          super.onCreate(savedInstanceState)
     *          setContentView(R.layout.activity_test_client)
     *          AtlasFragmentNavigation.bind(this, R.id.nav_host_container)
     *      }
     *  }
     */
    fun bind(activity: FragmentActivity, containerId: Int = android.R.id.content) {
        hostActivity = activity
        this.containerId = containerId
    }

    private fun requireHost(): Pair<FragmentActivity, FragmentManager> {
        val act = hostActivity
            ?: error("AtlasFragmentNavigation not bound. Call AtlasFragmentNavigation.bind(activity, containerId) first.")
        return act to act.supportFragmentManager
    }

    // region AtlasNavigationService API

    override fun <T : ViewModel> navigateToPage(viewModelClass: KClass<T>, params: Any?) {
        navigateInternal(viewModelClass, params, clearBackStack = false, replaceCurrent = false)
    }

    override fun <T : ViewModel> navigateToPagePushAndReplace(viewModelClass: KClass<T>, params: Any?) {
        navigateInternal(viewModelClass, params, clearBackStack = true, replaceCurrent = false)
    }

    override fun <T : ViewModel> navigateToPagePushAndReplaceCurrentScreen(
        viewModelClass: KClass<T>,
        params: Any?
    ) {
        navigateInternal(viewModelClass, params, clearBackStack = false, replaceCurrent = true)
    }

    override fun <T : ViewModel> navigateToPageModal(viewModelClass: KClass<T>, params: Any?) {
        // For now treat modals like a normal push.
        navigateToPage(viewModelClass, params)
    }

    override fun <T : ViewModel> setNavigationStack(stack: List<T>, params: Any?) {
        // Not implemented for classical Fragments right now.
        // You could add replay / reconstruction here if needed.
    }

    override fun <T : ViewModel> getNavigationStack(): List<T> = emptyList()

    override fun popToRoot(animate: Boolean, params: Any?) {
        val (_, fm) = requireHost()
        deliverPopParamsToPrevious(params)
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun popPage(animate: Boolean, params: Any?) {
        val (_, fm) = requireHost()
        deliverPopParamsToPrevious(params)
        fm.popBackStack()
    }

    override fun popPagesWithCount(countOfPages: Int, animate: Boolean, params: Any?) {
        val (_, fm) = requireHost()
        if (countOfPages <= 0) return

        deliverPopParamsToPrevious(params)

        repeat(countOfPages) {
            if (fm.backStackEntryCount == 0) return
            fm.popBackStack()
        }
    }

    override fun popToPage(route: String, params: Any?) {
        val (_, fm) = requireHost()
        deliverPopParamsToPrevious(params)
        fm.popBackStack(route, 0)
    }

    override fun dismissModal(animate: Boolean, params: Any?) {
        popPage(animate, params)
    }

    // endregion

    // region Internal navigation helpers

    private fun <T : ViewModel> navigateInternal(
        viewModelClass: KClass<T>,
        params: Any?,
        clearBackStack: Boolean,
        replaceCurrent: Boolean
    ) {
        val (activity, fm) = requireHost()

        val screenKClass = viewModelToScreenMap[viewModelClass]
            ?: error("No screen registered for ${'$'}viewModelClass via @AtlasScreen")

        // Only enforce Fragment at runtime – generator is annotation-based.
        val fragment = try {
            @Suppress("UNCHECKED_CAST")
            (screenKClass.java.newInstance() as Fragment)
        } catch (e: Throwable) {
            error("Screen class ${'$'}screenKClass must extend androidx.fragment.app.Fragment for classical navigation")
        }

        val fragmentTag = screenKClass.qualifiedName

        val args = (fragment.arguments ?: Bundle())
        encodeParam(params)?.let { encoded ->
            args.putString(PUSH_KEY, encoded)
        }
        fragment.arguments = args

        if (clearBackStack) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else if (replaceCurrent && fm.backStackEntryCount > 0) {
            fm.popBackStack()
        }

        val tx = fm.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left,    // enter
                R.anim.slide_out_right,  // exit
                R.anim.slide_in_left,    // popEnter
                R.anim.slide_out_right   // popExit
            )
            .setReorderingAllowed(true)
            .replace(containerId, fragment, fragmentTag)

        tx.addToBackStack(fragmentTag)
        tx.commit()

        activity.runOnUiThread {
            fm.executePendingTransactions()
            bootstrapFragment(screenKClass)
        }
    }

    /**
     * Resolve VM for the given screen class and:
     *  - deliver push params (Pushable)
     *  - call bootstrapVmFromNavEngine()
     */
    private fun bootstrapFragment(screenKClass: KClass<*>) {
        val (_, fm) = requireHost()
        val fragmentTag = screenKClass.qualifiedName
        val fragment = fm.findFragmentByTag(fragmentTag) ?: return

        val vmKlass = viewModelToScreenMap.entries
            .firstOrNull { it.value == screenKClass }?.key ?: return

        val vm = resolveViewModel(vmKlass, owner = fragment)

        val payloadRaw = fragment.arguments?.getString(PUSH_KEY)
        if (payloadRaw != null && vm is Pushable<*>) {
            decodeParam(payloadRaw)?.let { decoded ->
                @Suppress("UNCHECKED_CAST")
                (vm as Pushable<Any>).onPushParams(decoded)
            }
        }

        vm.bootstrapVmFromNavEngine()
    }

    /**
     * When popping, find the new top Fragment, resolve its VM,
     * and call Poppable.onPopParams(...)
     */
    private fun deliverPopParamsToPrevious(params: Any?) {
        val (_, fm) = requireHost()
        if (params == null) return
        if (fm.fragments.isEmpty()) return

        val prev = fm.fragments.lastOrNull { it.isAdded } ?: return

        val vmKlass = viewModelToScreenMap.entries
            .firstOrNull { it.value == prev::class }?.key ?: return

        val encoded = encodeParam(params) ?: return
        val decoded = decodeParam(encoded) ?: return

        val vm = resolveViewModel(vmKlass, owner = prev)
        if (vm is Poppable<*>) {
            @Suppress("UNCHECKED_CAST")
            (vm as Poppable<Any>).onPopParams(decoded)
        }
    }

    private fun resolveViewModel(
        vmClass: KClass<out ViewModel>,
        owner: ViewModelStoreOwner
    ): ViewModel {
        @Suppress("UNCHECKED_CAST")
        val androidVmClass = vmClass.java as Class<androidx.lifecycle.ViewModel>
        val vm = ViewModelProvider(owner)[androidVmClass]
        @Suppress("UNCHECKED_CAST")
        return vm as ViewModel
    }

    private fun encodeParam(param: Any?): String? =
        param?.let {
            when (it) {
                is String, is Number, is Boolean -> it.toString()
                else -> Json.encodeToString(it)
            }
        }

    private fun decodeParam(encoded: String): Any? =
        encoded.toIntOrNull()
            ?: encoded.toDoubleOrNull()
            ?: if (encoded.equals("true", true) || encoded.equals("false", true)) {
                encoded.toBoolean()
            } else {
                runCatching { Json.decodeFromString<Any>(encoded) }.getOrNull() ?: encoded
            }

    // endregion
}
            """.trimIndent()
            )
        }

        val androidOut = outputAndroidDir.get().asFile
        androidOut.mkdirs()
        File(androidOut, "AtlasFragmentNavigation.kt").writeText(androidImpl)
    }
}

