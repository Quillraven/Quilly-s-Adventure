package com.game.quillyjumper.trigger

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.event.GameEventManager

abstract class Trigger(
    val gameEventManager: GameEventManager,
    val audioManager: AudioManager,
    val engine: Engine,
    val world: World
) {
    fun destroy() {
        gameEventManager.removeFromAllListeners(this)
    }
}