package com.game.quillyjumper

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.game.quillyjumper.ecs.component.EntityTypeComponent
import ktx.ashley.get
import ktx.log.logger

private val LOG = logger<PhysicContactListener>()

class PhysicContactListener : ContactListener {
    override fun beginContact(contact: Contact) {
        val entityA = contact.fixtureA.body.userData as Entity
        val entityB = contact.fixtureB.body.userData as Entity

        LOG.debug { "entity ${entityA[EntityTypeComponent.mapper]?.type} collides with ${entityB[EntityTypeComponent.mapper]?.type}" }
    }

    override fun endContact(contact: Contact) {
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }
}
