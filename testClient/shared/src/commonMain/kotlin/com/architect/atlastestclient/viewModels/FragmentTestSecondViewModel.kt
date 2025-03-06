package com.architect.atlastestclient.viewModels

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.annotations.ViewModels

@ViewModels
class FragmentTestSecondViewModel : ViewModel(){

    var q = ""
    fun processResult(){
        q = "Hello there"
    }
}