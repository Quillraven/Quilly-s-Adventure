package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

class PlayerComponent : Component {
    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}
