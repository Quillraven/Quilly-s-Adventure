package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.utils.Pool
import com.game.quillyjumper.assets.ParticleAssets
import ktx.ashley.get
import ktx.ashley.mapperFor

class ParticleComponent(var type: ParticleAssets = ParticleAssets.BLOOD) : Component, Pool.Poolable {
    lateinit var effect: ParticleEffectPool.PooledEffect

    companion object {
        val mapper = mapperFor<ParticleComponent>()
    }

    override fun reset() {
        effect.free()
    }
}

val Entity.particleCmp: ParticleComponent
    get() = this[ParticleComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a particle component which is null")
