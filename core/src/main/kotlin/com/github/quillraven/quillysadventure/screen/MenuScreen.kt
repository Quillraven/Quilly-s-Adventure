package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.quillysadventure.assets.SoundAssets
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.component.AnimationComponent
import com.github.quillraven.quillysadventure.ecs.component.FacingDirection
import com.github.quillraven.quillysadventure.ecs.component.facingCmp
import com.github.quillraven.quillysadventure.ecs.getCharacter
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.map.MapManager
import com.github.quillraven.quillysadventure.map.MapType
import com.github.quillraven.quillysadventure.ui.MenuHUD
import ktx.actors.*
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get

class MenuScreen(
    private val game: KtxGame<KtxScreen>,
    private val ecsEngine: Engine,
    private val mapManager: MapManager,
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService,
    private val rayHandler: RayHandler,
    private val gameViewport: Viewport,
    private val stage: Stage,
    bundle: I18NBundle
) : KtxScreen {
    private var lastSoundVolume = audioService.soundVolume
    private var lastMusicVolume = audioService.musicVolume

    private val hud: MenuHUD = MenuHUD(bundle).apply {
        newGameLabel.onClick { game.setScreen<GameScreen>() }
        // sound buttons increase / decrease sound volume
        soundWidget.audioIncreaseButton.onClick {
            audioService.soundVolume += 0.05f
            audioService.play(SoundAssets.PING)
            updateSoundVolume(audioService.soundVolume)
        }
        soundWidget.audioReduceButton.onClick {
            audioService.soundVolume -= 0.05f
            audioService.play(SoundAssets.PING)
            updateSoundVolume(audioService.soundVolume)
        }
        soundWidget.checkBox.onClickEvent { _, actor ->
            if (actor.isChecked) {
                audioService.soundVolume = lastSoundVolume
            } else {
                lastSoundVolume = audioService.soundVolume
                audioService.soundVolume = 0f
            }
        }
        // music buttons increase / decrease music volume
        musicWidget.audioIncreaseButton.onClick {
            audioService.musicVolume += 0.05f
            updateMusicVolume(audioService.musicVolume)
        }
        musicWidget.audioReduceButton.onClick {
            audioService.musicVolume -= 0.05f
            updateMusicVolume(audioService.musicVolume)
        }
        musicWidget.checkBox.onClickEvent { _, actor ->
            if (actor.isChecked) {
                audioService.musicVolume = lastMusicVolume
            } else {
                lastMusicVolume = audioService.musicVolume
                audioService.musicVolume = 0f
            }
        }
    }
    private val tmpEntities = Array<Entity>(2)

    override fun show() {
        mapManager.setMap(MapType.MAIN_MENU)
        stage.addActor(hud)
        stage.addActor(hud.creditsTable)

        // position UI elements
        hud.centerPosition(width = hud.width + 50f)
        hud.creditsTable.centerPosition(stage.width * 1.15f)

        // "fade in" menu hud
        hud.clearActions()
        hud += moveBy(-200f, 0f) + moveBy(200f, 0f, 2f, Interpolation.bounceOut)
        // and update volume information
        hud.updateSoundVolume(audioService.soundVolume)
        hud.updateMusicVolume(audioService.musicVolume)

        // adjust some characters to fit the menu scene
        ecsEngine.getCharacter(Character.GIRL, tmpEntities).forEach {
            it.facingCmp.direction = FacingDirection.LEFT
        }
        ecsEngine.entities.forEach {
            it[AnimationComponent.mapper]?.animationSpeed = 0.75f
        }
        gameEventManager.disablePlayerInput()
    }

    override fun hide() {
        stage.clear()
        // reset menu specific character cfgs
        ecsEngine.entities.forEach {
            it[AnimationComponent.mapper]?.animationSpeed = 1f
        }
        gameEventManager.enablePlayerInput()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        if (width != stage.viewport.screenWidth || height != stage.viewport.screenHeight) {
            rayHandler.resizeFBO(width / 4, height / 4)
        }
        gameViewport.update(width, height, true)
        rayHandler.useCustomViewport(
            gameViewport.screenX,
            gameViewport.screenY,
            gameViewport.screenWidth,
            gameViewport.screenHeight
        )
    }

    override fun render(delta: Float) {
        ecsEngine.update(delta)
        audioService.update()

        stage.viewport.apply()
        stage.act()
        stage.draw()
    }
}
