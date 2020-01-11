package com.game.quillyjumper

import com.badlogic.gdx.Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setWindowSizeLimits(1280, 720, -1, -1)
        setTitle("Quilly Jumper")
    }
    Lwjgl3Application(Main(disableAudio = false), config).logLevel = Application.LOG_DEBUG
}
