package com.game.quillyjumper.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.execute
import com.game.quillyjumper.input.InputKey

enum class GlobalState : State<EntityAgent> {
    CHECK_MOVE_INPUT {
        override fun update(agent: EntityAgent) {
            agent.entity.execute(
                AttackComponent.mapper,
                MoveComponent.mapper,
                JumpComponent.mapper
            ) { attack, move, jump ->
                if (agent.keyPressed(InputKey.KEY_ATTACK) && attack.attackTime <= 0f) {
                    attack.order = AttackOrder.ATTACK_ONCE
                }

                move.order = when {
                    agent.keyPressed(InputKey.KEY_LEFT) -> MoveOrder.LEFT
                    agent.keyPressed(InputKey.KEY_RIGHT) -> MoveOrder.RIGHT
                    else -> MoveOrder.NONE
                }

                jump.order = when {
                    agent.keyPressed(InputKey.KEY_JUMP) -> JumpOrder.JUMP
                    else -> JumpOrder.NONE
                }
            }
        }
    };

    override fun enter(agent: EntityAgent?) {
    }

    override fun exit(agent: EntityAgent?) {
    }

    override fun onMessage(agent: EntityAgent?, telegram: Telegram?) = false
}