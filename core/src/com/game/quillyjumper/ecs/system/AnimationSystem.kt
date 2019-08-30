package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.assets.AssetManager
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ecs.component.AnimationComponent
import com.game.quillyjumper.ecs.component.RenderComponent
import com.game.quillyjumper.graphics.Animation
import com.game.quillyjumper.graphics.AnimationType
import com.game.quillyjumper.graphics.ModelType
import com.game.quillyjumper.graphics.get
import ktx.ashley.allOf
import ktx.ashley.get
import java.util.*

class AnimationSystem(private val assets: AssetManager) :
    IteratingSystem(allOf(AnimationComponent::class, RenderComponent::class).get()) {
    private val animationCache = EnumMap<ModelType, EnumMap<AnimationType, Animation>>(ModelType::class.java)

    private operator fun EnumMap<ModelType, EnumMap<AnimationType, Animation>>.get(
        modelType: ModelType,
        animationType: AnimationType
    ): Animation {
        val animationMap = this.computeIfAbsent(modelType) { EnumMap(AnimationType::class.java) }
        return animationMap.computeIfAbsent(animationType) { assets[modelType, animationType] }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[AnimationComponent.mapper]?.let { aniCmp ->
            aniCmp.animationTime += deltaTime

            // check if aniCmp has to change and if yes retrieve it from assetmanager and store it in a cache
            // to have faster access in the future
            if (aniCmp.animationType != aniCmp.animation.animationType || aniCmp.modelType != aniCmp.animation.modelType || aniCmp.animation.keyFrames.isEmpty()) {
                aniCmp.animation = animationCache[aniCmp.modelType, aniCmp.animationType]
                aniCmp.animationTime = 0f
            }

            entity[RenderComponent.mapper]?.let { render ->
                render.sprite.apply {
                    val textureRegion = aniCmp.animation.getKeyFrame(aniCmp.animationTime, true)
                    texture = textureRegion.texture
                    setRegion(textureRegion)
                    // keep aspect ratio of original texture and scale it to fit into the world units
                    setSize(textureRegion.regionWidth * UNIT_SCALE, textureRegion.regionHeight * UNIT_SCALE)
                    setOriginCenter()
                }
            }
        }
    }
}
