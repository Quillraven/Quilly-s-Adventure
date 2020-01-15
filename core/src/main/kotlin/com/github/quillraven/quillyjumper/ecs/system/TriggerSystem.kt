package com.github.quillraven.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.quillraven.quillyjumper.ecs.component.RemoveComponent
import com.github.quillraven.quillyjumper.ecs.component.TriggerComponent
import com.github.quillraven.quillyjumper.ecs.component.triggerCmp
import com.github.quillraven.quillyjumper.event.GameEventListener
import com.github.quillraven.quillyjumper.event.GameEventManager
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
                if (trigger.update(deltaTime)) {
                    entity.add(engine.createComponent(RemoveComponent::class.java))
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
