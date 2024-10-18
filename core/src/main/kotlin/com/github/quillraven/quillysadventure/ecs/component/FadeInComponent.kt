package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class FadeInComponent : Component, Pool.Poolable {
    var targetAlpha: Float = 1f
    var startAlpha: Float = 0f
    var fadeTime: Float = 0f
    var maxFadeTime: Float = 1f

    companion object {
        val mapper = mapperFor<FadeInComponent>()
    }

    override fun reset() {
        startAlpha = 0f
        targetAlpha = 1f
        fadeTime = 0f
        maxFadeTime = 1f
    }
}


val Entity.fadeinCmp: FadeInComponent
    get() = this[FadeInComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a fadein component which is null")
