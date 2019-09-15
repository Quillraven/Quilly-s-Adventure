package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class FacingDirection { LEFT, RIGHT }

class FacingComponent(var direction: FacingDirection = FacingDirection.RIGHT) : Component {
    companion object {
        val mapper = mapperFor<FacingComponent>()
    }
}