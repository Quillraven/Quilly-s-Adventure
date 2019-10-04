package com.game.quillyjumper.ecs.system

import box2dLight.DirectionalLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import ktx.graphics.color

class LightSystem(private val rayHandler: RayHandler, private val camera: OrthographicCamera) : EntitySystem() {
    init {
        // TODO read ambient light values from map
        rayHandler.setBlurNum(3)
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.9f)
        // TODO dispose sun
        DirectionalLight(rayHandler, 512, color(1f, 1f, 1f, 0.5f), 225f)
    }

    override fun update(deltaTime: Float) {
        rayHandler.setCombinedMatrix(camera)
        rayHandler.updateAndRender()
    }
}