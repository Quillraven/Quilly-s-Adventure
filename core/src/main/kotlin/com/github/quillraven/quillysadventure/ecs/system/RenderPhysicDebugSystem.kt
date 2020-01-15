package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.Viewport

class RenderPhysicDebugSystem(
    private val world: World,
    private val viewport: Viewport,
    private val box2DDebugRenderer: Box2DDebugRenderer
) : EntitySystem() {
    init {
        // enable/disable box2d debug rendering
        setProcessing(false)
    }

    override fun update(deltaTime: Float) {
        box2DDebugRenderer.render(world, viewport.camera.combined)
    }
}
