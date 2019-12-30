package com.game.quillyjumper.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ability.FireballEffect
import com.game.quillyjumper.ability.SpinEffect
import com.game.quillyjumper.ai.DefaultEnemyState
import com.game.quillyjumper.ai.MinotaurState
import com.game.quillyjumper.ai.PlayerState
import com.game.quillyjumper.audio.AudioService
import com.game.quillyjumper.configuration.*
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.component.CameraLockComponent
import com.game.quillyjumper.ecs.component.EntityType
import com.game.quillyjumper.ecs.component.ModelType
import com.game.quillyjumper.ecs.component.PlayerComponent
import com.game.quillyjumper.ecs.system.*
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.event.Key
import com.game.quillyjumper.input.InputListener
import com.game.quillyjumper.map.MapManager
import com.game.quillyjumper.map.MapType
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.allOf

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    private val assets: AssetManager,
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService,
    private val world: World,
    private val engine: Engine,
    private val rayHandler: RayHandler,
    private val batch: SpriteBatch,
    private val mapRenderer: OrthogonalTiledMapRenderer,
    private val box2DDebugRenderer: Box2DDebugRenderer,
    private val stage: Stage
) : KtxScreen, InputListener {
    private val characterCfgCache = initCharacterConfigurations()
    private val itemCfgCache = initItemConfigurations(assets)
    private val viewport = FitViewport(16f, 9f)
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
                addSystem(TriggerSystem())
                addSystem(PhysicMoveSystem())
                addSystem(PhysicJumpSystem())
                addSystem(AbilitySystem())
                addSystem(AttackSystem(world))
                addSystem(DealDamageSystem())
                addSystem(TakeDamageSystem())
                addSystem(DeathSystem(audioService, gameEventManager))
                // out of bounds system must be before PhysicSystem because it deals damage to the player
                // and in order for the TakeDamageSystem to show the damage indicator at the correct location
                // we need to run through the PhysicSystem once to update the TransformComponent accordingly
                addSystem(OutOfBoundsSystem(gameEventManager))
                // player collision system must be before PhysicSystem because whenever the player collides
                // with a portal then its body location gets transformed and we need the physic system
                // to correctly update the TransformComponent which is e.g. used in the OutOfBoundsSystem
                addSystem(PlayerCollisionSystem(mapManager, audioService, gameEventManager))
                addSystem(PhysicSystem(world, this))
                addSystem(PlayerInputSystem(gameEventManager, this))
                addSystem(StateSystem())
                addSystem(AnimationSystem(assets, audioService))
                addSystem(CameraSystem(this, viewport.camera as OrthographicCamera))
                addSystem(ParticleSystem(assets, audioService))
                addSystem(FadeSystem())
                addSystem(RenderSystem(this, batch, viewport, mapRenderer))
                addSystem(RenderPhysicDebugSystem(world, viewport, box2DDebugRenderer))
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
        // add AudioService as MapChangeListener to play the music of the map whenever it gets changed
        gameEventManager.addMapChangeListener(audioService)
        // add OutOfBoundsSystem as MapChangeListener to update the boundaries of the world whenver the map changes
        gameEventManager.addMapChangeListener(engine.getSystem(OutOfBoundsSystem::class.java))
        // set initial map
        mapManager.setMap(MapType.MAP1)
    }

    override fun hide() {
        gameEventManager.removeInputListener(this)
        gameEventManager.removeMapChangeListener(engine.getSystem(RenderSystem::class.java))
        gameEventManager.removeMapChangeListener(engine.getSystem(CameraSystem::class.java))
        gameEventManager.removeMapChangeListener(audioService)
        gameEventManager.removeMapChangeListener(engine.getSystem(OutOfBoundsSystem::class.java))
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        if (width != stage.viewport.screenWidth || height != stage.viewport.screenHeight) {
            rayHandler.resizeFBO(width / 4, height / 4)
        }
        viewport.update(width, height, true)
        rayHandler.useCustomViewport(viewport.screenX, viewport.screenY, viewport.screenWidth, viewport.screenHeight)
    }

    override fun render(delta: Float) {
        // update all ecs engine systems including the render system which draws stuff on the screen
        engine.update(delta)
        // update audio manager to play any queued sound effects
        audioService.update()
    }

    override fun keyPressed(key: Key) {
        if (key == Key.EXIT) {
            // player pressed exit key -> go back to menu
            game.setScreen<MenuScreen>()
        }
    }

    private fun initCharacterConfigurations(): CharacterConfigurations {
        return characterConfigurations {
            cfg(Character.PLAYER, EntityType.PLAYER, ModelType.PLAYER) {
                speed = 4f
                size(0.5f, 0.8f)
                attackRange = 0.4f
                attackCooldown = 1f
                damage = 6f
                life = 40f
                mana = 10f
                armor = 2f
                defaultState = PlayerState.IDLE
                abilities.add(FireballEffect)
            }
            cfg(Character.BLUE_SLIME, EntityType.ENEMY, ModelType.BLUE_SLIME) {
                speed = 0.3f
                size(0.5f, 0.5f)
                attackRange = 0.15f
                damageDelay = 0.25f
                attackCooldown = 2f
                damage = 2f
                life = 10f
                defaultState = DefaultEnemyState.IDLE
                aggroRange = 2.5f
                xp = 10
            }
            cfg(Character.ORANGE_SLIME, EntityType.ENEMY, ModelType.ORANGE_SLIME) {
                speed = 0.5f
                size(0.4f, 0.4f)
                attackRange = 0.1f
                attackCooldown = 1.5f
                damage = 3f
                life = 5f
                defaultState = DefaultEnemyState.IDLE
                aggroRange = 3f
                xp = 15
            }
            cfg(Character.DWARF, EntityType.ENEMY, ModelType.DWARF) {
                speed = 0.6f
                size(0.5f, 0.6f)
                attackRange = 0.2f
                damageDelay = 0.45f
                attackCooldown = 1.5f
                damage = 3f
                life = 10f
                defaultState = DefaultEnemyState.IDLE
                aggroRange = 3f
                xp = 20
            }
            cfg(Character.FLIPPY, EntityType.NPC, ModelType.FLIPPY) {
                size(0.65f, 2f)
                collisionBodyOffset(3f * UNIT_SCALE, 0f)
            }
            cfg(Character.SAVE_POINT, EntityType.SAVE_POINT, ModelType.EYE_MONSTER)
            cfg(Character.MINOTAUR, EntityType.ENEMY, ModelType.MINOTAUR) {
                speed = 0.90f
                size(0.7f, 1.2f)
                attackRange = 0.7f
                damageDelay = 0.3f
                attackCooldown = 5f
                damage = 5f
                life = 50f
                defaultState = MinotaurState.IDLE
                aggroRange = 10f
                xp = 100
                abilities.add(SpinEffect)
            }
            cfg(Character.SKELETAL, EntityType.ENEMY, ModelType.SKELETAL) {
                speed = 0.6f
                size(0.4f, 0.8f)
                collisionBodyOffset(-3f * UNIT_SCALE, 0f)
                attackRange = 0.6f
                attackCooldown = 3.5f
                damage = 4f
                damageDelay = 0.7f
                life = 23f
                defaultState = DefaultEnemyState.IDLE
                aggroRange = 10f
                xp = 40
            }
        }
    }

    private fun initItemConfigurations(assets: AssetManager): ItemConfigurations {
        return itemConfigurations(assets) {
            cfg(Item.POTION_GAIN_LIFE, "potion_green_plus") {
                lifeBonus = 10
            }
            cfg(Item.POTION_GAIN_MANA, "potion_blue_plus") {
                manaBonus = 3
            }
        }
    }
}
