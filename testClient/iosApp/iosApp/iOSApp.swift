import SwiftUI
import shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            Text("Just Swift – No Kotlin").onAppear{
                CompTestStandard.companion.getTestSingle(name: "")
            }
        }
    }
}

#Preview {
    Text("Just Swift Preview")
}
