package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Sprite
import ktx.ashley.get
import ktx.ashley.mapperFor

class RenderComponent : Component {
    var sprite: Sprite = Sprite()

    companion object {
        val mapper = mapperFor<RenderComponent>()
    }
}

val Entity.renderCmp: RenderComponent
    get() = this[RenderComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a render component which is null")
