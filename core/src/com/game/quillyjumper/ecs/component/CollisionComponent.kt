package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import ktx.ashley.mapperFor

class CollisionComponent(
    val entities: Array<Entity> = Array(),
    var numGroundContacts: Int = 0
) : Component {
    companion object {
        val mapper = mapperFor<CollisionComponent>()
    }
}