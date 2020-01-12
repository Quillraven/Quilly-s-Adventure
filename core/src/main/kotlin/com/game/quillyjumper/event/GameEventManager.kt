package com.game.quillyjumper.event

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.input.InputListener
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener
import ktx.app.KtxInputAdapter

enum class Key {
    JUMP, ATTACK, CAST, EXIT
}

class GameEventManager : KtxInputAdapter {
    // input event related stuff
    private val inputListeners = Array<InputListener>()
    private var ignoreInput = false

    fun addInputListener(listener: InputListener) = inputListeners.add(listener)

    fun removeInputListener(listener: InputListener) = inputListeners.removeValue(listener, true)

    fun dispatchInputMoveEvent(percX: Float, percY: Float) {
        if (ignoreInput) return
        inputListeners.forEach { it.move(percX, percY) }
    }

    fun dispatchInputKeyPressEvent(key: Key) {
        if (ignoreInput) return
        inputListeners.forEach { it.keyPressed(key) }
    }

    fun dispatchInputKeyReleaseEvent(key: Key) {
        if (ignoreInput) return
        inputListeners.forEach { it.keyReleased(key) }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (ignoreInput) return true
        when (keycode) {
            Input.Keys.A -> dispatchInputMoveEvent(-1f, 0f)
            Input.Keys.D -> dispatchInputMoveEvent(1f, 0f)
            Input.Keys.SPACE -> dispatchInputKeyPressEvent(Key.JUMP)
            Input.Keys.CONTROL_LEFT -> dispatchInputKeyPressEvent(Key.ATTACK)
            Input.Keys.SHIFT_LEFT -> dispatchInputKeyPressEvent(Key.CAST)
            Input.Keys.ESCAPE -> dispatchInputKeyPressEvent(Key.EXIT)
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if (ignoreInput) return true
        when (keycode) {
            Input.Keys.A -> dispatchInputMoveEvent(if (Gdx.input.isKeyPressed(Input.Keys.D)) 1f else 0f, 0f)
            Input.Keys.D -> dispatchInputMoveEvent(if (Gdx.input.isKeyPressed(Input.Keys.A)) -1f else 0f, 0f)
            Input.Keys.SPACE -> dispatchInputKeyReleaseEvent(Key.JUMP)
            Input.Keys.CONTROL_LEFT -> dispatchInputKeyReleaseEvent(Key.ATTACK)
            Input.Keys.SHIFT_LEFT -> dispatchInputKeyReleaseEvent(Key.CAST)
            Input.Keys.ESCAPE -> dispatchInputKeyReleaseEvent(Key.EXIT)
        }
        return true
    }

    fun disablePlayerInput() {
        ignoreInput = true
        inputListeners.forEach {
            it.move(0f, 0f)
            it.keyReleased(Key.JUMP)
        }
    }

    fun enablePlayerInput() {
        ignoreInput = false
    }

    // map related stuff
    private val mapListeners = Array<MapChangeListener>()

    fun addMapChangeListener(listener: MapChangeListener) = mapListeners.add(listener)

    fun removeMapChangeListener(listener: MapChangeListener) = mapListeners.removeValue(listener, true)

    fun dispatchMapChangeEvent(newMap: Map) = mapListeners.forEach { it.mapChange(newMap) }

    // game event related stuff
    private val gameEventListeners = Array<GameEventListener>()

    fun addGameEventListener(listener: GameEventListener) = gameEventListeners.add(listener)

    fun removeGameEventListener(listener: GameEventListener) = gameEventListeners.removeValue(listener, true)

    fun dispatchGameActivateSavepointEvent(savepoint: Entity) =
        gameEventListeners.forEach { it.activateSavepoint(savepoint) }

    fun dispatchPlayerTriggerContact(player: Entity, trigger: Entity) =
        gameEventListeners.forEach { it.playerTriggerContact(player, trigger) }

    fun dispatchCharacterDeath(character: Entity) =
        gameEventListeners.forEach { it.characterDeath(character) }
}
