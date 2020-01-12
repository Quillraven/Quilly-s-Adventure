package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.game.quillyjumper.configuration.Character
import ktx.ashley.mapperFor
import ktx.ashley.get

class CharacterTypeComponent(var type: Character = Character.PLAYER) : Component, Pool.Poolable {
    override fun reset() {
        type = Character.PLAYER
    }

    companion object {
        val mapper = mapperFor<CharacterTypeComponent>()
    }
}

val Entity.characterCmp: CharacterTypeComponent
    get() = this[CharacterTypeComponent.mapper]
            ?: throw KotlinNullPointerException("Trying to access a character type component which is null")
