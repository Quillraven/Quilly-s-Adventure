package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.component.MoveDirection.*
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey.*
import ktx.ashley.allOf
import ktx.ashley.get

class PlayerStateSystem(private val controller: InputController) :
        IteratingSystem(allOf(PlayerComponent::class, StateComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[StateComponent.mapper]?.let { state ->
            when (state.stateType) {
                StateType.IDLE -> processIdleState(entity, state)
                StateType.RUN -> processRunState(entity, state)
            }
        }
    }

    private fun moveToIdlestate(player: Entity, state: StateComponent) {
        // switch state to idle state
        state.stateType = StateType.IDLE
        // update move direction to stop moving
        player[MoveComponent.mapper]?.direction = STOP
        // update jump direction to stop jump
        player[JumpComponent.mapper]?.direction = JumpDirection.STOP
        // update animation
        player[AnimationComponent.mapper]?.animationType = AnimationType.IDLE
    }

    private fun moveToJumpState(player: Entity, state: StateComponent) {
        // switch state to jump state
        state.stateType = StateType.JUMP
        // update jump direction to initiate jump
        player[JumpComponent.mapper]?.direction = JumpDirection.JUMPING
        // update animation
        player[AnimationComponent.mapper]?.animationType = AnimationType.JUMP
    }

    private fun processIdleState(player: Entity, state: StateComponent) {
        when {
            controller.isPressed(MoveRight) -> {
                // switch state to run state
                state.stateType = StateType.RUN
                // update move direction to initiate move
                player[MoveComponent.mapper]?.direction = RIGHT
                // update animation
                player[AnimationComponent.mapper]?.animationType = AnimationType.RUN
                // revert flip status of sprite to look to the right side
                player[RenderComponent.mapper]?.sprite?.flip(false, false)
            }
            controller.isPressed(MoveLeft) -> {
                // switch state to run state
                state.stateType = StateType.RUN
                // update move direction to initiate move
                player[MoveComponent.mapper]?.direction = LEFT
                // update animation
                player[AnimationComponent.mapper]?.animationType = AnimationType.RUN
                // revert flip status of sprite to look to the right side
                player[RenderComponent.mapper]?.sprite?.flip(true, false)
            }
            controller.isPressed(Jump) -> moveToJumpState(player, state)
        }
    }

    private fun processRunState(player: Entity, state: StateComponent) {
        player[PhysicComponent.mapper]?.let { physic ->
            player[MoveComponent.mapper]?.also { move ->
                val direction = move.direction
                val isMoving = physic.body.linearVelocity.x != 0f

                if (controller.isPressed(Jump)) {
                    moveToJumpState(player, state)
                } else if (!controller.isPressed(MoveLeft) && !controller.isPressed(MoveRight)) {
                    // stop movement
                    player[MoveComponent.mapper]?.direction = STOP
                    if (!isMoving) {
                        moveToIdlestate(player, state)
                    }
                } else if (direction == LEFT && !controller.isPressed(MoveLeft) && controller.isPressed(MoveRight)) {
                    // change move direction from left to right
                    move.direction = RIGHT
                    // flip sprite to look to the left side
                    player[RenderComponent.mapper]?.sprite?.flip(false, false)
                } else if (direction == RIGHT && !controller.isPressed(MoveRight) && controller.isPressed(MoveLeft)) {
                    // change move direction from right to left
                    move.direction = LEFT
                    // flip sprite to look to the left side
                    player[RenderComponent.mapper]?.sprite?.flip(true, false)
                }
            }
        }
    }
}