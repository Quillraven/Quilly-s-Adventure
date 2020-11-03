package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.github.quillraven.quillysadventure.ecs.component.CameraLockComponent
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.map.Map
import com.github.quillraven.quillysadventure.map.MapChangeListener
import ktx.ashley.allOf
import ktx.log.error
import ktx.log.logger
import ktx.math.vec2
import kotlin.math.max
import kotlin.math.min

private val LOG = logger<CameraSystem>()

class CameraSystem(
    engine: Engine,
    private val gameEventManager: GameEventManager,
    private val camera: OrthographicCamera
) : EntitySystem(), MapChangeListener {
    private val cameraEntities = engine.getEntitiesFor(allOf(CameraLockComponent::class).get())
    private val maxCameraPosition = vec2(0f, 0f)

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addMapChangeListener(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeMapChangeListener(this)
    }

    override fun update(deltaTime: Float) {
        if (cameraEntities.size() > 0) {
            // lock camera to the first entity
            if (cameraEntities.size() > 1) {
                LOG.error {
                    "There are more than one entities that should lock the camera. " +
                            "Current camera entities ${cameraEntities.size()}"
                }
            }

            val entityPos = cameraEntities[0].transfCmp.interpolatedPosition
            val camW = camera.viewportWidth * 0.5f
            val camH = camera.viewportHeight * 0.5f
            camera.position.apply {
                if (maxCameraPosition.isZero) {
                    // camera is not restricted to map boundaries
                    x = entityPos.x
                    y = entityPos.y
                } else {
                    // restrict camera to map boundaries
                    x = max(camW, min(entityPos.x, maxCameraPosition.x - camW))
                    y = max(camH, min(entityPos.y, maxCameraPosition.y - camH))
                }
            }
        }
    }

    override fun mapChange(newMap: Map) {
        val newWidth = newMap.width
        val newHeight = newMap.height
        if (newWidth >= camera.viewportWidth && newHeight >= camera.viewportHeight) {
            // lock camera to map boundaries
            maxCameraPosition.set(newWidth, newHeight)
        } else {
            // do not lock camera to map boundaries because map boundaries are smaller
            // than the camera. Only center camera on camera entity
            maxCameraPosition.set(0f, 0f)
        }
    }
}
