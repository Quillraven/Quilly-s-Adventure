package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.game.quillyjumper.ecs.component.AnimationType
import com.game.quillyjumper.ecs.component.MoveOrder
import com.game.quillyjumper.ecs.component.moveCmp
import com.game.quillyjumper.ecs.component.stateCmp

enum class DefaultEnemyState(override val aniType: AnimationType, override val loopAni: Boolean = true) : EntityState {
    IDLE(AnimationType.IDLE) {
        override fun update(entity: Entity) {
            if (entity.moveCmp.order != MoveOrder.NONE) entity.stateCmp.stateMachine.changeState(RUN)
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(entity: Entity) {
            if (entity.moveCmp.order == MoveOrder.NONE) entity.stateCmp.stateMachine.changeState(IDLE)
        }
    }
}