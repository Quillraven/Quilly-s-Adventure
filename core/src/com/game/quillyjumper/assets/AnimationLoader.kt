package com.game.quillyjumper.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.graphics.Animation
import com.game.quillyjumper.graphics.AnimationType
import com.game.quillyjumper.graphics.ModelType

class AnimationLoader(fileHandleResolver: FileHandleResolver) :
    SynchronousAssetLoader<Animation, AnimationLoader.AnimationLoaderParameter>(fileHandleResolver) {

    class AnimationLoaderParameter : AssetLoaderParameters<Animation>() {
        var modelType = ModelType.UNKNOWN
        var animationType = AnimationType.IDLE
        var frameDuration = 0.25f
        var regionKeys = Array<TextureAtlas.AtlasRegion>()
    }

    override fun getDependencies(
        fileName: String?,
        file: FileHandle?,
        parameter: AnimationLoaderParameter?
    ): Array<AssetDescriptor<Any>>? = null

    override fun load(
        assetManager: AssetManager,
        fileName: String?,
        file: FileHandle?,
        parameter: AnimationLoaderParameter
    ): Animation {
        return Animation(
            parameter.modelType,
            parameter.animationType,
            parameter.regionKeys,
            frameDuration = parameter.frameDuration
        )
    }
}