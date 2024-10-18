package com.github.quillraven.quillysadventure.configuration

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.github.quillraven.quillysadventure.assets.TextureAtlasAssets
import com.github.quillraven.quillysadventure.assets.get
import ktx.log.logger
import java.util.*

private val LOG = logger<ItemConfigurations>()

enum class Item {
    POTION_GAIN_LIFE,
    POTION_GAIN_MANA
}

class ItemCfg(val region: TextureAtlas.AtlasRegion) {
    var lifeBonus = 0
    var manaBonus = 0
}

class ItemConfigurations(assets: AssetManager) : EnumMap<Item, ItemCfg>(Item::class.java) {
    private val atlas = assets[TextureAtlasAssets.GAME_OBJECTS]
    private val defaultRegion = atlas.findRegion("error")!!
    private val defaultCfg = ItemCfg(defaultRegion)

    fun cfg(
        id: Item,
        regionKey: String,
        init: ItemCfg.() -> Unit = { }
    ) {
        if (this.containsKey(id)) {
            LOG.error { "Item configuration for id $id is already existing!" }
            return
        }
        var region = atlas.findRegion(regionKey)
        if (region == null) {
            LOG.error { "Wrong region for item configuration. Region: $regionKey" }
            region = defaultRegion
        }
        this[id] = ItemCfg(region).apply(init)
    }

    override operator fun get(key: Item): ItemCfg {
        val cfg = super.get(key)
        if (cfg == null) {
            LOG.error { "Trying to access item cfg $key which is not configured yet!" }
            return defaultCfg
        }
        return cfg
    }
}

inline fun itemConfigurations(assets: AssetManager, init: ItemConfigurations.() -> Unit) =
    ItemConfigurations(assets).apply(init)

fun loadItemConfigurations(assets: AssetManager): ItemConfigurations {
    return itemConfigurations(assets) {
        cfg(Item.POTION_GAIN_LIFE, "potion_green_plus") {
            lifeBonus = 10
        }
        cfg(Item.POTION_GAIN_MANA, "potion_blue_plus") {
            manaBonus = 3
        }
    }
}
