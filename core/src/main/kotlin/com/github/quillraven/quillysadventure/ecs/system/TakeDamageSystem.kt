package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.StringBuilder
import com.github.quillraven.quillysadventure.assets.ParticleAssets
import com.github.quillraven.quillysadventure.ecs.component.KillerComponent
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.StatsComponent
import com.github.quillraven.quillysadventure.ecs.component.TakeDamageComponent
import com.github.quillraven.quillysadventure.ecs.component.TransformComponent
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.component.takeDamageCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.ecs.floatingText
import com.github.quillraven.quillysadventure.ecs.particleEffect
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.ui.FontType
import ktx.ashley.allOf
import ktx.ashley.exclude

class TakeDamageSystem(private val gameEventManager: GameEventManager) : IteratingSystem(
    allOf(
        StatsComponent::class,
        TakeDamageComponent::class,
        TransformComponent::class
    ).exclude(RemoveComponent::class).get()
) {
    private val stringBuilder = StringBuilder(4)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val damage = entity.takeDamageCmp
        if (damage.damage <= 0f) {
            // if there is no damage then ignore it
            return
        }
        val stats = entity.statsCmp

        // deal damage and reset damage to zero to not deal the damage again in the next frame
        stats.life -= damage.damage
        stringBuilder.clear()
        stringBuilder.append(damage.damage.toInt())
        gameEventManager.dispatchCharacterDamagedEvent(entity, damage.damage, stats.life, stats.maxLife)
        damage.damage = 0f

        // after that step if the life is still <= 0 then the entity is really dead
        // store killer entity to give it experience for the killing blow
        if (stats.life <= 0f) {
            entity.add(engine.createComponent(KillerComponent::class.java).apply { killer = damage.source })
        }

        // create floating text to display damage number to player
        val transform = entity.transfCmp
        engine.floatingText(
            transform.position.x + transform.size.x * 0.5f,
            transform.position.y + transform.size.y,
            FontType.DEFAULT,
            stringBuilder,
            Color.RED,
            0f,
            -1f,
            1.25f
        )

        // show some blood splatter to indicate that the entity got hurt
        engine.particleEffect(
            transform.position.x + transform.size.x * 0.25f,
            transform.position.y + transform.size.y * 0.25f,
            ParticleAssets.BLOOD
        )
    }
}
