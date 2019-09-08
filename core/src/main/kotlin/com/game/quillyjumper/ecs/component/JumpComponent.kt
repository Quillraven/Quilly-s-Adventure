package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class JumpOrder {
    JUMP, NONE
}

class JumpComponent(
    var order: JumpOrder = JumpOrder.NONE,
    var jumpTime: Float = 0f
) : Component {
    companion object {
        val mapper = mapperFor<JumpComponent>()
    }
}
