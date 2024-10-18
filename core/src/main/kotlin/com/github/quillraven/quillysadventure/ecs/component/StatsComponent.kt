package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class StatsComponent : Component, Pool.Poolable {
    var damage: Float = 0f
    var life: Float = 1f
    var maxLife: Float = 1f
    var mana: Float = 0f
    var maxMana: Float = 0f
    var armor: Float = 0f
    var level: Int = 1
    var xp: Int = 0
    var alive: Boolean = true

    override fun reset() {
        alive = true
    }

    companion object {
        val mapper = mapperFor<StatsComponent>()
    }
}

val Entity.statsCmp: StatsComponent
    get() = this[StatsComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a stats component which is null")
