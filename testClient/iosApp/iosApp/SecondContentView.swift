//
//  SecondContentView.swift
//  iosApp
//
//  Created by Dan Gerchcovich on 15/7/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import shared
import SwiftUI

//@AtlasScreen(viewModel: DroidStandard.self, initial: false)
struct SecondContentView: View {
    let vm: DroidStandard
    
    var body: some View {
        Text("Sample").onTapGesture{
            vm.openSecondScreen()
        }
    }
}


