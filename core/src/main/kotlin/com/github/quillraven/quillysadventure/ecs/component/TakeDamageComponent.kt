package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class TakeDamageComponent : Component, Pool.Poolable {
    var damage: Float = 0f
    lateinit var source: Entity

    companion object {
        val mapper = mapperFor<TakeDamageComponent>()
    }

    override fun reset() {
        damage = 0f
    }
}

val Entity.takeDamageCmp: TakeDamageComponent
    get() = this[TakeDamageComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a take damage component which is null")
