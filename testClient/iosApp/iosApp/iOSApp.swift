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
                .bottomSheetRegistry(id: "BottomSheet1") { params in
                                    let header = params["header"] as? String ?? ""
                                    let message = params["message"] as? String ?? ""
                                    let onPress = params["onPressBtn"] as? () -> Void ?? {}

                                    BottomSheetContent(header: header, message: message, actionOnPress: onPress)
                                }
                .alertDialogRegistry(id: "AlertDialog1") { params in
                                    let header = params["header"] as? String ?? ""
                                    let message = params["message"] as? String ?? ""
                                    let onPress = params["onPressBtn"] as? () -> Void ?? {}

                                    BottomSheetContent(header: header, message: message, actionOnPress: onPress)
                                }
            
            
        }
    }
}

#Preview {
    Text("Just Swift Preview")
}
