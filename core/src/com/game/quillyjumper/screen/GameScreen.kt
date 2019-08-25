package com.game.quillyjumper.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.ecs.system.PhysicSystem
import com.game.quillyjumper.ecs.system.RenderSystem
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.box2d.body

class GameScreen(private val game: KtxGame<KtxScreen>,
                 private val world: World,
                 private val batch: SpriteBatch,
                 private val box2DDebugRenderer: Box2DDebugRenderer) : KtxScreen {
    private val viewport = FitViewport(32f, 18f)
    private val engine = PooledEngine().apply {
        addSystem(PhysicSystem(world, this))
        addSystem(RenderSystem(batch, viewport, world, box2DDebugRenderer))
    }

    override fun show() {
        // TODO remove testing stuff
        // floor
        world.body(BodyDef.BodyType.StaticBody) {
            position.set(16f, 1f)
            box(width = 30f, height = 1f) {
                // it.dispose()
            }
            //dispose shape??
        }
        // falling object
        world.body(BodyDef.BodyType.DynamicBody) {
            position.set(16f, 17f)
            box(width = 1f, height = 1f) {
                density = 1f
                restitution = 0.5f
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        // update all ecs engine systems including the render system which draws stuff on the screen
        engine.update(delta)
    }
}