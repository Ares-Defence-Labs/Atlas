//
//  ParentTabHolder.swift
//  iosApp
//
//  Created by Dan Gerchcovich on 29/6/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import shared

//@AtlasScreen(viewModel: TabParentViewModel.self, initial: true)
struct TabParentView: View {
    let vm : TabParentViewModel
    
    @State private var selectedTabIndex = 0
    
    var body: some View {
//            TabView(selection: $selectedTabIndex) {
//                buildTab(CoreDashboardTabViewModel.self,
//                         selectedTabIndex: $selectedTabIndex,
//                         tabIndex: 0,
//                         selectedTabItemBuilder: {
//                    Label("Dashboard", systemImage: "house.fill")
//                        .foregroundStyle(.yellow)
//                },
//                         deselectedTabItemBuilder: {
//                    Label("Dashboard", systemImage: "house.fill")
//                        .foregroundStyle(.black)
//                }) { vm in
//                    ContentView(vm: vm)
//                }
//                
//                buildTab(DroidStandardThird.self,
//                         selectedTabIndex: $selectedTabIndex,
//                         tabIndex: 1,
//                         selectedTabItemBuilder: {
//                            EmptyView() // used for creating space between the bottom tabs
//                         },
//                         deselectedTabItemBuilder: {
//                            EmptyView() // used for creating space between the bottom tabs
//                         }) { vm in
//                    ContentViewFab(vm: vm)
//                }
//                
//                buildTab(CoreSettingsTabViewModel.self,
//                         selectedTabIndex: $selectedTabIndex,
//                         tabIndex: 2,
//                         selectedTabItemBuilder: {
//                    Label("Settings", systemImage: "square.and.arrow.up")
//                        .foregroundStyle(.yellow)
//                },
//                         deselectedTabItemBuilder: {
//                    Label("Settings", systemImage: "square.and.arrow.up")
//                        .foregroundStyle(.black)
//                }) { vm in
//                    SettingsTab(vm: vm)
//                }
                
            }
//            .overlay(
//                VStack {
//                    Spacer()
//                    HStack {
//                        Spacer()
//                        Button(action: {
//                            selectedTabIndex = 1
//                        }) {
//                            ZStack {
//                                Circle()
//                                    .fill(Color.yellow)
//                                    .frame(width: 64, height: 64)
//                                    .shadow(radius: 4)
//                                
//                                Image(systemName: "fuelpump.fill")
//                                    .font(.system(size: 24))
//                                    .foregroundColor(selectedTabIndex == 1 ? Color.red : Color.gray)
//                            }
//                        }
//                        .padding(.bottom, 62)
//                        Spacer()
//                    }
//                }
//            )
    //}
}

//struct FuelPumpFAB: AtlasTabItemView {
//    func selectedTabItem() -> some View {
//        Image(systemName: "fuelpump.fill")
//            .font(.system(size: 24))
//            .foregroundColor(.blue)
//            .frame(width: 56, height: 56)
//    }
//    
//    func deselectedTabItem() -> some View {
//        Image(systemName: "fuelpump")
//            .font(.system(size: 24))
//            .foregroundColor(.red)
//            .frame(width: 56, height: 56)
//    }
//}
//
