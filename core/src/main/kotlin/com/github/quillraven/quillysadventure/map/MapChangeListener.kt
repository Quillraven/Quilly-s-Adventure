package com.github.quillraven.quillysadventure.map

interface MapChangeListener {
    fun beforeMapChange() = Unit
    fun mapChange(newMap: Map) = Unit
}
