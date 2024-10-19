package com.github.quillraven.quillysadventure.ui.action

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class ScaleToRegionWidth : TemporalAction(0f) {
    var targetPercentage: Float = 0f
    private var startWidth = 0f
    private var origWidth = 0
    private lateinit var atlasRegion: TextureAtlas.AtlasRegion

    override fun setTarget(target: Actor) {
        super.setTarget(target)
        startWidth = target.width
        if (target is Image && target.drawable is TextureRegionDrawable) {
            atlasRegion = (target.drawable as TextureRegionDrawable).region as TextureAtlas.AtlasRegion
            origWidth = atlasRegion.originalWidth
        }
    }

    override fun update(percent: Float) {
        val targetValue = MathUtils.lerp(startWidth, origWidth * targetPercentage, percent)
        atlasRegion.regionWidth = targetValue.toInt()
        target.width = targetValue
    }
}


fun scaleToRegionWidth(targetPercentage: Float = 0f, duration: Float = 0f): ScaleToRegionWidth =
    Actions.action(ScaleToRegionWidth::class.java).apply {
        this.targetPercentage = targetPercentage
        this.duration = duration
    }
