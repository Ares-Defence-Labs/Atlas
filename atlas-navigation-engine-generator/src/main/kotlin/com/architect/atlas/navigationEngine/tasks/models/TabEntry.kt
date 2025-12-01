package com.architect.atlas.navigationEngine.tasks.models

data class TabEntry(
    val viewModel: String,
    val screen: String,
    val holderViewModel: String,
    val position: Int
)