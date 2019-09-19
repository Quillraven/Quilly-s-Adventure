package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.Viewport
import com.game.quillyjumper.ecs.component.FloatingTextComponent
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.graphics.use
import ktx.math.vec2

class FloatingTextSystem(
    private val batch: SpriteBatch,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport
) : SortedIteratingSystem(
    allOf(FloatingTextComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
) {
    private val camera = uiViewport.camera as OrthographicCamera
    private val projectionVector = vec2()

    override fun update(deltaTime: Float) {
        // always sort entities before rendering
        forceSort()
        // update camera to set the correct matrix for rendering later on
        uiViewport.apply()
        batch.use(camera.combined) {
            // render entities
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[FloatingTextComponent.mapper]?.let { text ->
            // 1) reduce lifespan and check if text should be removed
            text.lifeSpan -= deltaTime
            if (text.lifeSpan <= 0f) {
                entity.add(engine.createComponent(RemoveComponent::class.java))
                return
            }

            entity[TransformComponent.mapper]?.let { transform ->
                // 2) move floating text according to speed
                transform.position.set(
                    transform.position.x + text.speed.x * deltaTime,
                    transform.position.y + text.speed.y * deltaTime
                )

                // 3) render text
                projectionVector.set(transform.position)
                // transform world to screen coordinates
                gameViewport.project(projectionVector)
                // transform screen to UI coordinates
                uiViewport.unproject(projectionVector)
                text.font.color = text.color
                text.font.draw(batch, text.stringBuilder, projectionVector.x, projectionVector.y)
            }
        }
    }
}