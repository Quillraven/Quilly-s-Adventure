package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.StringBuilder
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.floatingText
import com.game.quillyjumper.ecs.particleEffect
import ktx.ashley.allOf
import ktx.ashley.exclude
import kotlin.math.max

class DamageSystem :
    IteratingSystem(allOf(DamageComponent::class, CollisionComponent::class).exclude(RemoveComponent::class).get()) {
    private val stringBuilder = StringBuilder(4)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.damageCmp.run {
            lifeSpan -= deltaTime
            if (lifeSpan <= 0f) {
                entity.add(engine.createComponent(RemoveComponent::class.java))
            } else {
                val sourceType = source.typeCmp.type
                entity.collCmp.entities.forEach { collEntity ->
                    if (sourceType.isEnemy(collEntity.typeCmp.type) && !damagedEntities.contains(collEntity)) {
                        val stats = collEntity.statsCmp
                        if (stats.life <= 0) {
                            // entity is already dead -> no need to deal damage
                            return@forEach
                        }

                        // entity was not damaged yet and it is still alive -> deal damage to it
                        // let damage fluctuate within 10%
                        val damageDealt = damage * MathUtils.random(0.9f, 1.1f)
                        // formula : dealtDamage = damage of source reduced by armor value
                        // armor of 1 reduces the damage by 1%; armor 10 reduces by 10%, armor 100 reduces by 100%
                        val damageValue = max(damageDealt * (1f - stats.armor * 0.01f), 0f)
                        stats.life -= damageValue

                        // TODO dispatch damage taken / death event so that abilities can react and maybe prevent the death
                        // after that step if the life is still <= 0 then the entity is really dead
                        // store killer entity to give it experience for the killing blow
                        collEntity.add(engine.createComponent(KillerComponent::class.java).apply { killer = source })

                        // create floating text to display damage number to player
                        val transform = collEntity.transfCmp
                        stringBuilder.clear()
                        stringBuilder.append(damageValue.toInt())
                        engine.floatingText(
                            transform.position.x + transform.size.x * 0.5f,
                            transform.position.y + transform.size.y,
                            FontType.DEFAULT,
                            stringBuilder,
                            Color.RED,
                            0f,
                            -1f,
                            1.25f
                        )
                        engine.particleEffect(
                            transform.position.x + transform.size.x * 0.25f,
                            transform.position.y + transform.size.y * 0.25f,
                            ParticleAssets.BLOOD
                        )

                        // remember entities that got already damaged once to not
                        // damage them every frame
                        damagedEntities.add(collEntity)
                    }
                }
            }
        }
    }
}