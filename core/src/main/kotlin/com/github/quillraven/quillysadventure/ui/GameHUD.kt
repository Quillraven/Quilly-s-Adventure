package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.event.Key
import com.github.quillraven.quillysadventure.ui.widget.MapInfoWidget
import com.github.quillraven.quillysadventure.ui.widget.PlayerInfoWidget
import com.github.quillraven.quillysadventure.ui.widget.mapInfoWidget
import com.github.quillraven.quillysadventure.ui.widget.playerInfoWidget
import ktx.actors.onChangeEvent
import ktx.actors.onTouchEvent
import ktx.scene2d.*

class GameHUD(private val gameEventManager: GameEventManager, skin: Skin = Scene2DSkin.defaultSkin) : Table(skin),
    KTable {
    val infoWidget: PlayerInfoWidget
    val mapInfoWidget: MapInfoWidget

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
        }.onChangeEvent { _, actor ->
            gameEventManager.dispatchInputMoveEvent(actor.knobPercentX, actor.knobPercentY)
        }

        // player information for life/mana in bottom center
        infoWidget = playerInfoWidget { it.expandX().spaceLeft(275f).padBottom(30f).bottom() }

        // action buttons for jumping, attacking and casting in bottom right corner
        table {
            defaults().bottom().right().fillX()
            imageButton(ImageButtonStyles.JUMP.name) {
                it.padBottom(70f).padRight(-20f)
            }.onTouchEvent(
                downListener = { _, _ -> this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.JUMP) },
                upListener = { _, _ -> this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.JUMP) }
            )
            verticalGroup {
                imageButton(ImageButtonStyles.ATTACK.name).onTouchEvent(
                    downListener = { _, _ -> this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.ATTACK) },
                    upListener = { _, _ -> this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.ATTACK) }
                )
                imageButton(ImageButtonStyles.FIREBALL.name).onTouchEvent(
                    downListener = { _, _ -> this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.CAST) },
                    upListener = { _, _ -> this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.CAST) }
                )
                space(20f)
            }
            pack()
        }

        setFillParent(true)
        pack()
    }
}
