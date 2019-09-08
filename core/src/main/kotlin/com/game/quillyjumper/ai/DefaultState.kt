package com.game.quillyjumper.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.ecs.component.AnimationType

enum class DefaultState(private val aniType: AnimationType, private val loopAni: Boolean = true) : State<EntityAgent> {
    IDLE(AnimationType.IDLE);

    override fun enter(agent: EntityAgent) {
        agent.animation.apply {
            this.animationType = aniType
            this.loopAnimation = loopAni
        }
        agent.state.stateTime = 0f
    }

    override fun exit(agent: EntityAgent) {
    }

    override fun update(agent: EntityAgent) {
    }

    override fun onMessage(agent: EntityAgent, telegram: Telegram): Boolean = false
}