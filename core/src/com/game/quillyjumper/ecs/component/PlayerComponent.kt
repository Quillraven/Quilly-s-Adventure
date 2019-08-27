package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.get
import ktx.ashley.mapperFor

class PlayerComponent : Component {
    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}
