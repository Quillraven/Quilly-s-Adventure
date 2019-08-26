package com.game.quillyjumper.screen

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.MusicAssets
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.load
import ktx.app.KtxGame
import ktx.app.KtxScreen

class LoadingScreen(private val game: KtxGame<KtxScreen>,
                    private val stage: Stage,
                    private val assets: AssetManager,
                    private val inputController: InputController,
                    private val audioManager: AudioManager,
                    private val world: World,
                    private val batch: SpriteBatch,
                    private val box2DDebugRenderer: Box2DDebugRenderer) : KtxScreen {
    override fun show() {
        MusicAssets.values().forEach { assets.load(it) }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        if (assets.update()) {
            // all assets are loaded -> add remaining screens to our game now because
            // now they can access the different assets that they need
            game.addScreen(GameScreen(game, inputController, audioManager, world, batch, box2DDebugRenderer))
            game.addScreen(MenuScreen(game, audioManager, stage))
            game.addScreen(EndScreen(game))
            // go to the menu screen once everything is loaded
            game.setScreen<MenuScreen>()
            // cleanup loading screen stuff
            game.removeScreen<LoadingScreen>()
            dispose()
        }

        // render UI stuff
        stage.viewport.apply()
        stage.act()
        stage.draw()
    }
}