package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.assets.SoundAssets
import ktx.ashley.get
import ktx.ashley.mapperFor

enum class ModelType {
    UNKNOWN,
    PLAYER,
    BLUE_SLIME,
    FLIPPY,
    EYE_MONSTER
}

enum class AnimationType {
    IDLE, RUN, JUMP, FALL, ATTACK, DEATH, CAST
}

class Animation(
    val modelType: ModelType,
    val animationType: AnimationType,
    val sound: SoundAssets,
    regionKeys: Array<TextureAtlas.AtlasRegion>,
    frameDuration: Float = 1 / 10f
) : com.badlogic.gdx.graphics.g2d.Animation<TextureAtlas.AtlasRegion>(frameDuration, regionKeys)

class AnimationComponent(
    var modelType: ModelType = ModelType.UNKNOWN,
    var animationType: AnimationType = AnimationType.IDLE,
    var animationTime: Float = 0f,
    var mode: PlayMode = PlayMode.LOOP
) : Component {
    // animation gets initialized in AnimationSystem
    lateinit var animation: Animation

    fun isAnimationFinished() = animation.isAnimationFinished(animationTime)

    companion object {
        val mapper = mapperFor<AnimationComponent>()
    }
}

val Entity.aniCmp: AnimationComponent
    get() = this[AnimationComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access an animation component which is null")