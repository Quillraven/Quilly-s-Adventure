package com.game.quillyjumper.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.assets.MapAssets
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.configuration.CharacterCfgCache
import com.game.quillyjumper.configuration.Item
import com.game.quillyjumper.configuration.ItemCfgCache
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.component.PlayerComponent
import com.game.quillyjumper.ecs.item
import com.game.quillyjumper.ecs.scenery
import com.game.quillyjumper.ecs.system.*
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputListener
import ktx.app.KtxGame
import ktx.app.KtxScreen

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    private val assets: AssetManager,
    private val characterCfgCache: CharacterCfgCache,
    private val itemCfgCache: ItemCfgCache,
    private val gameEventManager: GameEventManager,
    private val inputController: InputController,
    private val audioManager: AudioManager,
    private val world: World,
    private val batch: SpriteBatch,
    private val mapRenderer: OrthogonalTiledMapRenderer,
    private val box2DDebugRenderer: Box2DDebugRenderer
) : KtxScreen, InputListener {
    private val viewport = FitViewport(32f, 18f)
    private val engine = PooledEngine().apply {
        addSystem(PhysicMoveSystem())
        addSystem(PhysicJumpSystem(audioManager))
        addSystem(PhysicSystem(world, this))
        addSystem(PlayerCollisionSystem())
        addSystem(PlayerStateSystem(inputController))
        addSystem(AnimationSystem(assets))
        addSystem(RenderSystem(batch, viewport, world, mapRenderer, box2DDebugRenderer))
    }

    override fun show() {
        // add game screen as input listener to react when the player wants to quit the game (=exit key pressed)
        gameEventManager.addInputListener(this)

        // play nice background music ;)
        audioManager.play(MusicAssets.LEVEL_1)

        // TODO remove testing stuff
        // player
        engine.character(characterCfgCache[Character.PLAYER], world, 16f, 3f, 1).apply {
            add(engine.createComponent(PlayerComponent::class.java))
        }
        // floor
        engine.scenery(world, 1f, 1f, 30f, 1f)
        engine.scenery(world, 18f, 2f, 2f, 1f)
        // enemy
        engine.character(characterCfgCache[Character.BLUE_SLIME], world, 14f, 3f)
        // item
        engine.item(itemCfgCache[Item.POTION_GAIN_LIFE], world, assets, 18.5f, 4f)
        engine.item(itemCfgCache[Item.POTION_GAIN_MANA], world, assets, 21f, 4f)
        // set map to render
        mapRenderer.map = assets[MapAssets.TEST_MAP]
    }

    override fun hide() {
        gameEventManager.removeInputListener(this)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        // update all ecs engine systems including the render system which draws stuff on the screen
        engine.update(delta)
    }

    override fun exit() {
        // player pressed exit key -> go back to menu
        game.setScreen<MenuScreen>()
    }
}