package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class MoveOrder {
    LEFT, RIGHT, NONE;
}

class MoveComponent(
    var order: MoveOrder = MoveOrder.NONE,
    var speed: Float = 0f,
    var maxSpeed: Float = 1f
) : Component {
    companion object {
        val mapper = mapperFor<MoveComponent>()
    }
}
