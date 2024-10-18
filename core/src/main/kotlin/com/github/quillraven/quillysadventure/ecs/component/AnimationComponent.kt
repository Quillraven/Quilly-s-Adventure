package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.assets.SoundAssets
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class ModelType {
    UNKNOWN,
    PLAYER,
    BLUE_SLIME,
    ORANGE_SLIME,
    FLIPPY,
    EYE_MONSTER,
    MINOTAUR,
    SKELETAL,
    DWARF,
    GIRL
}

enum class AnimationType {
    IDLE, RUN, JUMP, FALL, ATTACK, ATTACK2, ATTACK3, DEATH, CAST, GUARD
}

class Animation(
    val modelType: ModelType,
    val animationType: AnimationType,
    val sound: SoundAssets,
    regionKeys: Array<TextureAtlas.AtlasRegion>,
    frameDuration: Float = 1 / 10f
) : com.badlogic.gdx.graphics.g2d.Animation<TextureAtlas.AtlasRegion>(frameDuration, regionKeys)

class AnimationComponent : Component, Pool.Poolable {
    var modelType: ModelType = ModelType.UNKNOWN
    var animationType: AnimationType = AnimationType.IDLE
    var animationTime: Float = 0f
    var animationSpeed: Float = 1f
    var mode: PlayMode = PlayMode.LOOP

    // animation gets initialized in AnimationSystem
    lateinit var animation: Animation

    fun isAnimationFinished() = animation.isAnimationFinished(animationTime)

    override fun reset() {
        modelType = ModelType.UNKNOWN
        animationType = AnimationType.IDLE
        mode = PlayMode.LOOP
        animationSpeed = 1f
        animationTime = 0f
    }

    companion object {
        val mapper = mapperFor<AnimationComponent>()
    }
}

val Entity.aniCmp: AnimationComponent
    get() = this[AnimationComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an animation component which is null")
