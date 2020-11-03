package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.event.Key
import com.github.quillraven.quillysadventure.ui.widget.MapInfoWidget
import com.github.quillraven.quillysadventure.ui.widget.PlayerInfoWidget
import com.github.quillraven.quillysadventure.ui.widget.StatsWidget
import com.github.quillraven.quillysadventure.ui.widget.mapInfoWidget
import com.github.quillraven.quillysadventure.ui.widget.playerInfoWidget
import ktx.actors.centerPosition
import ktx.actors.onChangeEvent
import ktx.actors.onClick
import ktx.actors.onTouchEvent
import ktx.actors.plusAssign
import ktx.scene2d.KTable
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.imageButton
import ktx.scene2d.table
import ktx.scene2d.touchpad
import ktx.scene2d.verticalGroup

class GameHUD(
    private val gameEventManager: GameEventManager,
    statsTitle: String,
    skillText: String,
    skin: Skin = Scene2DSkin.defaultSkin
) : Table(skin),
    KTable {
    val infoWidget: PlayerInfoWidget
    val mapInfoWidget: MapInfoWidget
    val statsWidget = StatsWidget(statsTitle, skillText).apply { color.a = 0f }
    val skillButton = ImageButton(skin).apply {
        onTouchEvent(
            onDown = { _ ->
                if (this.style.imageDown != null) {
                    // active skill button
                    this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.CAST)
                }
            },
            onUp = {
                if (this.style.imageDown != null) {
                    // active skill button
                    this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.CAST)
                }
            }
        )
    }

    init {
        defaults().fillX().pad(10f, 10f, 10f, 10f)

        mapInfoWidget = mapInfoWidget {
            it.colspan(3).padLeft(325f).row()
        }

        // add dummy cell in the middle to push map info to the top and other elements to the bottom
        add().expand().colspan(3).row()

        // touch pad on bottom left corner
        touchpad(0f) { cell ->
            cell.left().size(250f, 250f)
        }.onChangeEvent {
            gameEventManager.dispatchInputMoveEvent(this.knobPercentX, this.knobPercentY)
        }

        // player information for life/mana in bottom center
        infoWidget = playerInfoWidget { it.expandX().spaceLeft(275f).padBottom(30f).bottom() }
        infoWidget.imagePlayer.onClick {
            statsWidget.centerPosition()
            statsWidget += fadeIn(1f)
        }

        // action buttons for jumping, attacking and casting in bottom right corner
        table {
            defaults().bottom().right().fillX()
            imageButton(ImageButtonStyles.JUMP.name) {
                it.padBottom(70f).padRight(-20f)
            }.onTouchEvent(
                onDown = { _ -> this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.JUMP) },
                onUp = { this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.JUMP) }
            )
            verticalGroup {
                imageButton(ImageButtonStyles.ATTACK.name).onTouchEvent(
                    onDown = { _ -> this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.ATTACK) },
                    onUp = { this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.ATTACK) }
                )
                this.addActor(this@GameHUD.skillButton)
                space(20f)
            }
            pack()
        }

        setFillParent(true)
        pack()

        // group is not rotated or scaled and therefore we do not need to transform every draw call
        // -> increased draw performance because we avoid flushing the batch
        isTransform = false
    }
}
