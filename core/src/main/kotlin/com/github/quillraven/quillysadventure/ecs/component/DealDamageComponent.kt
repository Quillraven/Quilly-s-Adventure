package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class DealDamageComponent : Component, Pool.Poolable {
    var damage: Float = 0f
    var lifeSpan: Float = 0f
    val damagedEntities: ObjectSet<Entity> = ObjectSet(4)
    var damageDelay: Float = 0f

    lateinit var source: Entity

    companion object {
        val mapper = mapperFor<DealDamageComponent>()
    }

    override fun reset() {
        damagedEntities.clear()
        damage = 0f
        lifeSpan = 0f
        damageDelay = 0f
    }
}

val Entity.dealDamageCmp: DealDamageComponent
    get() = this[DealDamageComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a deal damage component which is null")
