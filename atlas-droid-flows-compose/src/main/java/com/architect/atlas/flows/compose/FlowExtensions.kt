package com.architect.atlas.flows.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

@Composable
fun <T> StateFlow<T>.bind(): State<T> = collectAsState()

@Composable
fun <T> MutableStateFlow<T>.bindTwoWay(): MutableState<T> {
    val state = collectAsState()
    val localState = remember { mutableStateOf(state.value) }

    // Sync Flow → UI
    LaunchedEffect(state.value) {
        if (localState.value != state.value) {
            localState.value = state.value
        }
    }

    // Sync UI → Flow
    LaunchedEffect(localState.value) {
        if (value != localState.value) {
            value = localState.value
        }
    }

    return localState
}


@Composable
fun <T> StateFlow<T>.observe(onEach: (T) -> Unit) {
    val state = this@observe.collectAsState()

    LaunchedEffect(state.value) {
        onEach(state.value)
    }
}


@Composable
fun <T> StateFlow<T>.observeOnMain(onEach: (T) -> Unit) {
    val state = this@observeOnMain.collectAsState()

    LaunchedEffect(state.value) {
        withContext(Dispatchers.Main.immediate) {
            onEach(state.value)
        }
    }
}
