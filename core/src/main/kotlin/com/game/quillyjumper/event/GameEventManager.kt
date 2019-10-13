package com.game.quillyjumper.event

import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.ecs.component.AttackOrder
import com.game.quillyjumper.ecs.component.CastOrder
import com.game.quillyjumper.ecs.component.JumpOrder
import com.game.quillyjumper.ecs.component.MoveOrder
import com.game.quillyjumper.input.InputListener
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener

class GameEventManager {
    // input event related stuff
    private val inputListener = Array<InputListener>()

    fun addInputListener(listener: InputListener) = inputListener.add(listener)

    fun removeInputListener(listener: InputListener) = inputListener.removeValue(listener, true)

    fun dispatchInputMoveEvent(order: MoveOrder) = inputListener.forEach { it.move(order) }

    fun dispatchInputJumpEvent(order: JumpOrder) = inputListener.forEach { it.jump(order) }

    fun dispatchInputAttackEvent(order: AttackOrder) = inputListener.forEach { it.attack(order) }

    fun dispatchInputCastEvent(order: CastOrder) = inputListener.forEach { it.cast(order) }

    fun dispatchInputExitEvent() = inputListener.forEach { it.exit() }

    // map related stuff
    private val mapListener = Array<MapChangeListener>()

    fun addMapChangeListener(listener: MapChangeListener) = mapListener.add(listener)

    fun removeMapChangeListener(listener: MapChangeListener) = mapListener.removeValue(listener, true)

    fun dispatchMapChangeEvent(newMap: Map) = mapListener.forEach { it.mapChange(newMap) }
}