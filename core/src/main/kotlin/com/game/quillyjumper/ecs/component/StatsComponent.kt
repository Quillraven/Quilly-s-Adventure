package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import ktx.ashley.mapperFor

class StatsComponent(var damage: Float = 0f, var life: Float = 1f, var armor: Float = 0f) : Component {
    companion object {
        val mapper = mapperFor<StatsComponent>()
    }
}

val Entity.statsCmp: StatsComponent
    get() = this[StatsComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a stats component which is null")