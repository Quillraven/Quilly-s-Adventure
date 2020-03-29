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
import ktx.ashley.get
import ktx.collections.iterate
import ktx.math.vec2

interface TriggerAction : Pool.Poolable {
    fun update(deltaTime: Float): Boolean

    override fun reset() {
    }
}

data class TriggerActionPlayMusic(
    var musicType: MusicAssets = MusicAssets.MENU,
    var loop: Boolean = false,
    var waitForMusicCompletion: Boolean = false
) : TriggerAction, Music.OnCompletionListener {
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

data class TriggerActionCreateCharacter(
    var type: Character = Character.PLAYER,
    var spawnLocation: Vector2 = vec2(),
    var fadeTime: Float = 0f,
    var holdPosition: Boolean = false
) : TriggerAction {
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

data class TriggerActionSetPlayerInput(var enable: Boolean = true) : TriggerAction {
    private val gameEventManager = Gdx.app.gameEventManager

    override fun update(deltaTime: Float): Boolean {
        if (enable) gameEventManager.enablePlayerInput() else gameEventManager.disablePlayerInput()
        return true
    }

    override fun reset() {
        enable = true
    }
}

data class TriggerActionDelay(var delay: Float = 0f) : TriggerAction {
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

data class TriggerActionEnablePortal(var portalID: Int = 0) : TriggerAction {
    private val engine = Gdx.app.ecsEngine

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

data class TriggerActionWaitCreatedCharacterDeath(
    val createCharacterActions: Array<TriggerActionCreateCharacter> = Array<TriggerActionCreateCharacter>(3)
) : TriggerAction, GameEventListener {
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

data class TriggerActionSelectCharacterByType(var type: Character = Character.PLAYER) : TriggerActionSelectCharacter() {
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

data class TriggerActionMoveCharacter(var targetLocation: Vector2 = vec2()) : TriggerAction {
    lateinit var selectCharacterAction: TriggerActionSelectCharacter

    override fun update(deltaTime: Float): Boolean {
        selectCharacterAction.character.run {
            physicCmp.body.setTransform(targetLocation, 0f)
            transfCmp.position.set(targetLocation)
        }
        return true
    }
}

data class TriggerActionDamageCharacter(var damage: Float = 0f) : TriggerAction {
    lateinit var selectCharacterAction: TriggerActionSelectCharacter

    override fun update(deltaTime: Float): Boolean {
        selectCharacterAction.character.takeDamageCmp.run {
            damage += this@TriggerActionDamageCharacter.damage
            source = selectCharacterAction.character
        }
        return true
    }
}

data class TriggerActionDeactivateTrigger(var reset: Boolean = false) : TriggerAction {
    lateinit var trigger: Trigger

    override fun update(deltaTime: Float): Boolean {
        trigger.active = false
        trigger.resetActions()
        return true
    }
}

data class TriggerActionShowDialog(var dialogKey: String = "") : TriggerAction {
    private val gameEventManager = Gdx.app.gameEventManager


    override fun update(deltaTime: Float): Boolean {
        gameEventManager.dispatchShowDialogEvent(dialogKey)
        return true
    }
}

data class TriggerActionMoveOrderCharacter(var order: MoveOrder = MoveOrder.NONE) : TriggerAction {
    lateinit var selectCharacterAction: TriggerActionSelectCharacter


    override fun update(deltaTime: Float): Boolean {
        selectCharacterAction.character.moveCmp.order = order
        selectCharacterAction.character.aniCmp.animationType = AnimationType.RUN
        return true
    }
}

data class TriggerActionPlayAnimationCharacter(
    var type: AnimationType = AnimationType.IDLE,
    var waitForCompletion: Boolean = false,
    var mode: Animation.PlayMode = Animation.PlayMode.NORMAL
) : TriggerAction {
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
