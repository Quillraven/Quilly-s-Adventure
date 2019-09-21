package com.game.quillyjumper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.KeyboardEventDispatcher
import com.game.quillyjumper.screen.LoadingScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.box2d.earthGravity
import ktx.freetype.generateFont
import ktx.inject.Context
import ktx.log.logger
import ktx.scene2d.Scene2DSkin
import ktx.style.button
import ktx.style.label
import ktx.style.skin

private val LOG = logger<Main>()

const val UNIT_SCALE = 1 / 32f
const val FIXTURE_TYPE_FOOT_SENSOR = 2

class Main : KtxGame<KtxScreen>() {
    private val ctx = Context()
    private val profiler by lazy { GLProfiler(Gdx.graphics).apply { enable() } }

    override fun create() {
        // init Box2D - the next call avoids some issues with older devices where the box2d libraries were not loaded correctly
        Box2D.init()

        // setup context and register stuff that should also be disposed at the end of the game lifecycle
        ctx.register {
            bindSingleton(SpriteBatch(2048))
            bindSingleton(AssetManager().apply {
                // we use tmx tiled maps created via the Tiled tool and therefore
                // we use the TmxMapLoader for our assetmanager to be able to
                // load/unload .tmx files
                setLoader(TiledMap::class.java, TmxMapLoader(fileHandleResolver))
            })
            bindSingleton(AudioManager(ctx.inject()))
            bindSingleton(GameEventManager())
            // register keyboard event dispatcher after we initiate the game event manager
            // because the event dispatcher is using the game event manager to dispatch input events
            bindSingleton<InputProcessor>(KeyboardEventDispatcher(ctx.inject()))
            bindSingleton(InputController().apply { ctx.inject<GameEventManager>().addInputListener(this) })
            bindSingleton(Stage(FitViewport(1280f, 720f), ctx.inject<SpriteBatch>()))
            bindSingleton(createSKin())
            bindSingleton(createWorld(earthGravity).apply { setContactListener(PhysicContactListener()) })
            bindSingleton(Box2DDebugRenderer())
            bindSingleton(OrthogonalTiledMapRenderer(null, UNIT_SCALE, ctx.inject<SpriteBatch>()))
        }

        // we need a multiplexer to react on the following input events
        // UI widget --> Stage
        // keyboard --> InputProcessor (KeyboardEventDispatcher)
        Gdx.input.inputProcessor = InputMultiplexer(ctx.inject(), ctx.inject<Stage>())
        // set our created skin as the default skin for scene2d stuff
        Scene2DSkin.defaultSkin = ctx.inject()

        // initial screen is the loading screen which is loading all assets for the game
        addScreen(
            LoadingScreen(
                this, // game instance to switch screens
                ctx.inject(), // stage
                ctx.inject(), // assets
                ctx.inject(), // game event manager
                ctx.inject(), // input controller
                ctx.inject(), // audio manager
                ctx.inject(), // physic world
                ctx.inject(), // sprite batch
                ctx.inject(), // tiled map renderer
                ctx.inject() // box2d debug renderer
            )
        )
        setScreen<LoadingScreen>()
    }

    private fun createSKin(): Skin {
        // generate fonts for the skin
        val generator = FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"))
        val defaultFont = generator.generateFont { size = 24 }
        // dispose font generator after creating all .ttf fonts that we need
        generator.dispose()

        // generate skin
        return skin { skin ->
            // add generated fonts as resources to the skin to correctly dispose them
            add("defaultFont", defaultFont)

            // default label style
            label { font = skin.getFont("defaultFont") }
            // default button style
            button { }
        }
    }

    override fun render() {
        profiler.reset()
        super.render()
    }

    override fun dispose() {
        LOG.debug { "Draw calls: ${profiler.drawCalls}" }
        LOG.debug { "Texture bindings: ${profiler.textureBindings}" }
        LOG.debug { "Sprites in batch: ${ctx.inject<SpriteBatch>().maxSpritesInBatch}" }

        // dispose all disposables which are part of our context
        ctx.dispose()
    }
}
