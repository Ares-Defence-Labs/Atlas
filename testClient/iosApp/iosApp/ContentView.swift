import SwiftUI
import shared


//@AtlasScreen(viewModel: DroidStandard.self, initial: true)
struct ContentView: View {
    let vm : DroidStandard?
    
    var body: some View {
        Text("Testing Button").onTapGesture {
            NavigationEngine.shared.routeWithParams(viewModelType: "DroidStandardSecond", params: "Hello There")
        }.onAppear{
            
        }
    }
}


//@AtlasScreen(viewModel: DroidStandardSecond.self)
struct ContentViewSecond: View {
    let vm : DroidStandardSecond?
//    let greet = Greeting().greet()
    
    var body: some View {
        Text("Hello There")
    }
}

