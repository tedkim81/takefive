package com.teuskim.takefive.common

import android.content.Context

class ClickYNs(context: Context) {

    private val pref: OmokPreference

    init {
        pref = OmokPreference.getInstance(context)
    }

    private fun clickButton(position: Int) {
        val yns = pref.clickYNs
        val result = yns.substring(0, position) + "Y" + yns.substring(position + 1, yns.length)
        pref.setClickYNS(result)
    }

    fun clickMissionGame() {
        clickButton(POSITION_MISSIONGAME)
    }

    fun clickAchievement() {
        clickButton(POSITION_ACHIEVEMENT)
    }

    fun clickRanking() {
        clickButton(POSITION_RANKING)
    }

    fun clickSettings() {
        clickButton(POSITION_SETTINGS)
    }

    fun clickShare() {
        clickButton(POSITION_SHARE)
    }

    fun clickFriendGame() {
        clickButton(POSITION_FRIENDGAME)
    }

    private fun didClickButton(position: Int): Boolean {
        val yns = pref.clickYNs
        return yns[position] == 'Y'
    }

    fun didClickMissionGame(): Boolean {
        return didClickButton(POSITION_MISSIONGAME)
    }

    fun didClickAchievement(): Boolean {
        return didClickButton(POSITION_ACHIEVEMENT)
    }

    fun didClickRanking(): Boolean {
        return didClickButton(POSITION_RANKING)
    }

    fun didClickSettings(): Boolean {
        return didClickButton(POSITION_SETTINGS)
    }

    fun didClickShare(): Boolean {
        return didClickButton(POSITION_SHARE)
    }

    fun didClickFriendGame(): Boolean {
        return didClickButton(POSITION_FRIENDGAME)
    }

    fun didClickNone(): Boolean {
        return DEFAULT_YNS == pref.clickYNs
    }

    companion object {

        val DEFAULT_YNS = "NNNNNN"

        private val POSITION_MISSIONGAME = 0
        private val POSITION_ACHIEVEMENT = 1
        private val POSITION_RANKING = 2
        private val POSITION_SETTINGS = 3
        private val POSITION_SHARE = 4
        private val POSITION_FRIENDGAME = 5
    }
}
