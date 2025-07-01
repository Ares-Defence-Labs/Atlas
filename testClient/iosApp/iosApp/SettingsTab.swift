//
//  SettingsTab.swift
//  iosApp
//
//  Created by Dan Gerchcovich on 29/6/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared

//@AtlasSwiftTab(CoreSettingsTabViewModel::class, position = 2, holder = TabParentViewModel::class)
struct SettingsTab: View {
    let vm : CoreSettingsTabViewModel
    
    var body: some View {
        Text("Settings Fourth Screen").background(Color.blue)
            .containerShape(Rectangle())
            .onTapGesture {
                NotificationCenter.default.post(
                            name: .openAlertDialog,
                            object: nil,
                            userInfo: [
                                "id": "AlertDialog1",
                                "header": "Hello there",
                                "message": "Sample message received",
                                "onPressBtn": {
                                    
                                    NotificationCenter.default.post(
                                                name: .dismissAlertDialog,
                                                object: nil,
                                                userInfo: [
                                                    "id": "AlertDialog1",
                                                ]
                                            )
                                    
                                },
                                "heightOffset": 50
                            ]
                        )
            }.frame(maxWidth: .infinity, maxHeight: 50)
    }
}

extension ViewModel: ObservableObject {
    
}
