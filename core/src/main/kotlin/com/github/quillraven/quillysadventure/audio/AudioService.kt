package com.github.quillraven.quillysadventure.audio

import com.badlogic.gdx.audio.Music
import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.assets.SoundAssets
import com.github.quillraven.quillysadventure.map.Map
import com.github.quillraven.quillysadventure.map.MapChangeListener

interface AudioService : MapChangeListener {
    var soundVolume: Float
    var musicVolume: Float

    fun play(type: MusicAssets, loop: Boolean = true, completeListener: Music.OnCompletionListener? = null) {}
    fun play(type: SoundAssets) {}
    fun update() {}

    override fun mapChange(newMap: Map) {}
}

object NullAudioService : AudioService {
    override var soundVolume: Float = 0f
    override var musicVolume: Float = 0f
    private val defaultMusic = object : Music {
        override fun isPlaying() = false

        override fun isLooping() = false

        override fun setOnCompletionListener(listener: Music.OnCompletionListener?) = Unit

        override fun pause() = Unit

        override fun setPan(pan: Float, volume: Float) = Unit

        override fun getPosition() = 0f

        override fun setLooping(isLooping: Boolean) = Unit

        override fun getVolume() = 0f

        override fun play() = Unit

        override fun stop() = Unit

        override fun setVolume(volume: Float) = Unit

        override fun setPosition(position: Float) = Unit

        override fun dispose() = Unit
    }

    override fun play(type: MusicAssets, loop: Boolean, completeListener: Music.OnCompletionListener?) =
        completeListener?.onCompletion(defaultMusic) ?: Unit
}
