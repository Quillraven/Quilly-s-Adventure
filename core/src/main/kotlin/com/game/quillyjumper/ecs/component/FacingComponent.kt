package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class FacingDirection { LEFT, RIGHT }

class FacingComponent(var direction: FacingDirection = FacingDirection.RIGHT) : Component, Pool.Poolable {
    companion object {
        val mapper = mapperFor<FacingComponent>()
    }

    override fun reset() {
        direction = FacingDirection.RIGHT
    }
}

val Entity.facingCmp: FacingComponent
    get() = this[FacingComponent.mapper]
            ?: throw KotlinNullPointerException("Trying to access a facing component which is null")
