package com.game.quillyjumper.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.configuration.*
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.component.CameraLockComponent
import com.game.quillyjumper.ecs.component.EntityType
import com.game.quillyjumper.ecs.component.ModelType
import com.game.quillyjumper.ecs.component.PlayerComponent
import com.game.quillyjumper.ecs.system.*
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputListener
import com.game.quillyjumper.map.MapManager
import com.game.quillyjumper.map.MapType
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.allOf

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    assets: AssetManager,
    private val gameEventManager: GameEventManager,
    inputController: InputController,
    private val audioManager: AudioManager,
    world: World,
    batch: SpriteBatch,
    mapRenderer: OrthogonalTiledMapRenderer,
    box2DDebugRenderer: Box2DDebugRenderer
) : KtxScreen, InputListener {
    private val characterCfgCache = initCharacterConfigurations()
    private val itemCfgCache = initItemConfigurations(assets)
    private val viewport = ExtendViewport(16f, 9f, 0f, 9f)
    private val engine = PooledEngine().apply {
        addSystem(PhysicMoveSystem())
        addSystem(PhysicJumpSystem(audioManager))
        addSystem(PhysicSystem(world, this))
        addSystem(PlayerCollisionSystem())
        addSystem(PlayerStateSystem(inputController))
        addSystem(AnimationSystem(assets))
        addSystem(CameraSystem(this, viewport.camera as OrthographicCamera))
        addSystem(RenderSystem(batch, viewport, world, mapRenderer, box2DDebugRenderer))
        // create player entity
        character(characterCfgCache[Character.PLAYER], world, 0f, 0f, 1) {
            with<PlayerComponent>()
            with<CameraLockComponent>()
        }
    }
    private val playerEntities = engine.getEntitiesFor(allOf(PlayerComponent::class).get())
    private val mapManager =
        MapManager(assets, world, engine, characterCfgCache, itemCfgCache, playerEntities, gameEventManager)

    override fun show() {
        // add game screen as input listener to react when the player wants to quit the game (=exit key pressed)
        gameEventManager.addInputListener(this)
        // add RenderSystem as MapChangeListener to update the mapRenderer whenever the map changes
        gameEventManager.addMapChangeListener(engine.getSystem(RenderSystem::class.java))
        // add CameraSystem as MapChangeListener to update the camera boundaries whenever the map changes
        gameEventManager.addMapChangeListener(engine.getSystem(CameraSystem::class.java))
        // add AudioManager as MapChangeListener to play the music of the map whenever it gets changed
        gameEventManager.addMapChangeListener(audioManager)
        // set map
        mapManager.setMap(MapType.TEST_MAP)
        //mapManager.setMap(MapType.TEST_MAP3x3)
    }

    override fun hide() {
        gameEventManager.removeInputListener(this)
        gameEventManager.removeMapChangeListener(engine.getSystem(RenderSystem::class.java))
        gameEventManager.removeMapChangeListener(engine.getSystem(CameraSystem::class.java))
        gameEventManager.removeMapChangeListener(audioManager)
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


    private fun initCharacterConfigurations(): CharacterConfigurations {
        return characterConfigurations {
            cfg(Character.PLAYER, EntityType.PLAYER, ModelType.PLAYER) {
                speed = 4f
                size(0.5f, 0.8f)
            }
            cfg(Character.BLUE_SLIME, EntityType.ENEMY, ModelType.BLUE_SLIME) {
                speed = 1f
                size(0.5f, 0.5f)
            }
        }
    }

    private fun initItemConfigurations(assets: AssetManager): ItemConfigurations {
        return itemConfigurations(assets) {
            cfg(Item.POTION_GAIN_LIFE, "potion_green_plus")
            cfg(Item.POTION_GAIN_MANA, "potion_blue_plus")
        }
    }
}