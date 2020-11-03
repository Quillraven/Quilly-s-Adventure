package com.github.quillraven.quillysadventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.github.quillraven.quillysadventure.ui.LabelStyles
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import ktx.scene2d.checkBox
import ktx.scene2d.label
import ktx.scene2d.textButton

class AudioVolumeWidget(text: String, skin: Skin) : Table(skin), KTable {
    val checkBox: CheckBox
    val audioReduceButton: TextButton
    val audioIncreaseButton: TextButton
    val label: Label

    init {
        defaults().spaceLeft(5f)
        checkBox = checkBox("") { isChecked = true }
        audioReduceButton = textButton("-") { it.bottom() }
        label = label(text, LabelStyles.LARGE.name) {
            setAlignment(Align.center)
            it.space(0f, 15f, 0f, 15f).padTop(10f).width(160f)
        }
        audioIncreaseButton = textButton("+") { it.bottom() }
    }
}

inline fun <S> KWidget<S>.audioVolumeWidget(
    text: String,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: AudioVolumeWidget.(S) -> Unit = {}
) = actor(AudioVolumeWidget(text, skin), init)
