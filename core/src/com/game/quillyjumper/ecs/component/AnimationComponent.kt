package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.game.quillyjumper.graphics.Animation
import com.game.quillyjumper.graphics.AnimationType
import com.game.quillyjumper.graphics.ModelType
import ktx.ashley.mapperFor

class AnimationComponent(
    var modelType: ModelType = ModelType.UNKNOWN,
    var animationType: AnimationType = AnimationType.IDLE,
    var animationTime: Float = 0f
) : Component {
    // animation gets initialized in AnimationSystem
    lateinit var animation: Animation

    companion object {
        val mapper = mapperFor<AnimationComponent>()
    }
}