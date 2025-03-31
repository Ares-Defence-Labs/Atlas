package com.architect.atlas.liveData.bindings.liveData

import com.architect.atlas.liveData.LiveData
import kotlin.random.Random

fun generateUniqueId(): String {
    return Random.nextBytes(16)
        .joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
}

open class LiveDataObserver {
    val id: String = generateUniqueId()

    open fun dispose() {}
}

class LiveDataBoolObserver(
    private val liveData: LiveData<Boolean>,
    private val action: (Boolean?) -> Unit
) : LiveDataObserver() {

    private val observer: (Boolean) -> Unit = { action(it) }

    init {
        action(liveData.value)
        liveData.addObserver(observer)
    }

    override fun dispose() {
        liveData.removeObserver(observer)
    }
}

// MARK: - Int Observer

class LiveDataIntObserver(
    private val liveData: LiveData<Int>,
    private val action: (Int?) -> Unit
) : LiveDataObserver() {

    private val observer: (Int) -> Unit = { action(it) }

    init {
        action(liveData.value)
        liveData.addObserver(observer)
    }

    override fun dispose() {
        liveData.removeObserver(observer)
    }
}

// MARK: - Double Observer

class LiveDataDoubleObserver(
    private val liveData: LiveData<Double>,
    private val action: (Double?) -> Unit
) : LiveDataObserver() {

    private val observer: (Double) -> Unit = { action(it) }

    init {
        action(liveData.value)
        liveData.addObserver(observer)
    }

    override fun dispose() {
        liveData.removeObserver(observer)
    }
}

// Enum Observer
class LiveDataEnumObserver<T : Enum<T>>(
    private val liveData: LiveData<T>,
    private val onChanged: (String?) -> Unit
) : LiveDataObserver() {

    private val observer: (T) -> Unit = { enumValue ->
        onChanged(enumValue.name)
    }

    init {
        liveData.addObserver(observer)
        onChanged(liveData.value.name)
    }

    override fun dispose() {
        liveData.removeObserver(observer)
    }
}

// MARK: - String Observer

class LiveDataStringObserver(
    private val liveData: LiveData<String>,
    private val action: (String?) -> Unit
) : LiveDataObserver() {

    private val observer: (String) -> Unit = { action(it) }

    init {
        action(liveData.value)
        liveData.addObserver(observer)
    }

    override fun dispose() {
        liveData.removeObserver(observer)
    }
}

class LiveDataListObserver<T>(
    private val liveData: LiveData<List<T>>,
    private val action: (List<T>?) -> Unit
) : LiveDataObserver() {

    private val observer: (List<T>) -> Unit = { action(it) }

    init {
        action(liveData.value)
        liveData.addObserver(observer)
    }

    override fun dispose() {
        liveData.removeObserver(observer)
    }
}