package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import ktx.ashley.mapperFor

class TransformComponent(var position: Vector2 = Vector2(0f, 0f),
                         var z: Int = 0,
                         var prevPosition: Vector2 = Vector2(0f, 0f),
                         var interpolatedPosition: Vector2 = Vector2(0f, 0f)) : Component {
    companion object {
        val mapper = mapperFor<TransformComponent>()
    }
}