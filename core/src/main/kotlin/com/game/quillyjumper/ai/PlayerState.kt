package com.game.quillyjumper.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.assets.SoundAssets
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.execute
import ktx.ashley.get

enum class PlayerState(private val aniType: AnimationType, private val loopAni: Boolean = true) : State<EntityAgent> {
    IDLE(AnimationType.IDLE) {
        override fun update(agent: EntityAgent) {
            agent.entity.execute(
                JumpComponent.mapper,
                MoveComponent.mapper,
                AttackComponent.mapper,
                StateComponent.mapper
            ) { jump, move, attack, state ->
                when {
                    jump.order == JumpOrder.JUMP -> state.stateMachine.changeState(JUMP)
                    move.order != MoveOrder.NONE -> state.stateMachine.changeState(RUN)
                    attack.order != AttackOrder.NONE -> state.stateMachine.changeState(ATTACK)
                }
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(agent: EntityAgent) {
            agent.entity.execute(
                JumpComponent.mapper,
                PhysicComponent.mapper,
                AttackComponent.mapper,
                StateComponent.mapper
            ) { jump, physic, attack, state ->
                when {
                    jump.order == JumpOrder.JUMP -> state.stateMachine.changeState(JUMP)
                    physic.body.linearVelocity.x == 0f -> state.stateMachine.changeState(IDLE)
                    attack.order != AttackOrder.NONE -> state.stateMachine.changeState(ATTACK)
                }
            }
        }
    },
    JUMP(AnimationType.JUMP, false) {
        override fun enter(agent: EntityAgent) {
            agent.audioManager.play(SoundAssets.PLAYER_JUMP)
            super.enter(agent)
        }

        override fun update(agent: EntityAgent) {
            agent.entity.execute(
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
        override fun update(agent: EntityAgent) {
            agent.entity.execute(
                JumpComponent.mapper,
                CollisionComponent.mapper,
                StateComponent.mapper,
                PhysicComponent.mapper
            ) { jump, collision, state, physic ->
                jump.order = JumpOrder.NONE
                if (collision.numGroundContacts > 0) {
                    // reached ground again
                    state.stateMachine.changeState(if (physic.body.linearVelocity.x != 0f) RUN else IDLE)
                }
            }
        }
    },
    ATTACK(AnimationType.ATTACK, false) {
        override fun enter(agent: EntityAgent) {
            agent.audioManager.play(SoundAssets.SWING)
            super.enter(agent)
        }

        override fun update(agent: EntityAgent) {
            // stop any movement
            agent.entity[MoveComponent.mapper]?.order = MoveOrder.NONE

            agent.entity.execute(
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

    override fun enter(agent: EntityAgent) {
        agent.entity.execute(AnimationComponent.mapper, StateComponent.mapper) { animation, state ->
            animation.run {
                this.animationType = aniType
                this.loopAnimation = loopAni
            }
            state.stateTime = 0f
        }
    }

    override fun exit(agent: EntityAgent) {
    }

    override fun onMessage(agent: EntityAgent, telegram: Telegram): Boolean = false
}
