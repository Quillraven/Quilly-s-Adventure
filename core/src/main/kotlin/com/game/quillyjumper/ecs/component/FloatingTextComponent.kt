package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.StringBuilder
import ktx.ashley.get
import ktx.ashley.mapperFor
import ktx.math.vec2

class FloatingTextComponent(
    val stringBuilder: StringBuilder = StringBuilder(4),
    val speed: Vector2 = vec2(),
    var lifeSpan: Float = 0f,
    var color: Color = Color.WHITE
) : Component, Pool.Poolable {
    lateinit var font: BitmapFont

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