package com.game.quillyjumper.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.ShaderPrograms
import com.game.quillyjumper.audio.AudioService
import com.game.quillyjumper.characterConfigurations
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.configuration.Item
import com.game.quillyjumper.configuration.ItemConfigurations
import com.game.quillyjumper.configuration.itemConfigurations
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.component.CameraLockComponent
import com.game.quillyjumper.ecs.component.PlayerComponent
import com.game.quillyjumper.ecs.system.*
import com.game.quillyjumper.event.GameEventListener
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.event.Key
import com.game.quillyjumper.input.InputListener
import com.game.quillyjumper.map.MapManager
import com.game.quillyjumper.map.MapType
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.allOf
import ktx.ashley.get

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    private val assets: AssetManager,
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService,
    private val world: World,
    private val engine: Engine,
    private val rayHandler: RayHandler,
    private val shaderPrograms: ShaderPrograms,
    private val batch: SpriteBatch,
    private val mapRenderer: OrthogonalTiledMapRenderer,
    private val box2DDebugRenderer: Box2DDebugRenderer,
    private val stage: Stage
) : KtxScreen, InputListener, GameEventListener {
    private val characterCfgCache = Gdx.app.characterConfigurations
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
                addSystem(TriggerSystem(gameEventManager))
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
                addSystem(RenderSystem(this, batch, viewport, mapRenderer, shaderPrograms))
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
        // screen needs to be an event listener to switch to game over screen when player dies
        gameEventManager.addGameEventListener(this)
    }

    override fun hide() {
        gameEventManager.removeInputListener(this)
        gameEventManager.removeMapChangeListener(engine.getSystem(RenderSystem::class.java))
        gameEventManager.removeMapChangeListener(engine.getSystem(CameraSystem::class.java))
        gameEventManager.removeMapChangeListener(audioService)
        gameEventManager.removeMapChangeListener(engine.getSystem(OutOfBoundsSystem::class.java))
        gameEventManager.removeGameEventListener(this)
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
        // TODO remove debug stuff
        when {
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> {
                engine.getSystem(RenderSystem::class.java).setNormalColor()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> {
                engine.getSystem(RenderSystem::class.java).setGrayScale()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) -> {
                engine.getSystem(RenderSystem::class.java).setSepia()
            }
        }

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

    override fun characterDeath(character: Entity) {
        if (character[PlayerComponent.mapper] != null) {
            game.setScreen<EndScreen>()
        }
    }
}
