package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.TriggerComponent
import com.game.quillyjumper.ecs.component.triggerCmp
import com.game.quillyjumper.event.GameEventListener
import com.game.quillyjumper.event.GameEventManager
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
        trigger.triggerCmp.trigger.active = true
    }
}
