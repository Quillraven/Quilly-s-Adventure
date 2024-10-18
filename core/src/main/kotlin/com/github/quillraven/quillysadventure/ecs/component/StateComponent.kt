package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class StateComponent : Component, Pool.Poolable {
    var stateTime: Float = 0f
    val stateMachine: DefaultStateMachine<Entity, State<Entity>> = DefaultStateMachine()

    companion object {
        val mapper = mapperFor<StateComponent>()
    }

    override fun reset() {
        stateMachine.globalState = null
    }
}

val Entity.stateCmp: StateComponent
    get() = this[StateComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a state component which is null")
