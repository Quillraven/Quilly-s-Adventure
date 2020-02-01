package com.github.quillraven.quillysadventure.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.AttackComponent
import com.github.quillraven.quillysadventure.ecs.component.AttackOrder
import com.github.quillraven.quillysadventure.ecs.component.CastOrder
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.ecs.component.StateComponent
import com.github.quillraven.quillysadventure.ecs.component.StatsComponent
import com.github.quillraven.quillysadventure.ecs.component.abilityCmp
import com.github.quillraven.quillysadventure.ecs.component.aggroCmp
import com.github.quillraven.quillysadventure.ecs.component.aniCmp
import com.github.quillraven.quillysadventure.ecs.component.attackCmp
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp

enum class MinotaurState(
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
                        changeToRandomAttackState(entity.stateCmp)
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
                        changeToRandomAttackState(entity.stateCmp)
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
    // Slash Attack
    ATTACK(AnimationType.ATTACK, Animation.PlayMode.NORMAL) {
        override fun enter(entity: Entity) {
            entity.attackCmp.order = AttackOrder.ATTACK_ONCE
            entity.moveCmp.lockMovement = true
            entity.statsCmp.damage = 7f
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
            entity.statsCmp.damage = 5f
        }
    },
    // Spin Attack
    ATTACK2(AnimationType.ATTACK2, Animation.PlayMode.NORMAL) {
        override fun enter(entity: Entity) {
            entity.moveCmp.lockMovement = true
            entity.abilityCmp.order = CastOrder.CAST
            setSpinAttackValues(entity.attackCmp, entity.statsCmp)
            super.enter(entity)
        }

        override fun update(entity: Entity) {
            if (entity.aniCmp.isAnimationFinished()) {
                entity.stateCmp.stateMachine.changeState(IDLE)
            }
        }

        override fun exit(entity: Entity) {
            entity.moveCmp.lockMovement = false
            entity.abilityCmp.order = CastOrder.NONE
            setSlashAttackValues(entity.attackCmp, entity.statsCmp)
            super.exit(entity)
        }
    },
    // Poke Attack
    ATTACK3(AnimationType.ATTACK3, Animation.PlayMode.NORMAL) {
        override fun enter(entity: Entity) {
            val attack = entity.attackCmp
            attack.order = AttackOrder.ATTACK_ONCE
            setPokeAttackValues(attack, entity.statsCmp)
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
            setSlashAttackValues(entity.attackCmp, entity.statsCmp)
        }
    };

    fun setSlashAttackValues(attackCmp: AttackComponent, statsCmp: StatsComponent) {
        attackCmp.range = 0.7f
        attackCmp.damageDelay = 0.3f
        attackCmp.cooldown = 5f
        statsCmp.damage = 5f
    }

    fun setPokeAttackValues(attackCmp: AttackComponent, statsCmp: StatsComponent) {
        attackCmp.range = 0.4f
        attackCmp.damageDelay = 0f
        attackCmp.cooldown = 3f
        statsCmp.damage = 3f
    }

    fun setSpinAttackValues(attackCmp: AttackComponent, statsCmp: StatsComponent) {
        attackCmp.range = 0.8f
        attackCmp.damageDelay = 0f
        statsCmp.damage = 9f
    }

    fun changeToRandomAttackState(stateCmp: StateComponent) {
        val rand = MathUtils.random(99)
        when {
            rand < 50 -> stateCmp.stateMachine.changeState(ATTACK)
            rand < 65 -> stateCmp.stateMachine.changeState(ATTACK2)
            else -> stateCmp.stateMachine.changeState(ATTACK3)
        }
    }
}
