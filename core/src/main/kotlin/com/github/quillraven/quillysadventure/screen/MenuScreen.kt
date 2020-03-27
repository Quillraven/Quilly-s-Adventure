package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy
import com.badlogic.gdx.scenes.scene2d.ui.Image
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
import com.github.quillraven.quillysadventure.preferences
import com.github.quillraven.quillysadventure.ui.MenuHUD
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.actors.onClickEvent
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.util.*

private const val KEY_SOUND_VOLUME = "soundVolume"
private const val KEY_MUSIC_VOLUME = "musicVolume"
private const val KEY_SOUND_ENABLED = "soundEnabled"
private const val KEY_MUSIC_ENABLED = "musicEnabled"

class MenuScreen(
    private val game: KtxGame<KtxScreen>,
    ecsEngine: Engine,
    private val mapManager: MapManager,
    gameEventManager: GameEventManager,
    audioService: AudioService,
    rayHandler: RayHandler,
    gameViewport: Viewport,
    stage: Stage,
    bundle: I18NBundle,
    private val preferences: Preferences = Gdx.app.preferences
) : Screen(ecsEngine, audioService, bundle, stage, gameEventManager, rayHandler, gameViewport) {
    private var lastSoundVolume = audioService.soundVolume
    private var lastMusicVolume = audioService.musicVolume
    private lateinit var bannerTexture: Texture

    private val hud: MenuHUD = MenuHUD(bundle).apply {
        newGameLabel.onClick { game.setScreen<IntroScreen>() }
        continueLabel.onClick { game.setScreen<GameScreen>() }
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
        super.show()
        bannerTexture = if (bundle.locale == Locale.GERMAN) {
            Texture(Gdx.files.internal("ui/banner_de.png"))
        } else {
            Texture(Gdx.files.internal("ui/banner_en.png"))
        }
        val banner = Image(bannerTexture).apply {
            setPosition(30f, 820f)
            this += delay(1.5f) + moveBy(0f, -310f, 2f, Interpolation.bounceOut)
        }
        mapManager.setMap(MapType.MAIN_MENU)
        stage.addActor(hud)
        stage.addActor(hud.creditsTable)
        stage.addActor(banner)

        // position UI elements
        hud.centerPosition(hud.width + 50f, stage.height * 0.95f)
        hud.creditsTable.centerPosition(stage.width * 1.15f, stage.height * 0.95f)

        // "fade in" menu hud
        hud.clearActions()
        hud += moveBy(-400f, 0f) + moveBy(400f, 0f, 2f, Interpolation.bounceOut)

        // adjust some characters to fit the menu scene
        engine.getCharacter(Character.GIRL, tmpEntities).forEach {
            it.facingCmp.direction = FacingDirection.LEFT
        }
        engine.entities.forEach {
            it[AnimationComponent.mapper]?.animationSpeed = 0.75f
        }
        gameEventManager.disablePlayerInput()

        // read menu settings from preferences and update UI
        lastSoundVolume = preferences[KEY_SOUND_VOLUME, 1f]
        lastMusicVolume = preferences[KEY_MUSIC_VOLUME, 1f]
        audioService.soundVolume = lastSoundVolume
        audioService.musicVolume = lastMusicVolume
        hud.run {
            updateSoundVolume(audioService.soundVolume)
            updateMusicVolume(audioService.musicVolume)
            soundWidget.checkBox.isChecked = preferences[KEY_SOUND_ENABLED, true]
            if (!soundWidget.checkBox.isChecked) {
                audioService.soundVolume = 0f
            }
            musicWidget.checkBox.isChecked = preferences[KEY_MUSIC_ENABLED, true]
            if (!musicWidget.checkBox.isChecked) {
                audioService.musicVolume = 0f
            }
        }
    }

    override fun hide() {
        super.hide()
        // reset menu specific character cfgs
        engine.entities.forEach {
            it[AnimationComponent.mapper]?.animationSpeed = 1f
        }
        gameEventManager.enablePlayerInput()
        bannerTexture.dispose()

        // store menu settings
        preferences.flush {
            this[KEY_SOUND_VOLUME] = audioService.soundVolume
            this[KEY_MUSIC_VOLUME] = audioService.musicVolume
            this[KEY_SOUND_ENABLED] = hud.soundWidget.checkBox.isChecked
            this[KEY_MUSIC_ENABLED] = hud.musicWidget.checkBox.isChecked
        }
    }
}
