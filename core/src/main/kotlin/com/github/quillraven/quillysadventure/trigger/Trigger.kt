package com.github.quillraven.quillysadventure.trigger

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.ReflectionPool
import com.github.quillraven.quillysadventure.trigger.action.TriggerAction
import com.github.quillraven.quillysadventure.trigger.condition.TriggerCondition
import ktx.collections.iterate

class Trigger : Pool.Poolable {
    private val mutableConditions = Array<TriggerCondition>(4)
    val conditions = ImmutableArray(mutableConditions)
    private val mutableActions = Array<TriggerAction>(8)
    val actions = ImmutableArray(mutableActions)
    private var currentIdx = 0
    lateinit var activatingCharacter: Entity
    var active = true

    fun update(deltaTime: Float): Boolean {
        while (active && currentIdx < actions.size() && actions[currentIdx].update(deltaTime)) {
            if (active) ++currentIdx
        }

        return currentIdx >= actions.size()
    }

    fun checkConditions(): Boolean {
        conditions.forEach {
            if (!it.evaluate()) return false
        }

        return true
    }

    override fun reset() {
        active = true
        currentIdx = 0
        mutableActions.iterate { action, iterator ->
            ReflectionPool(action.javaClass).free(action)
            iterator.remove()
        }
        mutableConditions.iterate { condition, iterator ->
            ReflectionPool(condition.javaClass).free(condition)
            iterator.remove()
        }
    }

    fun resetActions() {
        currentIdx = 0
    }

    fun addAction(action: TriggerAction) = mutableActions.add(action)

    inline fun <reified T : TriggerAction> action(init: T.() -> Unit): T {
        val newAction = ReflectionPool(T::class.java).obtain()
        newAction.init()
        addAction(newAction)
        return newAction
    }

    inline fun actions(configure: Trigger.() -> Unit = {}): Trigger {
        this.configure()
        return this
    }

    fun addCondition(condition: TriggerCondition) = mutableConditions.add(condition)

    inline fun <reified T : TriggerCondition> condition(init: T.() -> Unit): T {
        val newCondition = ReflectionPool(T::class.java).obtain()
        newCondition.init()
        addCondition(newCondition)
        return newCondition
    }

    inline fun conditions(configure: Trigger.() -> Unit = {}): Trigger {
        this.configure()
        return this
    }

    companion object {
        val pool = ReflectionPool(Trigger::class.java, 8)
    }
}
