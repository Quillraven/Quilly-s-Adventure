package com.github.quillraven.quillysadventure.configuration

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.github.quillraven.quillysadventure.UNIT_SCALE
import com.github.quillraven.quillysadventure.ability.AbilityEffect
import com.github.quillraven.quillysadventure.ability.SpinEffect
import com.github.quillraven.quillysadventure.ai.DefaultEnemyState
import com.github.quillraven.quillysadventure.ai.DefaultState
import com.github.quillraven.quillysadventure.ai.EntityState
import com.github.quillraven.quillysadventure.ai.MinotaurState
import com.github.quillraven.quillysadventure.ai.PlayerState
import com.github.quillraven.quillysadventure.ecs.component.EntityType
import com.github.quillraven.quillysadventure.ecs.component.ModelType
import ktx.log.error
import ktx.log.logger
import ktx.math.vec2
import java.util.*

private val LOG = logger<CharacterConfigurations>()

enum class Character {
    PLAYER,
    BLUE_SLIME,
    ORANGE_SLIME,
    FLIPPY,
    SAVE_POINT,
    MINOTAUR,
    SKELETAL,
    DWARF,
    GIRL
}

class CharacterCfg(val characterType: Character, val entityType: EntityType, val modelType: ModelType) {
    var speed = 0f
    val size = vec2(1f, 1f)
    val collBodyOffset = vec2(0f, 0f)
    var attackRange = 0f
    var attackCooldown = 0f
    var damage = 0f
    var damageDelay = 0f
    var life = 0f
    var mana = 0f
    var armor = 0f
    var defaultState: EntityState = DefaultState.NONE
    var aggroRange = 0f
    var xp = 0
    val abilities = Array<AbilityEffect>(0)

    fun size(width: Float, height: Float): Vector2 = size.set(width, height)

    fun collisionBodyOffset(offsetX: Float, offsetY: Float): Vector2 = collBodyOffset.set(offsetX, offsetY)
}

class CharacterConfigurations : EnumMap<Character, CharacterCfg>(Character::class.java) {
    private val defaultCfg = CharacterCfg(Character.PLAYER, EntityType.OTHER, ModelType.UNKNOWN)

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
        this[id] = CharacterCfg(id, entityType, modelType).apply(init)
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

fun loadCharacterConfigurations(): CharacterConfigurations {
    return characterConfigurations {
        cfg(Character.PLAYER, EntityType.PLAYER, ModelType.PLAYER) {
            speed = 4f
            size(0.5f, 0.8f)
            attackRange = 0.4f
            attackCooldown = 1f
            damage = 6f
            life = 40f
            mana = 10f
            armor = 2f
            defaultState = PlayerState.IDLE
        }
        cfg(Character.BLUE_SLIME, EntityType.ENEMY, ModelType.BLUE_SLIME) {
            speed = 0.3f
            size(0.5f, 0.5f)
            attackRange = 0.15f
            damageDelay = 0.25f
            attackCooldown = 2f
            damage = 2f
            life = 10f
            defaultState = DefaultEnemyState.IDLE
            aggroRange = 2.5f
            xp = 10
        }
        cfg(Character.ORANGE_SLIME, EntityType.ENEMY, ModelType.ORANGE_SLIME) {
            speed = 0.5f
            size(0.4f, 0.4f)
            attackRange = 0.1f
            attackCooldown = 1.5f
            damage = 3f
            life = 5f
            defaultState = DefaultEnemyState.IDLE
            aggroRange = 3f
            xp = 15
        }
        cfg(Character.DWARF, EntityType.ENEMY, ModelType.DWARF) {
            speed = 0.6f
            size(0.5f, 0.6f)
            attackRange = 0.2f
            damageDelay = 0.45f
            attackCooldown = 1.5f
            damage = 3f
            life = 10f
            defaultState = DefaultEnemyState.IDLE
            aggroRange = 3f
            xp = 20
        }
        cfg(Character.FLIPPY, EntityType.NPC, ModelType.FLIPPY) {
            size(0.65f, 2f)
            collisionBodyOffset(3f * UNIT_SCALE, 0f)
        }
        cfg(Character.SAVE_POINT, EntityType.SAVE_POINT, ModelType.EYE_MONSTER)
        cfg(Character.GIRL, EntityType.NPC, ModelType.GIRL) {
            size(0.5f, 0.6f)
            speed = 0.9f
        }
        cfg(Character.MINOTAUR, EntityType.ENEMY, ModelType.MINOTAUR) {
            speed = 0.90f
            size(0.7f, 1.2f)
            attackRange = 0.4f
            damageDelay = 0.3f
            attackCooldown = 5f
            damage = 5f
            life = 50f
            defaultState = MinotaurState.IDLE
            aggroRange = 10f
            xp = 100
            abilities.add(SpinEffect)
        }
        cfg(Character.SKELETAL, EntityType.ENEMY, ModelType.SKELETAL) {
            speed = 0.6f
            size(0.4f, 0.8f)
            collisionBodyOffset(-3f * UNIT_SCALE, 0f)
            attackRange = 0.6f
            attackCooldown = 3.5f
            damage = 4f
            damageDelay = 0.7f
            life = 23f
            defaultState = DefaultEnemyState.IDLE
            aggroRange = 10f
            xp = 40
        }
    }
}
