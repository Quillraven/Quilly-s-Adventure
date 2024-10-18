package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class FacingDirection { LEFT, RIGHT }

class FacingComponent : Component, Pool.Poolable {
    var direction: FacingDirection = FacingDirection.RIGHT

    override fun reset() {
        direction = FacingDirection.RIGHT
    }

    companion object {
        val mapper = mapperFor<FacingComponent>()
    }
}

val Entity.facingCmp: FacingComponent
    get() = this[FacingComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a facing component which is null")
