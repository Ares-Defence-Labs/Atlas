package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.dsl.AtlasDI
import kotlin.reflect.KClass

abstract class AtlasActivityAppCompat<Binding: ViewBinding, VM: ViewModel> : AppCompatActivity() {
    protected val viewModel: VM by lazy {
        AtlasDI.resolveServiceNullable(viewModelType) ?: ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[viewModelType]
    }
    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(): Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewBindingInflate().root)
    }
}

