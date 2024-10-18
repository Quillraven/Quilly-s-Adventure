package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class AttackOrder { NONE, START, ATTACK_ONCE }

class AttackComponent : Component, Pool.Poolable {
    var range: Float = 1f
    var cooldown: Float = 1f
    var attackTime: Float = 0f
    var order: AttackOrder = AttackOrder.NONE
    var damageDelay: Float = 0f

    companion object {
        val mapper = mapperFor<AttackComponent>()
        val attackBoundingArea = Rectangle()
        val entityBoundingArea = Rectangle()
    }

    fun canAttack() = attackTime <= 0f

    fun inAttackRange(transformA: TransformComponent, transformB: TransformComponent): Boolean {
        attackBoundingArea.set(
            transformA.position.x - range,
            transformA.position.y,
            transformA.size.x + 2 * range,
            transformA.size.y
        )
        entityBoundingArea.set(
            transformB.position.x,
            transformB.position.y,
            transformB.size.x,
            transformB.size.y
        )

        return attackBoundingArea.overlaps(entityBoundingArea)
    }

    override fun reset() {
        attackTime = 0f
        order = AttackOrder.NONE
        damageDelay = 0f
    }
}

val Entity.attackCmp: AttackComponent
    get() = this[AttackComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an attack component which is null")
