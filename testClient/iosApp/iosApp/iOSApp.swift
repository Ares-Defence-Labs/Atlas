import SwiftUI
import UIKit
import shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            UIKitNavWrapperView()
                .ignoresSafeArea().onAppear(){
                    TestIOS.companion.registerServices()
                }
        }
    }
}
