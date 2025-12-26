package pt.rafap.kpb.app

import pt.rafap.kpb.utils.audio.AudioModifier
import pt.rafap.kpb.utils.audio.AudioPool
import pt.rafap.kpb.utils.audio.AudioWrapper
import pt.rafap.kpb.utils.loadResource

object Audio {
    private val pool: AudioPool

    init {
        pool = AudioPool.buildAudioPool {
            add(load("BGM", "audios/background-music.wav", AudioModifier().setToLoopInfinitely()))
            add(load("HIT", "audios/hit.wav"))
            add(load("PIECE", "audios/putPiece.wav"))
            add(load("MEGA", "audios/MEGALOVANIA.wav", AudioModifier().setToLoopInfinitely()))
        }
    }

    private fun load(name: String, path: String, modifier: AudioModifier = AudioModifier()): AudioWrapper {
        val file = loadResource(path)
        return AudioWrapper.loadAudio(name, file.toURI().toURL(), modifier)
    }

    fun playBGM() {
        pool.play("BGM")
    }

    fun stopBGM() {
        pool.stop("BGM")
    }

    fun playMega() {
        pool.play("MEGA")
    }

    fun playHit() {
        pool.play("HIT")
    }

    fun playPiece() {
        pool.play("PIECE")
    }

    fun stopAll() {
        pool.stopAll()
    }
}

