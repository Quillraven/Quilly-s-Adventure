package com.game.quillyjumper.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.component.RenderComponent
import com.game.quillyjumper.ecs.gameObject
import com.game.quillyjumper.ecs.system.PhysicSystem
import com.game.quillyjumper.ecs.system.RenderSystem
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get
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
    private val player = engine.gameObject(world, 16f, 17f)

    override fun show() {
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
        // set test texture for player
        player[RenderComponent.mapper]?.let { render ->
            render.sprite.texture = Texture("graphics/adventurer-idle-00.png")
            render.sprite.setRegion(0, 0, render.sprite.texture.width, render.sprite.texture.height)
            render.sprite.setOriginCenter()
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
        // update all ecs engine systems including the render system which draws stuff on the screen
        engine.update(delta)
    }
}