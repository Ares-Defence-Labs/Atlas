import SwiftUI
import shared

@main
struct iOSApp: App {
    init(){
        TestIOS.companion.registerServices(atlasNavigationService: IOSAtlasNavigationService())
      print ("CLASSES FOUND \(SwiftClassGenerator.companion.getClazz(type: DroidStandard.self))")
    }
    var body: some Scene {
        WindowGroup {
            UIKitNavWrapperView()
        }
    }
}

#Preview {
    Text("Just Swift Preview")
}
