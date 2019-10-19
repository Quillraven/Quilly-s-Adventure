package com.game.quillyjumper.input

import com.game.quillyjumper.event.Key

interface InputListener {
    fun move(percX: Float, percY: Float) {}

    fun keyPressed(key: Key) {}

    fun keyReleased(key: Key) {}
}