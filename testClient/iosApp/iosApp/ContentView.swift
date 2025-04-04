import SwiftUI
import shared

struct ContentView: View {
	let greet = Greeting().greet()

	var body: some View {
        VStack {
//            Text("Centered")
//                .foregroundColor(Color(hex: AtlasColors.companion.green.swiftUIColor()))
//                .frame(maxWidth: .infinity)
//                .multilineTextAlignment(.center)
        }.onAppear(){
            
            
        }
	}
}


class TestComps{
    
    func helloTest() -> String{
        return "Hello there"
    }
}

class TestStandard{
    
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
