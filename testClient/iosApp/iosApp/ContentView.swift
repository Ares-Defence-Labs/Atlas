import SwiftUI
import shared


//@AtlasScreen(viewModel: DroidStandard.self, initial: true)
struct ContentView: View {
    let greet = Greeting().greet()
    
    var body: some View {
        Text("")
    }
}


//@AtlasScreen(viewModel: DroidStandardSecond.self, initial: true)
struct ContentViewSecond: View {
    let greet = Greeting().greet()
    
    var body: some View {
        Text("")
    }
}
