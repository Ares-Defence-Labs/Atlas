import SwiftUI
import shared

//@AtlasSwiftTab(CoreDashboardTabViewModel::class, position = 0, holder = TabParentViewModel::class)
struct ContentView: View {
    let vm: CoreDashboardTabViewModel
    
    var body: some View {
        Text("Dashboard Screen")
            .background(Color.red)
            .containerShape(Rectangle())
            .frame(maxWidth: .infinity, maxHeight: 50).onTapGesture {
                print("Posting bottom sheet notification")
                NotificationCenter.default.post(
                            name: .openBottomSheet,
                            object: nil,
                            userInfo: [
                                "id": "BottomSheet1",
                                "header": "Hello there",
                                "message": "Sample message received",
                                "onPressBtn": { print("Button pressed") },
                                "heightOffset": 50
                            ]
                        )
                }
    }
}





