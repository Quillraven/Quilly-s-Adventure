package com.github.quillraven.quillysadventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.LabelStyles
import com.github.quillraven.quillysadventure.ui.get
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.actors.txt
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor

class MapInfoWidget(
    skin: Skin,
    private val label: Label = Label("", skin, LabelStyles.MAP_INFO.name)
) : WidgetGroup(label, Image(skin[Images.SKULL])), KGroup {
    init {
        // move skull image to center of label
        getChild(1).run { setPosition(160f, 20f) }
        // set label config
        label.run {
            setAlignment(Align.center, Align.center)
            y = -30f
            setSize(600f, 70f)
        }
        // hide map info initially until show is called
        color.a = 0f

        // group is not rotated or scaled and therefore we do not need to transform every draw call
        // -> increased draw performance because we avoid flushing the batch
        isTransform = false
    }

    fun show(text: String, duration: Float = 3.5f) {
        label.txt = "[BLACK]$text"
        clearActions()
        this += alpha(0f) + fadeIn(1f) + delay(duration) + fadeOut(1f)
    }

    override fun getPrefHeight(): Float = 120f

    override fun getPrefWidth(): Float = 140f
}

inline fun <S> KWidget<S>.mapInfoWidget(
    skin: Skin = Scene2DSkin.defaultSkin,
    init: MapInfoWidget.(S) -> Unit = {}
) = actor(MapInfoWidget(skin), init)
