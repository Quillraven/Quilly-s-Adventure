package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
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

val Entity.jumpCmp: JumpComponent
    get() = this[JumpComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a jump component which is null")
