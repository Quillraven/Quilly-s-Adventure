package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Disposable
import ktx.ashley.mapperFor

class PhysicComponent : Component, Disposable {
    companion object {
        val mapper = mapperFor<PhysicComponent>()
    }

    lateinit var body: Body

    override fun dispose() {
        body.userData = null
        body.world.destroyBody(body)
    }
}