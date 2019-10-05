package com.game.quillyjumper.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ai.DefaultEnemyState
import com.game.quillyjumper.ai.PlayerState
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
import ktx.scene2d.Scene2DSkin

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    private val assets: AssetManager,
    private val gameEventManager: GameEventManager,
    private val inputController: InputController,
    private val audioManager: AudioManager,
    private val world: World,
    private val rayHandler: RayHandler,
    private val batch: SpriteBatch,
    private val mapRenderer: OrthogonalTiledMapRenderer,
    private val box2DDebugRenderer: Box2DDebugRenderer,
    private val stage: Stage
) : KtxScreen, InputListener {
    private val characterCfgCache = initCharacterConfigurations()
    private val itemCfgCache = initItemConfigurations(assets)
    private val viewport = ExtendViewport(16f, 9f, 0f, 9f)
    private val engine = PooledEngine()
    private val playerEntities = engine.getEntitiesFor(allOf(PlayerComponent::class).get())
    private val mapManager =
        MapManager(
            assets,
            world,
            rayHandler,
            engine,
            characterCfgCache,
            itemCfgCache,
            playerEntities,
            gameEventManager
        )

    override fun show() {
        if (engine.systems.size() == 0) {
            // initialize engine
            engine.apply {
                addSystem(PhysicMoveSystem())
                addSystem(PhysicJumpSystem())
                addSystem(AttackSystem(world))
                addSystem(DamageSystem(Scene2DSkin.defaultSkin.getFont("defaultFont")))
                addSystem(DeathSystem())
                addSystem(PhysicSystem(world, this))
                addSystem(PlayerCollisionSystem(mapManager))
                addSystem(AggroSystem())
                addSystem(PlayerInputSystem(inputController))
                addSystem(StateSystem())
                addSystem(AnimationSystem(assets, audioManager))
                addSystem(CameraSystem(this, viewport.camera as OrthographicCamera))
                addSystem(ParticleSystem(assets))
                addSystem(RenderSystem(this, batch, viewport, world, mapRenderer, box2DDebugRenderer))
                addSystem(LightSystem(rayHandler, viewport.camera as OrthographicCamera))
                addSystem(FloatingTextSystem(batch, viewport, stage.viewport))
                addSystem(RemoveSystem(this))
                // create player entity
                character(
                    characterCfgCache[Character.PLAYER],
                    world,
                    0f,
                    0f,
                    1
                ) {
                    with<PlayerComponent>()
                    with<CameraLockComponent>()
                }
            }
        }

        // add game screen as input listener to react when the player wants to quit the game (=exit key pressed)
        gameEventManager.addInputListener(this)
        // add RenderSystem as MapChangeListener to update the mapRenderer whenever the map changes
        gameEventManager.addMapChangeListener(engine.getSystem(RenderSystem::class.java))
        // add CameraSystem as MapChangeListener to update the camera boundaries whenever the map changes
        gameEventManager.addMapChangeListener(engine.getSystem(CameraSystem::class.java))
        // add AudioManager as MapChangeListener to play the music of the map whenever it gets changed
        gameEventManager.addMapChangeListener(audioManager)
        // set map
        mapManager.setMap(MapType.MAP1)
    }

    override fun hide() {
        gameEventManager.removeInputListener(this)
        gameEventManager.removeMapChangeListener(engine.getSystem(RenderSystem::class.java))
        gameEventManager.removeMapChangeListener(engine.getSystem(CameraSystem::class.java))
        gameEventManager.removeMapChangeListener(audioManager)
    }

    override fun resize(width: Int, height: Int) {
        if (width != viewport.screenWidth || height != viewport.screenHeight) {
            rayHandler.resizeFBO(width / 4, height / 4)
        }
        viewport.update(width, height, true)
        rayHandler.useCustomViewport(viewport.screenX, viewport.screenY, viewport.screenWidth, viewport.screenHeight)
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
                attackRange = 0.4f
                attackCooldown = 1f
                damage = 6f
                life = 80f
                armor = 2f
                defaultState = PlayerState.IDLE
            }
            cfg(Character.BLUE_SLIME, EntityType.ENEMY, ModelType.BLUE_SLIME) {
                speed = 0.3f
                size(0.5f, 0.5f)
                attackRange = 0.15f
                attackCooldown = 2f
                damage = 2f
                life = 10f
                defaultState = DefaultEnemyState.IDLE
                aggroRange = 2.5f
            }
            cfg(Character.FLIPPY, EntityType.OTHER, ModelType.FLIPPY) {
                size(0.65f, 2f)
                collisionBodyOffset(3f * UNIT_SCALE, 0f)
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