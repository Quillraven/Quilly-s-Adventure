package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.component.MoveDirection.*
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey.*
import ktx.ashley.allOf
import ktx.ashley.get

class PlayerStateSystem(private val input: InputController) :
        IteratingSystem(allOf(PlayerComponent::class, StateComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[StateComponent.mapper]?.let { state ->
            when (state.stateType) {
                StateType.IDLE -> processIdleState(entity, state)
                StateType.RUN -> processRunState(entity, state)
                StateType.JUMP -> processJumpState(entity, state)
                StateType.FALL -> processFallState(entity, state)
            }
        }
    }

    private fun moveToState(player: Entity, state: StateComponent, stateType: StateType, animationType: AnimationType) {
        state.stateType = stateType
        player[AnimationComponent.mapper]?.animationType = animationType
    }

    private fun setMoveDirection(player: Entity, move: MoveComponent, moveDirection: MoveDirection) {
        move.direction = moveDirection
        player[RenderComponent.mapper]?.sprite?.setFlip(moveDirection == LEFT, false)
    }

    private fun moveToRunState(
            player: Entity,
            state: StateComponent,
            move: MoveComponent,
            moveDirection: MoveDirection
    ) {
        moveToState(player, state, StateType.RUN, AnimationType.RUN)
        setMoveDirection(player, move, moveDirection)
    }

    private fun moveToJumpState(player: Entity, state: StateComponent) {
        moveToState(player, state, StateType.JUMP, AnimationType.JUMP)
        player[JumpComponent.mapper]?.direction = JumpDirection.JUMPING
    }

    private fun processIdleState(player: Entity, state: StateComponent) {
        player[MoveComponent.mapper]?.let { move ->
            when {
                input.isPressed(MoveRight) -> moveToRunState(player, state, move, RIGHT)
                input.isPressed(MoveLeft) -> moveToRunState(player, state, move, LEFT)
                input.isPressed(Jump) -> moveToJumpState(player, state)
            }
        }
    }

    private fun checkAndUpdateMoveDirectionIfNecessary(player: Entity, move: MoveComponent) {
        val direction = move.direction
        if (direction.isStopOrLeft() && !input.isPressed(MoveLeft) && input.isPressed(MoveRight)) {
            // move direction changed from left to right
            setMoveDirection(player, move, RIGHT)
        } else if (direction.isStopOrRight() && !input.isPressed(MoveRight) && input.isPressed(MoveLeft)) {
            // move direction changed from right to left
            setMoveDirection(player, move, LEFT)
        }
    }

    private fun processRunState(player: Entity, state: StateComponent) {
        player[PhysicComponent.mapper]?.let { physic ->
            player[MoveComponent.mapper]?.also { move ->
                val isMoving = physic.body.linearVelocity.x != 0f

                if (input.isPressed(Jump)) {
                    moveToJumpState(player, state)
                } else if (!input.isPressed(MoveLeft) && !input.isPressed(MoveRight)) {
                    // stop movement
                    player[MoveComponent.mapper]?.direction = STOP
                    if (!isMoving) {
                        moveToState(player, state, StateType.IDLE, AnimationType.IDLE)
                    }
                } else {
                    checkAndUpdateMoveDirectionIfNecessary(player, move)
                }
            }
        }
    }

    private fun processJumpState(player: Entity, state: StateComponent) {
        player[PhysicComponent.mapper]?.let { physic ->
            player[JumpComponent.mapper]?.let { jump ->
                player[MoveComponent.mapper]?.let { move ->
                    val isMoving = physic.body.linearVelocity.x != 0f

                    // allow player to change move direction in mid air
                    checkAndUpdateMoveDirectionIfNecessary(player, move)

                    if (jump.direction == JumpDirection.JUMPING && !input.isPressed(Jump)) {
                        // player wants to stop the jump
                        // set direction to FALLING because for some reason if we set it to stop
                        // then the else if below gets triggered BEFORE the player is really standing
                        // again on the ground. This means that the move animation will play while falling.
                        // I have no idea why this happens but setting the direction to FALLING solves it
                        jump.direction = JumpDirection.FALLING
                    } else if (jump.direction == JumpDirection.STOP) {
                        // jump  has stopped -> go to run or idle state
                        val targetState = if (isMoving) StateType.RUN else StateType.IDLE
                        val targetAnimation = if (isMoving) AnimationType.RUN else AnimationType.IDLE
                        moveToState(player, state, targetState, targetAnimation)
                    } else if (jump.direction == JumpDirection.FALLING) {
                        moveToState(player, state, StateType.FALL, AnimationType.FALL)
                    }
                }
            }
        }
    }

    private fun processFallState(player: Entity, state: StateComponent) {
        player[PhysicComponent.mapper]?.let { physic ->
            player[JumpComponent.mapper]?.let { jump ->
                player[MoveComponent.mapper]?.let { move ->
                    val isMoving = physic.body.linearVelocity.x != 0f

                    // allow player to change move direction in mid air
                    checkAndUpdateMoveDirectionIfNecessary(player, move)

                    if (jump.direction == JumpDirection.STOP) {
                        // fall  has stopped -> go to run or idle state
                        val targetState = if (isMoving) StateType.RUN else StateType.IDLE
                        val targetAnimation = if (isMoving) AnimationType.RUN else AnimationType.IDLE
                        moveToState(player, state, targetState, targetAnimation)
                    }
                }
            }
        }
    }
}