package com.game.quillyjumper.configuration

import com.badlogic.gdx.math.Vector2
import com.game.quillyjumper.ecs.component.EntityType
import com.game.quillyjumper.ecs.component.ModelType
import ktx.log.logger
import ktx.math.vec2
import java.util.*

private val LOG = logger<CharacterConfigurations>()

enum class Character {
    PLAYER,
    BLUE_SLIME,
    FLIPPY
}

class CharacterCfg(val entityType: EntityType, val modelType: ModelType) {
    var speed = 0f
    val size = vec2(1f, 1f)
    val collBodyOffset = vec2(0f, 0f)

    fun size(width: Float, height: Float): Vector2 = size.set(width, height)

    fun collisionBodyOffset(offsetX: Float, offsetY: Float): Vector2 = collBodyOffset.set(offsetX, offsetY)
}

class CharacterConfigurations : EnumMap<Character, CharacterCfg>(Character::class.java) {
    private val defaultCfg = CharacterCfg(EntityType.OTHER, ModelType.UNKNOWN)

    fun cfg(
        id: Character,
        entityType: EntityType,
        modelType: ModelType,
        init: CharacterCfg.() -> Unit = { Unit }
    ) {
        if (this.containsKey(id)) {
            LOG.error { "Character configuration for id $id is already existing!" }
            return
        }
        this[id] = CharacterCfg(entityType, modelType).apply(init)
    }

    override operator fun get(key: Character): CharacterCfg {
        val cfg = super.get(key)
        if (cfg == null) {
            LOG.error { "Trying to access character cfg $key which is not configured yet!" }
            return defaultCfg
        }
        return cfg
    }
}

inline fun characterConfigurations(init: CharacterConfigurations.() -> Unit) = CharacterConfigurations().apply(init)