package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.quillraven.quillysadventure.ecs.component.AbilityComponent
import com.github.quillraven.quillysadventure.ecs.component.CastOrder
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.StatsComponent
import com.github.quillraven.quillysadventure.ecs.component.abilityCmp
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.log.error
import ktx.log.logger

private val LOG = logger<AbilitySystem>()


class AbilitySystem(private val gameEventManager: GameEventManager) :
    IteratingSystem(allOf(AbilityComponent::class, StatsComponent::class).exclude(RemoveComponent::class).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.abilityCmp.let { ability ->
            ability.abilities.forEach { it.update(deltaTime) }

            if (ability.order == CastOrder.CAST) {
                if (ability.abilityToCastIdx < 0 || ability.abilityToCastIdx >= ability.abilities.size) {
                    LOG.error {
                        "Trying to cast an invalid ability of index ${ability.abilityToCastIdx}." +
                                "Available abilities: ${ability.abilities}"
                    }
                    return
                }

                // cast ability if not on cooldown and of entity has enough mana
                val abilityToCast = ability.abilities[ability.abilityToCastIdx]
                if (abilityToCast.canCast()) {
                    abilityToCast.cast(gameEventManager)
                }

                // reset cast order
                ability.order = CastOrder.NONE
            }
        }
    }
}
