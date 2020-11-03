package com.github.quillraven.quillysadventure.screen

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.quillysadventure.audio.AudioService
import com.github.quillraven.quillysadventure.ecs.system.DebugSystem
import com.github.quillraven.quillysadventure.ecs.system.FloatingTextSystem
import com.github.quillraven.quillysadventure.ecs.system.LightSystem
import com.github.quillraven.quillysadventure.ecs.system.RenderPhysicDebugSystem
import com.github.quillraven.quillysadventure.ecs.system.RenderSystem
import com.github.quillraven.quillysadventure.event.GameEventListener
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.ui.widget.DialogWidget
import ktx.app.KtxScreen
import ktx.log.error
import ktx.log.logger
import java.util.*

private val LOG = logger<Screen>()

abstract class Screen(
    val engine: Engine,
    val audioService: AudioService,
    val bundle: I18NBundle,
    val stage: Stage,
    val gameEventManager: GameEventManager,
    private val rayHandler: RayHandler,
    private val viewport: Viewport
) : KtxScreen, GameEventListener {
    private val dialog = DialogWidget()
    private var systemsActive = true

    override fun show() {
        stage.addActor(dialog)
        dialog.setPosition(-2000f, 0f)
        gameEventManager.addGameEventListener(this)

        dialog.hideDialog(0f)
        systemsActive = true
        engine.systems.forEach {
            if (!it.isDebugOrRenderSystem()) {
                it.setProcessing(true)
            }
        }
    }

    override fun hide() {
        stage.clear()
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
        this is DebugSystem || this is RenderPhysicDebugSystem  // debug systems
                || this is RenderSystem || this is FloatingTextSystem // render systems
                || this is LightSystem // box2d light system


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

        engine.update(delta)
        audioService.update()

        stage.viewport.apply()
        stage.act()
        stage.draw()
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
