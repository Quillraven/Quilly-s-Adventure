package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.utils.Pool
import com.game.quillyjumper.assets.ParticleAssets
import ktx.ashley.get
import ktx.ashley.mapperFor

class ParticleComponent(
    var type: ParticleAssets = ParticleAssets.BLOOD,
    // offset values for particle effects e.g. for our missiles because
    // missiles are using a box2d body and therefore the x/y coordinate of the
    // transform component is the bottom left corner but we want to render of course
    // in the center of the box2d body
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
) : Component, Pool.Poolable {
    lateinit var effect: ParticleEffectPool.PooledEffect

    companion object {
        val mapper = mapperFor<ParticleComponent>()
    }

    override fun reset() {
        offsetX = 0f
        offsetY = 0f
        effect.free()
    }
}

val Entity.particleCmp: ParticleComponent
    get() = this[ParticleComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a particle component which is null")
