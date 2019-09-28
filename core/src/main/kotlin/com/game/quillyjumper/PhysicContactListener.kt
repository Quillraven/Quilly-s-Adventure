package com.game.quillyjumper

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.*
import com.game.quillyjumper.ecs.component.EntityType
import com.game.quillyjumper.ecs.component.aggroCmp
import com.game.quillyjumper.ecs.component.collCmp
import com.game.quillyjumper.ecs.component.typeCmp
import com.game.quillyjumper.ecs.isRemoved

class PhysicContactListener : ContactListener {
    /**
     * @param srcFixture the fixture of the entity for which you want to update the collision data
     * @param srcEntity the entity of the srcFixture
     * @param collFixture the fixture of the colliding entity from [beginContact] method
     * @param collEntity the colliding entity from [beginContact] method
     */
    private fun addCollisionData(srcFixture: Fixture, srcEntity: Entity, collFixture: Fixture, collEntity: Entity) {
        if (!srcEntity.typeCmp.type.hasCollisionComponent()) return

        val collEntityType = collEntity.typeCmp.type
        when (srcFixture.userData) {
            FIXTURE_TYPE_FOOT_SENSOR -> if (collEntityType == EntityType.SCENERY) srcEntity.collCmp.numGroundContacts++
            FIXTURE_TYPE_AGGRO_SENSOR -> {
                if (collEntityType == EntityType.PLAYER && !collFixture.isSensor) {
                    srcEntity.aggroCmp.aggroEntities.add(collEntity)
                }
            }
            else -> if (!collFixture.isSensor) srcEntity.collCmp.entities.add(collEntity)
        }
    }

    /**
     * @param srcFixture the fixture of the entity for which you want to update the collision data
     * @param srcEntity the entity of the srcFixture
     * @param collFixture the fixture of the colliding entity from [endContact] method
     * @param collEntity the colliding entity from [endContact] method
     */
    private fun removeCollisionData(srcFixture: Fixture, srcEntity: Entity, collFixture: Fixture, collEntity: Entity) {
        if (srcEntity.isRemoved() || !srcEntity.typeCmp.type.hasCollisionComponent() || collEntity.isRemoved()) return

        val collEntityType = collEntity.typeCmp.type
        when (srcFixture.userData) {
            FIXTURE_TYPE_FOOT_SENSOR -> if (collEntityType == EntityType.SCENERY) srcEntity.collCmp.numGroundContacts--
            FIXTURE_TYPE_AGGRO_SENSOR -> {
                if (collEntityType == EntityType.PLAYER && !collFixture.isSensor) {
                    srcEntity.aggroCmp.aggroEntities.remove(collEntity)
                }
            }
            else -> if (!collFixture.isSensor) srcEntity.collCmp.entities.remove(collEntity)
        }
    }

    override fun beginContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val entityA = fixtureA.body.userData as Entity
        val fixtureB = contact.fixtureB
        val entityB = fixtureB.body.userData as Entity

        addCollisionData(fixtureA, entityA, fixtureB, entityB)
        addCollisionData(fixtureB, entityB, fixtureA, entityA)
    }

    override fun endContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val entityA = fixtureA.body.userData as Entity
        val fixtureB = contact.fixtureB
        val entityB = fixtureB.body.userData as Entity

        removeCollisionData(fixtureA, entityA, fixtureB, entityB)
        removeCollisionData(fixtureB, entityB, fixtureA, entityA)
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }
}
