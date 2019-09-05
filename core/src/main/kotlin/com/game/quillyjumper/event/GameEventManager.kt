package com.game.quillyjumper.event

import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.ecs.component.JumpDirection
import com.game.quillyjumper.ecs.component.MoveDirection
import com.game.quillyjumper.input.InputListener
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener

class GameEventManager {
    // input event related stuff
    private val inputListener = Array<InputListener>()

    fun addInputListener(listener: InputListener) = inputListener.add(listener)

    fun removeInputListener(listener: InputListener) = inputListener.removeValue(listener, true)

    fun dispatchInputMoveEvent(direction: MoveDirection) = inputListener.forEach { it.move(direction) }

    fun dispatchInputJumpEvent(direction: JumpDirection) = inputListener.forEach { it.jump(direction) }

    fun dispatchInputExitEvent() = inputListener.forEach { it.exit() }

    // map related stuff
    private val mapListener = Array<MapChangeListener>()

    fun addMapChangeListener(listener: MapChangeListener) = mapListener.add(listener)

    fun removeMapChangeListener(listener: MapChangeListener) = mapListener.removeValue(listener, true)

    fun dispatchMapChangeEvent(newMap: Map) = mapListener.forEach { it.mapChange(newMap) }
}