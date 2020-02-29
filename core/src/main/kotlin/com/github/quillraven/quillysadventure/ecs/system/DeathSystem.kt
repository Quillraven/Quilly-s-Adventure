package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.StringBuilder
import com.github.quillraven.quillysadventure.ai.PlayerState
import com.github.quillraven.quillysadventure.assets.SoundAssets
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.ecs.component.KillerComponent
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.StatsComponent
import com.github.quillraven.quillysadventure.ecs.component.heal
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.ecs.floatingText
import com.github.quillraven.quillysadventure.ecs.isRemoved
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.ui.FontType
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get

class DeathSystem(
    private val audioService: AudioService,
    private val gameEventManager: GameEventManager,
    private val lvlUpText: String,
    private val xpAbbreviation: String
) :
    IteratingSystem(allOf(StatsComponent::class).exclude(RemoveComponent::class).get()) {
    private val xpInfoBuilder = StringBuilder(8)

    fun getNeededExperience(currentLevel: Int): Int = when (currentLevel) {
        1 -> 70
        2 -> 190
        3 -> 300
        4 -> 600
        else -> Int.MAX_VALUE
    }

    private fun canLevelUp(currentLevel: Int, currentXP: Int): Boolean = currentXP >= getNeededExperience(currentLevel)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val entityStats = entity.statsCmp
        if (!entityStats.alive) {
            gameEventManager.dispatchCharacterDeathEvent(entity)

            // entity is dead -> remove it from the game if it is a non-player entity
            // otherwise heal player and move him out of bounds to respawn at latest checkpoint
            if (entity[PlayerComponent.mapper] == null) {
                entity.add(engine.createComponent(RemoveComponent::class.java))
            } else {
                // the next line triggers the OutOfBoundsSystem
                entity.physicCmp.body.setTransform(-1f, -1f, 0f)
                // fully heal player
                entity.heal(engine, entityStats.maxLife, entityStats.maxMana, true)
                // change state to IDLE because right now player is in DEATH state which
                // would set the ALIVE flag again to false. To avoid that we change to a different state
                entity.stateCmp.stateMachine.changeState(PlayerState.IDLE)
            }

            // entity is dead (life <=0) and death animation is finished
            entity[KillerComponent.mapper]?.let { killerCmp ->
                // there is a killing entity specified -> grant XP for killing blow
                val killer = killerCmp.killer
                if (killer.isRemoved() || killer[PlayerComponent.mapper] == null) {
                    // killer was already removed from ECS or killer is not player -> do nothing
                    return@let
                }

                val xpGained = entityStats.xp
                val stats = killer.statsCmp
                val transform = killer.transfCmp
                // add xp of dying entity to killer entity
                stats.xp += xpGained
                gameEventManager.dispatchCharacterXPGainedEvent(killer, stats.xp, getNeededExperience(stats.level))

                var showLevelUpInfo = false
                while (canLevelUp(stats.level, stats.xp)) {
                    // entity has enough XP for level up -> increase level
                    stats.level++
                    showLevelUpInfo = true
                    gameEventManager.dispatchCharacterLevelUpEvent(
                        killer,
                        stats.level,
                        stats.xp,
                        getNeededExperience(stats.level)
                    )
                }

                if (showLevelUpInfo) {
                    audioService.play(SoundAssets.LEVEL_UP)

                    // show level up information to player
                    xpInfoBuilder.clear()
                    xpInfoBuilder.append("$lvlUpText: ")
                    xpInfoBuilder.append(stats.level)
                    engine.floatingText(
                        transform.position.x + transform.size.x * 0.5f,
                        transform.position.y + transform.size.y,
                        FontType.LARGE,
                        xpInfoBuilder,
                        Color.GOLD,
                        0f,
                        -0.4f,
                        3f
                    )
                }

                // show experience gain information to player
                xpInfoBuilder.clear()
                xpInfoBuilder.append("+")
                xpInfoBuilder.append(xpGained)
                xpInfoBuilder.append(" $xpAbbreviation")
                engine.floatingText(
                    transform.position.x + transform.size.x * 0.5f,
                    transform.position.y + transform.size.y,
                    FontType.LARGE,
                    xpInfoBuilder,
                    Color.PURPLE,
                    0f,
                    -0.8f,
                    2f
                )
            }
        }
    }
}
