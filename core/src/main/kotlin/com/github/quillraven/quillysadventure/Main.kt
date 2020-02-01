package com.github.quillraven.quillysadventure

import box2dLight.Light
import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.quillraven.quillysadventure.assets.I18nAssets
import com.github.quillraven.quillysadventure.assets.get
import com.github.quillraven.quillysadventure.audio.DefaultAudioService
import com.github.quillraven.quillysadventure.audio.NullAudioService
import com.github.quillraven.quillysadventure.configuration.CharacterConfigurations
import com.github.quillraven.quillysadventure.configuration.ItemConfigurations
import com.github.quillraven.quillysadventure.configuration.loadCharacterConfigurations
import com.github.quillraven.quillysadventure.configuration.loadItemConfigurations
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.screen.LoadingScreen
import com.github.quillraven.quillysadventure.ui.createSkin
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.box2d.earthGravity
import ktx.inject.Context
import ktx.log.logger

private val LOG = logger<Main>()

const val UNIT_SCALE = 1 / 32f
const val FIXTURE_TYPE_FOOT_SENSOR = 1
const val FIXTURE_TYPE_AGGRO_SENSOR = 2 shl 0
// category = "I am a ..."
const val FILTER_CATEGORY_SCENERY = 0x0001.toShort()
const val FILTER_CATEGORY_GAME_OBJECT = 0x0002.toShort()
const val FILTER_CATEGORY_ITEM = 0x0004.toShort()
const val FILTER_CATEGORY_LIGHT = 0x0008.toShort()
// mask = "I will collide with ..."
const val FILTER_MASK_LIGHTS = FILTER_CATEGORY_SCENERY

fun Application.getAudioService() = (this.applicationListener as Main).audioService
val Application.game: Main
    get() = (applicationListener as Main)
val Application.world: World
    get() = game.world
val Application.ecsEngine: Engine
    get() = game.ecsEngine
val Application.gameEventManager: GameEventManager
    get() = game.gameEventManager
val Application.characterConfigurations: CharacterConfigurations
    get() = game.characterConfigurations
val Application.itemConfigurations: ItemConfigurations
    get() = game.itemConfigurations

class Main(
    private val disableAudio: Boolean = false,
    private val logLevel: Int = Application.LOG_ERROR
) :
    KtxGame<KtxScreen>() {
    private val ctx = Context()
    private val profiler by lazy { GLProfiler(Gdx.graphics).apply { enable() } }
    val audioService by lazy {
        if (disableAudio) {
            NullAudioService
        } else {
            DefaultAudioService(ctx.inject(), gameEventManager)
        }
    }
    val world by lazy { createWorld(earthGravity).apply { setContactListener(PhysicContactListener()) } }
    val ecsEngine by lazy { PooledEngine() }
    val gameEventManager by lazy { GameEventManager() }
    val characterConfigurations by lazy { loadCharacterConfigurations() }
    val itemConfigurations by lazy { loadItemConfigurations(ctx.inject()) }

    override fun create() {
        Gdx.app.logLevel = logLevel

        // init Box2D - the next call avoids some issues with older devices where the box2d libraries were not loaded correctly
        Box2D.init()

        // setup context and register stuff that should also be disposed at the end of the game lifecycle
        ctx.register {
            bindSingleton(ShaderPrograms())
            bindSingleton(SpriteBatch(2048))
            bindSingleton(AssetManager().apply {
                // we use tmx tiled maps created via the Tiled tool and therefore
                // we use the TmxMapLoader for our assetmanager to be able to
                // load/unload .tmx files
                setLoader(TiledMap::class.java, TmxMapLoader(fileHandleResolver))
            })
            bindSingleton(Stage(FitViewport(1280f, 720f), ctx.inject<SpriteBatch>()))
            bindSingleton(createSkin(ctx.inject()))
            bindSingleton(RayHandler(world))
            bindSingleton(Box2DDebugRenderer())
            bindSingleton(OrthogonalTiledMapRenderer(null, UNIT_SCALE, ctx.inject<SpriteBatch>()))
        }

        // we need a multiplexer to react on the following input events
        // UI widget --> Stage
        // keyboard --> InputProcessor (GameEventManager)
        Gdx.input.inputProcessor = InputMultiplexer(gameEventManager, ctx.inject<Stage>())

        // box2d light should not create shadows for dynamic game objects
        Light.setGlobalContactFilter(FILTER_CATEGORY_LIGHT, 0, FILTER_MASK_LIGHTS)

        // initial screen is the loading screen which is loading all assets for the game
        addScreen(
            LoadingScreen(
                this, // game instance to switch screens
                ctx.inject<AssetManager>()[I18nAssets.DEFAULT],
                ctx.inject(), // stage
                ctx.inject(), // assets
                gameEventManager, // game event manager
                audioService,
                world, // physic world
                ecsEngine, // entity component engine
                ctx.inject(), // ray handler
                ctx.inject(), // shaders
                ctx.inject(), // sprite batch
                ctx.inject(), // tiled map renderer
                ctx.inject() // box2d debug renderer
            )
        )
        setScreen<LoadingScreen>()
    }

    override fun render() {
        profiler.reset()
        super.render()
    }

    override fun dispose() {
        LOG.debug { "Draw calls: ${profiler.drawCalls}" }
        LOG.debug { "Texture bindings: ${profiler.textureBindings}" }
        LOG.debug { "Sprites in batch: ${ctx.inject<SpriteBatch>().maxSpritesInBatch}" }

        // dispose all disposables which are mostly part of our context
        world.dispose()
        ctx.dispose()
    }
}
