package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class EntityType {
    PLAYER,
    SCENERY,
    ENEMY,
    NPC,
    ITEM,
    SAVE_POINT,
    PORTAL,
    DAMAGE_EMITTER,
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