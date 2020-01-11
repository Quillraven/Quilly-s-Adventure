package com.game.quillyjumper.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor

class ImageButtonWidget(
    imageName: Images,
    skin: Skin,
    imageOffsetX: Float,
    imageOffsetY: Float,
    private val button: Button = Button(skin, "round"),
    image: Image = Image(skin[imageName]).apply {
        touchable = Touchable.disabled
        setPosition(imageOffsetX, imageOffsetY)
    }
) : WidgetGroup(button, image), KGroup {
    override fun getPrefWidth(): Float = button.width

    override fun getPrefHeight(): Float = button.height

    inline fun onTouch(
        crossinline touchDownListener: () -> Unit,
        crossinline touchUpListener: () -> Unit
    ): ClickListener {
        val clickListener = object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                touchDownListener()
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) = touchUpListener()
        }
        this.addListener(clickListener)
        return clickListener
    }
}

inline fun <S> KWidget<S>.imageButtonWidget(
    image: Images,
    imageOffsetX: Float = 0f,
    imageOffsetY: Float = 0f,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: ImageButtonWidget.(S) -> Unit = {}
) = actor(ImageButtonWidget(image, skin, imageOffsetX, imageOffsetY), init)
