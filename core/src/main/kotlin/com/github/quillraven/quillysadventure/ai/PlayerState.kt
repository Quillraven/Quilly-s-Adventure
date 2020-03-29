package com.github.quillraven.quillysadventure.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.AttackOrder
import com.github.quillraven.quillysadventure.ecs.component.CastOrder
import com.github.quillraven.quillysadventure.ecs.component.CollisionComponent
import com.github.quillraven.quillysadventure.ecs.component.JumpOrder
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.ecs.component.PhysicComponent
import com.github.quillraven.quillysadventure.ecs.component.abilityCmp
import com.github.quillraven.quillysadventure.ecs.component.aniCmp
import com.github.quillraven.quillysadventure.ecs.component.attackCmp
import com.github.quillraven.quillysadventure.ecs.component.collCmp
import com.github.quillraven.quillysadventure.ecs.component.jumpCmp
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import kotlin.math.abs

enum class PlayerState(
    override val aniType: AnimationType,
    override val aniMode: Animation.PlayMode = Animation.PlayMode.LOOP
) : EntityState {
    IDLE(AnimationType.IDLE) {
        override fun update(entity: Entity) {
            with(entity.stateCmp.stateMachine) {
                when {
                    entity.abilityCmp.order == CastOrder.BEGIN_CAST -> changeState(CAST)
                    entity.attackCmp.order == AttackOrder.START -> changeState(ATTACK)
                    entity.jumpCmp.order == JumpOrder.JUMP -> changeState(JUMP)
                    entity.moveCmp.order != MoveOrder.NONE -> changeState(RUN)
                    isFalling(entity.physicCmp, entity.collCmp) -> changeState(FALL)
                }
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(entity: Entity) {
            val physicCmp = entity.physicCmp
            val velocity = physicCmp.body.linearVelocity
            with(entity.stateCmp.stateMachine) {
                when {
                    entity.abilityCmp.order == CastOrder.BEGIN_CAST -> changeState(CAST)
                    entity.attackCmp.order == AttackOrder.START -> changeState(ATTACK)
                    entity.jumpCmp.order == JumpOrder.JUMP -> changeState(JUMP)
                    isFalling(physicCmp, entity.collCmp) -> changeState(FALL)
                    entity.moveCmp.order == MoveOrder.NONE && abs(velocity.x) <= 0.5f -> changeState(IDLE)
                }
            }
        }
    },
    JUMP(AnimationType.JUMP, Animation.PlayMode.NORMAL) {
        override fun update(entity: Entity) {
            val collision = entity.collCmp
            with(entity.stateCmp) {
                if (isFalling(entity.physicCmp, collision) || stateTime >= entity.jumpCmp.maxJumpTime) {
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
    ATTACK(AnimationType.ATTACK, Animation.PlayMode.NORMAL) {
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
    CAST(AnimationType.CAST, Animation.PlayMode.NORMAL) {

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
    },
    FAKE_DEATH(AnimationType.DEATH, Animation.PlayMode.NORMAL) {
        override fun enter(entity: Entity) {
            super.enter(entity)
            entity.aniCmp.animationSpeed = 0.5f
        }

        override fun exit(entity: Entity) {
            super.exit(entity)
            entity.aniCmp.animationSpeed = 1f
        }
    };

    fun isFalling(physic: PhysicComponent, collision: CollisionComponent) =
        physic.body.linearVelocity.y < 0f && collision.numGroundContacts == 0
}
