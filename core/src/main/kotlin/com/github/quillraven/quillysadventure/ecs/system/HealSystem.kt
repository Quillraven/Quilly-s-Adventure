package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.quillysadventure.ai.DefaultGlobalState
import com.github.quillraven.quillysadventure.ecs.component.HealComponent
import com.github.quillraven.quillysadventure.ecs.component.MoveComponent
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.StateComponent
import com.github.quillraven.quillysadventure.ecs.component.StatsComponent
import com.github.quillraven.quillysadventure.ecs.component.healCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.max

class HealSystem(private val gameEventManager: GameEventManager) :
    IteratingSystem(allOf(HealComponent::class, StatsComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val heal = entity.healCmp
        val stats = entity.statsCmp


        if (heal.resurrect) {
            stats.run {
                alive = true
                // in case of death the life might be negative and the follow up heal
                // might not be sufficient. Therefore, life is reset to at least 1 life
                life = max(1f, life)
                mana = max(1f, mana)
            }
            // the death state will lock the movement and also removes the global check alive state.
            // Since we want to resurrect the entity we need to revert those changes as well
            entity[MoveComponent.mapper]?.lockMovement = false
            entity[StateComponent.mapper]?.stateMachine?.globalState = DefaultGlobalState.CHECK_ALIVE
            entity.remove(RemoveComponent::class.java)
        }
        if (heal.life > 0) {
            stats.life = MathUtils.clamp(stats.life + heal.life, 0f, stats.maxLife)
            gameEventManager.dispatchCharacterHealLifeEvent(entity, heal.life, stats.life, stats.maxLife)
        }
        if (heal.mana > 0) {
            stats.mana = MathUtils.clamp(stats.mana + heal.mana, 0f, stats.maxMana)
            gameEventManager.dispatchCharacterHealManaEvent(entity, heal.mana, stats.mana, stats.maxMana)
        }

        entity.remove(HealComponent::class.java)
    }
}
