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

class NullAudioService : AudioService