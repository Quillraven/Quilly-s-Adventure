package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.StringBuilder
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.floatingText
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get

class DeathSystem : IteratingSystem(allOf(StatsComponent::class).exclude(RemoveComponent::class).get()) {
    private val xpInfoBuilder = StringBuilder(8)

    private fun canLevelUp(currentLevel: Int, currentXP: Int): Boolean {
        return when {
            currentLevel == 1 && currentXP >= 60f -> true
            currentLevel == 2 && currentXP >= 100f -> true
            currentLevel == 3 && currentXP >= 150f -> true
            currentLevel == 4 && currentXP >= 250f -> true
            else -> false
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (!entity.statsCmp.alive) {
            // entity is dead (life <=0) and death animation is finished
            entity[KillerComponent.mapper]?.let { killerCmp ->
                // there is a killing entity specified -> grant XP for killing blow
                val killer = killerCmp.killer
                val xpGained = entity.statsCmp.xp
                val stats = killer.statsCmp
                val transform = killer.transfCmp
                // add xp of dying entity to killer entity
                stats.xp += xpGained

                var showLevelUpInfo = false
                while (canLevelUp(stats.level, stats.xp)) {
                    // entity has enough XP for level up -> increase level
                    stats.level++
                    showLevelUpInfo = true
                }

                if (showLevelUpInfo) {
                    // show level up information to player
                    xpInfoBuilder.clear()
                    xpInfoBuilder.append("New Level: ")
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
                xpInfoBuilder.append(" XP")
                engine.floatingText(
                    transform.position.x + transform.size.x * 0.5f,
                    transform.position.y + transform.size.y,
                    FontType.DEFAULT,
                    xpInfoBuilder,
                    Color.PURPLE,
                    0f,
                    -0.8f,
                    2f
                )
            }

            // entity is dead -> remove it from the game
            entity.add(engine.createComponent(RemoveComponent::class.java))
        }
    }
}