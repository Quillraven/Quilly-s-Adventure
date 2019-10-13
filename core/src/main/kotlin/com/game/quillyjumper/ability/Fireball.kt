package com.game.quillyjumper.ability

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.ecs.component.transfCmp
import com.game.quillyjumper.ecs.missile

class Fireball(owner: Entity, world: World, engine: Engine) : Ability(owner, world, engine) {
    override val cost = 0
    override fun getCooldownTime() = 2f

    override fun cast() {
        super.cast()
        with(owner.transfCmp) {
            engine.missile(
                owner,
                world,
                position.x + size.x,
                position.y + size.y * 0.25f,
                0.5f,
                0.5f,
                4f,
                1f,
                5f,
                ParticleAssets.FIREBALL
            )
        }
    }
}