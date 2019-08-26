package com.game.quillyjumper.event

import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.ecs.component.JumpDirection
import com.game.quillyjumper.ecs.component.MoveDirection
import com.game.quillyjumper.input.InputListener

class GameEventManager {
    private val inputListener = Array<InputListener>()

    fun addInputListener(listener: InputListener) = inputListener.add(listener)

    fun removeInputListener(listener: InputListener) = inputListener.removeValue(listener, true)

    fun dispatchInputMoveEvent(direction: MoveDirection) = inputListener.forEach { it.move(direction) }

    fun dispatchInputJumpEvent(direction: JumpDirection) = inputListener.forEach { it.jump(direction) }
}