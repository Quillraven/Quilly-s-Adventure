package com.game.quillyjumper.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.event.Key
import ktx.actors.onChangeEvent
import ktx.scene2d.*

class GameHUD(private val gameEventManager: GameEventManager, skin: Skin = Scene2DSkin.defaultSkin) : Table(skin),
    KTable {
    val infoWidget: PlayerInfoWidget

    init {
        defaults().fillX().bottom().pad(10f, 10f, 10f, 10f)

        // touch pad on bottom left corner
        touchpad(0f) { cell ->
            cell.left().size(250f, 250f)
        }.onChangeEvent { _, actor ->
            gameEventManager.dispatchInputMoveEvent(actor.knobPercentX, actor.knobPercentY)
        }

        // player information for life/mana in bottom center
        infoWidget = playerInfoWidget { it.expandX().spaceLeft(275f).padBottom(30f) }

        // action buttons for jumping, attacking and casting in bottom right corner
        table {
            defaults().bottom().right().fillX()
            imageButton(ImageButtonStyles.JUMP.name) {
                it.padBottom(70f).padRight(-20f)
            }.onTouch(
                touchDownListener = { this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.JUMP) },
                touchUpListener = { this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.JUMP) }
            )
            verticalGroup {
                imageButton(ImageButtonStyles.ATTACK.name).onTouch(
                    touchDownListener = { this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.ATTACK) },
                    touchUpListener = { this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.ATTACK) }
                )
                imageButton(ImageButtonStyles.FIREBALL.name).onTouch(
                    touchDownListener = { this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.CAST) },
                    touchUpListener = { this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.CAST) }
                )
                space(20f)
            }
            pack()
        }

        setFillParent(true)
        bottom()
        pack()
    }
}
