package com.game.quillyjumper.input

import com.game.quillyjumper.ecs.component.JumpDirection
import com.game.quillyjumper.ecs.component.MoveDirection

enum class InputKey {
    KEY_LEFT,
    KEY_RIGHT,
    KEY_JUMP
}

private operator fun BooleanArray.get(inputKey: InputKey) = this[inputKey.ordinal]

private operator fun BooleanArray.set(inputKey: InputKey, value: Boolean) = this.set(inputKey.ordinal, value)

class InputController : InputListener {
    private val keyState = BooleanArray(InputKey.values().size) { false }

    fun isPressed(inputKey: InputKey) = keyState[inputKey]

    override fun move(direction: MoveDirection) {
        when (direction) {
            MoveDirection.STOP -> {
                keyState[InputKey.KEY_LEFT] = false
                keyState[InputKey.KEY_RIGHT] = false
            }
            MoveDirection.LEFT -> {
                keyState[InputKey.KEY_LEFT] = true
                keyState[InputKey.KEY_RIGHT] = false
            }
            MoveDirection.RIGHT -> {
                keyState[InputKey.KEY_LEFT] = false
                keyState[InputKey.KEY_RIGHT] = true
            }
        }
    }

    override fun jump(direction: JumpDirection) {
        when (direction) {
            JumpDirection.JUMPING -> keyState[InputKey.KEY_JUMP] = true
            else -> keyState[InputKey.KEY_JUMP] = false
        }
    }
}
