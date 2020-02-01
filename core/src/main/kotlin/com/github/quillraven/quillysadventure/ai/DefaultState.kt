package com.github.quillraven.quillysadventure.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.g2d.Animation
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.AttackOrder
import com.github.quillraven.quillysadventure.ecs.component.JumpOrder
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.ecs.component.aniCmp
import com.github.quillraven.quillysadventure.ecs.component.attackCmp
import com.github.quillraven.quillysadventure.ecs.component.jumpCmp
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp

interface EntityState : State<Entity> {
    val aniType: AnimationType
    val aniMode: Animation.PlayMode

    override fun enter(entity: Entity) {
        entity.aniCmp.run {
            animationType = aniType
            mode = aniMode
            animationTime = 0f
        }
        entity.stateCmp.stateTime = 0f
    }

    override fun exit(entity: Entity) {
    }

    override fun update(entity: Entity) {
    }

    override fun onMessage(entity: Entity, telegram: Telegram): Boolean = false
}

enum class DefaultState(
    override val aniType: AnimationType,
    override val aniMode: Animation.PlayMode = Animation.PlayMode.LOOP
) : EntityState {
    NONE(AnimationType.IDLE, Animation.PlayMode.LOOP_PINGPONG),
    DEATH(AnimationType.DEATH, Animation.PlayMode.NORMAL) {
        override fun enter(entity: Entity) {
            super.enter(entity)
            with(entity.moveCmp) {
                order = MoveOrder.NONE
                lockMovement = true
            }
            entity.jumpCmp.order = JumpOrder.NONE
            entity.attackCmp.order = AttackOrder.NONE
        }

        override fun update(entity: Entity) {
            if (entity.aniCmp.isAnimationFinished()) {
                entity.statsCmp.alive = false
            }
        }
    };
}
