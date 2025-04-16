package com.architect.atlas.flows.compose

import androidx.compose.material.Checkbox
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material.Switch
import androidx.compose.material.Slider

import androidx.compose.material.RadioButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.StateFlow

@Composable
fun VisibleIf(
    visibleFlow: StateFlow<Boolean>,
    content: @Composable () -> Unit
) {
    val isVisible by visibleFlow.bind()

    if (isVisible) {
        Box {
            content()
        }
    }
}

@Composable
fun <T> BindableRadioButton(
    value: T,
    groupState: MutableStateFlow<T>,
    label: String,
    modifier: Modifier = Modifier
) {
    val selected = groupState.bind()

    Row(
        modifier = modifier.clickable { groupState.value = value }
    ) {
        RadioButton(
            selected = selected.value == value,
            onClick = { groupState.value = value }
        )
        Text(text = label)
    }
}

@Composable
fun BindableSlider(
    state: MutableStateFlow<Float>,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
) {
    val sliderValue = state.bindTwoWay()

    Slider(
        value = sliderValue.value,
        onValueChange = { sliderValue.value = it },
        valueRange = valueRange,
        modifier = modifier
    )
}

@Composable
fun BindableCheckbox(
    state: MutableStateFlow<Boolean>,
    modifier: Modifier = Modifier
) {
    val checked = state.bindTwoWay()

    Checkbox(
        checked = checked.value,
        onCheckedChange = { checked.value = it },
        modifier = modifier
    )
}

@Composable
fun BindableTextField(
    state: MutableStateFlow<String>,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val text = state.bindTwoWay()

    TextField(
        value = text.value,
        onValueChange = { text.value = it },
        label = label?.let { { androidx.compose.material.Text(it) } },
        modifier = modifier
    )
}

@Composable
fun BindableSwitch(
    state: MutableStateFlow<Boolean>,
    modifier: Modifier = Modifier
) {
    val checked = state.bindTwoWay()

    Switch(
        checked = checked.value,
        onCheckedChange = { checked.value = it },
        modifier = modifier
    )
}