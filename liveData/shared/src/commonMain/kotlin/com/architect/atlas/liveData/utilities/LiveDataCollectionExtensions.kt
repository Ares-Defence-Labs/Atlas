package com.architect.atlas.liveData.utilities

import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.MediatorLiveData
import com.architect.atlas.liveData.MutableLiveData
import kotlin.jvm.JvmName

fun <IT, OT> LiveData<IT?>.mapOrNull(function: (IT) -> OT): LiveData<OT?> {
    return map { newValue ->
        when (newValue) {
            null -> null
            else -> function(newValue)
        }
    }
}

fun <OT> LiveData<Boolean?>.mapTrueOrNull(function: () -> OT): LiveData<OT?> {
    return map { newValue ->
        when (newValue) {
            true -> function()
            else -> null
        }
    }
}

fun <T> LiveData<T?>.required(initialValue: T): LiveData<T> = MediatorLiveData(initialValue).apply {
    addSource(this@required) { newValue ->
        if (newValue == null) return@addSource

        value = newValue
    }
}

fun <T> LiveData<T>.distinct(): LiveData<T> {
    val source = this
    return MediatorLiveData(value).apply {
        addSource(source) { newValue ->
            if (newValue == value) return@addSource
            value = newValue
        }
    }
}

fun LiveData<String?>.orEmpty(): LiveData<String> {
    return map { it.orEmpty() }
}

@JvmName("ListLiveDataOrEmpty")
fun <T> LiveData<List<T>?>.orEmpty(): LiveData<List<T>> {
    return map { it.orEmpty() }
}

fun LiveData<Boolean>.revert() = map { !it }

fun <T> MutableLiveData<T>.readOnly(): LiveData<T> = this

fun <T, OT> LiveData<T>.map(function: (T) -> OT): LiveData<OT> {
    val mutableLiveData = MutableLiveData(function(value))
    addObserver { mutableLiveData.value = function(it) }
    return mutableLiveData
}

fun <T, OT> LiveData<T>.flatMap(function: (T) -> LiveData<OT>): LiveData<OT> {
    var shadowLiveData: LiveData<OT>? = null
    var mutableLiveData: MutableLiveData<OT>? = null

    val shadowObserver: (OT) -> Unit = {
        mutableLiveData!!.value = it
    }

    addObserver { newValue ->
        shadowLiveData?.removeObserver(shadowObserver)

        val newShadowLiveData = function(newValue)
        shadowLiveData = newShadowLiveData

        if (mutableLiveData == null) {
            mutableLiveData = MutableLiveData(newShadowLiveData.value)
        }

        newShadowLiveData.addObserver(shadowObserver)
    }

    return mutableLiveData!!
}

@Deprecated(
    message = "Use mediatorOf() instead",
    replaceWith = ReplaceWith(
        expression = "mediatorOf()",
        imports = arrayOf("dev.icerock.moko.mvvm.livedata.mediatorOf")
    )
)
fun <OT, I1T, I2T> LiveData<I1T>.mergeWith(
    secondLiveData: LiveData<I2T>,
    function: (I1T, I2T) -> OT
): MediatorLiveData<OT> {
    return MediatorLiveData(function(value, secondLiveData.value))
        .compose(this, secondLiveData, function)
}

fun <T, OT> LiveData<T>.mapBuffered(function: (current: T, new: T) -> OT): LiveData<OT> {
    var current = value
    return map { newValue ->
        val result = function(current, newValue)
        current = newValue
        result
    }
}

fun <T, OT> LiveData<T>.flatMapBuffered(function: (current: T, new: T) -> LiveData<OT>): LiveData<OT> {
    var current = value
    return flatMap { newValue ->
        val result = function(current, newValue)
        current = newValue
        result
    }
}