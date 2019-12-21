package com.game.quillyjumper.audio

import com.badlogic.gdx.audio.Music
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.assets.SoundAssets
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener


interface AudioService : MapChangeListener {
    fun play(type: MusicAssets, loop: Boolean = true, completeListener: Music.OnCompletionListener? = null) {}
    fun play(type: SoundAssets) {}
    fun update() {}
    override fun mapChange(newMap: Map) {}
}

class NullAudioService : AudioService {
    companion object {
        private val defaultMusic = object : Music {
            override fun isPlaying() = false

            override fun isLooping() = false

            override fun setOnCompletionListener(listener: Music.OnCompletionListener?) {
            }

            override fun pause() {
            }

            override fun setPan(pan: Float, volume: Float) {
            }

            override fun getPosition() = 0f

            override fun setLooping(isLooping: Boolean) {
            }

            override fun getVolume() = 0f

            override fun play() {
            }

            override fun stop() {
            }

            override fun setVolume(volume: Float) {
            }

            override fun setPosition(position: Float) {
            }

            override fun dispose() {
            }
        }
    }

    override fun play(type: MusicAssets, loop: Boolean, completeListener: Music.OnCompletionListener?) {
        completeListener?.onCompletion(defaultMusic)
    }
}