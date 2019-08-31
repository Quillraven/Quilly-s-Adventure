package com.game.quillyjumper.input

import com.game.quillyjumper.ecs.component.JumpDirection
import com.game.quillyjumper.ecs.component.MoveDirection

enum class InputKey {
    MoveLeft,
    MoveRight,
    Jump
}

private operator fun BooleanArray.get(inputKey: InputKey) = this[inputKey.ordinal]

private operator fun BooleanArray.set(inputKey: InputKey, value: Boolean) = this.set(inputKey.ordinal, value)

class InputController : InputListener {
    private val keyState = BooleanArray(InputKey.values().size) { false }

    fun isPressed(inputKey: InputKey) = keyState[inputKey]

    override fun move(direction: MoveDirection) {
        when (direction) {
            MoveDirection.STOP -> {
                keyState[InputKey.MoveLeft] = false
                keyState[InputKey.MoveRight] = false
            }
            MoveDirection.LEFT -> {
                keyState[InputKey.MoveLeft] = true
                keyState[InputKey.MoveRight] = false
            }
            MoveDirection.RIGHT -> {
                keyState[InputKey.MoveLeft] = false
                keyState[InputKey.MoveRight] = true
            }
        }
    }

    override fun jump(direction: JumpDirection) {
        when (direction) {
            JumpDirection.JUMPING -> keyState[InputKey.Jump] = true
            else -> keyState[InputKey.Jump] = false
        }
    }
}
