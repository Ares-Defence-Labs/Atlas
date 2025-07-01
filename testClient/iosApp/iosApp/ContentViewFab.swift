//
//  FabContent.swift
//  iosApp
//
//  Created by Dan Gerchcovich on 29/6/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared

//@AtlasSwiftTab(DroidStandardThird::class, position = 1, holder = TabParentViewModel::class)
struct ContentViewFab: View {
    let vm : DroidStandardThird
    
    var body: some View {
        Text("FAB Screen").background(Color.red)
            .containerShape(Rectangle())
            .onTapGesture {
               // /vm.openThirdScreenEntireStack()
            }.frame(maxWidth: .infinity, maxHeight: 50)
    }
}

