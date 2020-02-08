package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.quillysadventure.ability.Ability
import com.github.quillraven.quillysadventure.ability.FireballEffect
import com.github.quillraven.quillysadventure.assets.SoundAssets
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.ecs.component.PlayerComponent
import com.github.quillraven.quillysadventure.ecs.component.abilityCmp
import com.github.quillraven.quillysadventure.ecs.component.statsCmp
import com.github.quillraven.quillysadventure.ecs.system.DeathSystem
import com.github.quillraven.quillysadventure.ecs.system.DebugSystem
import com.github.quillraven.quillysadventure.ecs.system.FloatingTextSystem
import com.github.quillraven.quillysadventure.ecs.system.RenderPhysicDebugSystem
import com.github.quillraven.quillysadventure.ecs.system.RenderSystem
import com.github.quillraven.quillysadventure.event.GameEventListener
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.event.Key
import com.github.quillraven.quillysadventure.input.InputListener
import com.github.quillraven.quillysadventure.map.Map
import com.github.quillraven.quillysadventure.map.MapChangeListener
import com.github.quillraven.quillysadventure.map.MapManager
import com.github.quillraven.quillysadventure.map.MapType
import com.github.quillraven.quillysadventure.ui.GameHUD
import com.github.quillraven.quillysadventure.ui.ImageButtonStyles
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.widget.DialogWidget
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get
import ktx.log.logger
import java.util.*

private val LOG = logger<GameScreen>()

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    private val bundle: I18NBundle,
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService,
    private val engine: Engine,
    private val mapManager: MapManager,
    private val rayHandler: RayHandler,
    private val viewport: Viewport,
    private val stage: Stage
) : KtxScreen, InputListener, GameEventListener, MapChangeListener {
    private val hud = GameHUD(gameEventManager, bundle["statsTitle"], bundle["skills"])
    private val dialog = DialogWidget()
    private var gameOver = false
    private var systemsActive = true

    private val lifeTxt = bundle["life"]
    private val manaTxt = bundle["mana"]
    private val levelTxt = bundle["level"]

    private val xpTxt = bundle["xp"]

    override fun show() {
        gameOver = false

        // setup game UI
        stage.addActor(hud)
        stage.addActor(hud.statsWidget.apply {
            addSkill(bundle["fireball"], bundle["requiresLvl3"], Images.IMAGE_FIREBALL)
        })
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
                    skillSelected(ImageButtonStyles.FIREBALL)
                }
            }
        })
        stage.addActor(dialog)
        dialog.setPosition(-2000f, 0f)

        // add game screen as input listener to react when the player wants to quit the game (=exit key pressed)
        gameEventManager.addInputListener(this)
        // add screen as MapChangeListener to show the map name information when changing maps
        gameEventManager.addMapChangeListener(this)
        // set initial map
        mapManager.setMap(MapType.MAP1)
        // screen needs to be an event listener to switch to game over screen when player dies
        gameEventManager.addGameEventListener(this)

        // set player hud info (life, mana, attack ready, etc.)
        engine.entities.forEach {
            val playerCmp = it[PlayerComponent.mapper]
            if (playerCmp != null) {
                with(it.statsCmp) {
                    hud.infoWidget.resetHudValues(this.life / this.maxLife, this.mana / this.maxMana)
                    hud.statsWidget.updateLevel(levelTxt, this.level)
                        .updateExperience(
                            xpTxt,
                            this.xp,
                            engine.getSystem(DeathSystem::class.java).getNeededExperience(this.level)
                        )
                        .updateLife(lifeTxt, this.life.toInt(), this.maxLife.toInt())
                        .updateMana(manaTxt, this.mana.toInt(), this.maxMana.toInt())
                        .updateDamage(bundle["damage"], this.damage.toInt())
                        .updateArmor(bundle["armor"], this.armor.toInt())
                }
                if (playerCmp.tutorialProgress == 0) {
                    playerCmp.tutorialProgress++
                    showTutorialInfo(0, 2f)
                }
            }
        }
    }

    private fun skillSelected(skillImageStyle: ImageButtonStyles) {
        hud.skillButton.style = hud.skin.get(skillImageStyle.name, ImageButton.ImageButtonStyle::class.java)
    }

    override fun hide() {
        stage.clear()
        gameEventManager.removeInputListener(this)
        gameEventManager.removeMapChangeListener(this)
        gameEventManager.removeGameEventListener(this)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        if (width != stage.viewport.screenWidth || height != stage.viewport.screenHeight) {
            rayHandler.resizeFBO(width / 4, height / 4)
        }
        viewport.update(width, height, true)
        rayHandler.useCustomViewport(viewport.screenX, viewport.screenY, viewport.screenWidth, viewport.screenHeight)
    }

    private fun EntitySystem.isDebugOrRenderSystem() =
        this is DebugSystem || this is RenderPhysicDebugSystem || // debug systems
                this is RenderSystem || this is FloatingTextSystem // render systems

    override fun render(delta: Float) {
        if (dialog.color.a > 0f && systemsActive) {
            // dialog is shown -> disable some systems to stop the game simulation until dialog is closed
            systemsActive = false
            engine.systems.forEach {
                if (!it.isDebugOrRenderSystem()) {
                    it.setProcessing(false)
                }
            }
        } else if (dialog.color.a <= 0f && !systemsActive) {
            // dialog was closed -> return to normal game simulation
            systemsActive = true
            engine.systems.forEach {
                if (!it.isDebugOrRenderSystem()) {
                    it.setProcessing(true)
                }
            }
        }

        // update all ecs engine systems including the render system which draws stuff on the screen
        engine.update(delta)
        // update audio manager to play any queued sound effects
        audioService.update()

        // render UI
        stage.viewport.apply()
        stage.act()
        stage.draw()

        if (gameOver) {
            // process gameover at the end of a frame because switching screens within engine.update is a bad idea.
            // The reason is that the hide method of GameScreen will be called and therefore some game events
            // will not be processed correctly because listeners are removed
            game.setScreen<EndScreen>()
        }
    }

    override fun keyPressed(key: Key) {
        if (key == Key.EXIT) {
            // player pressed exit key -> go back to menu
            game.setScreen<MenuScreen>()
        }
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
            if (playerCmp.tutorialProgress == 2) {
                ++playerCmp.tutorialProgress
                showTutorialInfo(2)
            }
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
            if (playerCmp.tutorialProgress == 1) {
                ++playerCmp.tutorialProgress
                showTutorialInfo(1)
            }
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
                .updateExperience(xpTxt, xp, xpNeeded)

            if (level == 3) {
                hud.statsWidget.activateSkill(0)
            }
        }
    }

    override fun characterXPGained(character: Entity, xp: Int, xpNeeded: Int) {
        if (character[PlayerComponent.mapper] != null) {
            hud.statsWidget.updateExperience(xpTxt, xp, xpNeeded)
        }
    }

    override fun mapChange(newMap: Map) {
        hud.mapInfoWidget.show(bundle["map.name.${newMap.type}"])
    }

    private fun showTutorialInfo(tutorialNumber: Int, popupDelay: Float = 0f) {
        try {
            dialog.showDialog(bundle["tutorial.$tutorialNumber"], popupDelay)
        } catch (e: MissingResourceException) {
            LOG.error(e) { "Missing tutorial text for index $tutorialNumber" }
        }
    }

    /**
     * Function to react on dialog events from the GameEventManager.
     * Shows a dialog by retrieving all pages of a dialog from the I18N bundle.
     * The given dialogKey is used to lookup the localized string in the bundle
     * by adding a ".1", ".2", ... at the end of it. If a certain number cannot be
     * found then the dialog is shown.
     */
    override fun showDialogEvent(dialogKey: String) {
        try {
            dialog.showDialog(bundle["$dialogKey.1"])
        } catch (e: MissingResourceException) {
            LOG.error(e) { "There is no dialog in the resourcebundle with key $dialogKey.1" }
            return
        }

        try {
            // check for additional dialog pages that should be added
            // to the dialog
            var additionalPage = 2
            while (true) {
                dialog.addPage(bundle["$dialogKey.$additionalPage"])
                ++additionalPage
            }
        } catch (e: MissingResourceException) {
            // no more pages for given dialog key
        }
    }
}
