package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.damageEmitter
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get

class AttackSystem(private val world: World) : IteratingSystem(
    allOf(
        AttackComponent::class,
        TransformComponent::class,
        StatsComponent::class,
        FacingComponent::class
    ).exclude(RemoveComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[AttackComponent.mapper]?.let { attack ->
            if (attack.attackTime <= 0f && attack.order == AttackOrder.ATTACK_ONCE) {
                // entity wants to attack and has no cooldown on its attack
                // 1) set cooldown
                attack.attackTime = attack.cooldown
                // 2) create damage emitter entity
                entity[TransformComponent.mapper]?.let { transform ->
                    entity[FacingComponent.mapper]?.let { facing ->
                        val offsetX = when (facing.direction) {
                            FacingDirection.LEFT -> -attack.range
                            else -> transform.size.x
                        }
                        entity[StatsComponent.mapper]?.let { stats ->
                            engine.damageEmitter(
                                world,
                                transform.position.x + offsetX,
                                transform.position.y,
                                attack.range,
                                transform.size.y,
                                stats.damage,
                                0.25f,
                                entity
                            )
                        }
                    }
                }
            }
            attack.attackTime -= deltaTime
        }
    }
}