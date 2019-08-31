package com.game.quillyjumper

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.*
import com.game.quillyjumper.ecs.component.CollisionComponent
import com.game.quillyjumper.ecs.component.EntityType
import com.game.quillyjumper.ecs.component.EntityTypeComponent
import ktx.ashley.get

class PhysicContactListener : ContactListener {
    /**
     * @param fixture the fixture of the entity for which you want to update the collision data
     * @param collCmp collision component of the entity for which you want to update the collision data
     * @param entity the colliding entity from [beginContact] method
     */
    private fun addCollisionData(fixture: Fixture, collCmp: CollisionComponent, entity: Entity) {
        if (fixture.userData == FIXTURE_TYPE_FOOT_SENSOR) {
            if (entity[EntityTypeComponent.mapper]?.type == EntityType.SCENERY) {
                collCmp.numGroundContacts++
            }
        } else {
            collCmp.entities.add(entity)
        }
    }

    /**
     * @param fixture the fixture of the entity for which you want to update the collision data
     * @param collCmp collision component of the entity for which you want to update the collision data
     * @param entity the colliding entity from [endContact] method
     */
    private fun removeCollisionData(fixture: Fixture, collCmp: CollisionComponent, entity: Entity) {
        if (fixture.userData == FIXTURE_TYPE_FOOT_SENSOR) {
            if (entity[EntityTypeComponent.mapper]?.type == EntityType.SCENERY) {
                collCmp.numGroundContacts--
            }
        } else {
            collCmp.entities.removeValue(entity, true)
        }
    }

    override fun beginContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val entityA = fixtureA.body.userData as Entity
        val fixtureB = contact.fixtureB
        val entityB = fixtureB.body.userData as Entity

        entityA[CollisionComponent.mapper]?.let { addCollisionData(fixtureA, it, entityB) }
        entityB[CollisionComponent.mapper]?.let { addCollisionData(fixtureB, it, entityA) }
    }

    override fun endContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val entityA = fixtureA.body.userData as Entity
        val fixtureB = contact.fixtureB
        val entityB = fixtureB.body.userData as Entity

        entityA[CollisionComponent.mapper]?.let { removeCollisionData(fixtureA, it, entityB) }
        entityB[CollisionComponent.mapper]?.let { removeCollisionData(fixtureB, it, entityA) }
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }
}
