package com.game.quillyjumper.input

import com.game.quillyjumper.ecs.component.JumpOrder
import com.game.quillyjumper.ecs.component.MoveOrder

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

    override fun move(order: MoveOrder) {
        keyState[InputKey.KEY_LEFT] = order == MoveOrder.LEFT
        keyState[InputKey.KEY_RIGHT] = order == MoveOrder.RIGHT
    }

    override fun jump(order: JumpOrder) {
        keyState[InputKey.KEY_JUMP] = order == JumpOrder.JUMP
    }
}
