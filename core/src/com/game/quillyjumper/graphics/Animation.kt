package com.game.quillyjumper.graphics

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array

enum class ModelType {
    UNKNOWN,
    PLAYER
}

enum class AnimationType {
    IDLE, RUN, JUMP, FALL
}

class Animation(
    val modelType: ModelType,
    val animationType: AnimationType,
    regionKeys: Array<TextureAtlas.AtlasRegion>,
    frameDuration: Float = 1 / 15f
) : com.badlogic.gdx.graphics.g2d.Animation<TextureAtlas.AtlasRegion>(frameDuration, regionKeys)
