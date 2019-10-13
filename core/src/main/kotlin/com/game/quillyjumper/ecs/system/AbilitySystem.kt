package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.log.logger

private val LOG = logger<AbilitySystem>()

class AbilitySystem :
    IteratingSystem(allOf(AbilityComponent::class, StatsComponent::class).exclude(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.abilityCmp.let { ability ->
            ability.abilities.forEach { it.update(deltaTime) }

            if (ability.order == CastOrder.CAST) {
                if (ability.abilityToCastIdx < 0 || ability.abilityToCastIdx >= ability.abilities.size) {
                    LOG.error { "Trying to cast an invalid ability of index ${ability.abilityToCastIdx}. Available abilities: ${ability.abilities}" }
                    return
                }

                // cast ability if not on cooldown and of entity has enough mana
                val abilityToCast = ability.abilities[ability.abilityToCastIdx]
                if (abilityToCast.canCast()) {
                    abilityToCast.cast()
                }

                // reset cast order
                ability.order = CastOrder.NONE
            }
        }
    }
}