package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

inline fun Actor.onTouch(
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
