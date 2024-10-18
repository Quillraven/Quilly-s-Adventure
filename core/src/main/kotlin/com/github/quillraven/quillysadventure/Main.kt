package com.github.quillraven.quillysadventure

import box2dLight.Light
import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
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
import com.github.quillraven.quillysadventure.assets.TextureAtlasAssets
import com.github.quillraven.quillysadventure.assets.get
import com.github.quillraven.quillysadventure.assets.load
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
import ktx.app.clearScreen
import ktx.box2d.createWorld
import ktx.box2d.earthGravity
import ktx.graphics.use
import ktx.inject.Context
import ktx.inject.register
import ktx.log.logger

private val LOG = logger<Main>()

const val VIRTUAL_W = 1280
const val VIRTUAL_H = 720
private const val PREF_NAME = "quilly-adventure"
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

val Application.game: Main
    get() = (applicationListener as Main)

fun Application.getAudioService() = game.audioService
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
val Application.preferences: Preferences
    get() = game.preferences

fun Batch.drawTransitionFBOs(prevFBO: FrameBuffer, nextFBO: FrameBuffer, alpha: Float) {
    clearScreen(0f, 0f, 0f, 1f)
    // render the buffers without any special viewport because they contain a
    // 1:1 pixel matching texture for the entire screen
    Gdx.gl20.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
    use {
        it.projectionMatrix = it.projectionMatrix.idt()
        it.setColor(1f, 1f, 1f, 1f - alpha)
        it.draw(prevFBO.colorBufferTexture, -1f, 1f, 2f, -2f)
        it.setColor(1f, 1f, 1f, alpha)
        it.draw(nextFBO.colorBufferTexture, -1f, 1f, 2f, -2f)
    }
}

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
    val preferences: Preferences by lazy { Gdx.app.getPreferences(PREF_NAME) }

    private lateinit var currentFrameBuffer: FrameBuffer
    private lateinit var nextFrameBuffer: FrameBuffer
    private var transitionScreens = false
    private val maxTransitionTime = 1f
    private var transitionTime = maxTransitionTime

    override fun create() {
        Gdx.app.logLevel = logLevel

        currentFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, VIRTUAL_W, VIRTUAL_H, false)
        nextFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, VIRTUAL_W, VIRTUAL_H, false)

        // init Box2D - the next call avoids some issues with older devices where the box2d libraries were not loaded correctly
        Box2D.init()

        val assetManager = AssetManager().apply {
            // we use tmx tiled maps created via the Tiled tool and therefore
            // we use the TmxMapLoader to be able to load/unload .tmx files
            setLoader(TiledMap::class.java, TmxMapLoader(fileHandleResolver))
        }
        // load textures for skin
        assetManager.load(TextureAtlasAssets.UI)
        assetManager.load(I18nAssets.DEFAULT)
        assetManager.finishLoading()

        // setup context and register stuff that should also be disposed at the end of the game lifecycle
        ctx.register {
            bindSingleton(ShaderPrograms())
            bindSingleton(SpriteBatch(2048))
            bindSingleton(assetManager)
            bindSingleton(Stage(FitViewport(VIRTUAL_W.toFloat(), VIRTUAL_H.toFloat()), ctx.inject<SpriteBatch>()))
            bindSingleton(createSkin(assetManager[TextureAtlasAssets.UI]))
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
                this@Main, // game instance to switch screens
                assetManager[I18nAssets.DEFAULT],
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

    override fun <Type : KtxScreen> setScreen(type: Class<Type>) {
        if (transitionScreens) return

        transitionScreens = true

        // render current screen to FBO
        currentFrameBuffer.bind()
        clearScreen(0f, 0f, 0f, 1f)
        currentScreen.render(1 / 30f)

        // change screen
        super.setScreen(type)

        // render next screen to FBO
        nextFrameBuffer.bind()
        clearScreen(0f, 0f, 0f, 1f)
        currentScreen.render(1 / 30f)

        // return to original framebuffer for rendering
        FrameBuffer.unbind()
        transitionScreens = false
        transitionTime = 0f
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        currentFrameBuffer.dispose()
        currentFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, width, height, false)
        nextFrameBuffer.dispose()
        nextFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, width, height, false)
    }

    override fun render() {
        profiler.reset()
        if (transitionTime < maxTransitionTime) {
            // mix previous and current screen snapshot together
            // screenshots are taken within setScreen method
            transitionTime += Gdx.graphics.deltaTime
            ctx.inject<SpriteBatch>()
                .drawTransitionFBOs(currentFrameBuffer, nextFrameBuffer, transitionTime / maxTransitionTime)
        } else {
            // no screen transition -> render current active screen
            super.render()
        }
    }

    override fun dispose() {
        LOG.debug { "Draw calls: ${profiler.drawCalls}" }
        LOG.debug { "Texture bindings: ${profiler.textureBindings}" }
        LOG.debug { "Sprites in batch: ${ctx.inject<SpriteBatch>().maxSpritesInBatch}" }

        // dispose all disposables which are mostly part of our context
        currentFrameBuffer.dispose()
        nextFrameBuffer.dispose()
        world.dispose()
        ctx.dispose()
        super.dispose()
    }
}
