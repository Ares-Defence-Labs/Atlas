package com.architect.atlas.navigationEngine.tasks.models

data class TabEntrySwift(
    val viewModel: String,
    val screen: String,
    val position: Int,
    val holder: String,
    val initialSelected: Boolean
)