package com.game.quillyjumper.ability

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.ecs.component.statsCmp
import kotlin.math.max

abstract class Ability(val owner: Entity, val world: World, val engine: Engine) {
    private var cooldown = 0f
    abstract val cost: Int

    abstract fun getCooldownTime(): Float

    fun canCast() = owner.statsCmp.mana >= cost && cooldown <= 0f

    open fun cast() {
        with(owner.statsCmp) {
            mana -= cost
            cooldown = getCooldownTime()
        }
    }

    open fun update(deltaTime: Float) {
        cooldown = max(0f, cooldown - deltaTime)
    }
}