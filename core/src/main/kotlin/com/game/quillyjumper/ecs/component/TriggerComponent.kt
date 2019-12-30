package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.game.quillyjumper.trigger.Trigger
import ktx.ashley.get
import ktx.ashley.mapperFor

class TriggerComponent : Component, Pool.Poolable {
    lateinit var trigger: Trigger
    var triggerUpdate = false

    companion object {
        val mapper = mapperFor<TriggerComponent>()
    }

    override fun reset() {
        trigger.cleanup()
        triggerUpdate = false
    }
}

val Entity.triggerCmp: TriggerComponent
    get() = this[TriggerComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a trigger component which is null")
