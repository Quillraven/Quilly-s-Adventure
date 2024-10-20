package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.quillysadventure.ability.Ability
import com.github.quillraven.quillysadventure.ability.FireballEffect
import com.github.quillraven.quillysadventure.assets.SoundAssets
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.drawTransitionFBOs
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.abilityCmp
import com.github.quillraven.quillysadventure.ecs.component.playerCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.system.DeathSystem
import com.github.quillraven.quillysadventure.ecs.system.FloatingTextSystem
import com.github.quillraven.quillysadventure.ecs.system.KEY_SAVE_STATE
import com.github.quillraven.quillysadventure.ecs.system.LightSystem
import com.github.quillraven.quillysadventure.ecs.system.RenderSystem
import com.github.quillraven.quillysadventure.ecs.system.SaveState
import com.github.quillraven.quillysadventure.ecs.system.SaveSystem
import com.github.quillraven.quillysadventure.ecs.system.TutorialSystem
import com.github.quillraven.quillysadventure.ecs.system.TutorialType
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.event.Key
import com.github.quillraven.quillysadventure.input.InputListener
import com.github.quillraven.quillysadventure.map.Map
import com.github.quillraven.quillysadventure.map.MapChangeListener
import com.github.quillraven.quillysadventure.map.MapManager
import com.github.quillraven.quillysadventure.map.MapType
import com.github.quillraven.quillysadventure.preferences
import com.github.quillraven.quillysadventure.ui.GameHUD
import com.github.quillraven.quillysadventure.ui.ImageButtonStyles
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.get
import com.github.quillraven.quillysadventure.ui.widget.ConfirmDialogWidget
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.ashley.get
import ktx.math.component1
import ktx.math.component2
import ktx.preferences.get
import ktx.scene2d.Scene2DSkin

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    bundle: I18NBundle,
    gameEventManager: GameEventManager,
    audioService: AudioService,
    engine: Engine,
    private val mapManager: MapManager,
    rayHandler: RayHandler,
    viewport: Viewport,
    stage: Stage,
    private val batch: Batch = stage.batch,
    private val preferences: Preferences = Gdx.app.preferences
) : Screen(engine, audioService, bundle, stage, gameEventManager, rayHandler, viewport), InputListener,
    MapChangeListener {
    private val closeButton = Image(Scene2DSkin.defaultSkin[Images.BUTTON_CLOSE]).apply { setScale(0.75f) }
    private val hud = GameHUD(gameEventManager, bundle["statsTitle"], bundle["skills"]).apply {
        statsWidget.apply {
            addSkill(bundle["fireball"], bundle["requiresLvl3"], Images.IMAGE_FIREBALL)
        }
    }
    private val backToMenuConfirmDialog = ConfirmDialogWidget(
        bundle["backToMenu.info"],
        bundle["yes"],
        bundle["no"]
    ).apply {
        yesLabel.onClick {
            stage.root.removeActor(this@apply)
            game.setScreen<MenuScreen>()
        }
        noLabel.onClick {
            stage.root.removeActor(this@apply)
        }
    }

    private var gameOver = false

    private val tutorialSystem = TutorialSystem(gameEventManager)
    private val saveSystem = SaveSystem(preferences, mapManager)

    private val lifeTxt = bundle["life"]
    private val manaTxt = bundle["mana"]
    private val levelTxt = bundle["level"]

    private val xpTxt = bundle["xp"]
    private val xpAbbreviation = bundle["xpAbbreviation"]

    private var ignoreMapTransition = false
    private val maxMapTransitionTime = 1f
    private var mapTransitionTime = maxMapTransitionTime
    private var prevMapFrameBuffer = FrameBuffer(
        Pixmap.Format.RGB888,
        Gdx.graphics.width,
        Gdx.graphics.height,
        false
    )
    private var currentMapFrameBuffer = FrameBuffer(
        Pixmap.Format.RGB888,
        Gdx.graphics.width,
        Gdx.graphics.height,
        false
    )

    override fun show() {
        super.show()
        gameOver = false

        // setup game UI
        setupGameUI()

        // get save state to reset to last save state values
        val saveState: SaveState? = preferences[KEY_SAVE_STATE]

        // add game screen as input listener to react when the player wants to quit the game (=exit key pressed)
        gameEventManager.addInputListener(this)
        // add screen as MapChangeListener to show the map name information when changing maps
        gameEventManager.addMapChangeListener(this)

        // set initial map
        initMapManager(saveState)

        // game screen specific systems
        engine.addSystem(tutorialSystem)
        engine.addSystem(saveSystem)

        // set player hud info (life, mana, attack ready, etc.)
        engine.entities.forEach { entity ->
            val playerCmp = entity[PlayerComponent.mapper]
            if (playerCmp != null) {
                initPlayerProperties(saveState, entity)
                updatePlayerHUD(entity)
            }
        }
    }

    private fun updatePlayerHUD(player: Entity) {
        with(player.statsCmp) {
            hud.infoWidget.resetHudValues(this.life / this.maxLife, this.mana / this.maxMana)
            hud.statsWidget.updateLevel(levelTxt, this.level)
                .updateExperience(
                    xpTxt,
                    xpAbbreviation,
                    this.xp,
                    engine.getSystem(DeathSystem::class.java).getNeededExperience(this.level)
                )
                .updateLife(lifeTxt, this.life.toInt(), this.maxLife.toInt())
                .updateMana(manaTxt, this.mana.toInt(), this.maxMana.toInt())
                .updateDamage(bundle["damage"], this.damage.toInt())
                .updateArmor(bundle["armor"], this.armor.toInt())

            if (level >= 3) {
                hud.statsWidget.activateSkill(0)
            }
        }

        if (player.abilityCmp.hasAbility(FireballEffect)) {
            hud.skillButton.style =
                hud.skin.get(
                    ImageButtonStyles.FIREBALL.name,
                    ImageButton.ImageButtonStyle::class.java
                )
        }
    }

    private fun initPlayerProperties(
        saveState: SaveState?,
        player: Entity
    ) {
        if (saveState != null) {
            val statsCmp = player.statsCmp
            statsCmp.damage = saveState.damage
            statsCmp.life = saveState.life
            statsCmp.maxLife = saveState.maxLife
            statsCmp.mana = saveState.mana
            statsCmp.maxMana = saveState.maxMana
            statsCmp.armor = saveState.armor
            statsCmp.level = saveState.level
            statsCmp.xp = saveState.xp

            player.abilityCmp.run {
                this.abilityToCastIdx = saveState.abilityToCastIdx
                saveState.abilities.forEach {
                    this.addAbility(player, it)
                }
            }

            val playerCmp = player.playerCmp
            val (x, y) = saveState.checkpoint
            playerCmp.checkpoint.set(x, y)
            mapManager.movePlayer(x, y)
            playerCmp.tutorials.clear()
            for (i in 0 until saveState.tutorials.size) {
                playerCmp.tutorials.add(TutorialType.entries[saveState.tutorials[i]])
            }
        }
    }

    private fun initMapManager(saveState: SaveState?) {
        mapTransitionTime = maxMapTransitionTime
        ignoreMapTransition = true
        mapManager.mapEntityCache.clear()
        if (saveState != null) {
            saveState.mapEntities.forEach {
                mapManager.storeMapEntities(MapType.entries[it.key], it.value)
            }
            mapManager.setMap(saveState.currentMap)
        } else {
            mapManager.setMap(MapType.MAP1)
        }
    }

    private fun setupGameUI() {
        stage.addActor(closeButton)
        closeButton.setPosition(0f, stage.height - closeButton.height * closeButton.scaleY)
        closeButton.onClick { switchToMenuScreen() }

        stage.addActor(hud)
        stage.addActor(hud.statsWidget)
        hud.statsWidget.setPosition(-2000f, 0f)
        hud.statsWidget.skill(0)?.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (tapCount == 2) {
                    // when skill icon in stats menu gets double tapped then make it an active skill
                    // for the cast button
                    engine.entities.forEach {
                        if (it[PlayerComponent.mapper] != null) {
                            it.abilityCmp.addAbility(it, FireballEffect)
                        }
                    }
                    hud.skillButton.style =
                        hud.skin.get(ImageButtonStyles.FIREBALL.name, ImageButton.ImageButtonStyle::class.java)
                }
            }
        })
    }

    override fun hide() {
        super.hide()
        gameEventManager.removeInputListener(this)
        gameEventManager.removeMapChangeListener(this)
        engine.removeSystem(tutorialSystem)
        engine.removeSystem(saveSystem)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        prevMapFrameBuffer.dispose()
        prevMapFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, width, height, false)
        currentMapFrameBuffer.dispose()
        currentMapFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, width, height, false)
    }

    override fun render(delta: Float) {
        if (mapTransitionTime >= maxMapTransitionTime) {
            // no map transition -> render current active screen
            super.render(delta)

            if (gameOver) {
                // process gameover at the end of a frame because switching screens within engine.update is a bad idea.
                // The reason is that the hide method of GameScreen will be called and therefore some game events
                // will not be processed correctly because listeners are removed
                game.setScreen<EndScreen>()
            }
        }

        if (mapTransitionTime < maxMapTransitionTime) {
            if (mapTransitionTime == 0f) {
                // render current screen to FBO
                currentMapFrameBuffer.bind()
                clearScreen(0f, 0f, 0f, 1f)
                engine.update(1 / 30f)

                // return to original framebuffer for rendering
                FrameBuffer.unbind()
            }

            // mix previous and current screen snapshot together
            // screenshots are taken within beforeMapChange and mapChange method
            mapTransitionTime += delta
            batch.drawTransitionFBOs(
                prevMapFrameBuffer,
                currentMapFrameBuffer,
                mapTransitionTime / maxMapTransitionTime
            )
            stage.viewport.apply()
            stage.draw()
        }
    }

    override fun keyPressed(key: Key) {
        if (key == Key.EXIT) {
            // player pressed exit key -> go back to menu
            switchToMenuScreen()
        }
    }

    private fun switchToMenuScreen() {
        stage.addActor(backToMenuConfirmDialog)
        backToMenuConfirmDialog.centerPosition()
        backToMenuConfirmDialog.toFront()
    }

    override fun characterDeath(character: Entity) {
        if (character[PlayerComponent.mapper] != null) {
            gameOver = true
        }
    }

    override fun characterDamaged(character: Entity, damage: Float, life: Float, maxLife: Float) {
        val playerCmp = character[PlayerComponent.mapper]
        if (playerCmp != null) {
            hud.infoWidget.scaleLifeBarTo(life / maxLife)
            hud.statsWidget.updateLife(lifeTxt, life.toInt(), maxLife.toInt())
        }
    }

    override fun characterHealLife(character: Entity, healAmount: Float, life: Float, maxLife: Float) {
        if (character[PlayerComponent.mapper] != null) {
            hud.infoWidget.scaleLifeBarTo(life / maxLife)
            hud.statsWidget.updateLife(lifeTxt, life.toInt(), maxLife.toInt())
        }
    }

    override fun characterHealMana(character: Entity, healAmount: Float, mana: Float, maxMana: Float) {
        if (character[PlayerComponent.mapper] != null) {
            hud.infoWidget.scaleManaBarTo(mana / maxMana)
            hud.statsWidget.updateMana(manaTxt, mana.toInt(), maxMana.toInt())
        }
    }

    override fun characterCast(character: Entity, ability: Ability, cost: Int, mana: Float, maxMana: Float) {
        if (character[PlayerComponent.mapper] != null) {
            hud.infoWidget.scaleManaBarTo(mana / maxMana)
            hud.statsWidget.updateMana(manaTxt, mana.toInt(), maxMana.toInt())
        }
    }

    override fun characterAttack(character: Entity) {
        val playerCmp = character[PlayerComponent.mapper]
        if (playerCmp != null) {
            hud.infoWidget.disableAttackIndicator()
        }
    }

    override fun characterAttackReady(character: Entity) {
        if (character[PlayerComponent.mapper] != null) {
            hud.infoWidget.activateAttackIndicator()
            audioService.play(SoundAssets.PING)
        }
    }

    override fun characterLevelUp(character: Entity, level: Int, xp: Int, xpNeeded: Int) {
        if (character[PlayerComponent.mapper] != null) {
            hud.statsWidget.updateLevel(levelTxt, level)
                .updateExperience(xpTxt, xpAbbreviation, xp, xpNeeded)

            if (level == 3) {
                hud.statsWidget.activateSkill(0)
            }
        }
    }

    override fun characterXPGained(character: Entity, xp: Int, xpNeeded: Int) {
        if (character[PlayerComponent.mapper] != null) {
            hud.statsWidget.updateExperience(xpTxt, xpAbbreviation, xp, xpNeeded)
        }
    }

    override fun beforeMapChange() {
        if (ignoreMapTransition) return

        // render current screen to FBO
        prevMapFrameBuffer.bind()
        clearScreen(0f, 0f, 0f, 1f)
        engine.getSystem(RenderSystem::class.java).update(0f)
        engine.getSystem(LightSystem::class.java).update(0f)
        engine.getSystem(FloatingTextSystem::class.java).update(0f)

        // return to original framebuffer for rendering
        FrameBuffer.unbind()
        mapTransitionTime = 0f
    }

    override fun mapChange(newMap: Map) {
        ignoreMapTransition = false
        hud.mapInfoWidget.show(bundle["map.name.${newMap.type}"])
    }

    override fun dispose() {
        super.dispose()
        currentMapFrameBuffer.dispose()
        prevMapFrameBuffer.dispose()
    }
}
