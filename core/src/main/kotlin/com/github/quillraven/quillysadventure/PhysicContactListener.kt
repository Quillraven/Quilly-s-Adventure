package com.github.quillraven.quillysadventure

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.github.quillraven.quillysadventure.ecs.component.CollisionComponent
import com.github.quillraven.quillysadventure.ecs.component.EntityType
import com.github.quillraven.quillysadventure.ecs.component.EntityTypeComponent
import com.github.quillraven.quillysadventure.ecs.component.aggroCmp
import com.github.quillraven.quillysadventure.ecs.component.collCmp
import com.github.quillraven.quillysadventure.ecs.component.typeCmp
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
                if (!collFixture.isSensor || collEntityType.hasPlayerCollision) {
                    srcEntity.collCmp.entities.add(collEntity)
                }
            }
        }
    }

    /**
     * Special `isRemoved` method for ContactListener because it seems to get triggered strangely from
     * time to time and entities are not 100% cleaned removed at that time.
     * Therefore two additional checks are added that it is really a relevant **collision entity** with a certain **type**.
     */
    private fun isRemoved(srcEntity: Entity, collEntity: Entity) =
        srcEntity.isRemoved() || collEntity.isRemoved()
                || srcEntity[CollisionComponent.mapper] == null
                || collEntity[EntityTypeComponent.mapper] == null

    /**
     * @param srcFixture the fixture of the entity for which you want to update the collision data
     * @param srcEntity the entity of the srcFixture
     * @param collFixture the fixture of the colliding entity from [endContact] method
     * @param collEntity the colliding entity from [endContact] method
     */
    private fun removeCollisionData(srcFixture: Fixture, srcEntity: Entity, collFixture: Fixture, collEntity: Entity) {
        if (isRemoved(srcEntity, collEntity)) {
            return
        }

        val collEntityType = collEntity.typeCmp.type
        when (srcFixture.userData) {
            FIXTURE_TYPE_FOOT_SENSOR -> if (collEntityType == EntityType.SCENERY) srcEntity.collCmp.numGroundContacts--
            FIXTURE_TYPE_AGGRO_SENSOR -> {
                if (collEntityType == EntityType.PLAYER && !collFixture.isSensor) {
                    srcEntity.aggroCmp.aggroEntities.remove(collEntity)
                }
            }
            else -> {
                if (!collFixture.isSensor || collEntityType.hasPlayerCollision) {
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
        val typeA = (contact.fixtureA.body.userData as Entity).typeCmp.type
        val typeB = (contact.fixtureB.body.userData as Entity).typeCmp.type

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

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit
}
