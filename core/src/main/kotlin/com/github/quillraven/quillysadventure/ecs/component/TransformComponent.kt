package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor
import ktx.math.vec2

class TransformComponent : Component, Comparable<TransformComponent>, Pool.Poolable {
    var position: Vector2 = vec2(0f, 0f)
    var z: Int = 0
    var prevPosition: Vector2 = vec2(0f, 0f)
    var interpolatedPosition: Vector2 = vec2(0f, 0f)
    var size: Vector2 = vec2(0f, 0f)

    // entities are sorted first by their z index (background/foreground) and then by their
    // y-coordinate of their position on the current layer (=z index)
    override fun compareTo(other: TransformComponent): Int {
        val zDiff = other.z.compareTo(z)
        return if (zDiff == 0) other.position.y.compareTo(position.y) else zDiff
    }

    companion object {
        val mapper = mapperFor<TransformComponent>()
    }

    override fun reset() {
        position.set(0f, 0f)
        prevPosition.set(position)
        interpolatedPosition.set(position)
        size.set(0f, 0f)
    }
}

val Entity.transfCmp: TransformComponent
    get() = this[TransformComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a transform component which is null")
