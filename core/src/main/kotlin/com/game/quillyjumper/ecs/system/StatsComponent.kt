package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

class StatsComponent(var damage: Float = 0f, var life: Float = 1f, var armor: Float = 0f) : Component {
    companion object {
        val mapper = mapperFor<StatsComponent>()
    }
}