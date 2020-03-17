package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.playerCmp
import com.github.quillraven.quillysadventure.event.GameEventListener
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get

private const val WELCOME_TUTORIAL_DELAY = 2.0f

enum class TutorialType {
    None,
    Welcome,
    Attack,
    Damaged,
    LevelUp,
    NewSkill,
    Checkpoint
}

class TutorialSystem(private val gameEventManager: GameEventManager) :
        IteratingSystem(allOf(PlayerComponent::class).exclude(RemoveComponent::class).get()), GameEventListener {
    private var welcomeDelay = WELCOME_TUTORIAL_DELAY
    private var showTutorialType = TutorialType.None

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addGameEventListener(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeGameEventListener(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (welcomeDelay > 0f) {
            welcomeDelay -= deltaTime
            if (welcomeDelay <= 0f) {
                showTutorialType = TutorialType.Welcome
            }
        }

        if (showTutorialType != TutorialType.None && entity.playerCmp.tutorials.add(showTutorialType)) {
            // tutorial hint not yet shown -> show it
            gameEventManager.dispatchShowDialogEvent("tutorial.$showTutorialType")
            showTutorialType = TutorialType.None
        }
    }

    override fun characterAttack(character: Entity) {
        if (character[PlayerComponent.mapper] != null) {
            showTutorialType = TutorialType.Attack
        }
    }

    override fun characterDamaged(character: Entity, damage: Float, life: Float, maxLife: Float) {
        if (character[PlayerComponent.mapper] != null) {
            showTutorialType = TutorialType.Damaged
        }
    }

    override fun characterLevelUp(character: Entity, level: Int, xp: Int, xpNeeded: Int) {
        showTutorialType = when (level) {
            2 -> TutorialType.LevelUp
            3 -> TutorialType.NewSkill
            else -> TutorialType.None
        }
    }

    override fun activateSavepoint(savepoint: Entity) {
        showTutorialType = TutorialType.Checkpoint
    }
}
