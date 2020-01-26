package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.quillraven.quillysadventure.ShaderPrograms
import com.github.quillraven.quillysadventure.assets.*
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.characterConfigurations
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.character
import com.github.quillraven.quillysadventure.ecs.component.CameraLockComponent
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.system.*
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.itemConfigurations
import com.github.quillraven.quillysadventure.map.MapManager
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.allOf

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
            val gameViewport = FitViewport(16f, 9f)
            val charCfgs = Gdx.app.characterConfigurations
            val bundle = assets[I18nAssets.DEFAULT]
            // create map manager that is used for main menu and game
            val mapManager = MapManager(
                assets,
                world,
                rayHandler,
                ecsEngine,
                charCfgs,
                Gdx.app.itemConfigurations,
                ecsEngine.getEntitiesFor(allOf(PlayerComponent::class).get()),
                gameEventManager
            )
            // create engine systems so that we have everything that we need for the main menu and game to render the characters, etc.
            ecsEngine.run {
                addSystem(TriggerSystem(gameEventManager))
                addSystem(PhysicMoveSystem())
                // facing system must come after move system because facing is set within move system
                addSystem(FacingSystem())
                addSystem(PhysicJumpSystem())
                addSystem(AbilitySystem(gameEventManager))
                addSystem(AttackSystem(world, gameEventManager))
                addSystem(DealDamageSystem())
                addSystem(TakeDamageSystem(gameEventManager))
                addSystem(DeathSystem(audioService, gameEventManager))
                // out of bounds system must be before PhysicSystem because it transforms the player's body
                // and we need to run through the PhysicSystem once to update the TransformComponent accordingly
                addSystem(OutOfBoundsSystem(gameEventManager))
                // player collision system must be before PhysicSystem because whenever the player collides
                // with a portal then its body location gets transformed and we need the physic system
                // to correctly update the TransformComponent which is e.g. used in the OutOfBoundsSystem
                addSystem(PlayerCollisionSystem(mapManager, audioService, gameEventManager))
                // heal system must come after take damage, death and collision system because all of these
                // systems are creating healcomponents internally in some cases that need to be considered
                // in the same frame
                addSystem(HealSystem(gameEventManager))
                addSystem(PhysicSystem(world, this))
                addSystem(PlayerInputSystem(gameEventManager, this))
                addSystem(StateSystem())
                addSystem(AnimationSystem(assets, audioService))
                addSystem(CameraSystem(this, gameEventManager, gameViewport.camera as OrthographicCamera))
                addSystem(ParticleSystem(assets, audioService))
                addSystem(FadeSystem())
                addSystem(RenderSystem(this, gameEventManager, batch, gameViewport, mapRenderer, shaderPrograms))
                addSystem(RenderPhysicDebugSystem(world, gameViewport, box2DDebugRenderer))
                addSystem(LightSystem(rayHandler, gameViewport.camera as OrthographicCamera))
                addSystem(FloatingTextSystem(batch, gameViewport, stage.viewport))
                addSystem(RemoveSystem(this))
            }
            // create player entity that is shown in the menu and used in the game
            ecsEngine.character(charCfgs[Character.PLAYER], world, 0f, 0f, 1) {
                with<PlayerComponent>()
                with<CameraLockComponent>()
            }
            // all assets are loaded -> add remaining screens to our game now because
            // now they can access the different assets that they need
            game.addScreen(
                GameScreen(
                    game,
                    bundle,
                    gameEventManager,
                    audioService,
                    ecsEngine,
                    mapManager,
                    rayHandler,
                    gameViewport,
                    stage
                )
            )
            game.addScreen(
                MenuScreen(
                    game,
                    ecsEngine,
                    mapManager,
                    gameEventManager,
                    audioService,
                    rayHandler,
                    gameViewport,
                    stage,
                    bundle
                )
            )
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
