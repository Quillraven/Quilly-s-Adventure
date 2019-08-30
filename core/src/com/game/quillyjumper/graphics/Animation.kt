package com.game.quillyjumper.graphics

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.assets.AnimationLoader
import com.game.quillyjumper.assets.TextureAtlasAssets
import com.game.quillyjumper.assets.get

enum class ModelType {
    UNKNOWN,
    PLAYER
}

enum class AnimationType {
    IDLE, RUN, JUMP, FALL
}

//TODO check what happens if regionKeys is not passed as argument
// will we always create a new empty array or will the empty array instance be reused
// to avoid a lot of GC when we create a lot of AnimationComponent instances
class Animation(
    val modelType: ModelType,
    val animationType: AnimationType,
    regionKeys: Array<out TextureRegion>,
    frameDuration: Float = 0.25f
) : com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>(frameDuration, regionKeys)

fun AssetManager.loadAllAnimations() {
    val aniParameter = AnimationLoader.AnimationLoaderParameter()
    val atlas = this[TextureAtlasAssets.GAME_OBJECTS]

    // load all animations linked to real models
    ModelType.values().forEach { modelType ->
        // unknown is not a real model and is used as a fallback in case an animation is not loaded correctly
        if (modelType == ModelType.UNKNOWN) return@forEach

        AnimationType.values().forEach { animationType ->
            aniParameter.apply {
                this.modelType = modelType
                this.animationType = animationType
                frameDuration = 1 / 20f
                //TODO optimization: avoid creating new array instances and also remember the textureregions
                // so that we do not need to look them up all the time
                regionKeys = atlas.findRegions("${modelType.name.toLowerCase()}/${animationType.name.toLowerCase()}")
            }

            this.load("${modelType}${animationType}", Animation::class.java, aniParameter)
            this.finishLoading()
        }
    }

    // load fallback animation for assetmanager in case we try to access an animation
    // that was not loaded correctly
    aniParameter.apply {
        this.modelType = ModelType.UNKNOWN
        this.animationType = AnimationType.IDLE
        frameDuration = 1 / 20f
        regionKeys = atlas.findRegions("error")
    }

    this.load("${aniParameter.modelType}${aniParameter.animationType}", Animation::class.java, aniParameter)
    this.finishLoading()
}

operator fun AssetManager.get(
    modelType: ModelType,
    animationType: AnimationType
): Animation {
    val animation = this.get("${modelType}${animationType}", Animation::class.java)
    if (animation == null) {
        // return error animation because given model and animation type was not loaded
        //TODO print error message
        return this.get("${ModelType.UNKNOWN}${AnimationType.IDLE}", Animation::class.java)
    }
    return animation
}