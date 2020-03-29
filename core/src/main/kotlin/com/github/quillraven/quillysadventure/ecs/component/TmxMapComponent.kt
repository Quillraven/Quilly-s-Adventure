package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class TmxMapComponent : Component, Pool.Poolable {
    var id = -1

    override fun reset() {
        id = -1
    }

    companion object {
        val mapper = mapperFor<TmxMapComponent>()
    }
}
