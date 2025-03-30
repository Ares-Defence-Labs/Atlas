package com.architect.atlas.liveData.utilities

import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.MediatorLiveData

fun <OT, I1T, I2T> MediatorLiveData<OT>.compose(
    firstInput: LiveData<I1T>,
    secondInput: LiveData<I2T>,
    function: (I1T, I2T) -> OT
): MediatorLiveData<OT> {
    listOf(firstInput, secondInput).forEach {
        addSource(it) {
            value = function(firstInput.value, secondInput.value)
        }
    }

    return this
}

fun <OT, IT, LD : LiveData<out IT>> MediatorLiveData<OT>.composition(
    liveDataList: List<LD>,
    function: (List<IT>) -> OT
): MediatorLiveData<OT> {
    liveDataList.forEach { liveData ->
        addSource(liveData) { _ ->
            val values = liveDataList.map { it.value }
            value = function(values)
        }
    }

    return this
}

fun <OT, IT, LD : LiveData<out IT>> List<LD>.mediator(function: (List<IT>) -> OT): LiveData<OT> {
    val initialValue = function(map { it.value })
    return MediatorLiveData(initialValue)
        .composition(this, function)
}

fun <LD : LiveData<Boolean>> List<LD>.any(value: Boolean): LiveData<Boolean> =
    mediator { values ->
        values.firstOrNull { it == value } != null
    }

fun <LD : LiveData<Boolean>> List<LD>.all(value: Boolean): LiveData<Boolean> =
    mediator { values ->
        values.all { it == value }
    }