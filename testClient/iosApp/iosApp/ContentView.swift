import SwiftUI
import shared


//@AtlasScreen(viewModel: DroidStandard.self, initial: true)
struct ContentView: View {
    let vm: DroidStandard
//    @StateObject var name: FlowBinding<String>
//    @StateObject var samples: FlowBinding<[SampleProcess]>
    
    
//    init(vm: DroidStandard) {
//
//        self.vm = vm
//        _name = StateObject(wrappedValue: FlowBinding(flow: vm.testText.asSwiftFlow(), adapter: stringAdapter))
//        _samples = StateObject(wrappedValue: FlowBinding(flow: vm.resultsSamples.asSwiftFlow(), adapter: makeListAdapter(SampleProcess.self)))
//    }
    
    var body: some View {
//        List(samples.value, id: \.id) { person in
//            Text(person.name)
//        }
//
//        VStack{
//            Text(name)
//        }.onAppear{
//            let currentValue = stringAdapter.fromKotlin(vm.testText.asSwiftFlow().getValue())
//            observe(vm.testText.asSwiftFlow(), adapter: stringAdapter) { res in
//            print ("RES \(res)")
//            }
//        }
//        
//        List {
//            ForEachBinding(samples.listBinding, id: \.self) { sample in
//                //let rec = sample.wrappedValue as Sample
//                Text(sample[\.result].value())
//            }
//        }
        Text("Hello There OTF FONT FILE").font(Font(AtlasFonts.companion.testfontfile(size: 12)))
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

