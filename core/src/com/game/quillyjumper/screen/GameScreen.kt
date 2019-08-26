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
import com.game.quillyjumper.ecs.component.MoveComponent
import com.game.quillyjumper.ecs.component.MoveDirection
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.gameObject
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
        addSystem(PhysicSystem(world, this))
        addSystem(RenderSystem(batch, viewport, world, box2DDebugRenderer))
    }
    private val player = engine.gameObject(world, TextureRegion(Texture("graphics/adventurer-idle-00.png")), 16f, 17f, width = 0.5f, height = 0.8f, speed = 4f, collBodyOffsetX = 4f * UNIT_SCALE)

    override fun show() {
        audioManager.play(MusicAssets.LEVEL_1)

        // TODO remove testing stuff
        // floor
        world.body(BodyDef.BodyType.StaticBody) {
            position.set(16f, 1f)
            box(width = 30f, height = 1f)
        }
        // water
        world.body(BodyDef.BodyType.StaticBody) {
            position.set(16f, 6f)
            userData = "WATER"
            box(width = 20f, height = 4f).isSensor = true
        }
        // make player bounce
        player[PhysicComponent.mapper]?.let { physic ->
            physic.body.fixtureList[0].restitution = 0.7f
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        //TODO remove teststuff
        if (inputController.isPressed(InputKey.MoveRight)) {
            player[MoveComponent.mapper]?.direction = MoveDirection.RIGHT
        } else if (inputController.isPressed(InputKey.MoveLeft)) {
            player[MoveComponent.mapper]?.direction = MoveDirection.LEFT
        } else {
            player[MoveComponent.mapper]?.direction = MoveDirection.STOP
        }

        // update all ecs engine systems including the render system which draws stuff on the screen
        engine.update(delta)
    }
}