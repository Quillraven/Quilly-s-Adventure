package com.game.quillyjumper

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.ObjectMap
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.assets.SoundAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener
import ktx.collections.iterate
import java.util.*

class AudioManager(private val assets: AssetManager) : MapChangeListener {
    private var music: Music? = null
    private var musicVolume = 1f
    private var soundVolume = 1f
    private val soundCache = EnumMap<SoundAssets, Sound>(SoundAssets::class.java)
    private val musicCache = EnumMap<MusicAssets, Music>(MusicAssets::class.java)
    private val soundQueue = ObjectMap<SoundAssets, Sound>(8)

    fun play(type: MusicAssets, loop: Boolean = true): Music {
        // stop current music
        music?.stop()
        // play new music
        val newMusic = musicCache.computeIfAbsent(type) { assets[type] }.apply {
            volume = (musicVolume * type.volumeScale)
            isLooping = loop
            play()
        }
        music = newMusic
        return newMusic
    }

    fun play(type: SoundAssets) {
        if (soundQueue.containsKey(type)) {
            // sound already queued -> do not add it multiple times
            return
        }
        // sound not queued yet -> queue it
        soundQueue.put(type, soundCache.computeIfAbsent(type) { assets[type] })
    }

    /**
     * Plays any queued sound effects via [play] and clears the sound queue.
     */
    fun update() {
        soundQueue.iterate { key, value, iterator ->
            value.play(soundVolume * key.volumeScale)
            iterator.remove()
        }
    }

    override fun mapChange(newMap: Map) {
        play(newMap.type.music)
    }
}