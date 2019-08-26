package com.game.quillyjumper.input

import com.game.quillyjumper.ecs.component.MoveDirection

interface InputListener {
    fun move(direction: MoveDirection)
}