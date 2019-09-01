package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class StateType {
    IDLE, RUN, JUMP, FALL
}

class StateComponent(var stateType: StateType = StateType.IDLE) : Component {
    companion object {
        val mapper = mapperFor<StateComponent>()
    }
}