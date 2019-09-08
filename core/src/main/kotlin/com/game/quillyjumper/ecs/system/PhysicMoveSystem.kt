package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.game.quillyjumper.ecs.component.MoveComponent
import com.game.quillyjumper.ecs.component.MoveOrder
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.component.RenderComponent
import ktx.ashley.allOf
import ktx.ashley.get

class PhysicMoveSystem : IteratingSystem(allOf(MoveComponent::class, PhysicComponent::class).get()) {
    companion object {
        private val stopAlpha = Interpolation.exp10Out
        private val moveAlpha = Interpolation.circleOut
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[MoveComponent.mapper]?.let { move ->
            entity[PhysicComponent.mapper]?.let { physic ->
                when {
                    move.order == MoveOrder.NONE && move.speed != 0f -> {
                        // entity should not move but it is still moving -> stop it
                        move.speed = stopAlpha.apply(move.speed, 0f, deltaTime)
                        if (move.speed < 0.1f && move.speed > -0.1f) {
                            // tolerance for movement stop to avoid playing the run
                            // animation although the entity visually already stopped
                            move.speed = 0f
                        }
                    }
                    move.order == MoveOrder.RIGHT && move.speed != move.maxSpeed -> {
                        // entity should move right but did not reach maximum speed yet
                        move.speed = moveAlpha.apply(move.speed, move.maxSpeed, deltaTime * 0.1f)
                    }
                    move.order == MoveOrder.LEFT && move.speed != -move.maxSpeed -> {
                        // entity should move left but did not reach maximum speed yet
                        move.speed = moveAlpha.apply(move.speed, -move.maxSpeed, deltaTime * 0.1f)
                    }
                }

                // set the value of the impulse that will be applied before each physic step call
                physic.impulse.x = physic.body.mass * (move.speed - physic.body.linearVelocity.x)

                // in case the entity is rendered then flip its sprite according to the move direction
                entity[RenderComponent.mapper]?.also { render ->
                    when {
                        move.speed > 0f -> render.sprite.setFlip(false, render.sprite.isFlipY)
                        move.speed < 0f -> render.sprite.setFlip(true, render.sprite.isFlipY)
                    }
                }
            }
        }
    }
}