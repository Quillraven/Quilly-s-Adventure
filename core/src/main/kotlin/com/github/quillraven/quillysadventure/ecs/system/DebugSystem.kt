package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.ashley.get

class DebugSystem(private val gameEventManager: GameEventManager) : EntitySystem() {
    init {
        setProcessing(true)
    }

    override fun update(deltaTime: Float) {
        when {
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_0) -> {
                val system = engine.getSystem(RenderPhysicDebugSystem::class.java)
                system.setProcessing(!system.checkProcessing())
            }
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> {
                engine.getSystem(RenderSystem::class.java).setNormalColor()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> {
                engine.getSystem(RenderSystem::class.java).setGrayScale()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) -> {
                engine.getSystem(RenderSystem::class.java).setSepia()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) -> {
                engine.entities.forEach {
                    val playerCmp = it[PlayerComponent.mapper]
                    if (playerCmp != null) {
                        it.statsCmp.level = 3
                        gameEventManager.dispatchCharacterLevelUpEvent(it, 3, 0, 300)
                    }
                }
            }
        }
    }
}
