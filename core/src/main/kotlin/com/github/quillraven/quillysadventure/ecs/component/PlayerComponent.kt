package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.ecs.system.TutorialType
import ktx.ashley.get
import ktx.ashley.mapperFor
import ktx.math.vec2
import java.util.*

class PlayerComponent : Component, Pool.Poolable {
    val tutorials: EnumSet<TutorialType> = EnumSet.noneOf(TutorialType::class.java)
    val checkpoint = vec2()

    override fun reset() {
        tutorials.clear()
        checkpoint.set(0f, 0f)
    }

    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}

val Entity.playerCmp: PlayerComponent
    get() = this[PlayerComponent.mapper]
            ?: throw KotlinNullPointerException("Trying to access a player component which is null")
