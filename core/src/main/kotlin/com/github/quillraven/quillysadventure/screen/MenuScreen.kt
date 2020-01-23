package com.github.quillraven.quillysadventure.screen

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.ui.MenuHUD
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxScreen

class MenuScreen(
    private val game: KtxGame<KtxScreen>,
    private val audioService: AudioService,
    private val stage: Stage
) : KtxScreen {
    private val hud: MenuHUD = MenuHUD().apply {
        newGameLabel.onClick { game.setScreen<GameScreen>() }
    }

    override fun show() {
        audioService.play(MusicAssets.MENU)
        stage.addActor(hud)
        hud.centerPosition()
    }

    override fun hide() {
        stage.clear()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        stage.viewport.apply()
        stage.act()
        stage.draw()
    }
}
