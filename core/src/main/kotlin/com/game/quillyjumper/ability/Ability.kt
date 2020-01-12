package com.game.quillyjumper.ability

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.ReflectionPool
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.ecs.component.FacingDirection
import com.game.quillyjumper.ecs.component.facingCmp
import com.game.quillyjumper.ecs.component.statsCmp
import com.game.quillyjumper.ecs.component.transfCmp
import com.game.quillyjumper.ecs.damageEmitter
import com.game.quillyjumper.ecs.missile
import com.game.quillyjumper.ecsEngine
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.world
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
        gameEventManager.dispatchCharacterCast(owner, this, effect.cost, stats.mana, stats.maxMana)
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
        val pool = ReflectionPool<Ability>(Ability::class.java, 32)
    }
}

interface AbilityEffect {
    val cooldown: Float
    val cost: Int

    fun trigger(ability: Ability)
}
