package com.game.quillyjumper.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.assets.MusicAssets
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.scene2d.button
import ktx.scene2d.label
import ktx.scene2d.table

class MenuScreen(
    private val game: KtxGame<KtxScreen>,
    private val audioManager: AudioManager,
    private val stage: Stage
) : KtxScreen {
    override fun show() {
        audioManager.play(MusicAssets.MENU)

        //TODO beautify UI and extract it to separate UI package to avoid creating the same table multiple times
        stage.root.addActor(table {
            defaults().pad(5f, 5f, 5f, 5f)

            button { cell -> label("New Game"); cell.fillX().row() }.onClick { game.setScreen<GameScreen>() }
            button { cell -> label("Continue"); cell.fillX().row() }
            button { cell -> label("[X] [-] Music [+]"); cell.fillX().row() }
            button { cell -> label("[X] [-] Sound [+]"); cell.fillX().row() }
            //TODO mention Schlaubi for his awesome support throughout the entire project (BOLD AND COLORFUL TEXT)
            // also add REDRUM on soundcloud for awesome music stuff!
            // Valvoorik just because he is awesome
            button { cell -> label("Credits"); cell.fillX().row() }
            button { cell -> label("Exit"); cell.fillX().row() }.onClick { Gdx.app.exit() }
            setFillParent(true)
        })
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