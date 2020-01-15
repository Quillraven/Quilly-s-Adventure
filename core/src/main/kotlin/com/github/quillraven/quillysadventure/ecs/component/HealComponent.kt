package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class HealComponent : Component, Pool.Poolable {
    var life = 0f
    var mana = 0f
    var resurrect = false

    override fun reset() {
        life = 0f
        mana = 0f
        resurrect = false
    }

    companion object {
        val mapper = mapperFor<HealComponent>()
    }
}

fun Entity.heal(engine: Engine, life: Float, mana: Float, resurrect: Boolean = false) {
    val heal = this[HealComponent.mapper]
    if (heal == null) {
        this.add(engine.createComponent(HealComponent::class.java).apply {
            this.life = life
            this.mana = mana
            this.resurrect = resurrect
        })
    } else {
        heal.life += life
        heal.mana += mana
        // set resurrect flag only if it is specified
        if (resurrect) heal.resurrect = true
    }
}

val Entity.healCmp: HealComponent
    get() = this[HealComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a heal component which is null")
