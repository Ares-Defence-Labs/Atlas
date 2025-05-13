import SwiftUI
import UIKit
import shared

@main
struct iOSApp: App {
    init(){
       // TestIOS.companion.registerServices()
    }
    var body: some Scene {
        WindowGroup {
            Text("Hello There OTF FONT FILE").font(Font(AtlasFonts.companion.testfontfile(size: 12)))
        }
    }
}
