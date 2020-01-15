package com.github.quillraven.quillyjumper.audio

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.ObjectMap
import com.github.quillraven.quillyjumper.assets.MusicAssets
import com.github.quillraven.quillyjumper.assets.SoundAssets
import com.github.quillraven.quillyjumper.assets.get
import com.github.quillraven.quillyjumper.map.Map
import ktx.collections.iterate
import java.util.*

class DefaultAudioService(private val assets: AssetManager) : AudioService {
    private var music: Music? = null
    private var musicVolume = 1f
    private var soundVolume = 1f
    private val soundCache = EnumMap<SoundAssets, Sound>(SoundAssets::class.java)
    private val musicCache = EnumMap<MusicAssets, Music>(MusicAssets::class.java)
    private val soundQueue = ObjectMap<SoundAssets, Sound>(8)

    override fun play(type: MusicAssets, loop: Boolean, completeListener: Music.OnCompletionListener?) {
        // stop current music
        music?.stop()
        // play new music
        music = musicCache.computeIfAbsent(type) { assets[type] }.apply {
            volume = (musicVolume * type.volumeScale)
            isLooping = loop
            if (completeListener != null) this.setOnCompletionListener(completeListener)
            play()
        }
    }

    override fun play(type: SoundAssets) {
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
    override fun update() {
        soundQueue.iterate { key, value, iterator ->
            value.play(soundVolume * key.volumeScale)
            iterator.remove()
        }
    }

    override fun mapChange(newMap: Map) {
        play(newMap.type.music)
    }
}