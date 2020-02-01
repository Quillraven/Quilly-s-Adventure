package com.github.quillraven.quillysadventure.input

import com.github.quillraven.quillysadventure.event.Key

interface InputListener {
    fun move(percX: Float, percY: Float) {}

    fun keyPressed(key: Key) {}

    fun keyReleased(key: Key) {}
}
