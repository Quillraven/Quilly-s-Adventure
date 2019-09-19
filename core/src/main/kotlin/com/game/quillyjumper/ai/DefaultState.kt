package com.game.quillyjumper.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.ecs.component.AnimationComponent
import com.game.quillyjumper.ecs.component.AnimationType
import com.game.quillyjumper.ecs.component.StateComponent
import com.game.quillyjumper.ecs.execute

enum class DefaultState(private val aniType: AnimationType, private val loopAni: Boolean = true) : State<EntityAgent> {
    IDLE(AnimationType.IDLE);

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

    override fun update(agent: EntityAgent) {
    }

    override fun onMessage(agent: EntityAgent, telegram: Telegram): Boolean = false
}