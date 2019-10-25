package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class EntityType {
    PLAYER,
    SCENERY,
    ENEMY,
    NPC,
    ITEM,
    PORTAL,
    DAMAGE_EMITTER,
    OTHER;
}

class EntityTypeComponent(var type: EntityType = EntityType.OTHER) : Component {
    companion object {
        val mapper = mapperFor<EntityTypeComponent>()
    }
}

val Entity.typeCmp: EntityTypeComponent
    get() = this[EntityTypeComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an entity type component which is null")