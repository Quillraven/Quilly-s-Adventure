package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.quillysadventure.ecs.component.AttackComponent
import com.github.quillraven.quillysadventure.ecs.component.AttackOrder
import com.github.quillraven.quillysadventure.ecs.component.FacingComponent
import com.github.quillraven.quillysadventure.ecs.component.FacingDirection
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.StatsComponent
import com.github.quillraven.quillysadventure.ecs.component.TransformComponent
import com.github.quillraven.quillysadventure.ecs.component.attackCmp
import com.github.quillraven.quillysadventure.ecs.component.facingCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.ecs.damageEmitter
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.exclude

class AttackSystem(private val world: World, private val gameEventManager: GameEventManager) : IteratingSystem(
    allOf(
        AttackComponent::class,
        TransformComponent::class,
        StatsComponent::class,
        FacingComponent::class
    ).exclude(RemoveComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.attackCmp.run {
            if (canAttack() && order == AttackOrder.ATTACK_ONCE) {
                // entity wants to attack and has no cooldown on its attack
                // 1) set cooldown
                gameEventManager.dispatchCharacterAttackEvent(entity)
                attackTime = cooldown
                // 2) create damage emitter entity
                val transform = entity.transfCmp
                val offsetX = when (entity.facingCmp.direction) {
                    FacingDirection.LEFT -> -range
                    else -> transform.size.x
                }
                engine.damageEmitter(
                    world,
                    transform.position.x + offsetX,
                    transform.position.y,
                    range,
                    transform.size.y,
                    entity.statsCmp.damage,
                    0.25f,
                    entity,
                    damageDelay
                )
            }

            if (attackTime > 0f) {
                attackTime -= deltaTime
                if (attackTime <= 0f) {
                    // ready for another attack
                    gameEventManager.dispatchCharacterAttackReadyEvent(entity)
                }
            }
        }
    }
}
