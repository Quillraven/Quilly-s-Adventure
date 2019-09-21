package com.game.quillyjumper.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import ktx.assets.getAsset
import ktx.assets.load

// music
enum class MusicAssets(val filePath: String, val volumeScale: Float = 0.15f) {
    MENU("music/menu.ogg"),
    LEVEL_1("music/level1.ogg"),
    LEVEL_1_REMASTERED("music/level1_remastered.ogg")
}

fun AssetManager.load(asset: MusicAssets) = load<Music>(asset.filePath)
operator fun AssetManager.get(asset: MusicAssets) = this.getAsset<Music>(asset.filePath)

// sound
enum class SoundAssets(val filePath: String, val volumeScale: Float = 1f) {
    UNKNOWN(""),
    PLAYER_JUMP("sounds/jump.ogg"),
    SWING("sounds/swing.ogg")
}

fun AssetManager.load(asset: SoundAssets) = load<Sound>(asset.filePath)
operator fun AssetManager.get(asset: SoundAssets) = this.getAsset<Sound>(asset.filePath)

// texture atlas
enum class TextureAtlasAssets(val filePath: String) {
    GAME_OBJECTS("graphics/gameObjects.atlas")
}

fun AssetManager.load(asset: TextureAtlasAssets) = load<TextureAtlas>(asset.filePath)
operator fun AssetManager.get(asset: TextureAtlasAssets) = this.getAsset<TextureAtlas>(asset.filePath)

// tiled map
enum class MapAssets(val filePath: String) {
    MAP_1("assets/map/map1.tmx"),
    TEST_MAP("assets/map/testmap.tmx")
}

fun AssetManager.load(asset: MapAssets) = load<TiledMap>(asset.filePath)
operator fun AssetManager.get(asset: MapAssets) = this.getAsset<TiledMap>(asset.filePath)