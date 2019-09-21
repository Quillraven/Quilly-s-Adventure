package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey
import ktx.ashley.allOf
import ktx.ashley.get

class PlayerInputSystem(private val input: InputController) : IteratingSystem(allOf(PlayerComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[AttackComponent.mapper]?.let { attack ->
            attack.order = when {
                input.isPressed(InputKey.KEY_ATTACK) && attack.attackTime <= 0f -> AttackOrder.START
                else -> AttackOrder.NONE
            }
        }

        entity[MoveComponent.mapper]?.order = when {
            input.isPressed(InputKey.KEY_LEFT) -> MoveOrder.LEFT
            input.isPressed(InputKey.KEY_RIGHT) -> MoveOrder.RIGHT
            else -> MoveOrder.NONE
        }

        entity[JumpComponent.mapper]?.order = when {
            input.isPressed(InputKey.KEY_JUMP) -> JumpOrder.JUMP
            else -> JumpOrder.NONE
        }
    }
}