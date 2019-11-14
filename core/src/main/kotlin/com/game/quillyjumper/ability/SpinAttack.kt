package com.game.quillyjumper.ability

import com.game.quillyjumper.ecs.component.attackCmp
import com.game.quillyjumper.ecs.component.statsCmp
import com.game.quillyjumper.ecs.component.transfCmp
import com.game.quillyjumper.ecs.damageEmitter

class SpinAttack : Ability() {
    override val cost = 0
    override fun getCooldownTime() = 0f

    override fun cast() {
        super.cast()
        val range = owner.attackCmp.range
        val damage = owner.statsCmp.damage
        with(owner.transfCmp) {
            engine.damageEmitter(
                world,
                position.x - range,
                position.y,
                size.x + 2 * range,
                size.y,
                damage,
                0.75f,
                owner
            )
        }
    }
}