package com.github.quillraven.quillysadventure.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.AttackOrder
import com.github.quillraven.quillysadventure.ecs.component.FacingDirection
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.ecs.component.aggroCmp
import com.github.quillraven.quillysadventure.ecs.component.aniCmp
import com.github.quillraven.quillysadventure.ecs.component.attackCmp
import com.github.quillraven.quillysadventure.ecs.component.facingCmp
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp

enum class DefaultEnemyState(
    override val aniType: AnimationType,
    override val aniMode: Animation.PlayMode = Animation.PlayMode.LOOP
) : EntityState {
    IDLE(AnimationType.IDLE) {
        override fun update(entity: Entity) {
            val aggroEntities = entity.aggroCmp.aggroEntities
            if (aggroEntities.size > 0) {
                // there are player units within aggro range
                // move towards the first unit or attack it if it is within range
                val attackCmp = entity.attackCmp
                if (attackCmp.inAttackRange(entity.transfCmp, aggroEntities.first().transfCmp)) {
                    // aggro entity is within attack range
                    // if enemy can attack then do it
                    // otherwise remain in current position and wait for attack to be ready
                    if (attackCmp.canAttack()) {
                        // enemy can attack and is within range
                        entity.stateCmp.stateMachine.changeState(ATTACK)
                    }
                } else {
                    // enemy is outside of range -> run towards aggro entity
                    entity.stateCmp.stateMachine.changeState(RUN)
                }
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(entity: Entity) {
            val aggroEntities = entity.aggroCmp.aggroEntities
            if (aggroEntities.size > 0) {
                // entities still in range -> move towards first entity
                // or attack it if within range
                val attackCmp = entity.attackCmp
                val transform = entity.transfCmp
                val aggroTransform = aggroEntities.first().transfCmp
                if (attackCmp.inAttackRange(transform, aggroTransform)) {
                    // aggro entity is within attack range -> stop movement and attack it if possible
                    entity.moveCmp.order = MoveOrder.NONE
                    if (attackCmp.canAttack()) {
                        entity.stateCmp.stateMachine.changeState(ATTACK)
                    }
                } else if (aggroTransform.position.x + aggroTransform.size.x < transform.position.x) {
                    // aggro entity is on the left side and not in attack range
                    entity.moveCmp.order = MoveOrder.LEFT
                } else {
                    // aggro entity is on the right side and not in attack range
                    entity.moveCmp.order = MoveOrder.RIGHT
                }
            } else {
                // no more entities in range -> go back to idle state and wait
                entity.stateCmp.stateMachine.changeState(IDLE)
            }
        }

        override fun exit(entity: Entity) {
            entity.moveCmp.order = MoveOrder.NONE
        }
    },
    ATTACK(AnimationType.ATTACK, Animation.PlayMode.NORMAL) {
        override fun enter(entity: Entity) {
            entity.attackCmp.order = AttackOrder.ATTACK_ONCE
            entity.moveCmp.lockMovement = true
            updateFacingForAttack(entity)
            super.enter(entity)
        }

        override fun update(entity: Entity) {
            if (entity.aniCmp.isAnimationFinished()) {
                entity.attackCmp.order = AttackOrder.NONE
                entity.stateCmp.stateMachine.changeState(IDLE)
            }
        }

        override fun exit(entity: Entity) {
            entity.moveCmp.lockMovement = false
        }
    };
}

fun updateFacingForAttack(entity: Entity) {
    val transformA = entity.transfCmp
    val transformB = entity.aggroCmp.aggroEntities.first().transfCmp
    val centerA = transformA.position.x + transformA.size.x * 0.5f
    val centerB = transformB.position.x + transformB.size.x * 0.5f

    entity.facingCmp.direction = when {
        centerA > centerB -> FacingDirection.LEFT
        else -> FacingDirection.RIGHT
    }
}
