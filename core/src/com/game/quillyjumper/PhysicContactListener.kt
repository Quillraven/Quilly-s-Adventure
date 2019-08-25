package com.game.quillyjumper

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.game.quillyjumper.ecs.component.PhysicComponent
import ktx.ashley.get
import ktx.log.logger

private val LOG = logger<PhysicContactListener>()

class PhysicContactListener : ContactListener {
    override fun beginContact(contact: Contact) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        if ("WATER" == bodyA.userData && bodyB.userData is Entity) {
            setGravityScaleOfEntity(bodyB.userData as Entity, 0.01f)
        } else if ("WATER" == bodyB.userData && bodyA.userData is Entity) {
            setGravityScaleOfEntity(bodyA.userData as Entity, 0.01f)
        }
    }

    override fun endContact(contact: Contact) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        if ("WATER" == bodyA.userData && bodyB.userData is Entity) {
            setGravityScaleOfEntity(bodyB.userData as Entity, 1f)
        } else if ("WATER" == bodyB.userData && bodyA.userData is Entity) {
            setGravityScaleOfEntity(bodyA.userData as Entity, 1f)
        }
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }

    private fun setGravityScaleOfEntity(entity: Entity, gravityScale: Float) {
        LOG.debug { "Adjusting gravityScale to $gravityScale" }
        entity[PhysicComponent.mapper]?.let { physic ->
            physic.body.gravityScale = gravityScale
            physic.body.applyForceToCenter(0f, 10f, true)
        }
    }
}
