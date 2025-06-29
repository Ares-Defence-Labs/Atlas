import SwiftUI
import shared

//@AtlasTab(CoreDashboardTabViewModel::class, position = 0, holder = TabParentViewModel::class)
struct ContentView: View {
    let vm: CoreDashboardTabViewModel
    
    var body: some View {
        Text("Dashboard Screen")
            .background(Color.red)
            .containerShape(Rectangle())
            .frame(maxWidth: .infinity, maxHeight: 50).onTapGesture {
               // vm.openSecondScreen()
                }
    }
}





