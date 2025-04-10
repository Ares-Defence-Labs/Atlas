import Foundation
import UIKit
import shared

class IOSAtlasNavigationService: NSObject, @preconcurrency AtlasNavigationService {
    @MainActor
    func setNavigationStack(stack: [ViewModel], params: Any?) {
        DispatchQueue.main.async {
              NavigationEngine.shared.setNavigationStack(stack: stack.map { "\($0)" }, params: params)
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

