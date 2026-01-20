package com.architect.atlas.navigationEngine.tasks.routingEngine.apple

import com.architect.atlas.navigationEngine.helpers.scanIosTabAnnotationsFromSwiftFiles
import com.architect.atlas.navigationEngine.tasks.models.ScreenMetadata
import com.architect.atlas.navigationEngine.tasks.models.TabEntrySwift
import com.architect.atlas.navigationEngine.tasks.routingEngine.apple.helpers.scanViewModelSwiftAnnotations
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class AppleIOSNavigationEngineGeneratorTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty
    @get:Input
    abstract var iOSOutputFiles: List<File>

    @get:Input
    abstract var projectCoreName: String

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    init {
        group = "AtlasNavigation"
        description =
            "Generates the platform-specific navigation engine implementations (IOS ONLY)"

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
    }

    @TaskAction
    fun generateNavigatorClass() {
        logger.lifecycle("WRITING NAVIGATION TO IOS")
        generateIOSNavigation(scanViewModelSwiftAnnotations(iOSOutputFiles).map {
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

            appendLine("   func setNavigationStack(stack: [String], params: Any?) { }")
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

            appendLine("     func popToPage(route: String, params: Any?) { }")
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
               private var didBootstrap = false
               init(rootView: Content, viewModel: ViewModel, viewModelName: String) {
                   self.viewModel = viewModel
                   self.viewModelName = viewModelName
                   super.init(rootView: rootView)
               }
               
               @objc required dynamic init?(coder aDecoder: NSCoder) {
                   fatalError("init(coder:) has not been implemented")
               }
               
               override func viewDidLoad() {
                   super.viewDidLoad()
                   if !didBootstrap {
                       didBootstrap = true
                       viewModel.bootstrapVmFromNavEngine()
                   }
               }
               
               override func viewDidAppear(_ animated: Bool) {
                    super.viewDidAppear(animated)
                    viewModel.onAppearing()
                    
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            guard let self = self else { return }
            if self.navigationController?.topViewController === self {
                self.viewModel.onAfterAppearing()
            }
        }
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
        @State private var didBootstrap = false
        let content: (VM) -> Content

        init(viewModel: VM, @ViewBuilder content: @escaping (VM) -> Content) {
            _viewModel = StateObject(wrappedValue: viewModel)
            self.content = content
        }

        var body: some View {
            content(viewModel)
                .onAppear {
                    if !didBootstrap {
                        didBootstrap = true
                        viewModel.bootstrapVmFromNavEngine()
                    }
                    viewModel.onAppearing()
                }
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
                    .padding(.bottom, geometry.safeAreaInsets.bottom + 48)
                    
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
            guard let viewModelName = viewModelClass.simpleName else { return }
            if let index = tabIndices[viewModelName] {
                selectedTabIndex = index
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
}

