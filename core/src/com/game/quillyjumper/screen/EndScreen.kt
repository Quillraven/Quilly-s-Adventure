package com.game.quillyjumper.screen

import ktx.app.KtxGame
import ktx.app.KtxScreen

class EndScreen(private val game : KtxGame<KtxScreen>) : KtxScreen {
    override fun show() {
        println("End")
    }
}