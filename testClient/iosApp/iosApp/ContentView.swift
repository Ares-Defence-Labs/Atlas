import SwiftUI
import shared


struct ContentView: View {
	let greet = Greeting().greet()

	var body: some View {
        VStack {
            
//            Image(uiImage: AtlasImages.companion.android_svg!) .frame(maxWidth: .infinity)
//                
//            Text("Centered")
//                .font(Font(AtlasFonts.companion.roboto_thin(size: 42) as! CTFont))
//                .frame(maxWidth: .infinity)
//                .multilineTextAlignment(.center)
        }.onAppear(){
            
            //AtlasFonts.companion.roboto_black(size: 12)
            
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
