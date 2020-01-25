package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import ktx.actors.centerPosition
import ktx.actors.contains
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.scene2d.KTable
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.label

class MenuHUD(
    bundle: I18NBundle,
    skin: Skin = Scene2DSkin.defaultSkin
) : Table(skin), KTable {
    val newGameLabel: Label
    private val creditsPane = ScrollPane(
        Label(bundle["credits.info"], skin).apply { setWrap(true) },
        skin
    ).apply {
        width = 550f
        height = 400f
        setScrollbarsVisible(true)
        fadeScrollBars = false
        variableSizeKnobs = false
    }

    init {
        defaults().pad(5f, 70f, 5f, 5f)

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
            if (creditsPane in stage.root) {
                creditsPane.remove()
            } else {
                stage.addActor(creditsPane)
                creditsPane.centerPosition(stage.width * 1.15f)
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
