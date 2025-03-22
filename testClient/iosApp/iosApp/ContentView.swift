import SwiftUI
import shared

struct ContentView: View {
	let greet = Greeting().greet()

	var body: some View {
        Text(greet).onAppear{
            
            print ("CLASS NAME : \(SwiftClassGenerator.companion.getClazz(type: TestSingle.self))")
            
            print ("RESULT FROM SERVICE \(TestIOS.companion.registerServices())")
            
           
            //AtlasDI.companion.resolveServiceNullableByName(clazz: <#T##String#>)
            
//            AtlasDI.companion.resolveServiceNullableByNameService(TestSingle.self);
//            
                print ("RUNNING SERVICE")
            
            
//            if let service = CompTestStandard.companion.getTestSingle(name: SwiftClassGenerator.companion.getClazz(type: TestSingle.self)) as? TestSingle {
//                    print("\(service.helloThere())")
//                }
//            else {
//                print ("NO SERVICE FOUND")
//            }
           
            
            
//            TesterShare.companion.processTest()
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
