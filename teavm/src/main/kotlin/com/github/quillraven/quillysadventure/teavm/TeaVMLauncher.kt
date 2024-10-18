@file:JvmName("TeaVMLauncher")

package com.github.quillraven.quillysadventure.teavm

import com.badlogic.gdx.Application
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration
import com.github.xpenatan.gdx.backends.teavm.TeaApplication
import com.github.quillraven.quillysadventure.Main

/** Launches the TeaVM/HTML application. */
fun main() {
    val config = TeaApplicationConfiguration("canvas").apply {
        //// If width and height are each greater than 0, then the app will use a fixed size.
        //width = 640
        //height = 480
        //// If width and height are both 0, then the app will use all available space.
        //width = 0
        //height = 0
        //// If width and height are both -1, then the app will fill the canvas size.
        width = 0
        height = 0
    }
    TeaApplication(Main(logLevel = Application.LOG_DEBUG), config)
}
