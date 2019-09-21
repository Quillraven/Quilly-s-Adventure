package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.StringBuilder
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.floatingText
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.logger
import kotlin.math.max

private val LOG = logger<DamageSystem>()

class DamageSystem(private val normalFont: BitmapFont) :
    IteratingSystem(allOf(DamageComponent::class, CollisionComponent::class).exclude(RemoveComponent::class).get()) {
    private val stringBuilder = StringBuilder(4)

    private fun isEnemy(source: Entity, collEntity: Entity): Boolean {
        source[EntityTypeComponent.mapper]?.let { type ->
            collEntity[EntityTypeComponent.mapper]?.let { collType ->
                return when (type.type) {
                    EntityType.PLAYER -> collType.type == EntityType.ENEMY
                    EntityType.ENEMY -> collType.type == EntityType.PLAYER
                    else -> false
                }
            }
        }
        return false
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[DamageComponent.mapper]?.let { damage ->
            damage.lifeSpan -= deltaTime
            if (damage.lifeSpan <= 0f) {
                entity.add(engine.createComponent(RemoveComponent::class.java))
            } else {
                entity[CollisionComponent.mapper]?.let { collision ->
                    collision.entities.forEach { collEntity ->
                        if (isEnemy(damage.source, collEntity) && !damage.damagedEntities.contains(collEntity)) {
                            collEntity[StatsComponent.mapper]?.let { stats ->
                                // entity was not damaged yet -> deal damage to it
                                // let damage fluctuate within 10%
                                val damageDealt = damage.damage * MathUtils.random(0.9f, 1.1f)
                                // formula : dealtDamage = damage of source - armor value of enemy
                                val damageValue = max(damageDealt - stats.armor, 0f)
                                stats.life -= damageValue

                                // create floating text to display damage number to player
                                collEntity[TransformComponent.mapper]?.let { transform ->
                                    stringBuilder.clear()
                                    stringBuilder.append(damageValue.toInt())
                                    engine.floatingText(
                                        transform.position.x + transform.size.x * 0.5f - 0.1f,
                                        transform.position.y + transform.size.y,
                                        normalFont,
                                        stringBuilder,
                                        Color.RED,
                                        0f,
                                        -1f,
                                        1.25f
                                    )
                                }

                                // remember entities that got already damaged once to not
                                // damage them every frame
                                damage.damagedEntities.add(collEntity)

                                LOG.debug { "$damageValue damage dealt. Life left: ${stats.life}" }
                            }
                        }
                    }
                }
            }
        }
    }
}