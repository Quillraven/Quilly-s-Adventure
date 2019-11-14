package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class AttackOrder { NONE, START, ATTACK_ONCE }

class AttackComponent(
    var range: Float = 1f,
    var cooldown: Float = 1f,
    var attackTime: Float = 0f,
    var order: AttackOrder = AttackOrder.NONE,
    var damageDelay: Float = 0f
) : Component, Pool.Poolable {
    companion object {
        val mapper = mapperFor<AttackComponent>()
    }

    fun canAttack() = attackTime <= 0f

    override fun reset() {
        attackTime = 0f
        order = AttackOrder.NONE
        damageDelay = 0f
    }
}

val Entity.attackCmp: AttackComponent
    get() = this[AttackComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an attack component which is null")