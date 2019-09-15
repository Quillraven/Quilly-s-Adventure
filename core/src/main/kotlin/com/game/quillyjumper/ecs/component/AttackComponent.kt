package com.game.quillyjumper.ecs.component

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

enum class AttackOrder { NONE, ATTACK_ONCE }

class AttackComponent(
    var range: Float = 1f,
    var cooldown: Float = 1f,
    var attackTime: Float = 0f,
    var order: AttackOrder = AttackOrder.NONE
) : Component {
    companion object {
        val mapper = mapperFor<AttackComponent>()
    }
}