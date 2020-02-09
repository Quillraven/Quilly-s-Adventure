package com.github.quillraven.quillysadventure.event

import com.badlogic.ashley.core.Entity
import com.github.quillraven.quillysadventure.ability.Ability
import com.github.quillraven.quillysadventure.trigger.Trigger

interface GameEventListener {
    fun activateSavepoint(savepoint: Entity) {}
    fun playerTriggerContact(player: Entity, trigger: Entity) {}
    fun characterDeath(character: Entity) {}
    fun characterDamaged(character: Entity, damage: Float, life: Float, maxLife: Float) {}
    fun characterHealLife(character: Entity, healAmount: Float, life: Float, maxLife: Float) {}
    fun characterHealMana(character: Entity, healAmount: Float, mana: Float, maxMana: Float) {}
    fun characterCast(character: Entity, ability: Ability, cost: Int, mana: Float, maxMana: Float) {}
    fun characterAttackReady(character: Entity) {}
    fun characterAttack(character: Entity) {}
    fun characterLevelUp(character: Entity, level: Int, xp: Int, xpNeeded: Int) {}
    fun characterXPGained(character: Entity, xp: Int, xpNeeded: Int) {}
    fun showDialogEvent(dialogKey: String) {}
    fun triggerFinishEvent(trigger: Trigger) {}
}
