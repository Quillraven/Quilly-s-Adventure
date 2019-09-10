package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class MoveOrder {
    LEFT, RIGHT, NONE;
}

class MoveComponent(
    var moveTime: Float = 0f,
    var maxSpeed: Float = 1f
) : Component {
    var order: MoveOrder = MoveOrder.NONE
        set(value) {
            if (value != field) moveTime = 0f
            field = value
        }

    companion object {
        val mapper = mapperFor<MoveComponent>()
    }
}
