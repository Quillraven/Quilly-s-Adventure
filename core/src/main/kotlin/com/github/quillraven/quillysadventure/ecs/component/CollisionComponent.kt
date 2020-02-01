package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class CollisionComponent(
    val entities: ObjectSet<Entity> = ObjectSet(4),
    var numGroundContacts: Int = 0
) : Component, Pool.Poolable {
    companion object {
        val mapper = mapperFor<CollisionComponent>()
    }

    override fun reset() {
        entities.clear()
        numGroundContacts = 0
    }
}

val Entity.collCmp: CollisionComponent
    get() = this[CollisionComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a collision component which is null")
