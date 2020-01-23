package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.scene2d.KTable
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.label

class MenuHUD(skin: Skin = Scene2DSkin.defaultSkin) : Table(skin), KTable {
    val newGameLabel: Label

    init {
        defaults().pad(5f, 70f, 5f, 5f)
        background = Scene2DSkin.defaultSkin[Images.DIALOG_LIGHT]

        newGameLabel = label("New Game", LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }
        label("Continue", LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }
        audioVolumeWidget("Music") { cell ->
            checkBox.onChange { println("${checkBox.isChecked}") }
            audioReduceButton.onClick { println("reduce") }
            audioIncreaseButton.onClick { println("increase") }
            cell.fillX().padLeft(0f).row()
        }
        audioVolumeWidget("Sound") { cell -> cell.fillX().padLeft(0f).row() }
        //TODO mention Schlaubi for his awesome support throughout the entire project (BOLD AND COLORFUL TEXT)
        // also add REDRUM on soundcloud for awesome music stuff!
        // Valvoorik just because he is awesome
        label("Credits", LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }
        label("Exit", LabelStyles.LARGE.name) { cell ->
            setAlignment(Align.center)
            cell.fillX().row()
        }.onClick { Gdx.app.exit() }
        pack()
    }
}
