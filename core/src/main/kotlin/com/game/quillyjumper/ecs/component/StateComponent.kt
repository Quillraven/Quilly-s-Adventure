package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.game.quillyjumper.ai.EntityAgent
import ktx.ashley.mapperFor


class StateComponent(val stateMachine: DefaultStateMachine<EntityAgent, State<EntityAgent>> = DefaultStateMachine()) :
    Component {
    companion object {
        val mapper = mapperFor<StateComponent>()
    }
}