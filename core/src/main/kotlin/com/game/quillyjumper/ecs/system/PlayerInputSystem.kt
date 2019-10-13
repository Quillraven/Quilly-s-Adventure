package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey
import ktx.ashley.allOf

class PlayerInputSystem(private val input: InputController) : IteratingSystem(allOf(PlayerComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        with(entity.abilityCmp) {
            order = when {
                input.isPressed(InputKey.KEY_CAST) -> {
                    abilityToCastIdx = abilities.size - 1
                    CastOrder.BEGIN_CAST
                }
                else -> CastOrder.NONE
            }
        }

        entity.attackCmp.run {
            order = when {
                input.isPressed(InputKey.KEY_ATTACK) && attackTime <= 0f -> AttackOrder.START
                else -> AttackOrder.NONE
            }
        }

        entity.moveCmp.order = when {
            input.isPressed(InputKey.KEY_LEFT) -> MoveOrder.LEFT
            input.isPressed(InputKey.KEY_RIGHT) -> MoveOrder.RIGHT
            else -> MoveOrder.NONE
        }

        entity.jumpCmp.order = when {
            input.isPressed(InputKey.KEY_JUMP) -> JumpOrder.JUMP
            else -> JumpOrder.NONE
        }
    }
}