package com.github.quillraven.quillysadventure.ability

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.ReflectionPool
import com.github.quillraven.quillysadventure.assets.ParticleAssets
import com.github.quillraven.quillysadventure.ecs.component.FacingDirection
import com.github.quillraven.quillysadventure.ecs.component.facingCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.ecs.damageEmitter
import com.github.quillraven.quillysadventure.ecs.missile
import com.github.quillraven.quillysadventure.ecsEngine
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.world
import kotlin.math.max

class Ability : Pool.Poolable {
    private val engine = Gdx.app.ecsEngine
    private val world = Gdx.app.world
    lateinit var owner: Entity
    lateinit var effect: AbilityEffect
    private var cooldown = 0f

    fun canCast() = owner.statsCmp.mana >= effect.cost && cooldown <= 0f

    fun cast(gameEventManager: GameEventManager) {
        val stats = owner.statsCmp
        stats.mana -= effect.cost
        cooldown = effect.cooldown
        effect.trigger(this)
        gameEventManager.dispatchCharacterCastEvent(owner, this, effect.cost, stats.mana, stats.maxMana)
    }

    fun update(deltaTime: Float) {
        cooldown = max(0f, cooldown - deltaTime)
    }

    fun createMissile(sizeXY: Float, damage: Float, lifeSpan: Float, particleAsset: ParticleAssets) {
        with(owner.transfCmp) {
            val facing = owner.facingCmp.direction
            engine.missile(
                owner,
                world,
                if (facing == FacingDirection.RIGHT) position.x + size.x else position.x,
                position.y + size.y * 0.25f,
                sizeXY,
                sizeXY,
                if (facing == FacingDirection.RIGHT) 4f else -4f,
                lifeSpan,
                damage,
                particleAsset,
                facing != FacingDirection.RIGHT
            )
        }
    }

    fun dealAreaDamage(damage: Float, range: Float, lifeSpan: Float) {
        with(owner.transfCmp) {
            engine.damageEmitter(
                world,
                position.x - range,
                position.y,
                size.x + 2 * range,
                size.y,
                damage,
                lifeSpan,
                owner
            )
        }
    }

    override fun reset() {
        cooldown = 0f
    }

    companion object {
        val pool = ReflectionPool(Ability::class.java, 32)
    }
}

interface AbilityEffect {
    val cooldown: Float
    val cost: Int

    fun trigger(ability: Ability)
}
