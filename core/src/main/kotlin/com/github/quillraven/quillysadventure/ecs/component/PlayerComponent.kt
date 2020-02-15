package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.ecs.system.TutorialType
import ktx.ashley.get
import ktx.ashley.mapperFor
import java.util.*

class PlayerComponent : Component, Pool.Poolable {
    val tutorials = EnumSet.noneOf(TutorialType::class.java)

    override fun reset() {
        tutorials.clear()
    }

    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}

val Entity.playerCmp: PlayerComponent
    get() = this[PlayerComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a player component which is null")
