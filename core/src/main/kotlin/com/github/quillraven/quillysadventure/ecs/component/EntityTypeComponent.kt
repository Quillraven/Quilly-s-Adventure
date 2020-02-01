package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class EntityType(
    // in case the entity type is a sensor it should still trigger
    // player contact events in box2d. Settings the next property
    // to true will have this effect
    val hasPlayerCollision: Boolean = false
) {
    PLAYER,
    SCENERY,
    ENEMY,
    NPC,
    ITEM(true),
    SAVE_POINT,
    PORTAL(true),
    DAMAGE_EMITTER,
    TRIGGER(true),
    OTHER;

    fun isEnemy(typeToCheck: EntityType): Boolean {
        return when (this) {
            PLAYER -> typeToCheck == ENEMY
            ENEMY -> typeToCheck == PLAYER
            else -> false
        }
    }
}

class EntityTypeComponent(var type: EntityType = EntityType.OTHER) : Component, Pool.Poolable {
    companion object {
        val mapper = mapperFor<EntityTypeComponent>()
    }

    override fun reset() {
        type = EntityType.OTHER
    }
}

val Entity.typeCmp: EntityTypeComponent
    get() = this[EntityTypeComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an entity type component which is null")
