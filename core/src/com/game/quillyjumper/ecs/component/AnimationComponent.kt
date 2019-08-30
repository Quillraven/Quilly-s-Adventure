package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.graphics.Animation
import com.game.quillyjumper.graphics.AnimationType
import com.game.quillyjumper.graphics.ModelType
import ktx.ashley.mapperFor

class AnimationComponent(
    var modelType: ModelType = ModelType.UNKNOWN,
    var animationType: AnimationType = AnimationType.IDLE,
    var animation: Animation = Animation(modelType, animationType, Array(TextureRegion::class.java)),
    var animationTime: Float = 0f
) : Component {
    companion object {
        val mapper = mapperFor<AnimationComponent>()
    }
}