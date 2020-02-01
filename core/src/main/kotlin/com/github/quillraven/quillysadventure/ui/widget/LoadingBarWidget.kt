package com.github.quillraven.quillysadventure.ui.widget

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.LabelStyles
import com.github.quillraven.quillysadventure.ui.get
import ktx.actors.plusAssign
import ktx.actors.txt
import ktx.scene2d.Scene2DSkin

class LoadingBarWidget(
    private val bundle: I18NBundle,
    skin: Skin = Scene2DSkin.defaultSkin,
    private val bar: Image = Image(skin[Images.BAR_GREEN]),
    private val label: Label = Label(bundle["loading"], skin, LabelStyles.LARGE.name)
) : WidgetGroup(
    Image(skin[Images.BAR_BACKGROUND]),
    bar,
    label
) {
    init {
        with(bar) {
            setPosition(19f, 22f)
            setSize(530f, 43f)
            scaleX = 0f
        }
        with(label) {
            setAlignment(Align.center, Align.center)
            setSize(this@LoadingBarWidget.prefWidth, this@LoadingBarWidget.prefHeight)
        }
    }

    fun scaleTo(percentage: Float, scaleDuration: Float = 0.1f) {
        bar.run {
            clearActions()
            this += scaleTo(MathUtils.clamp(percentage, 0f, 1f), 1f, scaleDuration)
        }
        if (percentage >= 1f) {
            label.txt = bundle["finishedLoading"]
        }
    }

    override fun getPrefHeight(): Float = 85f
    override fun getPrefWidth(): Float = 600f
}
