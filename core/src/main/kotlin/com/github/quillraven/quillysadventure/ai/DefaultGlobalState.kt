package com.github.quillraven.quillysadventure.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp

enum class DefaultGlobalState : State<Entity> {
    CHECK_ALIVE {
        override fun update(entity: Entity) {
            if (entity.statsCmp.life <= 0) {
                with(entity.stateCmp) {
                    // entity is dead -> no need to check for "alive" condition
                    // go to death state to play death animation before removing the entity
                    stateMachine.globalState = null
                    stateMachine.changeState(DefaultState.DEATH)
                }
            }
        }
    };

    override fun enter(entity: Entity?) = Unit

    override fun exit(entity: Entity?) = Unit

    override fun onMessage(entity: Entity?, telegram: Telegram?): Boolean = false
}
