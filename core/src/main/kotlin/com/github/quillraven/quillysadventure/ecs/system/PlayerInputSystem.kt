package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.github.quillraven.quillysadventure.ecs.component.AttackOrder
import com.github.quillraven.quillysadventure.ecs.component.CastOrder
import com.github.quillraven.quillysadventure.ecs.component.JumpOrder
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.abilityCmp
import com.github.quillraven.quillysadventure.ecs.component.attackCmp
import com.github.quillraven.quillysadventure.ecs.component.collCmp
import com.github.quillraven.quillysadventure.ecs.component.jumpCmp
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.event.Key
import com.github.quillraven.quillysadventure.input.InputListener
import ktx.ashley.allOf

class PlayerInputSystem(private val gameEventManager: GameEventManager, engine: Engine) :
    EntitySystem(), InputListener {
    private val entities = engine.getEntitiesFor(allOf(PlayerComponent::class).get())

    override fun addedToEngine(engine: Engine?) {
        gameEventManager.addInputListener(this)
        super.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine?) {
        gameEventManager.removeInputListener(this)
        super.removedFromEngine(engine)
    }

    override fun move(percX: Float, percY: Float) {
        when {
            percX > 0 -> entities.forEach { it.moveCmp.order = MoveOrder.RIGHT }
            percX < 0 -> entities.forEach { it.moveCmp.order = MoveOrder.LEFT }
            else -> entities.forEach { it.moveCmp.order = MoveOrder.NONE }
        }
    }

    override fun keyPressed(key: Key) {
        when (key) {
            Key.CAST -> entities.forEach {
                with(it.abilityCmp) {
                    if (canCast()) {
                        order = CastOrder.BEGIN_CAST
                    }
                }
            }
            Key.ATTACK -> entities.forEach {
                with(it.attackCmp) {
                    if (canAttack()) {
                        order = AttackOrder.START
                    }
                }
            }
            Key.JUMP -> entities.forEach {
                if (it.collCmp.numGroundContacts > 0) {
                    it.jumpCmp.order = JumpOrder.JUMP
                }
            }
            else -> {
            }
        }
    }

    override fun keyReleased(key: Key) {
        when (key) {
            Key.CAST -> entities.forEach { it.abilityCmp.order = CastOrder.NONE }
            Key.ATTACK -> entities.forEach { it.attackCmp.order = AttackOrder.NONE }
            Key.JUMP -> entities.forEach { it.jumpCmp.order = JumpOrder.NONE }
            else -> {
            }
        }
    }
}
