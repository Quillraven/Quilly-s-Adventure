package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.component.CharacterTypeComponent
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.ecs.component.renderCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.system.RenderSystem
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.map.MapManager
import com.github.quillraven.quillysadventure.map.MapType
import com.github.quillraven.quillysadventure.trigger.Trigger
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get

class IntroScreen(
    private val game: KtxGame<KtxScreen>,
    bundle: I18NBundle,
    audioService: AudioService,
    gameEventManager: GameEventManager,
    engine: Engine,
    private val mapManager: MapManager,
    rayHandler: RayHandler,
    viewport: Viewport,
    stage: Stage
) : Screen(engine, audioService, bundle, stage, gameEventManager, rayHandler, viewport) {
    override fun show() {
        super.show()

        mapManager.setMap(MapType.INTRO)
        engine.getSystem(RenderSystem::class.java).setSepia()

        engine.entities.forEach {
            val charType = it[CharacterTypeComponent.mapper]?.type
            when {
                it[PlayerComponent.mapper] != null -> {
                    // make player invisible. We only abuse him for the camera so that the camera
                    // is showing the intro scene
                    it.renderCmp.sprite.setAlpha(0f)
                }
                charType == Character.MINOTAUR -> {
                    // update minotaur so that he does not attack the invisible player
                    it.stateCmp.stateMachine.changeState(null)
                    it.moveCmp.maxSpeed = 1.8f
                }
                charType == Character.GIRL -> {
                    it.moveCmp.maxSpeed = 1.8f
                }
            }
        }
    }

    override fun hide() {
        super.hide()
        engine.getSystem(RenderSystem::class.java).setNormalColor()

        engine.entities.forEach {
            if (it[PlayerComponent.mapper] != null) {
                it.renderCmp.sprite.setAlpha(1f)
            }
        }
    }

    override fun triggerFinishEvent(trigger: Trigger) {
        game.setScreen<GameScreen>()
    }
}
