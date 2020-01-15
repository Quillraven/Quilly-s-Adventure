package com.github.quillraven.quillyjumper.ui

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor

class PlayerInfoWidget(
    skin: Skin,
    imageHealthbars: Image = Image(skin[Images.IMAGE_HEALTHBARS]),
    private val imageAttackIndicator: Image = Image(skin[Images.IMAGE_ATTACK_INDICATOR]),
    private val imageLifeBar: Image = Image(skin[Images.IMAGE_LIFEBAR]),
    private val imageManaBar: Image = Image(skin[Images.IMAGE_MANABAR])
) : WidgetGroup(
    Image(skin[Images.IMAGE_PLAYER_ICON]),
    imageAttackIndicator, imageLifeBar, imageManaBar, imageHealthbars
), KGroup {
    init {
        // offsets from player icon image
        val barOffsetX = 71f
        val barOffsetY = 7f
        imageAttackIndicator.setPosition(barOffsetX + 2f, barOffsetY + 15f)
        imageHealthbars.setPosition(barOffsetX, barOffsetY + 0f)
        imageLifeBar.setPosition(barOffsetX + 15f, barOffsetY + 15f)
        imageManaBar.setPosition(barOffsetX + 6f, barOffsetY + 6f)
    }

    fun setLifePercentage(percentage: Float) {
        imageLifeBar.run {
            clearActions()
            addAction(Actions.scaleTo(MathUtils.clamp(percentage, 0f, 1f), 1f, 1f))
        }
    }

    fun setManaPercentage(percentage: Float) {
        imageManaBar.run {
            clearActions()
            addAction(Actions.scaleTo(MathUtils.clamp(percentage, 0f, 1f), 1f, 1f))
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

    override fun getPrefHeight(): Float {
        return 100f
    }

    override fun getPrefWidth(): Float {
        return 200f
    }
}

inline fun <S> KWidget<S>.playerInfoWidget(
    skin: Skin = Scene2DSkin.defaultSkin,
    init: PlayerInfoWidget.(S) -> Unit = {}
) = actor(PlayerInfoWidget(skin), init)
