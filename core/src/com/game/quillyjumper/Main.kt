package com.game.quillyjumper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScreenViewport
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
import ktx.scene2d.Scene2DSkin
import ktx.style.button
import ktx.style.label
import ktx.style.skin

const val UNIT_SCALE = 1 / 32f
const val FIXTURE_TYPE_FOOT_SENSOR = 2

class Main : KtxGame<KtxScreen>() {
    private val ctx = Context()

    override fun create() {
        // init Box2D - the next call avoids some issues with older devices where the box2d libraries were not loaded correctly
        Box2D.init()

        // setup context and register stuff that should also be disposed at the end of the game lifecycle
        ctx.register {
            bindSingleton(SpriteBatch())
            bindSingleton(AssetManager())
            bindSingleton(AudioManager(ctx.inject()))
            bindSingleton(GameEventManager())
            // register keyboard event dispatcher after we initiate the game event manager
            // because the event dispatcher is using the game event manager to dispatch input events
            bindSingleton<InputProcessor>(KeyboardEventDispatcher(ctx.inject()))
            bindSingleton(InputController().apply { ctx.inject<GameEventManager>().addInputListener(this) })
            bindSingleton(Stage(ScreenViewport(), ctx.inject<SpriteBatch>()))
            bindSingleton(createSKin())
            bindSingleton(createWorld(earthGravity).apply { setContactListener(PhysicContactListener()) })
            bindSingleton(Box2DDebugRenderer())
        }

        // we need a multiplexer to react on the following input events
        // UI widget --> Stage
        // keyboard --> InputProcessor (KeyboardEventDispatcher)
        Gdx.input.inputProcessor = InputMultiplexer(ctx.inject(), ctx.inject<Stage>())
        // set our created skin as the default skin for scene2d stuff
        Scene2DSkin.defaultSkin = ctx.inject()

        // initial screen is the loading screen which is loading all assets for the game
        addScreen(LoadingScreen(this, ctx.inject(), ctx.inject(), ctx.inject(), ctx.inject(), ctx.inject(), ctx.inject(), ctx.inject()))
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

    override fun dispose() {
        // dispose all disposables which are part of our context
        ctx.dispose()
    }
}
