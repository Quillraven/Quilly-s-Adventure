package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class JumpDirection {
    JUMPING, FALLING, STOP
}

class JumpComponent(var direction: JumpDirection = JumpDirection.STOP,
                    var jumpSteps: Int = 0,
        // max jump steps describes the amount of steps the up force for the jump is applied
        // refer to the interval of the PhysicJumpSystem
        // e.g. if the interval is 1/60 then a value of 10 means that the force is applied over 0.16 seconds
        // which also means the user has 0.16 seconds time to release the jump button to interrupt the jump
                    var maxJumpSteps: Int = 10) : Component {
    companion object {
        val mapper = mapperFor<JumpComponent>()
    }
}
