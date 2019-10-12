package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.StringBuilder
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.floatingText
import com.game.quillyjumper.ecs.particleEffect
import ktx.ashley.allOf
import ktx.ashley.exclude
import kotlin.math.max

class DamageSystem(private val normalFont: BitmapFont) :
    IteratingSystem(allOf(DamageComponent::class, CollisionComponent::class).exclude(RemoveComponent::class).get()) {
    private val stringBuilder = StringBuilder(4)

    private fun isEnemy(sourceType: EntityType, collType: EntityType): Boolean {
        return when (sourceType) {
            EntityType.PLAYER -> collType == EntityType.ENEMY
            EntityType.ENEMY -> collType == EntityType.PLAYER
            else -> false
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.damageCmp.run {
            lifeSpan -= deltaTime
            if (lifeSpan <= 0f) {
                entity.add(engine.createComponent(RemoveComponent::class.java))
            } else {
                val sourceType = source.typeCmp.type
                entity.collCmp.entities.forEach { collEntity ->
                    if (isEnemy(sourceType, collEntity.typeCmp.type) && !damagedEntities.contains(collEntity)) {
                        // entity was not damaged yet -> deal damage to it
                        // let damage fluctuate within 10%
                        val stats = collEntity.statsCmp
                        val damageDealt = damage * MathUtils.random(0.9f, 1.1f)
                        // formula : dealtDamage = damage of source reduced by armor value
                        // armor of 1 reduces the damage by 1%; armor 10 reduces by 10%, armor 100 reduces by 100%
                        val damageValue = max(damageDealt * (1f - stats.armor * 0.01f), 0f)
                        stats.life -= damageValue

                        // create floating text to display damage number to player
                        val transform = collEntity.transfCmp
                        stringBuilder.clear()
                        stringBuilder.append(damageValue.toInt())
                        engine.floatingText(
                            transform.position.x + transform.size.x * 0.5f,
                            transform.position.y + transform.size.y,
                            normalFont,
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