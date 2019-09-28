package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.game.quillyjumper.ecs.component.*

enum class PlayerState(override val aniType: AnimationType, override val loopAni: Boolean = true) : EntityState {
    IDLE(AnimationType.IDLE) {
        override fun update(entity: Entity) {
            entity.stateCmp.run {
                when {
                    entity.jumpCmp.order == JumpOrder.JUMP -> stateMachine.changeState(JUMP)
                    entity.moveCmp.order != MoveOrder.NONE -> stateMachine.changeState(RUN)
                    entity.attackCmp.order == AttackOrder.START -> stateMachine.changeState(ATTACK)
                }
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(entity: Entity) {
            entity.stateCmp.run {
                when {
                    entity.jumpCmp.order == JumpOrder.JUMP -> stateMachine.changeState(JUMP)
                    entity.physicCmp.body.linearVelocity?.x == 0f -> stateMachine.changeState(IDLE)
                    entity.attackCmp.order == AttackOrder.START -> stateMachine.changeState(ATTACK)
                }
            }
        }
    },
    JUMP(AnimationType.JUMP, false) {
        override fun update(entity: Entity) {
            val physic = entity.physicCmp
            val collision = entity.collCmp
            val state = entity.stateCmp
            if ((physic.body.linearVelocity.y <= 0f && collision.numGroundContacts == 0) || state.stateTime > 0.8f) {
                // player is in mid-air and falling down OR player exceeds maximum jump time
                state.stateMachine.changeState(FALL)
            } else if (collision.numGroundContacts > 0 && entity.jumpCmp.order == JumpOrder.NONE) {
                // player is on ground and does not want to jump anymore
                state.stateMachine.changeState(if (physic.body.linearVelocity.x != 0f) RUN else IDLE)
            }
        }
    },
    FALL(AnimationType.FALL) {
        override fun update(entity: Entity) {
            // stop any jump movement
            entity.jumpCmp.order = JumpOrder.NONE

            if (entity.collCmp.numGroundContacts > 0) {
                // reached ground again
                entity.stateCmp.stateMachine.changeState(if (entity.physicCmp.body.linearVelocity.x != 0f) RUN else IDLE)
            }
        }
    },
    ATTACK(AnimationType.ATTACK, false) {
        override fun enter(entity: Entity) {
            entity.attackCmp.order = AttackOrder.ATTACK_ONCE
            super.enter(entity)
        }

        override fun update(entity: Entity) {
            // stop any movement
            entity.moveCmp.order = MoveOrder.NONE
            entity.jumpCmp.order = JumpOrder.NONE

            if (entity.aniCmp.isAnimationFinished()) {
                entity.attackCmp.order = AttackOrder.NONE
                entity.stateCmp.stateMachine.changeState(if (entity.physicCmp.body.linearVelocity.x != 0f) RUN else IDLE)
            }
        }
    };
}
