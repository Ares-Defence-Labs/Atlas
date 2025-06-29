//
//  SettingsTab.swift
//  iosApp
//
//  Created by Dan Gerchcovich on 29/6/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared

//@AtlasTab(CoreSettingsTabViewModel::class, position = 2, holder = TabParentViewModel::class)
struct SettingsTab: View {
    let vm : CoreSettingsTabViewModel
    
    var body: some View {
        Text("Settings Fourth Screen").background(Color.blue)
            .containerShape(Rectangle())
            .onTapGesture {
            //    vm.openThirdScreenEntireStack()
            }.frame(maxWidth: .infinity, maxHeight: 50)
    }
}

extension ViewModel: ObservableObject {
    
}
