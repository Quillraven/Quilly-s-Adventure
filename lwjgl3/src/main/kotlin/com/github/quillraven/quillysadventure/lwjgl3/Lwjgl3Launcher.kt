@file:JvmName("Lwjgl3Launcher")

package com.github.quillraven.quillysadventure.lwjgl3

import com.badlogic.gdx.Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.github.quillraven.quillysadventure.Main
import com.github.quillraven.quillysadventure.VIRTUAL_H
import com.github.quillraven.quillysadventure.VIRTUAL_W

/** Launches the desktop (LWJGL3) application. */
fun main() {
    // This handles macOS support and helps on Windows.
    if (StartupHelper.startNewJvmIfRequired())
        return
    Lwjgl3Application(
        Main(disableAudio = false, logLevel = Application.LOG_DEBUG),
        Lwjgl3ApplicationConfiguration().apply {
            setTitle("Quilly's Adventure")
            setWindowSizeLimits(VIRTUAL_W, VIRTUAL_H, -1, -1)

            setWindowIcon(*(arrayOf(144, 96, 72, 48).map { "ic_launcher_$it.png" }.toTypedArray()))
        })
}
