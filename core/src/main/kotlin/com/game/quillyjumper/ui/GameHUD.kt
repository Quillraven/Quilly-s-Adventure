package com.game.quillyjumper.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.event.Key
import ktx.actors.onChangeEvent
import ktx.scene2d.*

class GameHUD(private val gameEventManager: GameEventManager, skin: Skin = Scene2DSkin.defaultSkin) : Table(skin),
    KTable {
    init {
        defaults().fillX().bottom().pad(10f, 10f, 10f, 10f)

        touchpad(0f) { cell ->
            cell.left().size(250f, 250f).expandX()
        }.onChangeEvent { event, actor ->
            gameEventManager.dispatchInputMoveEvent(actor.knobPercentX, actor.knobPercentY)
        }

        table { cell ->
            defaults().bottom().right().fillX()
            imageButtonWidget(Images.IMAGE_JUMP) {
                it.padBottom(70f).padRight(-20f)
            }.onTouch(
                touchDownListener = { this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.JUMP) },
                touchUpListener = { this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.JUMP) }
            )
            verticalGroup {
                imageButtonWidget(Images.IMAGE_ATTACK, 20f, 20f).onTouch(
                    touchDownListener = { this@GameHUD.gameEventManager.dispatchInputKeyPressEvent(Key.ATTACK) },
                    touchUpListener = { this@GameHUD.gameEventManager.dispatchInputKeyReleaseEvent(Key.ATTACK) }
                )
                imageButtonWidget(Images.IMAGE_FIREBALL).onTouch(
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
