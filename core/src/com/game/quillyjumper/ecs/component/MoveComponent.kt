package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class MoveDirection { LEFT, RIGHT, STOP }

class MoveComponent(var direction: MoveDirection = MoveDirection.STOP,
                    var speed: Float = 0f,
                    var acceleration: Float = 6f,
                    var maxSpeed: Float = 1f) : Component {
    companion object {
        val mapper = mapperFor<MoveComponent>()
    }
}
