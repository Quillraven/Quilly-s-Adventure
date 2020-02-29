package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
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
import com.github.quillraven.quillysadventure.ecs.system.TutorialSystem
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
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.get

class GameScreen(
    private val game: KtxGame<KtxScreen>,
    bundle: I18NBundle,
    gameEventManager: GameEventManager,
    audioService: AudioService,
    engine: Engine,
    private val mapManager: MapManager,
    rayHandler: RayHandler,
    viewport: Viewport,
    stage: Stage
) : Screen(engine, audioService, bundle, stage, gameEventManager, rayHandler, viewport), InputListener,
    MapChangeListener {
    private val hud = GameHUD(gameEventManager, bundle["statsTitle"], bundle["skills"])
    private var gameOver = false

    private val tutorialSystem = TutorialSystem(gameEventManager)

    private val lifeTxt = bundle["life"]
    private val manaTxt = bundle["mana"]
    private val levelTxt = bundle["level"]

    private val xpTxt = bundle["xp"]
    private val xpAbbreviation = bundle["xpAbbreviation"]

    override fun show() {
        super.show()
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
                    hud.skillButton.style =
                        hud.skin.get(ImageButtonStyles.FIREBALL.name, ImageButton.ImageButtonStyle::class.java)
                }
            }
        })

        // add game screen as input listener to react when the player wants to quit the game (=exit key pressed)
        gameEventManager.addInputListener(this)
        // add screen as MapChangeListener to show the map name information when changing maps
        gameEventManager.addMapChangeListener(this)
        // set initial map
        mapManager.setMap(MapType.MAP1)

        engine.addSystem(tutorialSystem)
        // set player hud info (life, mana, attack ready, etc.)
        engine.entities.forEach {
            val playerCmp = it[PlayerComponent.mapper]
            if (playerCmp != null) {
                with(it.statsCmp) {
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
                }
            }
        }
    }

    override fun hide() {
        super.hide()
        gameEventManager.removeInputListener(this)
        gameEventManager.removeMapChangeListener(this)
        engine.removeSystem(tutorialSystem)
    }

    override fun render(delta: Float) {
        super.render(delta)

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

    override fun mapChange(newMap: Map) {
        hud.mapInfoWidget.show(bundle["map.name.${newMap.type}"])
    }
}
