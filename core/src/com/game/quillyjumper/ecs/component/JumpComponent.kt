package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class JumpDirection {
    JUMPING, FALLING, STOP
}

class JumpComponent(var direction: JumpDirection = JumpDirection.STOP,
                    var jumpTimer: Float = 0f,
                    var maxJumpTimer: Float = 0.5f) : Component {
    companion object {
        val mapper = mapperFor<JumpComponent>()
    }
}

val Entity.jump: JumpComponent
    get() = this[JumpComponent.mapper]!!