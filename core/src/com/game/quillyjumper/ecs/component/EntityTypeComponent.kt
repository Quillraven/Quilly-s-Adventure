package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class EntityType {
    Player,
    Scenery,
    Enemy,
    Item,
    Other
}

class EntityTypeComponent(var type: EntityType = EntityType.Other) : Component {
    companion object {
        val mapper = mapperFor<EntityTypeComponent>()
    }
}