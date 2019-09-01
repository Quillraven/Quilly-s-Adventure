package com.game.quillyjumper.configuration

import ktx.log.logger
import java.util.*

private val LOG = logger<ItemCfgCache>()

enum class Item {
    POTION_GAIN_LIFE,
    POTION_GAIN_MANA
}

class ItemCfg(val regionKey: String)

class ItemCfgCache : EnumMap<Item, ItemCfg>(Item::class.java) {
    private val defaultCfg = ItemCfg("error")

    fun itemCfg(
        id: Item,
        regionKey: String,
        init: ItemCfg.() -> Unit = { Unit }
    ) {
        if (this.containsKey(id)) {
            LOG.error { "Item configuration for id $id is already existing!" }
            return
        }
        this[id] = ItemCfg(regionKey).apply(init)
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

inline fun itemCfgCache(init: ItemCfgCache.() -> Unit) = ItemCfgCache().apply(init)