package com.game.quillyjumper

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.assets.SoundAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener

class AudioManager(private val assets: AssetManager) : MapChangeListener {
    private var music: Music? = null
    private var musicVolume = 1f
    private var soundVolume = 1f

    fun play(type: MusicAssets) {
        // stop current music
        music?.stop()
        // play new music
        music = assets[type].apply {
            volume = (musicVolume * type.volumeScale)
            isLooping = true
            play()
        }
    }

    fun play(type: SoundAssets) {
        assets[type].play(soundVolume * type.volumeScale)
    }

    override fun mapChange(newMap: Map) {
        play(newMap.type.music)
    }
}