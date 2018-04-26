package com.teuskim.takefive.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class OmokPreference private constructor(context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    var bestScore: Int
        get() = pref.getInt(KEY_BEST_SCORE, 0)
        set(score) {
            editor.putInt(KEY_BEST_SCORE, score)
            editor.commit()
        }

    var winsCount: Int
        get() = pref.getInt(KEY_WINS_COUNT, 0)
        set(winsCount) {
            editor.putInt(KEY_WINS_COUNT, winsCount)
            editor.commit()
        }

    var isSignedIn: Boolean
        get() = pref.getBoolean(KEY_IS_SIGNED_IN, false)
        set(isSignedIn) {
            editor.putBoolean(KEY_IS_SIGNED_IN, isSignedIn)
            editor.commit()
        }

    var isOnBgm: Boolean
        get() = pref.getBoolean(KEY_IS_ON_BGM, true)
        set(isOnBgm) {
            editor.putBoolean(KEY_IS_ON_BGM, isOnBgm)
            editor.commit()
        }

    var isOnGameSound: Boolean
        get() = pref.getBoolean(KEY_IS_ON_GAME_SOUND, true)
        set(isOnGameSound) {
            editor.putBoolean(KEY_IS_ON_GAME_SOUND, isOnGameSound)
            editor.commit()
        }

    var isOnInvitedOk: Boolean
        get() = pref.getBoolean(KEY_IS_ON_INVITED_OK, true)
        set(isOnInvitedOk) {
            editor.putBoolean(KEY_IS_ON_INVITED_OK, isOnInvitedOk)
            editor.commit()
        }

    var isOnFirstStageHelper: Boolean
        get() = pref.getBoolean(KEY_IS_ON_FIRST_STAGE_HELPER, true)
        set(isOnFirstStageHelper) {
            editor.putBoolean(KEY_IS_ON_FIRST_STAGE_HELPER, isOnFirstStageHelper)
            editor.commit()
        }

    val clickYNs: String
        get() = pref.getString(KEY_CLICK_YNS, ClickYNs.DEFAULT_YNS)

    var isPremium: Boolean
        get() = pref.getBoolean(KEY_IS_PREMIUM, false)
        set(isPremium) {
            editor.putBoolean(KEY_IS_PREMIUM, isPremium)
            editor.commit()
        }

    var sayingIndex: Int
        get() = pref.getInt(KEY_SAYING_INDEX, 0)
        set(sayingIndex) {
            editor.putInt(KEY_SAYING_INDEX, sayingIndex)
            editor.commit()
        }

    var sayingDay: Int
        get() = pref.getInt(KEY_SAYING_DAY, 0)
        set(sayingDay) {
            editor.putInt(KEY_SAYING_DAY, sayingDay)
            editor.commit()
        }

    var beginningStageLimit: Int
        get() = pref.getInt(KEY_BEGINNING_STAGE_LIMIT, 1)
        set(beginningStageLimit) {
            editor.putInt(KEY_BEGINNING_STAGE_LIMIT, beginningStageLimit)
            editor.commit()
        }

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(context)
        editor = pref.edit()
    }

    fun setClickYNS(clickYNs: String) {
        editor.putString(KEY_CLICK_YNS, clickYNs)
        editor.commit()
    }

    companion object {

        private val KEY_BEST_SCORE = "best_score"
        private val KEY_WINS_COUNT = "wins_count"
        private val KEY_IS_SIGNED_IN = "is_signed_in"
        private val KEY_IS_ON_BGM = "is_on_bgm"
        private val KEY_IS_ON_GAME_SOUND = "is_on_game_sound"
        private val KEY_IS_ON_INVITED_OK = "is_on_invited_popup"
        private val KEY_IS_ON_FIRST_STAGE_HELPER = "is_on_first_stage_helper"
        private val KEY_CLICK_YNS = "click_yns"
        private val KEY_IS_PREMIUM = "is_premium"
        private val KEY_SAYING_INDEX = "saying_index"
        private val KEY_SAYING_DAY = "saying_day"
        private val KEY_BEGINNING_STAGE_LIMIT = "beginning_stage_limit"

        private var instance: OmokPreference? = null

        fun getInstance(context: Context): OmokPreference {
            if (instance == null) {
                instance = OmokPreference(context)
            }
            return instance as OmokPreference
        }
    }

}