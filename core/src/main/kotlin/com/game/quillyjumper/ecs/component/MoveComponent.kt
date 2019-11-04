package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class MoveOrder {
    LEFT, RIGHT, NONE;
}

class MoveComponent(
    var moveTime: Float = 0f,
    var maxSpeed: Float = 1f,
    var lockMovement: Boolean = false
) : Component, Pool.Poolable {
    var order: MoveOrder = MoveOrder.NONE
        set(value) {
            if (value != field) moveTime = 0f
            field = value
        }

    companion object {
        val mapper = mapperFor<MoveComponent>()
    }

    override fun reset() {
        order = MoveOrder.NONE
        lockMovement = false
        moveTime = 0f
    }
}

val Entity.moveCmp: MoveComponent
    get() = this[MoveComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a move component which is null")