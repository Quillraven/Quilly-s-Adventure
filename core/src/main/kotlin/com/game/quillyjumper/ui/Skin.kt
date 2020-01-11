package com.game.quillyjumper.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.game.quillyjumper.assets.TextureAtlasAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.assets.load
import com.game.quillyjumper.ecs.system.FontType
import ktx.freetype.generateFont
import ktx.scene2d.Scene2DSkin
import ktx.style.*

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
        add(FontType.DEFAULT.skinKey, defaultFont)
        add(FontType.LARGE.skinKey, largeFont)

        // default label style
        label { font = skin.getFont(FontType.DEFAULT.skinKey) }
        // default button style
        button {}
        // default textButton style
        textButton {
            down = skin.getDrawable("btn_rect_down")
            up = skin.getDrawable("btn_rect_up")
            font = defaultFont
        }
        // checkbox
        checkBox {
            checkboxOn = skin.getDrawable("btn_chk")
            checkboxOff = skin.getDrawable("btn_unchk")
            font = defaultFont
        }
        // default window style
        window { }
    }

    return Scene2DSkin.defaultSkin;
}
