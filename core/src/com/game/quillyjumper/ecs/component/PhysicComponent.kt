package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Disposable
import ktx.ashley.get
import ktx.ashley.mapperFor

class PhysicComponent : Component, Disposable {
    companion object {
        val mapper = mapperFor<PhysicComponent>()
        val tmpVec2 = Vector2()
    }

    lateinit var body: Body

    override fun dispose() {
        body.userData = null
        body.world.destroyBody(body)
    }
}

val Entity.physic: PhysicComponent
    get() = this[PhysicComponent.mapper]!!