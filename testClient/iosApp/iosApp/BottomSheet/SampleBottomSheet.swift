//
//  SampleBottomSheet.swift
//  iosApp
//
//  Created by Dan Gerchcovich on 1/7/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct BottomSheetContent: View {
    let header: String
    let message: String
    let actionOnPress: () -> Void

    var body: some View {
        VStack {
            Text(header).font(.headline)
            Text(message).font(.body)
            Button("Confirm", action: actionOnPress)
        }
        .padding()
    }
}
