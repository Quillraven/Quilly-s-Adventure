package com.game.quillyjumper.event

import com.game.quillyjumper.ecs.component.MoveDirection

interface InputListener {
    fun move(direction: MoveDirection) {}
}