package com.game.quillyjumper.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.game.quillyjumper.ecs.component.JumpDirection
import com.game.quillyjumper.ecs.component.MoveDirection
import com.game.quillyjumper.event.GameEventManager
import ktx.app.KtxInputAdapter

class KeyboardEventDispatcher(private val gameEventManager: GameEventManager) : KtxInputAdapter {
    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.A -> gameEventManager.dispatchInputMoveEvent(MoveDirection.LEFT)
            Input.Keys.D -> gameEventManager.dispatchInputMoveEvent(MoveDirection.RIGHT)
            Input.Keys.SPACE -> gameEventManager.dispatchInputJumpEvent(JumpDirection.JUMPING)
            Input.Keys.ESCAPE -> gameEventManager.dispatchInputExitEvent()
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.A -> gameEventManager.dispatchInputMoveEvent(if (Gdx.input.isKeyPressed(Input.Keys.D)) MoveDirection.RIGHT else MoveDirection.STOP)
            Input.Keys.D -> gameEventManager.dispatchInputMoveEvent(if (Gdx.input.isKeyPressed(Input.Keys.A)) MoveDirection.LEFT else MoveDirection.STOP)
            Input.Keys.SPACE -> gameEventManager.dispatchInputJumpEvent(JumpDirection.STOP)
        }
        return true
    }
}