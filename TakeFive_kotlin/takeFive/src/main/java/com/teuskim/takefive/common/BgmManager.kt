package com.teuskim.takefive.common

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask

import com.teuskim.takefive.R

class BgmManager private constructor(private val context: Context) {
    private val mediaPlayer: MediaPlayer
    private val pref: OmokPreference
    private var currentType: Int = 0

    init {
        mediaPlayer = MediaPlayer()
        pref = OmokPreference.getInstance(context)
    }

    fun startForMenu() {
        start(1)
    }

    fun startForGame() {
        start(2)
    }

    private fun start(type: Int) {
        if (pref.isOnBgm == false || type == 0) {
            return
        }

        if (currentType != type) {
            currentType = type

            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            object : AsyncTask<Int, Void, Boolean>() {

                override fun doInBackground(vararg params: Int?): Boolean? {
                    try {
                        val fileResId: Int
                        if (params[0] == 2) {
                            fileResId = R.raw.omok_bgm_game
                        } else {
                            fileResId = R.raw.omok_bgm_menu
                        }
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.packageName + "/" + fileResId))
                        mediaPlayer.prepare()
                        mediaPlayer.isLooping = true
                    } catch (e: Exception) {
                        Log.test("bgm", e)
                        return false
                    }

                    return true
                }

                override fun onPostExecute(result: Boolean?) {
                    if (result!!) {
                        mediaPlayer.start()
                    }
                }

            }.execute(type)
        } else {
            if (mediaPlayer.isPlaying == false) {
                mediaPlayer.start()
            }
        }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    companion object {

        private var instance: BgmManager? = null

        fun getInstance(context: Context): BgmManager {
            if (instance == null) {
                instance = BgmManager(context)
            }
            return instance as BgmManager
        }
    }
}
