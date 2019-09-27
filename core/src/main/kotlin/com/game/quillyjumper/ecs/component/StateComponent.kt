package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import ktx.ashley.get
import ktx.ashley.mapperFor


class StateComponent(
    var stateTime: Float = 0f,
    val stateMachine: DefaultStateMachine<Entity, State<Entity>> = DefaultStateMachine()
) : Component {
    companion object {
        val mapper = mapperFor<StateComponent>()
    }
}

val Entity.stateCmp: StateComponent
    get() = this[StateComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a state component which is null")