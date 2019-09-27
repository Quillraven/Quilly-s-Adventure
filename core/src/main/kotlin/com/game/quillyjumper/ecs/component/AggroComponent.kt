package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import ktx.ashley.get
import ktx.ashley.mapperFor

class AggroComponent : Component {
    val aggroEntities = Array<Entity>(4)

    companion object {
        val mapper = mapperFor<AggroComponent>()
    }
}

val Entity.aggroCmp: AggroComponent
    get() = this[AggroComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an aggro component which is null")