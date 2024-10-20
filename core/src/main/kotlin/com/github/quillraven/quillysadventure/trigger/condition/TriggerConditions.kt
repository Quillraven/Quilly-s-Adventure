package com.github.quillraven.quillysadventure.trigger.condition

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.ecs.component.TmxMapComponent
import com.github.quillraven.quillysadventure.ecsEngine
import ktx.ashley.get

interface TriggerCondition : Pool.Poolable {
    fun evaluate(): Boolean

    override fun reset() = Unit
}

class TriggerConditionIsEntityAlive : TriggerCondition {
    var tmxMapID: Int = -1
    var checkAlive: Boolean = true
    private val engine = Gdx.app.ecsEngine

    /**
     * If [checkAlive] is **true** then this method returns true if and only if
     * the entity of the given ID is **alive**.
     *
     * If [checkAlive] is **false** then this method returns true if and only if
     * the entity of the given ID is **dead**.
     */
    override fun evaluate(): Boolean {
        engine.entities.forEach {
            val tmx = it[TmxMapComponent.mapper]
            if (tmx?.id == tmxMapID) {
                return checkAlive
            }
        }

        return !checkAlive
    }

    override fun reset() {
        tmxMapID = -1
        checkAlive = true
    }
}
