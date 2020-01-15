package com.github.quillraven.quillyjumper.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import ktx.assets.getAsset
import ktx.assets.load

// music
enum class MusicAssets(val filePath: String, val volumeScale: Float = 0.15f) {
    MENU("music/menu.ogg"),
    LEVEL_1("music/level1.ogg"),
    LEVEL_1_REMASTERED("music/level1_remastered.ogg"),
    LEVEL_2("music/cave.mp3"),
    BOSS_1("music/boss1.mp3"),
    FANFARE("music/fanfare.mp3")
}

fun AssetManager.load(asset: MusicAssets) = load<Music>(asset.filePath)
operator fun AssetManager.get(asset: MusicAssets) = this.getAsset<Music>(asset.filePath)

// sound
enum class SoundAssets(val filePath: String, val volumeScale: Float = 1f) {
    UNKNOWN(""),
    PLAYER_JUMP("sounds/jump.ogg"),
    SWING("sounds/swing.ogg"),
    SWING2("sounds/swing2.ogg", 0.75f),
    GNOME_DEATH("sounds/gnome_death.ogg", 0.5f),
    FIRE_BALL("sounds/fire_ball.ogg"),
    LEVEL_UP("sounds/level_up.ogg"),
    CHECK_POINT("sounds/save_point.ogg"),
    SLIME_DEATH("sounds/slime_death.ogg", 0.25f),
    POWER_UP_0("sounds/power_up_0.ogg"),
    BIG_SWING("sounds/big_swing.ogg"),
    BIG_SWING2("sounds/big_swing2.ogg"),
    SMALL_SWING("sounds/small_swing.ogg"),
    MINOTAUR_DEATH("sounds/minotaur_death.ogg"),
    SKELETAL_DEATH("sounds/skeletal_death.ogg"),
    PING("sounds/ping.ogg")
}

fun AssetManager.load(asset: SoundAssets) = load<Sound>(asset.filePath)
operator fun AssetManager.get(asset: SoundAssets) = this.getAsset<Sound>(asset.filePath)

// texture atlas
enum class TextureAtlasAssets(val filePath: String) {
    GAME_OBJECTS("graphics/gameObjects.atlas"),
    UI("ui/UI.atlas")
}

fun AssetManager.load(asset: TextureAtlasAssets) = load<TextureAtlas>(asset.filePath)
operator fun AssetManager.get(asset: TextureAtlasAssets) = this.getAsset<TextureAtlas>(asset.filePath)

// tiled map
enum class MapAssets(val filePath: String) {
    INTRO("map/intro.tmx"),
    MAP_1("map/map1.tmx"),
    MAP_2("map/map2.tmx"),
    TEST_MAP("map/testmap.tmx")
}

fun AssetManager.load(asset: MapAssets) = load<TiledMap>(asset.filePath)
operator fun AssetManager.get(asset: MapAssets) = this.getAsset<TiledMap>(asset.filePath)

// particle effects
enum class ParticleAssets(val filePath: String, val scale: Float = 1f, val sound: SoundAssets = SoundAssets.UNKNOWN) {
    BLOOD("particles/blood.p", 0.5f),
    PORTAL("particles/portal.p"),
    PORTAL2("particles/portal2.p", 0.5f),
    FIREBALL("particles/fireball.p", 0.3f, SoundAssets.FIRE_BALL)
}

fun AssetManager.load(asset: ParticleAssets, params: ParticleEffectLoader.ParticleEffectParameter) =
    load(asset.filePath, ParticleEffect::class.java, params)

operator fun AssetManager.get(asset: ParticleAssets) = this.getAsset<ParticleEffect>(asset.filePath)
