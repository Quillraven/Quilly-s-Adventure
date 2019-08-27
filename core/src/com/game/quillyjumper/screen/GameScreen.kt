package com.game.quillyjumper.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.MusicAssets
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.gameObject
import com.game.quillyjumper.ecs.system.PhysicJumpSystem
import com.game.quillyjumper.ecs.system.PhysicMoveSystem
import com.game.quillyjumper.ecs.system.PhysicSystem
import com.game.quillyjumper.ecs.system.RenderSystem
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get
import ktx.box2d.body

class GameScreen(private val game: KtxGame<KtxScreen>,
                 private val inputController: InputController,
                 private val audioManager: AudioManager,
                 private val world: World,
                 private val batch: SpriteBatch,
                 private val box2DDebugRenderer: Box2DDebugRenderer) : KtxScreen {
    private val viewport = FitViewport(32f, 18f)
    private val engine = PooledEngine().apply {
        addSystem(PhysicMoveSystem())
        addSystem(PhysicJumpSystem())
        addSystem(PhysicSystem(world, this))
        addSystem(RenderSystem(batch, viewport, world, box2DDebugRenderer))
    }
    private val player = engine.gameObject(world, TextureRegion(Texture("graphics/adventurer-idle-00.png")), 16f, 3f, width = 0.5f, height = 0.8f, speed = 4f, collBodyOffsetX = 4f * UNIT_SCALE).apply {
        add(engine.createComponent(PlayerComponent::class.java))
    }

    override fun show() {
        audioManager.play(MusicAssets.LEVEL_1)

        // TODO remove testing stuff
        // floor
        world.body(BodyDef.BodyType.StaticBody) {
            position.set(16f, 1f)
            box(width = 30f, height = 1f)
        }
        world.body(BodyDef.BodyType.StaticBody) {
            position.set(18f, 2f)
            box(width = 2f, height = 1f)
        }
        // water
        world.body(BodyDef.BodyType.StaticBody) {
            position.set(16f, 12f)
            userData = "WATER"
            box(width = 20f, height = 4f).isSensor = true
        }
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
}