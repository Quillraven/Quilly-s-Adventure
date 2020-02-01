package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.quillysadventure.ecs.component.CollisionComponent
import com.github.quillraven.quillysadventure.ecs.component.DealDamageComponent
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.collCmp
import com.github.quillraven.quillysadventure.ecs.component.dealDamageCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.component.takeDamageCmp
import com.github.quillraven.quillysadventure.ecs.component.typeCmp
import com.github.quillraven.quillysadventure.ecs.isRemoved
import ktx.ashley.allOf
import ktx.ashley.exclude
import kotlin.math.max

class DealDamageSystem :
    IteratingSystem(
        allOf(
            DealDamageComponent::class,
            CollisionComponent::class
        ).exclude(RemoveComponent::class).get()
    ) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.dealDamageCmp.run {
            lifeSpan -= deltaTime
            damageDelay -= deltaTime

            when {
                damageDelay > 0f -> return // nothing to do yet
                // check if life came to an end or if the damage source got already removed -> if yes remove the deal damage entity
                lifeSpan <= 0f || source.isRemoved() -> entity.add(engine.createComponent(RemoveComponent::class.java))
                else -> dealDamage(this, entity)
            }
        }
    }

    private fun dealDamage(damageComponent: DealDamageComponent, damageEmitter: Entity) {
        val sourceType = damageComponent.source.typeCmp.type
        damageEmitter.collCmp.entities.forEach { collEntity ->
            if (collEntity.isRemoved()) {
                // ignore entities that get removed at the end of the frame
                return@forEach
            }

            if (sourceType.isEnemy(collEntity.typeCmp.type) && !damageComponent.damagedEntities.contains(collEntity)) {
                val stats = collEntity.statsCmp
                if (stats.life <= 0) {
                    // entity is already dead -> no need to deal damage
                    return@forEach
                }

                // entity was not damaged yet and it is still alive -> deal damage to it
                // let damage fluctuate within 10%
                val damageDealt = damageComponent.damage * MathUtils.random(0.9f, 1.1f)
                // formula : dealtDamage = damage of source reduced by armor value
                // armor of 1 reduces the damage by 1%; armor 10 reduces by 10%, armor 100 reduces by 100%
                collEntity.takeDamageCmp.also {
                    it.damage += max(damageDealt * (1f - stats.armor * 0.01f), 0f)
                    it.source = damageComponent.source
                }

                // remember entities that got already damaged once to not
                // damage them every frame
                damageComponent.damagedEntities.add(collEntity)
            }
        }
    }
}
