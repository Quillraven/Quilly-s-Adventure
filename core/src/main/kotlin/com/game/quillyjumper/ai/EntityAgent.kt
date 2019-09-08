package com.game.quillyjumper.ai

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.fsm.State
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.input.InputController
import com.game.quillyjumper.input.InputKey

class EntityAgent(
    var entity: Entity,
    val input: InputController,
    val audioManager: AudioManager,
    var state: StateComponent,
    var physic: PhysicComponent,
    var collision: CollisionComponent,
    var animation: AnimationComponent,
    var render: RenderComponent,
    var move: MoveComponent,
    var jump: JumpComponent
) {
    fun keyPressed(key: InputKey) = input.isPressed(key)

    fun changeState(state: State<EntityAgent>) = this.state.stateMachine.changeState(state)
}
