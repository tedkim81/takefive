package com.teuskim.takefive

import java.util.Calendar

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import com.teuskim.takefive.common.ClickYNs
import com.teuskim.takefive.common.IGoogleAnalytics
import com.teuskim.takefive.common.MiscUtil
import com.teuskim.takefive.common.OmokPreference
//import com.teuskim.takefive.iab.Purchase
import com.teuskim.takefive.view.UfoAlertView

class MainActivity : BaseGameActivity() {

    private val RC_UNUSED = 5001

    private var viewCtrl: ViewController? = null
    private var pref: OmokPreference? = null
    private var clickYNs: ClickYNs? = null
    private val handler = Handler()
    private var sayingForShare: String? = null
    private var beginningStageLimit: Int = 0
    private var beginningStage: Int = 0
    private var mainMessageType: MainMessageType? = null

    private val famousSaying: String
        get() {
            val arr = resources.getStringArray(R.array.sayings)
            var sayingIndex = pref!!.sayingIndex
            val sayingDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            if (sayingDay != pref!!.sayingDay) {
                sayingIndex++
                pref!!.sayingDay = sayingDay
            }
            if (sayingIndex >= arr.size) {
                sayingIndex = 0
            }
            pref!!.sayingIndex = sayingIndex
            sayingForShare = getString(R.string.title_today_saying_for_share) + "\n" + arr[sayingIndex]
            return getString(R.string.title_today_saying) + arr[sayingIndex]
        }

    private enum class MainMessageType {
        MISSION_GAME, ACHIEVEMENT, RANKING, SHARE, BUY_ITEM, SAYING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewCtrl = ViewController()
        pref = OmokPreference.getInstance(applicationContext)
        clickYNs = ClickYNs(applicationContext)
        initGoogle()
    }

    private fun initGoogle() {
        if (pref!!.isSignedIn) {
            signIn()
        }
    }

//    override fun afterIab(item: Purchase?) {
//        super.afterIab(item)
//
//        if (item != null) {
//            viewCtrl!!.showMainMessage(getMainMessage(false))
//        }
//    }

    override fun onStart() {
        super.onStart()
        viewCtrl!!.setBestScore(pref!!.bestScore)
        viewCtrl!!.setWinsCount(pref!!.winsCount)
        beginningStageLimit = pref!!.beginningStageLimit
    }

    override fun onResume() {
        super.onResume()
        viewCtrl!!.adView!!.resume()
        viewCtrl!!.showMainMessage(getMainMessage(true))
    }

    override fun onPause() {
        viewCtrl!!.adView!!.pause()
        super.onPause()
    }

    override fun onDestroy() {
        viewCtrl!!.adView!!.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (ufoAlertView!!.isShown && UfoAlertView.AlertType.BEGIN_STAGE == ufoAlertView!!.alertType) {
            ufoAlertView!!.hideAnimate(null)
        } else {
            super.onBackPressed()
        }
    }

    private fun startGame() {
        if (pref!!.beginningStageLimit > 1) {
            ufoAlertView!!.showBeginningStageChooser(StageChooserAdapter(Runnable {
                val intent = Intent(applicationContext, GameActivity::class.java)
                intent.putExtra("beginning_stage", beginningStage)
                startActivity(intent)
            }))
        } else {
            val intent = Intent(applicationContext, GameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun moveToLeaderboard() {
        if (pref!!.isSignedIn) {
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                    .allLeaderboardsIntent
                    .addOnSuccessListener { intent -> startActivityForResult(intent, RC_UNUSED) }.addOnFailureListener {
                // do nothing
            }
        } else {
            signIn()
        }
    }

    private fun submitScoreAtFirst() {
        if (pref!!.isSignedIn == false) {
            val bestScore = pref!!.bestScore
            if (bestScore > 0) {
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                        .submitScore(getString(R.string.leaderboard_mission_mode), bestScore.toLong())
            }
        }
    }

    private fun moveToSettings() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun moveToShare() {
        val intent = Intent(applicationContext, ShareActivity::class.java)
        startActivity(intent)
    }

    private fun moveToInfo() {
        val intent = Intent(applicationContext, InfoActivity::class.java)
        startActivity(intent)
    }

    private fun moveToAchievement() {
        if (pref!!.isSignedIn) {
            Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                    .achievementsIntent
                    .addOnSuccessListener { intent -> startActivityForResult(intent, RC_UNUSED) }.addOnFailureListener {
                // do nothing
            }
        } else {
            signIn()
        }
    }

    private fun moveToGameWithFriend() {
        if (pref!!.isSignedIn) {
            startActivity(Intent(applicationContext, GameWithFriendActivity::class.java))
        } else {
            signIn()
        }
    }

    private fun getMainMessage(isShownAd: Boolean): Spanned {
        val msg: Spanned

        val params = viewCtrl!!.mainMessageView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_VERTICAL

        viewCtrl!!.adViewLayout!!.visibility = View.GONE

        if (clickYNs!!.didClickNone()) {
            // 아무버튼도 누르지 않았을때, "안녕하세요, 미션오목입니다. 처음이시죠? 미션게임을 해보세요~!"
            msg = Html.fromHtml(getString(R.string.main_message_1))
            mainMessageType = MainMessageType.MISSION_GAME

        } else if (clickYNs!!.didClickMissionGame() && clickYNs!!.didClickAchievement() == false) {
            // 미션게임버튼을 누른적 있고 업적버튼 누른적 없을때, "안녕하세요, 미션오목입니다. 이제 업적을 하나하나 수행해 보세요~!"
            msg = Html.fromHtml(getString(R.string.main_message_2))
            mainMessageType = MainMessageType.ACHIEVEMENT

        } else if (clickYNs!!.didClickMissionGame() && clickYNs!!.didClickRanking() == false) {
            // 미션게임버튼을 누른적 있고 랭킹버튼을 누른적 없을때, "안녕하세요, 김태우님. 지금 점수가 몇등인지 궁금하지 않으세요? 랭킹을 확인해 보세요~!"
            msg = Html.fromHtml(getString(R.string.main_message_3))
            mainMessageType = MainMessageType.RANKING

        } else if (clickYNs!!.didClickShare() == false) {
            // 공유버튼을 누른적 없을때, "안녕하세요, 감태우님. 친구들에게 미션오목을 공유하고 함께 즐겨보세요~!"
            msg = Html.fromHtml(getString(R.string.main_message_4))
            mainMessageType = MainMessageType.SHARE

        } else if (pref!!.isPremium || true) {  // TODO: 예전에 구현한 인앱빌링이 하위호환이 되지 않아서 일단 아이템 구매유도를 숨긴다.
            // 아이템 구매자이면 명언 보여주기
            msg = Html.fromHtml(famousSaying)
            params.gravity = Gravity.TOP
            mainMessageType = MainMessageType.SAYING

        } else if (isShownAd == false) {
            // 광고가 보이지 않는 경우, 아이템 구매유도 문구
            msg = Html.fromHtml(getString(R.string.main_message_6))
            mainMessageType = MainMessageType.BUY_ITEM

        } else {
            // 기타 다른 경우, 아이템 구매유도 문구
            msg = Html.fromHtml(getString(R.string.main_message_5))
            viewCtrl!!.adViewLayout!!.visibility = View.VISIBLE
            params.gravity = Gravity.TOP
            mainMessageType = MainMessageType.BUY_ITEM
        }
        viewCtrl!!.mainMessageView.layoutParams = params

        return msg
    }

    private fun onClickMainMessage() {
        when (mainMessageType) {
            MainActivity.MainMessageType.MISSION_GAME -> {
                startGame()
                clickYNs!!.clickMissionGame()
                googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_MAIN_MESSAGE_CLICK, IGoogleAnalytics.GA_LABEL_MISSION_GAME)
            }

            MainActivity.MainMessageType.ACHIEVEMENT -> {
                moveToAchievement()
                clickYNs!!.clickAchievement()
                googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_MAIN_MESSAGE_CLICK, IGoogleAnalytics.GA_LABEL_ACHIEVEMENT)
            }

            MainActivity.MainMessageType.RANKING -> {
                moveToLeaderboard()
                clickYNs!!.clickRanking()
                googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_MAIN_MESSAGE_CLICK, IGoogleAnalytics.GA_LABEL_LEADERBOARD)
            }

            MainActivity.MainMessageType.SHARE -> {
                moveToShare()
                clickYNs!!.clickShare()
                googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_MAIN_MESSAGE_CLICK, IGoogleAnalytics.GA_LABEL_SHARE)
            }

            MainActivity.MainMessageType.BUY_ITEM -> {
                openBuyItem()
                googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_MAIN_MESSAGE_CLICK, IGoogleAnalytics.GA_LABEL_BUY_ITEM)
            }

            MainActivity.MainMessageType.SAYING -> {
                shareSaying()
                googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_MAIN_MESSAGE_CLICK, IGoogleAnalytics.GA_LABEL_SAYING)
            }
        }
    }

    private fun shareSaying() {
        if (TextUtils.isEmpty(sayingForShare)) {
            return
        }
        sayingForShare = sayingForShare!!.replace("<br/>", "\n")
        val i = Intent(Intent.ACTION_SEND)
        i.putExtra(Intent.EXTRA_TEXT, sayingForShare)
        i.type = "text/plain"
        val i2 = Intent.createChooser(i, getString(R.string.title_saying_share_dialog))
        startActivity(i2)
    }


    internal inner class StageChooserAdapter(private val nextRunnable: Runnable) : BaseAdapter() {

        private val listener = View.OnClickListener { v ->
            playButtonClick()
            beginningStage = v.tag as Int + 1
            if (beginningStage <= beginningStageLimit) {
                ufoAlertView!!.hideAnimate(nextRunnable)
                googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_BEGINNING_STAGE, beginningStage.toLong())
            } else {
                showToast(R.string.text_cannot_beginning_stage)
            }
        }

        private val csl: ColorStateList

        init {
            val states = arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf())
            val colors = intArrayOf(-0x7f89939d, -0x89939d)
            csl = ColorStateList(states, colors)
        }

        override fun getCount(): Int {
            return 10
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v: TextView
            if (convertView == null) {
                v = TextView(applicationContext)
                val params = AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
                v.textSize = 25f
                val padding = MiscUtil.convertDpToPx(15f, resources)
                v.setPadding(0, padding, 0, padding)
                v.gravity = Gravity.CENTER_HORIZONTAL
                v.layoutParams = params
            } else {
                v = (convertView as TextView?)!!
            }

            v.text = getString(R.string.title_stage_format, position + 1)
            v.tag = position
            v.setOnClickListener(listener)

            if (position + 1 <= beginningStageLimit) {
                v.setTextColor(csl)
            } else {
                v.setTextColor(-0x7f89939d)
            }

            return v
        }

    }

    internal inner class ViewController {

        private val bestScoreView: TextView
        private val winsCountView: TextView
        val mainMessageView: TextView
        var adViewLayout: View? = null
        public var adView: AdView? = null

        init {
            val listener = View.OnClickListener { v ->
                playButtonClick()

                when (v.id) {
                    R.id.btn_start_mission_game -> {
                        startGame()
                        clickYNs!!.clickMissionGame()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_MISSION_GAME)
                    }

                    R.id.btn_achievement -> {
                        moveToAchievement()
                        clickYNs!!.clickAchievement()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_ACHIEVEMENT)
                    }

                    R.id.btn_game_with_friends -> {
                        moveToGameWithFriend()
                        clickYNs!!.clickFriendGame()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_MATCHUP)
                    }

                    R.id.btn_leader_board -> {
                        moveToLeaderboard()
                        clickYNs!!.clickRanking()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_LEADERBOARD)
                    }

                    R.id.btn_settings -> {
                        moveToSettings()
                        clickYNs!!.clickSettings()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_SETTINGS)
                    }

                    R.id.btn_share -> {
                        moveToShare()
                        clickYNs!!.clickShare()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_SHARE)
                    }

                    R.id.main_message -> onClickMainMessage()

                    R.id.btn_info -> {
                        moveToInfo()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_INFO)
                    }
                }/*
					case R.id.btn_logout:
						signOut();
						pref.setIsSignedIn(false);
						break;
					*/
            }

            bestScoreView = findViewById<View>(R.id.best_score) as TextView
            winsCountView = findViewById<View>(R.id.wins_count) as TextView
            findViewById<View>(R.id.btn_start_mission_game).setOnClickListener(listener)
            findViewById<View>(R.id.btn_achievement).setOnClickListener(listener)
            findViewById<View>(R.id.btn_game_with_friends).setOnClickListener(listener)
            findViewById<View>(R.id.btn_leader_board).setOnClickListener(listener)
            findViewById<View>(R.id.btn_settings).setOnClickListener(listener)
            findViewById<View>(R.id.btn_share).setOnClickListener(listener)
            findViewById<View>(R.id.btn_info).setOnClickListener(listener)

            mainMessageView = findViewById<View>(R.id.main_message) as TextView
            mainMessageView.setOnClickListener(listener)
            initAdView()
        }

        private fun initAdView() {
            adViewLayout = findViewById(R.id.ad_view_layout)
            adView = findViewById<View>(R.id.ad_view) as AdView
            adView!!.adListener = object : AdListener() {

                override fun onAdLoaded() {}

                override fun onAdFailedToLoad(errorCode: Int) {
                    adViewLayout!!.visibility = View.GONE
                    showMainMessage(getMainMessage(false))
                }

                override fun onAdOpened() {}

                override fun onAdClosed() {}

                override fun onAdLeftApplication() {}
            }
            adView!!.loadAd(AdRequest.Builder().build())

            handler.postDelayed({
                val adTextView = findViewById<View>(R.id.ad_text) as TextView
                val lp = adTextView.layoutParams as RelativeLayout.LayoutParams
                lp.height = adView!!.height
                adTextView.layoutParams = lp
            }, 500)
        }

        fun setBestScore(score: Int) {
            bestScoreView.text = getString(R.string.text_score_format, score)
        }

        fun setWinsCount(winsCount: Int) {
            winsCountView.text = getString(R.string.text_wins_count_format, winsCount)
        }

        fun showMainMessage(msg: Spanned) {
            mainMessageView.text = msg
        }

    }

}
