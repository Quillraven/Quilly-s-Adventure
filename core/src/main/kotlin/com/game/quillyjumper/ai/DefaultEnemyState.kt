package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.game.quillyjumper.ecs.component.*

enum class DefaultEnemyState(override val aniType: AnimationType, override val loopAni: Boolean = true) : EntityState {
    IDLE(AnimationType.IDLE) {
        override fun update(entity: Entity) {
            when {
                entity.attackCmp.order == AttackOrder.START -> entity.stateCmp.stateMachine.changeState(ATTACK)
                entity.moveCmp.order != MoveOrder.NONE -> entity.stateCmp.stateMachine.changeState(RUN)
            }

        }
    },
    RUN(AnimationType.RUN) {
        override fun update(entity: Entity) {
            when {
                entity.attackCmp.order == AttackOrder.START -> entity.stateCmp.stateMachine.changeState(ATTACK)
                entity.moveCmp.order == MoveOrder.NONE -> entity.stateCmp.stateMachine.changeState(IDLE)
            }
        }
    },
    ATTACK(AnimationType.ATTACK, false) {
        override fun enter(entity: Entity) {
            // stop any movement
            entity.moveCmp.order = MoveOrder.NONE
            entity.jumpCmp.order = JumpOrder.NONE
            // attack once
            entity.attackCmp.order = AttackOrder.ATTACK_ONCE
            // update animation
            super.enter(entity)
        }

        override fun update(entity: Entity) {
            if (entity.aniCmp.isAnimationFinished()) {
                entity.attackCmp.order = AttackOrder.NONE
                entity.stateCmp.stateMachine.changeState(IDLE)
            }
        }
    }
}