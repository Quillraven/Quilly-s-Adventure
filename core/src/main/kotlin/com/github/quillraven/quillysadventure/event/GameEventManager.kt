package com.github.quillraven.quillysadventure.event

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.Array
import com.github.quillraven.quillysadventure.ability.Ability
import com.github.quillraven.quillysadventure.input.InputListener
import com.github.quillraven.quillysadventure.map.Map
import com.github.quillraven.quillysadventure.map.MapChangeListener
import com.github.quillraven.quillysadventure.trigger.Trigger
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
        for (i in 0 until inputListeners.size) {
            inputListeners[i].run {
                move(0f, 0f)
                keyReleased(Key.JUMP)
            }
        }
    }

    fun enablePlayerInput() {
        ignoreInput = false
    }

    // map related stuff
    private val mapListeners = Array<MapChangeListener>()

    fun addMapChangeListener(listener: MapChangeListener) = mapListeners.add(listener)

    fun removeMapChangeListener(listener: MapChangeListener) = mapListeners.removeValue(listener, true)

    fun dispatchBeforeMapChangeEvent() = mapListeners.forEach { it.beforeMapChange() }

    fun dispatchMapChangeEvent(newMap: Map) = mapListeners.forEach { it.mapChange(newMap) }

    // game event related stuff
    private val gameEventListeners = Array<GameEventListener>()

    fun addGameEventListener(listener: GameEventListener) = gameEventListeners.add(listener)

    fun removeGameEventListener(listener: GameEventListener) = gameEventListeners.removeValue(listener, true)

    fun dispatchGameActivateSavepointEvent(savepoint: Entity) =
        gameEventListeners.forEach { it.activateSavepoint(savepoint) }

    fun dispatchPlayerTriggerContactEvent(player: Entity, trigger: Entity) =
        gameEventListeners.forEach { it.playerTriggerContact(player, trigger) }

    fun dispatchCharacterDeathEvent(character: Entity) =
        gameEventListeners.forEach { it.characterDeath(character) }

    fun dispatchCharacterDamagedEvent(character: Entity, damage: Float, life: Float, maxLife: Float) {
        gameEventListeners.forEach { it.characterDamaged(character, damage, life, maxLife) }
    }

    fun dispatchCharacterHealLifeEvent(character: Entity, healAmount: Float, life: Float, maxLife: Float) {
        gameEventListeners.forEach { it.characterHealLife(character, healAmount, life, maxLife) }
    }

    fun dispatchCharacterHealManaEvent(character: Entity, healAmount: Float, mana: Float, maxMana: Float) {
        gameEventListeners.forEach { it.characterHealMana(character, healAmount, mana, maxMana) }
    }

    fun dispatchCharacterCastEvent(character: Entity, ability: Ability, cost: Int, mana: Float, maxMana: Float) {
        gameEventListeners.forEach { it.characterCast(character, ability, cost, mana, maxMana) }
    }

    fun dispatchCharacterAttackReadyEvent(character: Entity) {
        gameEventListeners.forEach { it.characterAttackReady(character) }
    }

    fun dispatchCharacterAttackEvent(character: Entity) {
        gameEventListeners.forEach { it.characterAttack(character) }
    }

    fun dispatchCharacterLevelUpEvent(character: Entity, level: Int, xp: Int, xpNeeded: Int) {
        gameEventListeners.forEach { it.characterLevelUp(character, level, xp, xpNeeded) }
    }

    fun dispatchCharacterXPGainedEvent(character: Entity, xp: Int, xpNeeded: Int) {
        gameEventListeners.forEach { it.characterXPGained(character, xp, xpNeeded) }
    }

    fun dispatchShowDialogEvent(dialogKey: String) {
        gameEventListeners.forEach { it.showDialogEvent(dialogKey) }
    }

    fun dispatchTriggerFinishedEvent(trigger: Trigger) {
        gameEventListeners.forEach { it.triggerFinishEvent(trigger) }
    }
}
