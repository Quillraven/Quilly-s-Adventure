package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class EntityType {
    PLAYER,
    SCENERY,
    ENEMY,
    ITEM,
    PORTAL,
    OTHER
}

class EntityTypeComponent(var type: EntityType = EntityType.OTHER) : Component {
    companion object {
        val mapper = mapperFor<EntityTypeComponent>()
    }
}