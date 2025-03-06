//package com.architect.atlas.lifecycle
//
//import android.os.Bundle
//import com.architect.atlas.architecture.mvvm.ViewModel
//import kotlin.reflect.KClass
//
//abstract class AtlasComposeActivity<VM : ViewModel> : ComponentActivity() {
//    protected lateinit var binding: Binding
//    protected val viewModel: VM by lazy {
//        ViewModelProvider(this, AtlasViewModelFactory(viewModelType))
//            .get(viewModelType.java)
//    }
//
//    protected abstract val viewModelType: KClass<VM>
//    protected abstract fun viewBindingInflate(): Binding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(viewBindingInflate().root)
//    }
//}
//
