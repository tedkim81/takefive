package com.teuskim.takefive

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView

import com.teuskim.takefive.common.OmokPreference
//import com.teuskim.takefive.iab.Purchase
import com.teuskim.takefive.common.IGoogleAnalytics

class SettingsActivity : BaseGameActivity() {

    private var viewCtrl: ViewControllaer? = null
    private var pref: OmokPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        viewCtrl = ViewControllaer()
        viewCtrl!!.init()
        pref = OmokPreference.getInstance(applicationContext)
        initStates()
    }

    private fun initStates() {
        viewCtrl!!.setBgm(pref!!.isOnBgm)
        viewCtrl!!.setGameSound(pref!!.isOnGameSound)
        viewCtrl!!.setInvitedOk(pref!!.isOnInvitedOk)
        viewCtrl!!.setFirstStageHelper(pref!!.isOnFirstStageHelper)
    }

//    override fun afterIab(item: Purchase?) {
//        super.afterIab(item)
//
//        if (item == null) {
//            viewCtrl!!.showItemView()
//        } else {
//            viewCtrl!!.hideItemView()
//        }
//    }

    private fun toggleBgm() {
        val isOn = !viewCtrl!!.bgmOn!!.isShown
        viewCtrl!!.setBgm(isOn)
        pref!!.isOnBgm = isOn
        if (isOn) {
            startBgmForMenu()
            googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_BGM_ON)
        } else {
            pauseBgm()
            googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_BGM_OFF)
        }
    }

    private fun toggleGameSound() {
        val isOn = !viewCtrl!!.gameSoundOn!!.isShown
        viewCtrl!!.setGameSound(isOn)
        pref!!.isOnGameSound = isOn
        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, if (isOn) IGoogleAnalytics.GA_LABEL_GAME_SOUND_ON else IGoogleAnalytics.GA_LABEL_GAME_SOUND_OFF)
    }

    private fun toggleInvitedOk() {
        val isOn = !viewCtrl!!.invitedOkOn!!.isShown
        viewCtrl!!.setInvitedOk(isOn)
        pref!!.isOnInvitedOk = isOn
        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, if (isOn) IGoogleAnalytics.GA_LABEL_INVITED_OK_ON else IGoogleAnalytics.GA_LABEL_INVITED_OK_OFF)
    }

    private fun toggleFirstStageHelper() {
        val isOn = !viewCtrl!!.firstStageHelperOn!!.isShown
        viewCtrl!!.setFirstStageHelper(isOn)
        pref!!.isOnFirstStageHelper = isOn
        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, if (isOn) IGoogleAnalytics.GA_LABEL_FIRST_STAGE_HELPER_ON else IGoogleAnalytics.GA_LABEL_FIRST_STAGE_HELPER_OFF)
    }


    internal inner class ViewControllaer {
        var bgmOn: View? = null
        private var bgmOff: View? = null
        var gameSoundOn: View? = null
        private var gameSoundOff: View? = null
        var invitedOkOn: View? = null
        private var invitedOkOff: View? = null
        var firstStageHelperOn: View? = null
        private var firstStageHelperOff: View? = null
        private var textBuyItemView: TextView? = null
        private var btnBuyItem: View? = null

        fun init() {
            val listener = View.OnClickListener { v ->
                playButtonClick()

                when (v.id) {
                    R.id.btn_bgm -> toggleBgm()
                    R.id.btn_game_sound -> toggleGameSound()
                    R.id.btn_invited_ok -> toggleInvitedOk()
                    R.id.btn_first_stage_helper -> toggleFirstStageHelper()
                    R.id.btn_buy_item ->
                        //						openBuyItem();
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_BUY_ITEM)
                }
            }
            findViewById<View>(R.id.btn_bgm).setOnClickListener(listener)
            bgmOn = findViewById(R.id.bgm_on)
            bgmOff = findViewById(R.id.bgm_off)
            findViewById<View>(R.id.btn_game_sound).setOnClickListener(listener)
            gameSoundOn = findViewById(R.id.game_sound_on)
            gameSoundOff = findViewById(R.id.game_sound_off)
            findViewById<View>(R.id.btn_invited_ok).setOnClickListener(listener)
            invitedOkOn = findViewById(R.id.invited_ok_on)
            invitedOkOff = findViewById(R.id.invited_ok_off)
            findViewById<View>(R.id.btn_first_stage_helper).setOnClickListener(listener)
            firstStageHelperOn = findViewById(R.id.first_stage_helper_on)
            firstStageHelperOff = findViewById(R.id.first_stage_helper_off)

            textBuyItemView = findViewById<View>(R.id.text_buy_item) as TextView
            textBuyItemView!!.text = Html.fromHtml(getString(R.string.main_message_6))
            btnBuyItem = findViewById(R.id.btn_buy_item)
            btnBuyItem!!.setOnClickListener(listener)
            hideItemView()
        }

        fun setBgm(isOn: Boolean) {
            if (isOn) {
                bgmOn!!.visibility = View.VISIBLE
                bgmOff!!.visibility = View.GONE
            } else {
                bgmOn!!.visibility = View.GONE
                bgmOff!!.visibility = View.VISIBLE
            }
        }

        fun setGameSound(isOn: Boolean) {
            if (isOn) {
                gameSoundOn!!.visibility = View.VISIBLE
                gameSoundOff!!.visibility = View.GONE
            } else {
                gameSoundOn!!.visibility = View.GONE
                gameSoundOff!!.visibility = View.VISIBLE
            }
        }

        fun setInvitedOk(isOn: Boolean) {
            if (isOn) {
                invitedOkOn!!.visibility = View.VISIBLE
                invitedOkOff!!.visibility = View.GONE
            } else {
                invitedOkOn!!.visibility = View.GONE
                invitedOkOff!!.visibility = View.VISIBLE
            }
        }

        fun setFirstStageHelper(isOn: Boolean) {
            if (isOn) {
                firstStageHelperOn!!.visibility = View.VISIBLE
                firstStageHelperOff!!.visibility = View.GONE
            } else {
                firstStageHelperOn!!.visibility = View.GONE
                firstStageHelperOff!!.visibility = View.VISIBLE
            }
        }

        fun hideItemView() {
            textBuyItemView!!.visibility = View.GONE
            btnBuyItem!!.visibility = View.GONE
        }

        fun showItemView() {
            textBuyItemView!!.visibility = View.VISIBLE
            btnBuyItem!!.visibility = View.VISIBLE
        }
    }

}
