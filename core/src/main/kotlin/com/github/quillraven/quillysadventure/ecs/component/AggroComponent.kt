package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class AggroComponent : Component, Pool.Poolable {
    val aggroEntities = ObjectSet<Entity>(4)

    companion object {
        val mapper = mapperFor<AggroComponent>()
    }

    override fun reset() {
        aggroEntities.clear()
    }
}

val Entity.aggroCmp: AggroComponent
    get() = this[AggroComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an aggro component which is null")
