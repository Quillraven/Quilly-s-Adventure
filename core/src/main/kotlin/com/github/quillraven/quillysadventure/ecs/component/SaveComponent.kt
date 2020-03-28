package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class SaveComponent : Component, Pool.Poolable {
    override fun reset() = Unit
}
