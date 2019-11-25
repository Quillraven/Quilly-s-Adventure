package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.game.quillyjumper.trigger.Trigger
import ktx.ashley.mapperFor

class TriggerComponent : Component, Pool.Poolable {
    lateinit var trigger: Trigger

    companion object {
        val mapper = mapperFor<TriggerComponent>()
    }

    override fun reset() {
        trigger.cleanup()
    }
}