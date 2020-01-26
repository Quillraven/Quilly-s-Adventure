package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import ktx.actors.alpha
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.scene2d.KTable
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.label

class MenuHUD(
    bundle: I18NBundle,
    skin: Skin = Scene2DSkin.defaultSkin
) : Table(skin), KTable {
    val newGameLabel: Label
    val creditsTable = Table(skin).apply {
        this.background = skin[Images.MENU_BACKGROUND]
        defaults().pad(15f)

        add(ScrollPane(
            Label(bundle["credits.info"], skin).apply {
                setWrap(true)
            },
            skin
        ).apply {
            width = 550f
            height = 400f
            setScrollbarsVisible(true)
            fadeScrollBars = false
            variableSizeKnobs = false
        }).fill().size(550f, 400f)

        // need to call pack otherwise the background image is not showing up
        this.pack()
        this.alpha = 0f
    }

    init {
        background = skin[Images.MENU_BACKGROUND]
        defaults().pad(5f, 70f, 5f, 25f)

        newGameLabel = label(bundle["newGame"], LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }
        label(bundle["continue"], LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }
        audioVolumeWidget(bundle["music"]) { cell ->
            checkBox.onChange { println("${checkBox.isChecked}") }
            audioReduceButton.onClick { println("reduce") }
            audioIncreaseButton.onClick { println("increase") }
            cell.fillX().padLeft(0f).row()
        }
        audioVolumeWidget(bundle["sound"]) { cell -> cell.fillX().padLeft(0f).row() }
        label(bundle["credits"], LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }.onClick {
            creditsTable.clearActions()
            creditsTable += if (creditsTable.alpha > 0f) {
                fadeOut(0.5f)
            } else {
                fadeIn(1f)
            }
        }
        label(bundle["quitGame"], LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }.onClick { Gdx.app.exit() }
        pack()
        left()
    }
}
