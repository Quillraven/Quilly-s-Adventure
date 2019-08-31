package com.game.quillyjumper.screen

import ktx.app.KtxGame
import ktx.app.KtxScreen

class EndScreen(private val game: KtxGame<KtxScreen>) : KtxScreen {
    private var timer = 0f

    override fun show() {
        println("End")
        timer = 0f
    }

    override fun render(delta: Float) {
        timer += delta
        if (timer >= 5f) {
            game.setScreen<MenuScreen>()
        }
    }
}