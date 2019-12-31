package com.game.quillyjumper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import java.util.*

enum class ShaderType(val vertexName: String = "default", val fragmentName: String = "default") {
    DEFAULT,
    GRAYSCALE(fragmentName = "grayScale"),
    SEPIA(fragmentName = "sepia")
}

class ShaderPrograms : Disposable {
    private val defaultShader = loadShader(ShaderType.DEFAULT)
    private val shaderPrograms = EnumMap<ShaderType, ShaderProgram>(ShaderType::class.java)

    init {
        ShaderType.values().forEach {
            if (it == ShaderType.DEFAULT) {
                shaderPrograms[ShaderType.DEFAULT] = defaultShader
            } else {
                shaderPrograms[it] = loadShader(it)
            }
        }
    }

    private fun loadShader(type: ShaderType): ShaderProgram {
        ShaderProgram(
            Gdx.files.internal("shader/${type.vertexName}.vert"),
            Gdx.files.internal("shader/${type.fragmentName}.frag")
        ).run {
            if (!isCompiled) {
                throw GdxRuntimeException("Could not load ${type.vertexName}/${type.fragmentName} shader: $log")
            }
            return this
        }
    }

    operator fun get(type: ShaderType): ShaderProgram = shaderPrograms[type] ?: defaultShader

    override fun dispose() {
        shaderPrograms.values.forEach { it.dispose() }
    }
}
