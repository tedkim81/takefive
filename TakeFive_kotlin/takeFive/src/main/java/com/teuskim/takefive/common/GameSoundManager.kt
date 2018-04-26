package com.teuskim.takefive.common

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

import com.teuskim.takefive.R

class GameSoundManager private constructor(private val context: Context) {
    private val soundPool: SoundPool
    private val audioManager: AudioManager
    private val pref: OmokPreference

    private var idButtonClick: Int = 0
    private var idBoardTouch: Int = 0
    private var idUfoMove: Int = 0
    private var idCrash: Int = 0
    private var idMissionClear: Int = 0
    private var idMissionFail: Int = 0

    init {
        soundPool = SoundPool(4, AudioManager.STREAM_MUSIC, 0)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        pref = OmokPreference.getInstance(context)
        loadSounds()
    }

    private fun loadSounds() {
        idButtonClick = soundPool.load(context, R.raw.sound_button_click, 1)
        idBoardTouch = soundPool.load(context, R.raw.sound_board_touch, 1)
        idUfoMove = soundPool.load(context, R.raw.sound_ufo_move, 1)
        idCrash = soundPool.load(context, R.raw.sound_crush, 1)
        idMissionClear = soundPool.load(context, R.raw.sound_mission_clear, 1)
        idMissionFail = soundPool.load(context, R.raw.sound_mission_fail, 1)
    }

    private fun playSound(id: Int) {
        if (pref.isOnGameSound == false) {
            return
        }
        val streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        soundPool.play(id, streamVolume.toFloat(), streamVolume.toFloat(), 1, 0, 1f)
    }

    fun playButtonClick() {
        playSound(idButtonClick)
    }

    fun playBoardTouch() {
        playSound(idBoardTouch)
    }

    fun playUfoMove() {
        playSound(idUfoMove)
    }

    fun playCrash() {
        playSound(idCrash)
    }

    fun playMissionClear() {
        playSound(idMissionClear)
    }

    fun playMissionFail() {
        playSound(idMissionFail)
    }

    companion object {

        private var instance: GameSoundManager? = null

        fun getInstance(context: Context): GameSoundManager {
            if (instance == null) {
                instance = GameSoundManager(context)
            }
            return instance as GameSoundManager
        }
    }
}
