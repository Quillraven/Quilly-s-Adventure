package com.game.quillyjumper.event

import com.badlogic.ashley.core.Entity

interface GameEventListener {
    fun activateSavepoint(savepoint: Entity) {}
    fun playerTriggerContact(player: Entity, trigger: Entity) {}
    fun characterDeath(character: Entity) {}
}