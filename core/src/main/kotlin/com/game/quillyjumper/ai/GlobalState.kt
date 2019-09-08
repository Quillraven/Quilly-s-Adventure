package com.game.quillyjumper.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.ecs.component.JumpOrder
import com.game.quillyjumper.ecs.component.MoveOrder
import com.game.quillyjumper.input.InputKey

enum class GlobalState : State<EntityAgent> {
    CHECK_MOVE_INPUT {
        override fun update(agent: EntityAgent) {
            agent.move.order = when {
                agent.keyPressed(InputKey.KEY_LEFT) -> MoveOrder.LEFT
                agent.keyPressed(InputKey.KEY_RIGHT) -> MoveOrder.RIGHT
                else -> MoveOrder.NONE
            }

            agent.jump.order = when {
                agent.keyPressed(InputKey.KEY_JUMP) -> JumpOrder.JUMP
                else -> JumpOrder.NONE
            }
        }
    };

    override fun enter(agent: EntityAgent?) {
    }

    override fun exit(agent: EntityAgent?) {
    }

    override fun onMessage(agent: EntityAgent?, telegram: Telegram?) = false
}