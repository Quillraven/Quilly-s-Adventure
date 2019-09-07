package com.game.quillyjumper.ai

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.game.quillyjumper.ecs.component.AnimationType
import com.game.quillyjumper.ecs.component.JumpDirection
import com.game.quillyjumper.ecs.component.MoveDirection
import com.game.quillyjumper.input.InputKey.*

enum class PlayerState(private val aniType: AnimationType, private val loopAni: Boolean = true) : State<EntityAgent> {
    IDLE(AnimationType.IDLE) {
        override fun update(agent: EntityAgent) {
            when {
                agent.keyPressed(KEY_RIGHT) -> agent.changeState(RUN)
                agent.keyPressed(KEY_LEFT) -> agent.changeState(RUN)
                agent.keyPressed(KEY_JUMP) -> agent.changeState(JUMP)
            }
        }
    },
    RUN(AnimationType.RUN) {
        override fun update(agent: EntityAgent) {
            val isMoving = agent.physic.body.linearVelocity.x != 0f

            if (agent.keyPressed(KEY_JUMP)) {
                agent.changeState(JUMP)
            } else if (!agent.keyPressed(KEY_LEFT) && !agent.keyPressed(KEY_RIGHT)) {
                // stop movement
                agent.move.direction = MoveDirection.STOP
                if (!isMoving) {
                    agent.changeState(IDLE)
                }
            } else {
                checkAndUpdateMoveDirectionIfNecessary(agent)
            }

            val moveDirection = agent.move.direction
            if (moveDirection != MoveDirection.STOP) {
                agent.render.sprite.setFlip(moveDirection == MoveDirection.LEFT, false)
            }
        }
    },
    JUMP(AnimationType.JUMP, false) {
        override fun enter(agent: EntityAgent) {
            super.enter(agent)
            agent.jump.direction = JumpDirection.JUMPING
        }

        override fun update(agent: EntityAgent) {
            val isMoving = agent.physic.body.linearVelocity.x != 0f

            // allow player to change move direction in mid air
            checkAndUpdateMoveDirectionIfNecessary(agent)

            if (agent.jump.direction == JumpDirection.JUMPING && !agent.keyPressed(KEY_JUMP)) {
                // player wants to stop the jump
                agent.jump.direction = JumpDirection.STOP
            } else if (agent.jump.direction == JumpDirection.STOP) {
                // jump  has stopped -> go to run or idle state
                agent.changeState(if (isMoving) RUN else IDLE)
            } else if (agent.jump.direction == JumpDirection.FALLING) {
                agent.changeState(FALL)
            }
        }
    },
    FALL(AnimationType.FALL) {
        override fun update(agent: EntityAgent) {
            val isMoving = agent.physic.body.linearVelocity.x != 0f

            // allow player to change move direction in mid air
            checkAndUpdateMoveDirectionIfNecessary(agent)

            if (agent.jump.direction == JumpDirection.STOP) {
                // fall  has stopped -> go to run or idle state
                agent.changeState(if (isMoving) RUN else IDLE)
            }
        }
    };

    override fun enter(agent: EntityAgent) {
        agent.animation.apply {
            this.animationType = aniType
            this.loopAnimation = loopAni
        }
    }

    override fun exit(agent: EntityAgent) {
    }

    override fun onMessage(agent: EntityAgent, telegram: Telegram): Boolean = false

    private fun setMoveDirection(agent: EntityAgent, moveDirection: MoveDirection) {
        agent.move.direction = moveDirection
        if (moveDirection == MoveDirection.LEFT || moveDirection == MoveDirection.RIGHT) {
            // only update in case of a specific direction
            // otherwise keep the sprite flipped as  it is (e.g. if movement stops)
            agent.render.sprite.setFlip(moveDirection == MoveDirection.LEFT, false)
        }
    }

    fun checkAndUpdateMoveDirectionIfNecessary(agent: EntityAgent) {
        val direction = agent.move.direction
        if (direction.isStopOrLeft() && !agent.keyPressed(KEY_LEFT) && agent.keyPressed(KEY_RIGHT)) {
            // move direction changed from left to right
            setMoveDirection(agent, MoveDirection.RIGHT)
        } else if (direction.isStopOrRight() && !agent.keyPressed(KEY_RIGHT) && agent.keyPressed(KEY_LEFT)) {
            // move direction changed from right to left
            setMoveDirection(agent, MoveDirection.LEFT)
        } else if (!agent.keyPressed(KEY_LEFT) && !agent.keyPressed(KEY_RIGHT)) {
            setMoveDirection(agent, MoveDirection.STOP)
        }
    }
}
