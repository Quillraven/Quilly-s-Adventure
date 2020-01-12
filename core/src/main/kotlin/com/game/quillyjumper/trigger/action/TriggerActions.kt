package com.game.quillyjumper.trigger.action

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.game.quillyjumper.*
import com.game.quillyjumper.ai.DefaultState
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.component.FadeInComponent
import com.game.quillyjumper.ecs.component.ParticleComponent
import com.game.quillyjumper.ecs.component.portalCmp
import com.game.quillyjumper.ecs.component.stateCmp
import com.game.quillyjumper.ecs.findPortal
import com.game.quillyjumper.event.GameEventListener
import ktx.collections.iterate
import ktx.math.vec2

interface TriggerAction : Pool.Poolable {
    fun update(deltaTime: Float): Boolean

    override fun reset() {
    }
}

class TriggerActionPlayMusic : TriggerAction, Music.OnCompletionListener {
    private val audioService = Gdx.app.getAudioService()
    private var step = 0
    var musicType = MusicAssets.MENU
    var loop = false
    var waitForMusicCompletion = false

    override fun update(deltaTime: Float): Boolean {
        if (step == 0) {
            step = if (waitForMusicCompletion) {
                audioService.play(musicType, loop, this)
                1
            } else {
                audioService.play(musicType, loop)
                2
            }
        }

        return step == 2
    }

    override fun reset() {
        loop = false
        waitForMusicCompletion = false
        step = 0
    }

    override fun onCompletion(music: Music) {
        step = 2
    }
}

class TriggerActionCreateCharacter : TriggerAction {
    private val characterCfgs = Gdx.app.characterConfigurations
    private val world = Gdx.app.world
    private val engine = Gdx.app.ecsEngine
    private lateinit var createdCharacter: Entity
    var type = Character.PLAYER
    var spawnLocation = vec2()
    var fadeTime = 0f
    var holdPosition = false

    override fun update(deltaTime: Float): Boolean {
        createdCharacter = engine.character(
                characterCfgs[type],
                world,
                spawnLocation.x,
                spawnLocation.y
        ) { if (fadeTime > 0f) with<FadeInComponent> { maxFadeTime = this@TriggerActionCreateCharacter.fadeTime } }

        if (holdPosition) createdCharacter.stateCmp.stateMachine.changeState(DefaultState.NONE)

        return true
    }

    override fun reset() {
        spawnLocation.set(0f, 0f)
        fadeTime = 0f
        holdPosition = false
    }

    fun character() = createdCharacter
}

class TriggerActionSetPlayerInput : TriggerAction {
    private val gameEventManager = Gdx.app.gameEventManager
    var enable = true

    override fun update(deltaTime: Float): Boolean {
        if (enable) gameEventManager.enablePlayerInput() else gameEventManager.disablePlayerInput()
        return true
    }

    override fun reset() {
        enable = true
    }
}

class TriggerActionDelay : TriggerAction {
    var delay = 0f

    override fun update(deltaTime: Float): Boolean {
        delay -= deltaTime
        return delay <= 0f
    }

    override fun reset() {
        delay = 0f
    }
}

class TriggerActionResetState : TriggerAction {
    lateinit var createCharTrigger: TriggerActionCreateCharacter

    override fun update(deltaTime: Float): Boolean {
        with(createCharTrigger.character().stateCmp.stateMachine) {
            changeState(previousState)
        }
        return true
    }
}

class TriggerActionEnablePortal : TriggerAction {
    private val engine = Gdx.app.ecsEngine
    var portalID = 0

    override fun update(deltaTime: Float): Boolean {
        engine.findPortal(portalID) { portal ->
            portal.portalCmp.active = true
            portal.add(engine.createComponent(ParticleComponent::class.java).apply {
                type = ParticleAssets.PORTAL2
                offsetX = -0.5f
            })
        }
        return true
    }

    override fun reset() {
        portalID = 0
    }
}

class TriggerActionWaitCreatedCharacterDeath : TriggerAction, GameEventListener {
    private val gameEventManager = Gdx.app.gameEventManager
    private var step = 0
    val createdCharTriggers = Array<TriggerActionCreateCharacter>(3)

    override fun update(deltaTime: Float): Boolean {
        if (createdCharTriggers.isEmpty) step = 2

        if (step == 0) {
            gameEventManager.addGameEventListener(this)
            step = 1
        }

        return step == 2
    }

    override fun reset() {
        gameEventManager.removeGameEventListener(this)
        createdCharTriggers.clear()
        step = 0
    }

    override fun characterDeath(character: Entity) {
        createdCharTriggers.iterate { action, iterator ->
            if (action.character() == character) iterator.remove()
        }
    }
}
