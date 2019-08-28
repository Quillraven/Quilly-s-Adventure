package com.game.quillyjumper

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import ktx.assets.getAsset
import ktx.assets.load

// music
enum class MusicAssets(val filePath: String, val volumeScale: Float = 0.15f) {
    MENU("music/menu.ogg"),
    LEVEL_1("music/level1.ogg")
}

fun AssetManager.load(asset: MusicAssets) = load<Music>(asset.filePath)
operator fun AssetManager.get(asset: MusicAssets) = this.getAsset<Music>(asset.filePath)

// sound
enum class SoundAssets(val filePath: String, val volumeScale: Float = 1f) {
    PLAYER_JUMP("sounds/jump.ogg")
}

fun AssetManager.load(asset: SoundAssets) = load<Sound>(asset.filePath)
operator fun AssetManager.get(asset: SoundAssets) = this.getAsset<Sound>(asset.filePath)