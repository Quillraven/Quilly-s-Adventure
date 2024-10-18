package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.assets.ParticleAssets
import ktx.ashley.get
import ktx.ashley.mapperFor

class ParticleComponent : Component, Pool.Poolable {
    var type: ParticleAssets = ParticleAssets.BLOOD

    // offset values for particle effects e.g. for our missiles because
    // missiles are using a box2d body and therefore the x/y coordinate of the
    // transform component is the bottom left corner but we want to render of course
    // in the center of the box2d body
    var offsetX: Float = 0f
    var offsetY: Float = 0f
    var flipBy180Deg: Boolean = false

    lateinit var effect: ParticleEffectPool.PooledEffect

    companion object {
        val mapper = mapperFor<ParticleComponent>()
    }

    override fun reset() {
        offsetX = 0f
        offsetY = 0f
        if (flipBy180Deg) {
            // reset angles to original values
            // change all angles by 180 degrees
            effect.emitters.forEach {
                with(it.angle) {
                    setHigh(highMin - 180f, highMax - 180f)
                    setLow(lowMin - 180f, lowMax - 180f)
                }
            }
            flipBy180Deg = false
        }
        effect.free()
    }
}

val Entity.particleCmp: ParticleComponent
    get() = this[ParticleComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a particle component which is null")
