package com.github.quillraven.quillysadventure.ecs.system

import box2dLight.RayHandler
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera

class LightSystem(private val rayHandler: RayHandler, private val camera: OrthographicCamera) : EntitySystem() {
    override fun update(deltaTime: Float) {
        rayHandler.setCombinedMatrix(camera)
        rayHandler.updateAndRender()
    }
}
