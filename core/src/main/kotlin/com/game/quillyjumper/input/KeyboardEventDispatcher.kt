package com.game.quillyjumper.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.game.quillyjumper.ecs.component.AttackOrder
import com.game.quillyjumper.ecs.component.CastOrder
import com.game.quillyjumper.ecs.component.JumpOrder
import com.game.quillyjumper.ecs.component.MoveOrder
import com.game.quillyjumper.event.GameEventManager
import ktx.app.KtxInputAdapter

class KeyboardEventDispatcher(private val gameEventManager: GameEventManager) : KtxInputAdapter {
    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.A -> gameEventManager.dispatchInputMoveEvent(MoveOrder.LEFT)
            Input.Keys.D -> gameEventManager.dispatchInputMoveEvent(MoveOrder.RIGHT)
            Input.Keys.SPACE -> gameEventManager.dispatchInputJumpEvent(JumpOrder.JUMP)
            Input.Keys.ESCAPE -> gameEventManager.dispatchInputExitEvent()
            Input.Keys.CONTROL_LEFT -> gameEventManager.dispatchInputAttackEvent(AttackOrder.ATTACK_ONCE)
            Input.Keys.SHIFT_LEFT -> gameEventManager.dispatchInputCastEvent(CastOrder.BEGIN_CAST)
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.A -> gameEventManager.dispatchInputMoveEvent(if (Gdx.input.isKeyPressed(Input.Keys.D)) MoveOrder.RIGHT else MoveOrder.NONE)
            Input.Keys.D -> gameEventManager.dispatchInputMoveEvent(if (Gdx.input.isKeyPressed(Input.Keys.A)) MoveOrder.LEFT else MoveOrder.NONE)
            Input.Keys.SPACE -> gameEventManager.dispatchInputJumpEvent(JumpOrder.NONE)
            Input.Keys.CONTROL_LEFT -> gameEventManager.dispatchInputAttackEvent(AttackOrder.NONE)
            Input.Keys.SHIFT_LEFT -> gameEventManager.dispatchInputCastEvent(CastOrder.NONE)
        }
        return true
    }
}