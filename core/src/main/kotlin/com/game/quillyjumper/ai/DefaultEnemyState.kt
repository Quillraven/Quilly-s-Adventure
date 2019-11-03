package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation
import com.game.quillyjumper.ecs.component.*

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
                if (inAttackRange(entity.transfCmp, attackCmp, aggroEntities.first().transfCmp)) {
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
                if (inAttackRange(transform, attackCmp, aggroTransform)) {
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

            if (entity.aggroCmp.aggroEntities.first().transfCmp.position.x < entity.transfCmp.position.x) {
                // aggro entity is on the left side -> attack to the left
                entity.facingCmp.direction = FacingDirection.LEFT
            } else {
                // otherwise attack to the right
                entity.facingCmp.direction = FacingDirection.RIGHT
            }

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

    fun inAttackRange(
        transformCmp: TransformComponent,
        attackCmp: AttackComponent,
        aggroTransformCmp: TransformComponent
    ): Boolean {
        val posA = transformCmp.position
        val sizeA = transformCmp.size
        val posB = aggroTransformCmp.position
        val sizeB = aggroTransformCmp.size
        return (posA.x - attackCmp.range <= posB.x + sizeB.x && posA.x >= posB.x + sizeB.x) // aggro entity is within left attack range
                || (posA.x + sizeA.x <= posB.x && posA.x + sizeA.x + attackCmp.range >= posB.x) // aggro entity is within right attack range
    }
}