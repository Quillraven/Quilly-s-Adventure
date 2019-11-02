package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import ktx.ashley.mapperFor

class TakeDamageComponent(var damage: Float = 0f) : Component {
    lateinit var source: Entity

    companion object {
        val mapper = mapperFor<TakeDamageComponent>()
    }
}

val Entity.takeDamageCmp: TakeDamageComponent
    get() = this[TakeDamageComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a take damage component which is null")