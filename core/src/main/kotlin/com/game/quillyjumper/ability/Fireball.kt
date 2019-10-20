package com.game.quillyjumper.ability

import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.ecs.component.FacingDirection
import com.game.quillyjumper.ecs.component.facingCmp
import com.game.quillyjumper.ecs.component.transfCmp
import com.game.quillyjumper.ecs.missile

class Fireball : Ability() {
    override val cost = 5
    override fun getCooldownTime() = 2f

    override fun cast() {
        super.cast()
        with(owner.transfCmp) {
            val facing = owner.facingCmp.direction
            engine.missile(
                owner,
                world,
                if (facing == FacingDirection.RIGHT) position.x + size.x else position.x,
                position.y + size.y * 0.25f,
                0.5f,
                0.5f,
                if (facing == FacingDirection.RIGHT) 4f else -4f,
                1f,
                5f,
                ParticleAssets.FIREBALL
            )
        }
    }
}