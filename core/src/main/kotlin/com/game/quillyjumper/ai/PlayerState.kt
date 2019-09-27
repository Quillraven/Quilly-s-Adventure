package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.execute
import ktx.ashley.get

enum class PlayerState(override val aniType: AnimationType, override val loopAni: Boolean = true) : EntityState {
    IDLE(AnimationType.IDLE) {
        override fun update(entity: Entity) {
            entity[StateComponent.mapper]?.let { state ->
                when {
                    entity[JumpComponent.mapper]?.order == JumpOrder.JUMP -> state.stateMachine.changeState(JUMP)
                    entity[MoveComponent.mapper]?.order != MoveOrder.NONE -> state.stateMachine.changeState(RUN)
                    entity[AttackComponent.mapper]?.order == AttackOrder.START -> state.stateMachine.changeState(ATTACK)
                }
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(entity: Entity) {
            entity[StateComponent.mapper]?.let { state ->
                when {
                    entity[JumpComponent.mapper]?.order == JumpOrder.JUMP -> state.stateMachine.changeState(JUMP)
                    entity[PhysicComponent.mapper]?.body?.linearVelocity?.x == 0f -> state.stateMachine.changeState(IDLE)
                    entity[AttackComponent.mapper]?.order == AttackOrder.START -> state.stateMachine.changeState(ATTACK)
                }
            }
        }
    },
    JUMP(AnimationType.JUMP, false) {
        override fun update(entity: Entity) {
            entity.execute(
                PhysicComponent.mapper,
                CollisionComponent.mapper,
                StateComponent.mapper,
                JumpComponent.mapper
            ) { physic, collision, state, jump ->
                if ((physic.body.linearVelocity.y <= 0f && collision.numGroundContacts == 0) || state.stateTime > 0.8f) {
                    // player is in mid-air and falling down OR player exceeds maximum jump time
                    state.stateMachine.changeState(FALL)
                } else if (collision.numGroundContacts > 0 && jump.order == JumpOrder.NONE) {
                    // player is on ground and does not want to jump anymore
                    state.stateMachine.changeState(if (physic.body.linearVelocity.x != 0f) RUN else IDLE)
                }
            }
        }
    },
    FALL(AnimationType.FALL) {
        override fun update(entity: Entity) {
            // stop any jump movement
            entity[JumpComponent.mapper]?.order = JumpOrder.NONE

            entity.execute(
                CollisionComponent.mapper,
                StateComponent.mapper,
                PhysicComponent.mapper
            ) { collision, state, physic ->
                if (collision.numGroundContacts > 0) {
                    // reached ground again
                    state.stateMachine.changeState(if (physic.body.linearVelocity.x != 0f) RUN else IDLE)
                }
            }
        }
    },
    ATTACK(AnimationType.ATTACK, false) {
        override fun enter(entity: Entity) {
            entity[AttackComponent.mapper]?.order = AttackOrder.ATTACK_ONCE
            super.enter(entity)
        }

        override fun update(entity: Entity) {
            // stop any movement
            entity[MoveComponent.mapper]?.order = MoveOrder.NONE
            entity[JumpComponent.mapper]?.order = JumpOrder.NONE

            entity.execute(
                PhysicComponent.mapper,
                AnimationComponent.mapper,
                AttackComponent.mapper,
                StateComponent.mapper
            ) { physic, animation, attack, state ->
                if (animation.isAnimationFinished()) {
                    attack.order = AttackOrder.NONE
                    state.stateMachine.changeState(if (physic.body.linearVelocity.x != 0f) RUN else IDLE)
                }
            }
        }
    };
}
