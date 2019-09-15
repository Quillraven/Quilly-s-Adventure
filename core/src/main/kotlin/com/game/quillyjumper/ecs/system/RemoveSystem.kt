package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.CollisionComponent
import com.game.quillyjumper.ecs.component.DamageComponent
import com.game.quillyjumper.ecs.component.RemoveComponent
import ktx.ashley.allOf
import ktx.ashley.get

class RemoveSystem(engine: Engine) : IteratingSystem(allOf(RemoveComponent::class).get()), EntityListener {
    private val collisionEntities = engine.getEntitiesFor(allOf(CollisionComponent::class).get())
    private val damageEntities = engine.getEntitiesFor(allOf(DamageComponent::class).get())

    override fun addedToEngine(engine: Engine) {
        engine.addEntityListener(this)
        super.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(this)
        super.removedFromEngine(engine)
    }

    override fun entityAdded(entity: Entity?) {
        // do nothing
    }

    override fun entityRemoved(entity: Entity?) {
        // cleanup entity from any arrays where it is referenced
        collisionEntities.forEach { it[CollisionComponent.mapper]?.entities?.removeValue(entity, true) }
        damageEntities.forEach { it[DamageComponent.mapper]?.damagedEntities?.removeValue(entity, true) }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        engine.removeEntity(entity)
    }
}