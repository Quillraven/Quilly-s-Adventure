package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class MoveDirection {
    LEFT, RIGHT, STOP;

    fun isStopOrLeft() = this == LEFT || this == STOP

    fun isStopOrRight() = this == RIGHT || this == STOP
}

class MoveComponent(
    var direction: MoveDirection = MoveDirection.STOP,
    var speed: Float = 0f,
    var acceleration: Float = 6f,
    var maxSpeed: Float = 1f
) : Component {
    companion object {
        val mapper = mapperFor<MoveComponent>()
    }
}
