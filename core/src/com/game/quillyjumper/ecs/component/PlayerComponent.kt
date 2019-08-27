package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

class PlayerComponent : Component {
    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}
