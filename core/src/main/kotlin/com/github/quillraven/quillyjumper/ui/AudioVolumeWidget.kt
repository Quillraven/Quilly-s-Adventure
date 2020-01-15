package com.github.quillraven.quillyjumper.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.scene2d.*

class AudioVolumeWidget(text: String, skin: Skin) : Table(skin), KTable {
    val checkBox: KCheckBox
    val audioReduceButton: KTextButton
    val audioIncreaseButton: KTextButton

    init {
        defaults().spaceLeft(5f)
        checkBox = checkBox("") { isChecked = true }
        audioReduceButton = textButton("-") { it.bottom() }
        label(text) { it.space(0f, 15f, 0f, 15f).padTop(10f).width(80f) }
        audioIncreaseButton = textButton("+") { it.bottom() }
    }
}

inline fun <S> KWidget<S>.audioVolumeWidget(
    text: String,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: AudioVolumeWidget.(S) -> Unit = {}
) = actor(AudioVolumeWidget(text, skin), init)
