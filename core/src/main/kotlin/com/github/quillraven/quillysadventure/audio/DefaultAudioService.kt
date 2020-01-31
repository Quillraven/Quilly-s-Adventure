package com.github.quillraven.quillysadventure.audio

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.ObjectMap
import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.assets.SoundAssets
import com.github.quillraven.quillysadventure.assets.get
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.map.Map
import ktx.collections.iterate
import java.util.*

class DefaultAudioService(private val assets: AssetManager, gameEventManager: GameEventManager) : AudioService {
    private var music: Music? = null
    private var musicType = MusicAssets.MENU
    override var musicVolume = 1f
        set(value) {
            field = MathUtils.clamp(value, 0f, 1f)
            music?.volume = field * musicType.volumeScale
        }
    override var soundVolume = 1f
        set(value) {
            field = MathUtils.clamp(value, 0f, 1f)
        }
    private val soundCache = EnumMap<SoundAssets, Sound>(SoundAssets::class.java)
    private val musicCache = EnumMap<MusicAssets, Music>(MusicAssets::class.java)
    private val soundQueue = ObjectMap<SoundAssets, Sound>(8)

    init {
        gameEventManager.addMapChangeListener(this)
    }

    override fun play(type: MusicAssets, loop: Boolean, completeListener: Music.OnCompletionListener?) {
        // stop current music
        music?.stop()
        // play new music
        musicType = type
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
