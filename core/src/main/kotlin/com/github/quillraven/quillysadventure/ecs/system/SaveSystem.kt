package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IntMap
import com.github.quillraven.quillysadventure.ability.AbilityEffect
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.SaveComponent
import com.github.quillraven.quillysadventure.ecs.component.abilityCmp
import com.github.quillraven.quillysadventure.ecs.component.playerCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.map.MapManager
import com.github.quillraven.quillysadventure.map.MapType
import ktx.ashley.allOf
import ktx.collections.set
import ktx.math.vec2
import ktx.preferences.flush
import ktx.preferences.set

const val KEY_SAVE_STATE = "saveState"

class SaveState {
    lateinit var currentMap: MapType
    lateinit var mapEntities: IntMap<MutableList<Int>>
    lateinit var tutorials: MutableList<Int>
    lateinit var checkpoint: Vector2
    var abilityToCastIdx = -1
    lateinit var abilities: Array<AbilityEffect>
    var damage = 0f
    var life = 0f
    var maxLife = 0f
    var mana = 0f
    var maxMana = 0f
    var armor = 0f
    var level = 0
    var xp = 0
}

class SaveSystem(
    private val preferences: Preferences,
    private val mapManager: MapManager
) : IteratingSystem(allOf(PlayerComponent::class, SaveComponent::class).get()) {
    private val saveState = SaveState().apply {
        mapEntities = IntMap()
        tutorials = mutableListOf()
        checkpoint = vec2()
        abilities = Array()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        saveState.run {
            // map information
            currentMap = mapManager.currentMap()
            mapEntities.clear()
            mapManager.storeMapEntities(currentMap)
            mapManager.mapEntityCache.forEach { (mapType, entities) ->
                val entityList = mutableListOf<Int>()
                for (i in 0 until entities.size) {
                    entityList.add(entities[i])
                }
                mapEntities[mapType.ordinal] = entityList
            }
            // player component
            tutorials.clear()
            val playerCmp = entity.playerCmp
            playerCmp.tutorials.forEach { tutorials.add(it.ordinal) }
            checkpoint = playerCmp.checkpoint
            // ability component
            abilities.clear()
            val abilityCmp = entity.abilityCmp
            abilityToCastIdx = abilityCmp.abilityToCastIdx
            abilityCmp.abilities.forEach { abilities.add(it.effect) }
            // stats component
            val statsCmp = entity.statsCmp
            this.damage = statsCmp.damage
            this.life = statsCmp.life
            this.maxLife = statsCmp.maxLife
            this.mana = statsCmp.mana
            this.maxMana = statsCmp.maxMana
            this.armor = statsCmp.armor
            this.level = statsCmp.level
            this.xp = statsCmp.xp
        }

        preferences.flush {
            this[KEY_SAVE_STATE] = saveState
        }

        // remove component since the save is done
        entity.remove(SaveComponent::class.java)
    }
}
