package com.github.quillraven.quillysadventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.audioVolumeWidget
import com.github.quillraven.quillysadventure.ui.get
import ktx.actors.centerPosition
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.button
import ktx.scene2d.label
import ktx.scene2d.table

class MenuScreen(
    private val game: KtxGame<KtxScreen>,
    private val audioService: AudioService,
    private val stage: Stage
) : KtxScreen {
    override fun show() {
        audioService.play(MusicAssets.MENU)

        //TODO beautify UI and extract it to separate UI package to avoid creating the same table multiple times
        stage.root.addActor(Image(Scene2DSkin.defaultSkin[Images.DIALOG_LIGHT]).apply {
            centerPosition(this@MenuScreen.stage.width, this@MenuScreen.stage.height)
        })
        stage.root.addActor(
            table {
                defaults().pad(5f, 5f, 5f, 5f)

                button { cell ->
                    label("New Game"); cell.fillX().padLeft(60f).row()
                }.onClick { game.setScreen<GameScreen>() }
                button { cell -> label("Continue"); cell.fillX().padLeft(60f).row() }
                audioVolumeWidget("Music") { cell ->
                    checkBox.onChange { println("${checkBox.isChecked}") }
                    audioReduceButton.onClick { println("reduce") }
                    audioIncreaseButton.onClick { println("increase") }
                    cell.fillX().row()
                }
                audioVolumeWidget("Sound") { cell -> cell.fillX().row() }
                //TODO mention Schlaubi for his awesome support throughout the entire project (BOLD AND COLORFUL TEXT)
                // also add REDRUM on soundcloud for awesome music stuff!
                // Valvoorik just because he is awesome
                button { cell -> label("Credits"); cell.fillX().padLeft(60f).row() }
                button { cell -> label("Exit"); cell.fillX().padLeft(60f).row() }.onClick { Gdx.app.exit() }
                setFillParent(true)
                pack()
            }
        )
    }

    override fun hide() {
        stage.root.clear()
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
