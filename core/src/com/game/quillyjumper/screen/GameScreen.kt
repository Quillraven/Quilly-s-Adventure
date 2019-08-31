package com.game.quillyjumper.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.gameObject
import com.game.quillyjumper.ecs.system.*
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey
import com.game.quillyjumper.input.InputListener
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    private val assets: AssetManager,
    private val gameEventManager: GameEventManager,
    private val inputController: InputController,
    private val audioManager: AudioManager,
    private val world: World,
    private val batch: SpriteBatch,
    private val box2DDebugRenderer: Box2DDebugRenderer
) : KtxScreen, InputListener {
    private val viewport = FitViewport(32f, 18f)
    private val engine = PooledEngine().apply {
        addSystem(PhysicMoveSystem())
        addSystem(PhysicJumpSystem(audioManager))
        addSystem(PhysicSystem(world, this))
        addSystem(PlayerCollisionSystem())
        addSystem(AnimationSystem(assets))
        addSystem(RenderSystem(batch, viewport, world, box2DDebugRenderer))
    }
    private val player = engine.gameObject(
        EntityType.PLAYER,
        world, 16f, 3f,
        width = 0.5f, height = 0.8f,
        speed = 4f, collBodyOffsetX = 4f * UNIT_SCALE,
        createCharacterSensors = true
    ).apply {
        add(engine.createComponent(PlayerComponent::class.java))
        this[AnimationComponent.mapper]?.apply {
            modelType = ModelType.PLAYER
            animationType = AnimationType.RUN
        }
    }

    override fun show() {
        // add game screen as input listener to react when the player wants to quit the game (=exit key pressed)
        gameEventManager.addInputListener(this)

        audioManager.play(MusicAssets.LEVEL_1)

        // TODO remove testing stuff
        // floor
        engine.gameObject(
            EntityType.SCENERY,
            world,
            1f,
            1f,
            width = 30f,
            height = 1f,
            bodyType = BodyDef.BodyType.StaticBody
        )
        engine.gameObject(
            EntityType.SCENERY,
            world,
            18f,
            2f,
            width = 2f,
            height = 1f,
            bodyType = BodyDef.BodyType.StaticBody
        )
        // water
        engine.gameObject(
            EntityType.SCENERY,
            world,
            2f,
            12f,
            width = 28f,
            height = 4f,
            bodyType = BodyDef.BodyType.StaticBody,
            isSensor = true
        )
        // enemy
        engine.gameObject(EntityType.ENEMY, world, 14f, 3f, width = 1f, height = 1f)
        // item
        engine.gameObject(
            EntityType.ITEM,
            world,
            18.5f,
            4f,
            width = 1f,
            height = 1f,
            bodyType = BodyDef.BodyType.StaticBody,
            isSensor = true
        )
    }

    override fun hide() {
        gameEventManager.removeInputListener(this)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        //TODO remove teststuff
        player[MoveComponent.mapper]?.let { move ->
            move.direction = when {
                inputController.isPressed(InputKey.MoveRight) -> MoveDirection.RIGHT
                inputController.isPressed(InputKey.MoveLeft) -> MoveDirection.LEFT
                else -> MoveDirection.STOP
            }
        }
        player[JumpComponent.mapper]?.let { jump ->
            jump.direction = when {
                inputController.isPressed(InputKey.Jump) -> JumpDirection.JUMPING
                else -> JumpDirection.STOP
            }
        }

        // update all ecs engine systems including the render system which draws stuff on the screen
        engine.update(delta)
    }

    override fun exit() {
        // player pressed exit key -> go back to menu
        game.setScreen<MenuScreen>()
    }
}