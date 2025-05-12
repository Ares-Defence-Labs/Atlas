package com.architect.atlas.architecture.navigation

interface Pushable<Push : Any>{
    fun onPushParams(params: Push)
}