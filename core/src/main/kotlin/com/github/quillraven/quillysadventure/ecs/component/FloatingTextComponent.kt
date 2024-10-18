package com.github.quillraven.quillysadventure.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.StringBuilder
import com.github.quillraven.quillysadventure.ui.FontType
import ktx.ashley.get
import ktx.ashley.mapperFor
import ktx.math.vec2

class FloatingTextComponent : Component, Pool.Poolable {
    val stringBuilder: StringBuilder = StringBuilder(4)
    val speed: Vector2 = vec2()
    var lifeSpan: Float = 0f
    var color: Color = Color.WHITE
    var fontType: FontType = FontType.DEFAULT

    companion object {
        val mapper = mapperFor<FloatingTextComponent>()
    }

    override fun reset() {
        stringBuilder.clear()
    }
}

val Entity.floatingCmp: FloatingTextComponent
    get() = this[FloatingTextComponent.mapper]
        ?: throw KotlinNullPointerException("Trying to access a floating text component which is null")
