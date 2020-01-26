package com.github.quillraven.quillysadventure.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.github.quillraven.quillysadventure.assets.TextureAtlasAssets
import com.github.quillraven.quillysadventure.assets.get
import com.github.quillraven.quillysadventure.assets.load
import com.github.quillraven.quillysadventure.ecs.system.FontType
import ktx.freetype.generateFont
import ktx.scene2d.Scene2DSkin
import ktx.style.*

enum class Images(val imageName: String) {
    BUTTON_RECT_DOWN("btn_rect_down"),
    BUTTON_RECT_UP("btn_rect_up"),
    BUTTON_ROUND_UP("btn_round_up"),
    BUTTON_ROUND_DOWN("btn_round_down"),
    BUTTON_CHECK("btn_chk"),
    BUTTON_UNCHECK("btn_unchk"),
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
    MENU_BACKGROUND("menu_bgd")
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

operator fun Skin.get(image: Images): Drawable = this.getDrawable(image.imageName)

fun createSkin(assets: AssetManager): Skin {
    // generate fonts for the skin
    val generator = FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"))
    val defaultFont = generator.generateFont { size = 24 }
    val largeFont = generator.generateFont { size = 32 }
    // dispose font generator after creating all .ttf fonts that we need
    generator.dispose()

    // load textures for skin
    assets.load(TextureAtlasAssets.UI)
    assets.finishLoading()

    Scene2DSkin.defaultSkin = skin(assets[TextureAtlasAssets.UI]) { skin ->
        // add generated fonts as resources to the skin to correctly dispose them
        add(FontType.DEFAULT.skinKey, defaultFont.apply { data.markupEnabled = true })
        add(FontType.LARGE.skinKey, largeFont.apply { data.markupEnabled = true })

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
            font = defaultFont
        }
        // checkbox
        checkBox {
            checkboxOn = skin[Images.BUTTON_CHECK]
            checkboxOff = skin[Images.BUTTON_UNCHECK]
            font = defaultFont
        }
        // image button
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
            // background = skin[Images.MENU_BACKGROUND]
        }
    }

    return Scene2DSkin.defaultSkin
}
