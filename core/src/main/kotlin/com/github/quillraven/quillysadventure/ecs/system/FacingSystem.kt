package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.quillraven.quillysadventure.ecs.component.*
import ktx.ashley.allOf

class FacingSystem : IteratingSystem(allOf(FacingComponent::class, RenderComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.renderCmp.sprite.run {
            when (entity.facingCmp.direction) {
                FacingDirection.RIGHT -> setFlip(false, isFlipY)
                FacingDirection.LEFT -> setFlip(true, isFlipY)
            }
        }
    }
}
