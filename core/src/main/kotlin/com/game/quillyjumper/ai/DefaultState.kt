package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.ecs.component.AnimationType
import com.game.quillyjumper.ecs.component.aniCmp
import com.game.quillyjumper.ecs.component.stateCmp

interface EntityState : State<Entity> {
    val aniType: AnimationType
    val loopAni: Boolean

    override fun enter(entity: Entity) {
        entity.aniCmp.run {
            this.animationType = aniType
            this.loopAnimation = loopAni
        }
        entity.stateCmp.stateTime = 0f
    }

    override fun exit(entity: Entity) {
    }

    override fun update(entity: Entity) {
    }

    override fun onMessage(entity: Entity, telegram: Telegram): Boolean = false
}

enum class DefaultState(override val aniType: AnimationType, override val loopAni: Boolean = true) : EntityState {
    NONE(AnimationType.IDLE);
}