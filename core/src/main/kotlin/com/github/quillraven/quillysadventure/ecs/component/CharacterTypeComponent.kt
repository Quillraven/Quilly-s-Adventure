package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.configuration.Character
import ktx.ashley.mapperFor

class CharacterTypeComponent(var type: Character = Character.PLAYER) : Component, Pool.Poolable {
    override fun reset() {
        type = Character.PLAYER
    }

    companion object {
        val mapper = mapperFor<CharacterTypeComponent>()
    }
}
