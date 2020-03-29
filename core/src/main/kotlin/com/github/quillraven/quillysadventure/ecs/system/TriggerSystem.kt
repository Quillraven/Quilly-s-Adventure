package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.TriggerComponent
import com.github.quillraven.quillysadventure.ecs.component.triggerCmp
import com.github.quillraven.quillysadventure.event.GameEventListener
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.exclude

class TriggerSystem(private val gameEventManager: GameEventManager) :
    IteratingSystem(allOf(TriggerComponent::class).exclude(RemoveComponent::class).get()), GameEventListener {
    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addGameEventListener(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeGameEventListener(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        with(entity.triggerCmp) {
            if (trigger.active) {
                // trigger is active
                if (!trigger.checkConditions()) {
                    // but conditions are not met -> set to inactive
                    trigger.active = false
                } else if (trigger.update(deltaTime)) {
                    // conditions met and completed all of its actions -> remove it
                    entity.add(engine.createComponent(RemoveComponent::class.java))
                    gameEventManager.dispatchTriggerFinishedEvent(trigger)
                }
            }
        }
    }

    override fun playerTriggerContact(player: Entity, trigger: Entity) {
        trigger.triggerCmp.trigger.run {
            active = true
            activatingCharacter = player
        }
    }
}
