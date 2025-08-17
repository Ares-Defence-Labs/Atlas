
//
//  SecondContentView.swift
//  iosApp
//
//  Created by Dan Gerchcovich on 15/7/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import shared
import SwiftUI

//@AtlasScreen(viewModel: DroidStandardSecond.self, initial: false)
struct ThirdContentView: View {
    let vm: DroidStandardSecond
    
    var body: some View {
        VStack{
            Text("Sample").onTapGesture{
                vm.openThirdScreenPush()
            }
            
            Text("SamplePopRoot").onTapGesture{
                vm.popToRoot()
            }
        }
    }
}


