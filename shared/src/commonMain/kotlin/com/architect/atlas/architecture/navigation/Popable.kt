package com.architect.atlas.architecture.navigation

interface Poppable<Pop : Any>{
    fun onPopParams(params: Pop)
}