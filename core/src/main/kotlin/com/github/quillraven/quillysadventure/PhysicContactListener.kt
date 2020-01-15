package com.github.quillraven.quillysadventure

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.*
import com.github.quillraven.quillysadventure.ecs.component.*
import com.github.quillraven.quillysadventure.ecs.isRemoved
import ktx.ashley.get

class PhysicContactListener : ContactListener {
    /**
     * @param srcFixture the fixture of the entity for which you want to update the collision data
     * @param srcEntity the entity of the srcFixture
     * @param collFixture the fixture of the colliding entity from [beginContact] method
     * @param collEntity the colliding entity from [beginContact] method
     */
    private fun addCollisionData(srcFixture: Fixture, srcEntity: Entity, collFixture: Fixture, collEntity: Entity) {
        if (srcEntity[CollisionComponent.mapper] == null) return

        val collEntityType = collEntity.typeCmp.type
        when (srcFixture.userData) {
            FIXTURE_TYPE_FOOT_SENSOR -> if (collEntityType == EntityType.SCENERY) srcEntity.collCmp.numGroundContacts++
            FIXTURE_TYPE_AGGRO_SENSOR -> {
                if (collEntityType == EntityType.PLAYER && !collFixture.isSensor) {
                    srcEntity.aggroCmp.aggroEntities.add(collEntity)
                }
            }
            else -> {
                if (!collFixture.isSensor || collEntityType == EntityType.PORTAL || collEntityType == EntityType.ITEM || collEntityType == EntityType.TRIGGER) {
                    srcEntity.collCmp.entities.add(collEntity)
                }
            }
        }
    }

    /**
     * @param srcFixture the fixture of the entity for which you want to update the collision data
     * @param srcEntity the entity of the srcFixture
     * @param collFixture the fixture of the colliding entity from [endContact] method
     * @param collEntity the colliding entity from [endContact] method
     */
    private fun removeCollisionData(srcFixture: Fixture, srcEntity: Entity, collFixture: Fixture, collEntity: Entity) {
        if (srcEntity.isRemoved() || collEntity.isRemoved() || srcEntity[CollisionComponent.mapper] == null) return

        val collEntityType = collEntity.typeCmp.type
        when (srcFixture.userData) {
            FIXTURE_TYPE_FOOT_SENSOR -> if (collEntityType == EntityType.SCENERY) srcEntity.collCmp.numGroundContacts--
            FIXTURE_TYPE_AGGRO_SENSOR -> {
                if (collEntityType == EntityType.PLAYER && !collFixture.isSensor) {
                    srcEntity.aggroCmp.aggroEntities.remove(collEntity)
                }
            }
            else -> {
                if (!collFixture.isSensor || collEntityType == EntityType.PORTAL || collEntityType == EntityType.ITEM || collEntityType == EntityType.TRIGGER) {
                    srcEntity.collCmp.entities.remove(collEntity)
                }
            }
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

    /**
     * Certain objects should not really collide with each other but they should still trigger
     * contact events for [beginContact] and [endContact].
     *
     * E.g. the player can move through enemies and NPCs but he should still trigger contacts
     * in order for the enemies to attack him or for the NPC to show him the message dialog.
     *
     * Following objects do not physically collide:
     * * Player and Enemies
     * * Enemies with each other
     * * Player and NPCs
     */
    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        val fixtureA = contact.fixtureA
        val typeA = (fixtureA.body.userData as Entity).typeCmp.type
        val fixtureB = contact.fixtureB
        val typeB = (fixtureB.body.userData as Entity).typeCmp.type

        contact.isEnabled = when {
            typeA == EntityType.PLAYER && typeB == EntityType.ENEMY -> false
            typeA == EntityType.ENEMY && typeB == EntityType.PLAYER -> false
            typeA == EntityType.ENEMY && typeB == EntityType.ENEMY -> false
            typeA == EntityType.PLAYER && typeB == EntityType.NPC -> false
            typeA == EntityType.NPC && typeB == EntityType.PLAYER -> false
            typeA == EntityType.PLAYER && typeB == EntityType.SAVE_POINT -> false
            typeA == EntityType.SAVE_POINT && typeB == EntityType.PLAYER -> false
            else -> true
        }
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }
}
