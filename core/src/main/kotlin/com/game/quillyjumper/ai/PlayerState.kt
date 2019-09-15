package com.game.quillyjumper.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.assets.SoundAssets
import com.game.quillyjumper.ecs.component.AnimationType
import com.game.quillyjumper.ecs.component.AttackOrder
import com.game.quillyjumper.ecs.component.JumpOrder
import com.game.quillyjumper.ecs.component.MoveOrder

enum class PlayerState(private val aniType: AnimationType, private val loopAni: Boolean = true) : State<EntityAgent> {
    IDLE(AnimationType.IDLE) {
        override fun update(agent: EntityAgent) {
            when {
                agent.jump.order == JumpOrder.JUMP -> agent.changeState(JUMP)
                agent.move.order != MoveOrder.NONE -> agent.changeState(RUN)
                agent.attack.order != AttackOrder.NONE -> agent.changeState(ATTACK)
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(agent: EntityAgent) {
            when {
                agent.jump.order == JumpOrder.JUMP -> agent.changeState(JUMP)
                agent.physic.body.linearVelocity.x == 0f -> agent.changeState(IDLE)
                agent.attack.order != AttackOrder.NONE -> agent.changeState(ATTACK)
            }
        }
    },
    JUMP(AnimationType.JUMP, false) {
        override fun enter(agent: EntityAgent) {
            agent.audioManager.play(SoundAssets.PLAYER_JUMP)
            super.enter(agent)
        }

        override fun update(agent: EntityAgent) {
            if ((agent.physic.body.linearVelocity.y <= 0f && agent.collision.numGroundContacts == 0) || agent.state.stateTime > 0.8f) {
                // player is in mid-air and falling down OR player exceeds maximum jump time
                agent.changeState(FALL)
            } else if (agent.collision.numGroundContacts > 0 && agent.jump.order == JumpOrder.NONE) {
                // player is on ground and does not want to jump anymore
                agent.changeState(if (agent.physic.body.linearVelocity.x != 0f) RUN else IDLE)
            }
        }
    },
    FALL(AnimationType.FALL) {
        override fun update(agent: EntityAgent) {
            agent.jump.order = JumpOrder.NONE
            if (agent.collision.numGroundContacts > 0) {
                // reached ground again
                agent.changeState(if (agent.physic.body.linearVelocity.x != 0f) RUN else IDLE)
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
            agent.move.order = MoveOrder.NONE

            if (agent.animation.isAnimationFinished()) {
                agent.attack.order = AttackOrder.NONE
                agent.changeState(if (agent.physic.body.linearVelocity.x != 0f) RUN else IDLE)
            }
        }
    };

    override fun enter(agent: EntityAgent) {
        agent.animation.apply {
            this.animationType = aniType
            this.loopAnimation = loopAni
        }
        agent.state.stateTime = 0f
    }

    override fun exit(agent: EntityAgent) {
    }

    override fun onMessage(agent: EntityAgent, telegram: Telegram): Boolean = false
}
