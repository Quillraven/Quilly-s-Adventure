package com.game.quillyjumper.event

import com.badlogic.ashley.core.Entity

interface GameEventListener {
    fun activateSavepoint(savepoint: Entity) {}
}