package com.github.quillraven.quillysadventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.forever
import com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.LabelStyles
import com.github.quillraven.quillysadventure.ui.get
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.actors.txt
import ktx.scene2d.Scene2DSkin

class DialogWidget(skin: Skin = Scene2DSkin.defaultSkin) : Table(skin) {
    private val textLabel = Label("", skin).apply {
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
    private val nextLabel = Label(" [DARK_GRAY]>>>[] ", skin, LabelStyles.LARGE.name)
    private var currentPageIdx = 0
    private val pages = Array<String>(4)

    init {
        background = skin[Images.DIALOG_LIGHT]
        add(scrollPane).fill().size(610f, 400f).padLeft(50f)

        // button to click through pages of a dialog
        add(nextLabel).bottom().right().width(50f)
        nextLabel += forever(sequence(fadeIn(1f), fadeOut(1f)))
        nextLabel.onClick {
            if (currentPageIdx + 1 < pages.size) {
                // additional pages available -> show next page
                setPage(pages[++currentPageIdx])
            } else {
                // last page reached -> close dialog
                hideDialog()
            }
        }

        pack()
        color.a = 0f

        // group is not rotated or scaled and therefore we do not need to transform every draw call
        // -> increased draw performance because we avoid flushing the batch
        isTransform = false
    }

    private fun setPage(page: String) {
        textLabel.txt = "[BLACK]$page[]"
    }

    fun showDialog(firstPage: String, popupDelay: Float = 0f): DialogWidget {
        currentPageIdx = 0
        pages.clear()
        pages.add(firstPage)
        color.a = 0f
        centerPosition()
        this.clearActions()
        this += delay(popupDelay) + fadeIn(1f)
        setPage(firstPage)
        return this
    }

    fun hideDialog(fadeTime: Float = 1f) {
        this.clearActions()
        this += fadeOut(fadeTime) + Actions.moveBy(-2000f, 0f)
    }

    fun addPage(page: String): DialogWidget {
        pages.add(page)
        return this
    }
}
