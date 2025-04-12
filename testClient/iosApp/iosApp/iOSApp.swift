import SwiftUI
import UIKit
import shared

@main
struct iOSApp: App {
    init(){
        TestIOS.companion.registerServices()
    }
    var body: some Scene {
        WindowGroup {
            UIKitNavWrapperView()
                .ignoresSafeArea()
        }
    }
}
