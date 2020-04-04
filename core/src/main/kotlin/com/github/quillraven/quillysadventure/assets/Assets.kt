package com.github.quillraven.quillysadventure.assets

import com.badlogic.gdx.assets.loaders.ParticleEffectLoader
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.utils.I18NBundle
import ktx.assets.async.AssetStorage

// music
enum class MusicAssets(val filePath: String, val volumeScale: Float = 0.15f) {
    MENU("music/menu.ogg"),
    LEVEL_1("music/level1.ogg"),
    LEVEL_1_REMASTERED("music/level1_remastered.ogg"),
    LEVEL_2("music/cave.mp3"),
    BOSS_1("music/boss1.mp3"),
    FANFARE("music/fanfare.mp3"),
    GAME_OVER("music/gameover.ogg")
}

fun AssetStorage.loadAsync(asset: MusicAssets) = loadAsync<Music>(asset.filePath)
operator fun AssetStorage.get(asset: MusicAssets) = get<Music>(asset.filePath)

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

fun AssetStorage.loadAsync(asset: SoundAssets) = loadAsync<Sound>(asset.filePath)
operator fun AssetStorage.get(asset: SoundAssets) = get<Sound>(asset.filePath)

// texture atlas
enum class TextureAtlasAssets(val filePath: String) {
    GAME_OBJECTS("graphics/gameObjects.atlas"),
    UI("ui/UI.atlas")
}

fun AssetStorage.loadAsync(asset: TextureAtlasAssets) = loadAsync<TextureAtlas>(asset.filePath)
operator fun AssetStorage.get(asset: TextureAtlasAssets) = get<TextureAtlas>(asset.filePath)

// tiled map
enum class MapAssets(val filePath: String) {
    INTRO("map/intro.tmx"),
    MAP_1("map/map1.tmx"),
    MAP_2("map/map2.tmx"),
    TEST_MAP("map/testmap.tmx"),
    MAIN_MENU("map/menuMap.tmx"),
    GAME_OVER("map/gameover.tmx")
}

fun AssetStorage.loadAsync(asset: MapAssets) = loadAsync<TiledMap>(asset.filePath)
operator fun AssetStorage.get(asset: MapAssets) = get<TiledMap>(asset.filePath)

// particle effects
enum class ParticleAssets(val filePath: String, val scale: Float = 1f, val sound: SoundAssets = SoundAssets.UNKNOWN) {
    BLOOD("particles/blood.p", 0.5f),
    PORTAL("particles/portal.p"),
    PORTAL2("particles/portal2.p", 0.5f),
    FIREBALL("particles/fireball.p", 0.3f, SoundAssets.FIRE_BALL)
}

fun AssetStorage.loadAsync(asset: ParticleAssets, params: ParticleEffectLoader.ParticleEffectParameter) =
    loadAsync<ParticleEffect>(asset.filePath, params)

operator fun AssetStorage.get(asset: ParticleAssets) = get<ParticleEffect>(asset.filePath)

enum class I18nAssets(val filePath: String) {
    DEFAULT("ui/i18n")
}

fun AssetStorage.loadAsync(asset: I18nAssets) = loadAsync<I18NBundle>(asset.filePath)
operator fun AssetStorage.get(asset: I18nAssets) = get<I18NBundle>(asset.filePath)
