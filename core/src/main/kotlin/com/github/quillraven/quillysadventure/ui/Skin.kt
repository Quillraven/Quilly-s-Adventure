package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.scene2d.Scene2DSkin
import ktx.style.checkBox
import ktx.style.imageButton
import ktx.style.label
import ktx.style.scrollPane
import ktx.style.skin
import ktx.style.textButton
import ktx.style.touchpad

enum class Images(val imageName: String) {
    BUTTON_RECT_DOWN("btn_rect_down"),
    BUTTON_RECT_UP("btn_rect_up"),
    BUTTON_ROUND_UP("btn_round_up"),
    BUTTON_ROUND_DOWN("btn_round_down"),
    BUTTON_CHECK("btn_chk"),
    BUTTON_UNCHECK("btn_unchk"),
    BUTTON_CLOSE("btn_close"),
    DIALOG_LIGHT("dialog_light"),
    IMAGE_ATTACK("attack"),
    IMAGE_JUMP("jump"),
    IMAGE_FIREBALL("skill_0"),
    IMAGE_PLAYER_ICON("player"),
    IMAGE_HEALTHBARS("healthbars_background"),
    IMAGE_LIFEBAR("lifebar"),
    IMAGE_MANABAR("manabar"),
    IMAGE_ATTACK_INDICATOR("can_attack_indicator"),
    TOUCHPAD("touchpad"),
    KNOB("knob"),
    SKULL("skull"),
    DIALOG_TITLE("dialog_title"),
    VSCROLL("scroll_vertical"),
    SCROLL_KNOB("scroll_knob"),
    MENU_BACKGROUND("menu_bgd"),
    BAR_BACKGROUND("bar"),
    BAR_GREEN("bar_green")
}

enum class ImageButtonStyles {
    ATTACK,
    JUMP,
    FIREBALL
}

enum class LabelStyles {
    MAP_INFO,
    LARGE
}

enum class FontType(val skinKey: String) {
    DEFAULT("defaultFont"),
    LARGE("largeFont")
}

operator fun Skin.get(image: Images): Drawable = this.getDrawable(image.imageName)

private fun getBitmapFont(fntName: String, atlas: TextureAtlas) =
    BitmapFont(Gdx.files.internal("ui/$fntName.fnt"), atlas.findRegion(fntName)).apply {
        data.markupEnabled = true
    }

fun createSkin(atlas: TextureAtlas): Skin {
    Scene2DSkin.defaultSkin = skin(atlas) { skin ->
        // fonts
        add(FontType.DEFAULT.skinKey, getBitmapFont("font24", atlas))
        add(FontType.LARGE.skinKey, getBitmapFont("font32", atlas))

        // default label style
        label { font = skin.getFont(FontType.DEFAULT.skinKey) }
        label(LabelStyles.LARGE.name) { font = skin.getFont(FontType.LARGE.skinKey) }
        label(LabelStyles.MAP_INFO.name) {
            font = skin.getFont(FontType.LARGE.skinKey)
            background = skin[Images.DIALOG_TITLE]
        }

        // default textButton style
        textButton {
            down = skin[Images.BUTTON_RECT_DOWN]
            up = skin[Images.BUTTON_RECT_UP]
            font = skin.getFont(FontType.DEFAULT.skinKey)
        }

        // checkbox
        checkBox {
            checkboxOn = skin[Images.BUTTON_CHECK]
            checkboxOff = skin[Images.BUTTON_UNCHECK]
            font = skin.getFont(FontType.DEFAULT.skinKey)
        }

        // image button
        imageButton {
            down = skin[Images.BUTTON_ROUND_DOWN]
            up = skin[Images.BUTTON_ROUND_UP]
        }
        imageButton(ImageButtonStyles.ATTACK.name) {
            down = skin[Images.BUTTON_ROUND_DOWN]
            up = skin[Images.BUTTON_ROUND_UP]
            imageUp = skin[Images.IMAGE_ATTACK]
            imageDown = imageUp
        }
        imageButton(ImageButtonStyles.JUMP.name) {
            down = skin[Images.BUTTON_ROUND_DOWN]
            up = skin[Images.BUTTON_ROUND_UP]
            imageUp = skin[Images.IMAGE_JUMP]
            imageDown = imageUp
        }
        imageButton(ImageButtonStyles.FIREBALL.name) {
            down = skin[Images.BUTTON_ROUND_DOWN]
            up = skin[Images.BUTTON_ROUND_UP]
            imageUp = skin[Images.IMAGE_FIREBALL]
            imageDown = imageUp
        }

        // default touchpad style
        touchpad {
            knob = skin[Images.KNOB]
            background = skin[Images.TOUCHPAD]
        }

        // default scroll pane
        scrollPane {
            vScrollKnob = skin[Images.SCROLL_KNOB]
            vScroll = skin[Images.VSCROLL]
        }
    }

    return Scene2DSkin.defaultSkin
}
