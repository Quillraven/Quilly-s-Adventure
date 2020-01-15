package com.github.quillraven.quillyjumper.input

import com.github.quillraven.quillyjumper.event.Key

interface InputListener {
    fun move(percX: Float, percY: Float) {}

    fun keyPressed(key: Key) {}

    fun keyReleased(key: Key) {}
}