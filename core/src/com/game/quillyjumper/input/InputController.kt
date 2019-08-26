package com.game.quillyjumper.input

import com.game.quillyjumper.ecs.component.MoveDirection

enum class InputKey {
    MoveLeft,
    MoveRight,
    Jump
}

class InputController : InputListener {
    private val keyState = BooleanArray(InputKey.values().size) { false }

    fun isPressed(inputKey: InputKey) = keyState[inputKey.ordinal]

    override fun move(direction: MoveDirection) {
        when (direction) {
            MoveDirection.STOP -> {
                keyState[InputKey.MoveLeft.ordinal] = false
                keyState[InputKey.MoveRight.ordinal] = false
            }
            MoveDirection.LEFT -> {
                keyState[InputKey.MoveLeft.ordinal] = true
                keyState[InputKey.MoveRight.ordinal] = false
            }
            MoveDirection.RIGHT -> {
                keyState[InputKey.MoveLeft.ordinal] = false
                keyState[InputKey.MoveRight.ordinal] = true
            }
        }
    }
}