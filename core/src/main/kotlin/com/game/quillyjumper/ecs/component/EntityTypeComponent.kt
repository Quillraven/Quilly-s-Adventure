package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class EntityType {
    PLAYER,
    SCENERY,
    ENEMY,
    ITEM,
    PORTAL,
    DAMAGE_EMITTER,
    OTHER;

    // helper method for box2d contact listener to know if the entity is interested in collision events
    fun hasCollisionComponent() = this == PLAYER || this == ENEMY || this == DAMAGE_EMITTER
}

class EntityTypeComponent(var type: EntityType = EntityType.OTHER) : Component {
    companion object {
        val mapper = mapperFor<EntityTypeComponent>()
    }
}

val Entity.typeCmp: EntityTypeComponent
    get() = this[EntityTypeComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an entity type component which is null")