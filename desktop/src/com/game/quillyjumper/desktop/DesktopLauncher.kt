package com.game.quillyjumper.desktop

import com.badlogic.gdx.Application
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.game.quillyjumper.Main

fun main() {
    val config = LwjglApplicationConfiguration().apply {
        width = 1280
        height = 720
    }
    LwjglApplication(Main(), config).logLevel = Application.LOG_DEBUG
}
