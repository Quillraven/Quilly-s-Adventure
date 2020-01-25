package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.quillysadventure.ShaderPrograms
import com.github.quillraven.quillysadventure.assets.*
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.app.KtxGame
import ktx.app.KtxScreen

class LoadingScreen(
    private val game: KtxGame<KtxScreen>,
    private val stage: Stage,
    private val assets: AssetManager,
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService,
    private val world: World,
    private val ecsEngine: Engine,
    private val rayHandler: RayHandler,
    private val shaderPrograms: ShaderPrograms,
    private val batch: SpriteBatch,
    private val mapRenderer: OrthogonalTiledMapRenderer,
    private val box2DDebugRenderer: Box2DDebugRenderer
) : KtxScreen {
    override fun show() {
        // queue all assets that should be loaded
        MusicAssets.values().forEach { assets.load(it) }
        SoundAssets.values().forEach { if (it != SoundAssets.UNKNOWN) assets.load(it) }
        TextureAtlasAssets.values().forEach { if (it != TextureAtlasAssets.UI) assets.load(it) }
        MapAssets.values().forEach { assets.load(it) }
        val particleParam = ParticleEffectLoader.ParticleEffectParameter()
        particleParam.atlasFile = TextureAtlasAssets.GAME_OBJECTS.filePath
        ParticleAssets.values().forEach { assets.load(it, particleParam) }
        assets.load(I18nAssets.DEFAULT)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        if (assets.update()) {
            // all assets are loaded -> add remaining screens to our game now because
            // now they can access the different assets that they need
            game.addScreen(
                GameScreen(
                    game,
                    assets,
                    gameEventManager,
                    audioService,
                    world,
                    ecsEngine,
                    rayHandler,
                    shaderPrograms,
                    batch,
                    mapRenderer,
                    box2DDebugRenderer,
                    stage
                )
            )
            game.addScreen(MenuScreen(game, audioService, stage, assets))
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
