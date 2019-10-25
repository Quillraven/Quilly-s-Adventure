package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.game.quillyjumper.ecs.component.*
import kotlin.math.abs

enum class PlayerState(override val aniType: AnimationType, override val loopAni: Boolean = true) : EntityState {
    IDLE(AnimationType.IDLE) {
        override fun update(entity: Entity) {
            with(entity.stateCmp.stateMachine) {
                when {
                    entity.abilityCmp.order == CastOrder.BEGIN_CAST -> changeState(CAST)
                    entity.attackCmp.order == AttackOrder.START -> changeState(ATTACK)
                    entity.jumpCmp.order == JumpOrder.JUMP -> changeState(JUMP)
                    entity.moveCmp.order != MoveOrder.NONE -> changeState(RUN)
                }
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(entity: Entity) {
            val velocity = entity.physicCmp.body.linearVelocity
            with(entity.stateCmp.stateMachine) {
                when {
                    entity.abilityCmp.order == CastOrder.BEGIN_CAST -> changeState(CAST)
                    entity.attackCmp.order == AttackOrder.START -> changeState(ATTACK)
                    entity.jumpCmp.order == JumpOrder.JUMP -> changeState(JUMP)
                    velocity.y <= 0f && entity.collCmp.numGroundContacts == 0 -> changeState(FALL)
                    entity.moveCmp.order == MoveOrder.NONE && abs(velocity.x) <= 0.5f -> changeState(IDLE)
                }
            }
        }
    },
    JUMP(AnimationType.JUMP, false) {
        override fun update(entity: Entity) {
            val physic = entity.physicCmp
            val collision = entity.collCmp
            with(entity.stateCmp) {
                if ((physic.body.linearVelocity.y <= 0f && collision.numGroundContacts == 0) || stateTime >= entity.jumpCmp.maxJumpTime) {
                    // player is in mid-air and falling down OR player exceeds maximum jump time
                    stateMachine.changeState(FALL)
                } else if (collision.numGroundContacts > 0 && entity.jumpCmp.order == JumpOrder.NONE) {
                    // player is on ground again
                    stateMachine.changeState(IDLE)
                }
            }
        }

        override fun exit(entity: Entity) {
            entity.jumpCmp.order = JumpOrder.NONE
        }
    },
    FALL(AnimationType.FALL) {
        override fun update(entity: Entity) {
            if (entity.collCmp.numGroundContacts > 0) {
                // reached ground again
                entity.stateCmp.stateMachine.changeState(IDLE)
            }
        }
    },
    ATTACK(AnimationType.ATTACK, false) {
        override fun enter(entity: Entity) {
            entity.attackCmp.order = AttackOrder.ATTACK_ONCE
            entity.moveCmp.lockMovement = true
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
    },
    CAST(AnimationType.CAST, false) {

        override fun enter(entity: Entity) {
            entity.moveCmp.lockMovement = true
            super.enter(entity)
        }

        override fun update(entity: Entity) {
            entity.stateCmp.let { state ->
                with(entity.abilityCmp) {
                    if (state.stateTime >= 0.2f && order == CastOrder.BEGIN_CAST) {
                        order = CastOrder.CAST
                    }

                    if (entity.aniCmp.isAnimationFinished() || (state.stateTime < 0.2f && order == CastOrder.NONE)) {
                        state.stateMachine.changeState(IDLE)
                    }
                }
            }
        }

        override fun exit(entity: Entity) {
            entity.moveCmp.lockMovement = false
            super.exit(entity)
        }
    };
}
