package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.forever
import com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.quillysadventure.ai.PlayerState
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.CharacterTypeComponent
import com.github.quillraven.quillysadventure.ecs.component.aniCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.system.RenderSystem
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.map.MapManager
import com.github.quillraven.quillysadventure.map.MapType
import com.github.quillraven.quillysadventure.ui.LabelStyles
import ktx.actors.centerPosition
import ktx.actors.plusAssign
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get
import ktx.scene2d.Scene2DSkin

class EndScreen(
    private val game: KtxGame<KtxScreen>,
    engine: Engine,
    private val mapManager: MapManager,
    audioService: AudioService,
    bundle: I18NBundle,
    stage: Stage,
    gameEventManager: GameEventManager,
    rayHandler: RayHandler,
    viewport: Viewport
) :
    Screen(engine, audioService, bundle, stage, gameEventManager, rayHandler, viewport) {
    private val touchForMenuInfo =
        Label(bundle["touchForMenuInfo"], Scene2DSkin.defaultSkin, LabelStyles.LARGE.name).apply {
            wrap = true
        }
    private lateinit var ripTexture: Texture
    private lateinit var minotaur: Entity

    private var renderSystem = engine.getSystem(RenderSystem::class.java)
    private var grayness = 0f

    override fun show() {
        super.show()

        // show touch info to return to menu
        stage.addActor(touchForMenuInfo)
        with(touchForMenuInfo) {
            centerPosition()
            clearActions()
            this += forever(sequence(fadeIn(1f), fadeOut(1f)))
        }

        // fadein RIP image
        ripTexture = Texture(Gdx.files.internal("ui/death_img.png"))
        val ripImg = Image(ripTexture).apply {
            setColor(1f, 1f, 1f, 0f)
            this += fadeIn(2f)
        }
        stage.addActor(ripImg)
        ripImg.centerPosition(height = stage.height * 0.25f)

        // change to gameover map
        mapManager.setMap(MapType.GAME_OVER)

        // change renderer to grayScale
        grayness = 0f
        renderSystem.setColorShader(grayness)

        // disable player input
        gameEventManager.disablePlayerInput()

        // modify minotaur to not damage the player
        engine.entities.forEach {
            when (it[CharacterTypeComponent.mapper]?.type) {
                Character.MINOTAUR -> {
                    minotaur = it
                    it.stateCmp.stateMachine.changeState(null)
                    with(it.aniCmp) {
                        animationType = AnimationType.ATTACK2
                        mode = Animation.PlayMode.NORMAL
                    }
                }
                Character.PLAYER -> {
                    it.stateCmp.stateMachine.changeState(PlayerState.FAKE_DEATH)
                }
                else -> Unit
            }
        }
    }

    override fun hide() {
        super.hide()
        renderSystem.setNormalColor()
        gameEventManager.enablePlayerInput()
        ripTexture.dispose()
        engine.entities.forEach {
            when (it[CharacterTypeComponent.mapper]?.type) {
                Character.PLAYER -> {
                    it.stateCmp.stateMachine.changeState(PlayerState.IDLE)
                }
                else -> Unit
            }
        }
    }

    override fun render(delta: Float) {
        super.render(delta)

        // fade to gray over 5 seconds
        grayness += (delta * 0.2f)
        renderSystem.grayness = this.grayness

        val ani = minotaur.aniCmp
        if (ani.isAnimationFinished()) {
            ani.animationType = AnimationType.GUARD
            ani.mode = Animation.PlayMode.LOOP
        }

        if (Gdx.input.justTouched()) {
            game.setScreen<MenuScreen>()
        }
    }
}
