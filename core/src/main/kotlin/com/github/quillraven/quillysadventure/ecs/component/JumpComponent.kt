package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class JumpOrder {
    JUMP, NONE
}

class JumpComponent : Component, Pool.Poolable {
    var order: JumpOrder = JumpOrder.NONE
    var jumpTime: Float = 0f
    var maxJumpTime: Float = 1.1f

    companion object {
        val mapper = mapperFor<JumpComponent>()
    }

    override fun reset() {
        order = JumpOrder.NONE
        jumpTime = 0f
    }
}

val Entity.jumpCmp: JumpComponent
    get() = this[JumpComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a jump component which is null")
