import SwiftUI
import UIKit
import shared

@MainActor
public class NavigationEngine: NSObject {
    static let shared = NavigationEngine()
    var stack: [ViewModel] = []
    @MainActor public func routeWithParams(viewModelType: String, params: Any? = nil, isModal: Bool = false) {
        let nav = UIApplication.globalRootNav
        switch viewModelType {
        case "DroidStandard":
            let resolved = AtlasDI.companion.resolveServiceNullableByName(clazz: SwiftClassGenerator.companion.getClazz(type: DroidStandard.self)) as! DroidStandard
            if let pc = params {
                resolved.tryHandlePush(params: pc)
            }
            let view = ContentView(vm: resolved)
            let controller = UIHostingController(rootView: view)
            if isModal {
                nav?.present(controller, animated: true)
            } else {
                nav?.pushViewController(controller, animated: true)
            }
            let vm = resolved; stack.append(vm)
        case "DroidStandardSecond":
            let resolved = AtlasDI.companion.resolveServiceNullableByName(clazz: SwiftClassGenerator.companion.getClazz(type: DroidStandardSecond.self)) as! DroidStandardSecond
            if let pc = params {
                resolved.tryHandlePush(params: pc)
            }
            let view = ContentViewSecond(vm: resolved)
            let controller = UIHostingController(rootView: view)
            if isModal {
                nav?.present(controller, animated: true)
            } else {
                nav?.pushViewController(controller, animated: true)
            }
            let vm = resolved; stack.append(vm)
        default: break
        }
    }
   func setNavigationStack(stack: [String], params: Any?) { /* Not used directly */ }
   func getNavigationStack() -> [String] { return stack.map { String(describing: $0) } }
    func popToRoot(animate: Bool = true, params: Any? = nil) {
        if let prev = stack.first as? Poppable {
            if let pc = params {
                prev.onPopParams(params: pc)
            }
        }
        stack.removeAll()
        UIApplication.shared.rootNav?.popToRootViewController(animated: animate)
    }
     func popPage(animate: Bool = true, params: Any? = nil) {
        if stack.count >= 2 {
            let prev = stack[stack.count - 2] as? Poppable
            if let pc = params {
                prev?.onPopParams(params: pc)
            }
        }
        if !stack.isEmpty { stack.removeLast() }
        UIApplication.shared.rootNav?.popViewController(animated: animate)
    }
     func popPagesWithCount(count: Int, animate: Bool = true, params: Any? = nil) {
        guard let nav = UIApplication.shared.rootNav else { return }
        let targetIndex = max(nav.viewControllers.count - count, 1)
        let target = nav.viewControllers[targetIndex - 1]
        if stack.count > count {
            let prev = stack[stack.count - count - 1] as? Poppable
            if let pc = params {
                prev?.onPopParams(params: pc)
            }
        }
        stack.removeLast(min(count, stack.count))
        nav.popToViewController(target, animated: animate)
    }
     func popToPage(route: String, params: Any?) { /* Not implemented */ }
     func dismissModal(animate: Bool = true, params: Any? = nil) {
        UIApplication.shared.rootNav?.presentedViewController?.dismiss(animated: animate)
    }
}
struct UIKitNavWrapperView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let root = ContentView(
            vm: AtlasDI.companion.resolveServiceNullableByName(
                clazz: SwiftClassGenerator.companion.getClazz(type: DroidStandard.self)
            ) as? DroidStandard
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
