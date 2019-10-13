package com.game.quillyjumper.input

import com.game.quillyjumper.ecs.component.AttackOrder
import com.game.quillyjumper.ecs.component.CastOrder
import com.game.quillyjumper.ecs.component.JumpOrder
import com.game.quillyjumper.ecs.component.MoveOrder

interface InputListener {
    fun move(order: MoveOrder) {}

    fun jump(order: JumpOrder) {}

    fun attack(order: AttackOrder) {}

    fun cast(order: CastOrder) {}

    fun exit() {}
}