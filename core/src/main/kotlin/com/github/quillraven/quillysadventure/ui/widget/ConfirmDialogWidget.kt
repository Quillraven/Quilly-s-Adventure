package com.github.quillraven.quillysadventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.LabelStyles
import com.github.quillraven.quillysadventure.ui.get
import ktx.scene2d.Scene2DSkin

class ConfirmDialogWidget(
    infoTxt: String,
    yesTxt: String,
    noTxt: String,
    skin: Skin = Scene2DSkin.defaultSkin
) : Table(skin) {
    private val textLabel = Label("[BLACK]$infoTxt[]", skin).apply {
        wrap = true
        setAlignment(Align.topLeft)
    }
    private val scrollPane = ScrollPane(textLabel, skin).apply {
        width = 610f
        height = 400f
        setScrollbarsVisible(true)
        fadeScrollBars = false
        variableSizeKnobs = false
    }
    val yesLabel = Label(" [DARK_GRAY]$yesTxt[] ", skin, LabelStyles.LARGE.name)
    val noLabel = Label(" [DARK_GRAY]$noTxt[] ", skin, LabelStyles.LARGE.name)

    init {
        background = skin[Images.DIALOG_LIGHT]
        add(scrollPane).fill().size(610f, 300f).padTop(50f).padLeft(50f).colspan(2).row()

        add(yesLabel).bottom().fill().padBottom(25f).padLeft(50f)
        add(noLabel).bottom().fill().padBottom(25f)
        pack()

        // group is not rotated or scaled and therefore we do not need to transform every draw call
        // -> increased draw performance because we avoid flushing the batch
        isTransform = false
    }
}
