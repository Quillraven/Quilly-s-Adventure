package com.github.quillraven.quillysadventure.ui.widget

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.github.quillraven.quillysadventure.ui.Images
import com.github.quillraven.quillysadventure.ui.action.scaleToRegionWidth
import com.github.quillraven.quillysadventure.ui.get
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor

class PlayerInfoWidget(
    skin: Skin,
    imageHealthbars: Image = Image(skin[Images.IMAGE_HEALTHBARS]),
    val imagePlayer: Image = Image(skin[Images.IMAGE_PLAYER_ICON]),
    private val imageAttackIndicator: Image = Image(skin[Images.IMAGE_ATTACK_INDICATOR]),
    private val imageLifeBar: Image = Image(skin[Images.IMAGE_LIFEBAR]),
    private val imageManaBar: Image = Image(skin[Images.IMAGE_MANABAR])
) : WidgetGroup(
    imagePlayer, imageAttackIndicator, imageLifeBar, imageManaBar, imageHealthbars
), KGroup {
    init {
        // offsets from player icon image
        val barOffsetX = 71f
        val barOffsetY = 7f
        imageAttackIndicator.setPosition(barOffsetX + 2f, barOffsetY + 15f)
        imageHealthbars.setPosition(barOffsetX, barOffsetY + 0f)
        imageLifeBar.setPosition(barOffsetX + 15f, barOffsetY + 15f)
        imageManaBar.setPosition(barOffsetX + 6f, barOffsetY + 6f)

        // group is not rotated or scaled and therefore we do not need to transform every draw call
        // -> increased draw performance because we avoid flushing the batch
        isTransform = false
    }

    fun scaleLifeBarTo(percentage: Float, scaleDuration: Float = 1f) {
        imageLifeBar.run {
            clearActions()
            addAction(scaleToRegionWidth(MathUtils.clamp(percentage, 0f, 1f), scaleDuration))
        }
    }

    fun scaleManaBarTo(percentage: Float, scaleDuration: Float = 1f) {
        imageManaBar.run {
            clearActions()
            addAction(scaleToRegionWidth(MathUtils.clamp(percentage, 0f, 1f), scaleDuration))
        }
    }

    fun activateAttackIndicator() {
        imageAttackIndicator.run {
            clearActions()
            addAction(
                Actions.sequence(
                    Actions.alpha(0f),
                    Actions.alpha(1f, 0.1f),
                    Actions.alpha(0f, 0.1f),
                    Actions.alpha(1f, 0.1f)
                )
            )
        }
    }

    fun disableAttackIndicator() {
        imageAttackIndicator.run {
            clearActions()
            addAction(Actions.alpha(0.2f, 0.5f))
        }
    }

    fun resetHudValues(lifePercentage: Float, manaPercentage: Float) {
        scaleLifeBarTo(lifePercentage, 0f)
        scaleManaBarTo(manaPercentage, 0f)
        imageAttackIndicator.run {
            clearActions()
            this.color.a = 1f
        }
    }

    override fun getPrefHeight(): Float = 100f
    override fun getPrefWidth(): Float = 200f
}

inline fun <S> KWidget<S>.playerInfoWidget(
    skin: Skin = Scene2DSkin.defaultSkin,
    init: PlayerInfoWidget.(S) -> Unit = {}
) = actor(PlayerInfoWidget(skin), init)
