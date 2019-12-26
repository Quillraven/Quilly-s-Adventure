package com.game.quillyjumper

import com.badlogic.gdx.Application
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

fun main() {
    val config = LwjglApplicationConfiguration().apply {
        width = 1280
        height = 720
    }
    LwjglApplication(Main(disableAudio = false), config).logLevel = Application.LOG_DEBUG
}
