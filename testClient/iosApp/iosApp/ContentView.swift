import SwiftUI
import shared

//@AtlasTab(CoreDashboardTabViewModel::class, position = 0, holder = TabParentViewModel::class)
//@AtlasScreen(viewModel: DroidStandard.self, initial: true)
struct ContentView: View {
    let vm: DroidStandard
    
    var body: some View {
        Text("First Screen")
            .background(Color.blue)
            .containerShape(Rectangle())
            .frame(maxWidth: .infinity, maxHeight: 50).onTapGesture {
                vm.openSecondScreen()
                }
    }
}

//@AtlasScreen(viewModel: DroidStandardSecond.self)
struct ContentViewSecond: View {
    let vm : DroidStandardSecond
    
    var body: some View {
        Text("Second Screen").background(Color.blue)
            .containerShape(Rectangle())
            .onTapGesture {
                vm.openThirdScreenEntireStack()
            }.frame(maxWidth: .infinity, maxHeight: 50)
    }
}

//@AtlasScreen(viewModel: DroidStandardThird.self)
struct ContentViewThird: View {
    let vm : DroidStandardThird?
    
    var body: some View {
        Text("Third Screen").onTapGesture {
            
        }
    }
}



