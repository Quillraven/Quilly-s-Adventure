package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.assets.SoundAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf
import ktx.ashley.exclude
import java.util.*

class ParticleSystem(private val assets: AssetManager, private val audioManager: AudioManager) :
    IteratingSystem(allOf(ParticleComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()),
    EntityListener {
    private val effectPools = EnumMap<ParticleAssets, ParticleEffectPool>(ParticleAssets::class.java)

    init {
        ParticleAssets.values().forEach {
            assets[it].run {
                // in case of additive effects this increases the performance
                // because the sprite batch will not automatically switch its
                // blend mode. This means, we have to take care of the
                // blend mode switching ourselves
                setEmittersCleanUpBlendFunction(false)
                // scale correctly to world units otherwise the effect is super huge
                scaleEffect(UNIT_SCALE * it.scale)
            }
        }
    }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityRemoved(entity: Entity) {}

    override fun entityAdded(entity: Entity) {
        // create particle effect
        with(entity.particleCmp) {
            effect = effectPools.computeIfAbsent(type) { ParticleEffectPool(assets[type], 1, 2) }.obtain()
            if (type.sound != SoundAssets.UNKNOWN) {
                audioManager.play(type.sound)
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        // update effect position with real entity position
        val pos = entity.transfCmp.position
        entity.particleCmp.run {
            effect.setPosition(pos.x + offsetX, pos.y + offsetY)
            if (effect.isComplete) {
                entity.add(engine.createComponent(RemoveComponent::class.java))
            }
        }
    }
}