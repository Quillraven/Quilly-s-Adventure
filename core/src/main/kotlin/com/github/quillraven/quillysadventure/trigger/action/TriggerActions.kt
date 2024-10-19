package com.github.quillraven.quillysadventure.trigger.action

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.github.quillraven.quillysadventure.ai.DefaultState
import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.assets.ParticleAssets
import com.github.quillraven.quillysadventure.characterConfigurations
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.character
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.CharacterTypeComponent
import com.github.quillraven.quillysadventure.ecs.component.FadeInComponent
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.ecs.component.ParticleComponent
import com.github.quillraven.quillysadventure.ecs.component.aniCmp
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import com.github.quillraven.quillysadventure.ecs.component.portalCmp
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import com.github.quillraven.quillysadventure.ecs.component.takeDamageCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.ecs.findPortal
import com.github.quillraven.quillysadventure.ecsEngine
import com.github.quillraven.quillysadventure.event.GameEventListener
import com.github.quillraven.quillysadventure.gameEventManager
import com.github.quillraven.quillysadventure.getAudioService
import com.github.quillraven.quillysadventure.trigger.Trigger
import com.github.quillraven.quillysadventure.world
import ktx.ashley.addComponent
import ktx.ashley.get
import ktx.collections.iterate
import ktx.math.vec2

interface TriggerAction : Pool.Poolable {
    fun update(deltaTime: Float): Boolean

    override fun reset() {
    }
}

class TriggerActionPlayMusic : TriggerAction, Music.OnCompletionListener {
    var musicType: MusicAssets = MusicAssets.MENU
    var loop: Boolean = false
    var waitForMusicCompletion: Boolean = false
    private val audioService = Gdx.app.getAudioService()
    private var step = 0

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
    var type: Character = Character.PLAYER
    var spawnLocation: Vector2 = vec2()
    var fadeTime: Float = 0f
    var holdPosition: Boolean = false
    private val characterCfgs = Gdx.app.characterConfigurations
    private val world = Gdx.app.world
    private val engine = Gdx.app.ecsEngine
    private lateinit var createdCharacter: Entity

    override fun update(deltaTime: Float): Boolean {
        createdCharacter = engine.character(
            characterCfgs[type],
            world,
            spawnLocation.x,
            spawnLocation.y
        )
        if (fadeTime > 0f) {
            createdCharacter.addComponent<FadeInComponent>(engine) { maxFadeTime = fadeTime }
        }

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
    var enable: Boolean = true

    private val gameEventManager = Gdx.app.gameEventManager

    override fun update(deltaTime: Float): Boolean {
        if (enable) gameEventManager.enablePlayerInput() else gameEventManager.disablePlayerInput()
        return true
    }

    override fun reset() {
        enable = true
    }
}

class TriggerActionDelay : TriggerAction {
    var delay: Float = 0f

    override fun update(deltaTime: Float): Boolean {
        delay -= deltaTime
        return delay <= 0f
    }

    override fun reset() {
        delay = 0f
    }
}

class TriggerActionResetState : TriggerAction {
    lateinit var createCharacterAction: TriggerActionCreateCharacter

    override fun update(deltaTime: Float): Boolean {
        with(createCharacterAction.character().stateCmp.stateMachine) {
            changeState(previousState)
        }
        return true
    }
}

class TriggerActionEnablePortal : TriggerAction {
    var portalID: Int = 0
    private val engine = Gdx.app.ecsEngine

    override fun update(deltaTime: Float): Boolean {
        val portal = engine.findPortal(portalID) {} ?: return true
        portal.portalCmp.active = true
        val particleCmp = portal.addComponent<ParticleComponent>(engine)
        particleCmp.type = ParticleAssets.PORTAL2
        particleCmp.offsetX = -0.5f
        return true
    }

    override fun reset() {
        portalID = 0
    }
}

class TriggerActionWaitCreatedCharacterDeath : TriggerAction, GameEventListener {
    val createCharacterActions: Array<TriggerActionCreateCharacter> = Array(3)
    private val gameEventManager = Gdx.app.gameEventManager
    private var step = 0

    override fun update(deltaTime: Float): Boolean {
        if (createCharacterActions.isEmpty) step = 2

        if (step == 0) {
            gameEventManager.addGameEventListener(this)
            step = 1
        }

        return step == 2
    }

    override fun reset() {
        gameEventManager.removeGameEventListener(this)
        createCharacterActions.clear()
        step = 0
    }

    override fun characterDeath(character: Entity) {
        createCharacterActions.iterate { action, iterator ->
            if (action.character() == character) iterator.remove()
        }
    }
}

abstract class TriggerActionSelectCharacter : TriggerAction {
    lateinit var character: Entity
}

class TriggerActionSelectCharacterByType : TriggerActionSelectCharacter() {
    var type: Character = Character.PLAYER
    private val engine = Gdx.app.ecsEngine

    override fun update(deltaTime: Float): Boolean {
        engine.entities.forEach {
            val typeCmp = it[CharacterTypeComponent.mapper]
            if (typeCmp != null && typeCmp.type == this.type) {
                character = it
                return@forEach
            }
        }
        return true
    }
}

class TriggerActionSelectActivatingCharacter : TriggerActionSelectCharacter() {
    lateinit var trigger: Trigger

    override fun update(deltaTime: Float): Boolean {
        character = trigger.activatingCharacter
        return true
    }
}

class TriggerActionMoveCharacter : TriggerAction {
    var targetLocation: Vector2 = vec2()
    lateinit var selectCharacterAction: TriggerActionSelectCharacter

    override fun update(deltaTime: Float): Boolean {
        selectCharacterAction.character.run {
            physicCmp.body.setTransform(targetLocation, 0f)
            transfCmp.position.set(targetLocation)
        }
        return true
    }
}

class TriggerActionDamageCharacter : TriggerAction {
    var damage: Float = 0f
    lateinit var selectCharacterAction: TriggerActionSelectCharacter

    override fun update(deltaTime: Float): Boolean {
        selectCharacterAction.character.takeDamageCmp.run {
            damage += this@TriggerActionDamageCharacter.damage
            source = selectCharacterAction.character
        }
        return true
    }
}

class TriggerActionDeactivateTrigger : TriggerAction {
    var reset: Boolean = false
    lateinit var trigger: Trigger

    override fun update(deltaTime: Float): Boolean {
        trigger.active = false
        trigger.resetActions()
        return true
    }
}

class TriggerActionShowDialog : TriggerAction {
    var dialogKey: String = ""
    private val gameEventManager = Gdx.app.gameEventManager


    override fun update(deltaTime: Float): Boolean {
        gameEventManager.dispatchShowDialogEvent(dialogKey)
        return true
    }
}

class TriggerActionMoveOrderCharacter : TriggerAction {
    var order: MoveOrder = MoveOrder.NONE
    lateinit var selectCharacterAction: TriggerActionSelectCharacter


    override fun update(deltaTime: Float): Boolean {
        selectCharacterAction.character.moveCmp.order = order
        selectCharacterAction.character.aniCmp.animationType = AnimationType.RUN
        return true
    }
}

class TriggerActionPlayAnimationCharacter : TriggerAction {
    var type: AnimationType = AnimationType.IDLE
    var waitForCompletion: Boolean = false
    var mode: Animation.PlayMode = Animation.PlayMode.NORMAL
    lateinit var selectCharacterAction: TriggerActionSelectCharacter
    private var storeOrigSettings = true
    private var origMode = Animation.PlayMode.NORMAL
    private var origType = AnimationType.IDLE

    override fun update(deltaTime: Float): Boolean {
        val aniCmp = selectCharacterAction.character.aniCmp
        if (storeOrigSettings) {
            storeOrigSettings = false
            origMode = aniCmp.mode
            origType = aniCmp.animationType

            aniCmp.animationType = type
            aniCmp.mode = mode
        }

        val result = when {
            waitForCompletion -> aniCmp.isAnimationFinished()
            else -> true
        }
        if (result) {
            // restore original values
            aniCmp.mode = origMode
            aniCmp.animationType = origType
        }
        return result
    }

    override fun reset() {
        storeOrigSettings = true
        waitForCompletion = false
    }
}
