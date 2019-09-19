package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey

class EntityAgent(
    var entity: Entity,
    val input: InputController,
    val audioManager: AudioManager
) {
    fun keyPressed(key: InputKey) = input.isPressed(key)
}
