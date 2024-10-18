package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor
import ktx.math.vec2

class PhysicComponent : Component, Pool.Poolable {
    val impulse: Vector2 = vec2()

    companion object {
        val mapper = mapperFor<PhysicComponent>()
        val tmpVec2 = vec2()
    }

    lateinit var body: Body

    override fun reset() {
        body.world.destroyBody(body)
        // clear user data AFTER body gets destroyed
        // because destroying a body seems to trigger the
        // collision listener which then fails with a NPE
        // in case the userData is not an entity
        body.userData = null
        impulse.set(0f, 0f)
    }
}

val Entity.physicCmp: PhysicComponent
    get() = this[PhysicComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a physic component which is null")
