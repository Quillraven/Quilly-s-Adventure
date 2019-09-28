package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class DamageComponent(var damage: Float = 0f, var lifeSpan: Float = 0f, val damagedEntities: ObjectSet<Entity> = ObjectSet(4)) :
    Component, Pool.Poolable {
    lateinit var source: Entity

    companion object {
        val mapper = mapperFor<DamageComponent>()
    }

    override fun reset() {
        damagedEntities.clear()
    }
}

val Entity.damageCmp: DamageComponent
    get() = this[DamageComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a damage component which is null")