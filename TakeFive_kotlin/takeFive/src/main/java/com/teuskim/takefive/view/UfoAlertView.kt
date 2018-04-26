package com.teuskim.takefive.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.text.Spanned
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView

import com.google.android.gms.common.images.ImageManager
import com.teuskim.takefive.R
import com.teuskim.takefive.common.GameSoundManager
import com.teuskim.takefive.common.StageScore

class UfoAlertView : RelativeLayout {

    private var ufoExTouchPane: View? = null
    private var ufoLayout: View? = null

    // 알림
    private var noticeLayout: View? = null
    private var noticeTitleView: TextView? = null
    private var noticeMessageView: TextView? = null
    private var noticePointTitleView: TextView? = null
    private var noticePointView: TextView? = null

    // 점수
    private var scoreLayout: View? = null
    private var scoreTitleView: TextView? = null
    private var baseScoreView: TextView? = null
    private var costMissionScoreLayout: View? = null
    private var materialMissionScoreLayout: View? = null
    private var timeMissionScoreLayout: View? = null
    private var costMissionScoreView: TextView? = null
    private var materialMissionScoreView: TextView? = null
    private var timeMissionScoreView: TextView? = null
    private var bonusMissionLayout: View? = null
    private var bonusMissionMultiplierView: TextView? = null
    private var getScoreView: TextView? = null

    // 친구와대결에서 일시정지
    private var pause2Layout: View? = null

    // 초대받았을때
    private var invitedLayout: View? = null
    private var inviterImgView: ImageView? = null
    private var inviterNameView: TextView? = null

    // 컨펌(확인/취소버튼)
    private var confirmLayout: View? = null
    private var confirmMessageView: TextView? = null

    // 리스트뷰
    private var listLayout: View? = null
    private var listView: ListView? = null

    private var clickRunnable: Runnable? = null
    private var ufoSize: Int = 0
    private var gsManager: GameSoundManager? = null
    var alertType: AlertType? = null
        private set
    private var ufoButtonListener: UfoButtonListener? = null
    private var handler2: Handler = Handler()

    enum class AlertType {
        NOTICE, SCORE, MISSION_END, PAUSE2, INVITED, CONFIRM, BEGIN_STAGE
    }

    interface UfoButtonListener {
        fun onClick(position: Int)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.ufo_alert_view, this)
        visibility = View.GONE

        ufoExTouchPane = findViewById(R.id.ufo_ex_touch_pane)
        ufoLayout = findViewById(R.id.ufo_layout)
        ufoLayout!!.visibility = View.GONE
        ufoLayout!!.setOnClickListener {
            gsManager!!.playButtonClick()

            if (clickRunnable != null) {
                clickRunnable!!.run()
            }
        }
        noticeLayout = findViewById(R.id.notice_layout)
        noticeTitleView = findViewById<View>(R.id.notice_title) as TextView
        noticeMessageView = findViewById<View>(R.id.notice_message) as TextView
        noticePointTitleView = findViewById<View>(R.id.notice_point_title) as TextView
        noticePointView = findViewById<View>(R.id.notice_point) as TextView

        scoreLayout = findViewById(R.id.score_layout)
        scoreTitleView = findViewById<View>(R.id.score_title) as TextView
        baseScoreView = findViewById<View>(R.id.base_score) as TextView
        costMissionScoreLayout = findViewById(R.id.cost_mission_score_layout)
        costMissionScoreView = findViewById<View>(R.id.cost_mission_score) as TextView
        materialMissionScoreLayout = findViewById(R.id.material_mission_score_layout)
        materialMissionScoreView = findViewById<View>(R.id.material_mission_score) as TextView
        timeMissionScoreLayout = findViewById(R.id.time_mission_score_layout)
        timeMissionScoreView = findViewById<View>(R.id.time_mission_score) as TextView
        bonusMissionLayout = findViewById(R.id.bonus_mission_layout)
        bonusMissionMultiplierView = findViewById<View>(R.id.bonus_mission_multiplier) as TextView
        getScoreView = findViewById<View>(R.id.get_score) as TextView

        val ufoListener = OnClickListener { v ->
            if (ufoButtonListener == null) {
                return@OnClickListener
            }

            gsManager!!.playButtonClick()

            when (v.id) {
                R.id.btn_continue2 -> hideAnimateFromButton(0)
                R.id.btn_withdraw -> hideAnimateFromButton(1)
                R.id.btn_accept -> hideAnimateFromButton(0)
                R.id.btn_decline -> hideAnimateFromButton(1)
                R.id.btn_ok -> hideAnimateFromButton(0)
                R.id.btn_cancel -> hideAnimateFromButton(1)
            }
        }

        pause2Layout = findViewById(R.id.pause2_layout)
        findViewById<View>(R.id.btn_continue2).setOnClickListener(ufoListener)
        findViewById<View>(R.id.btn_withdraw).setOnClickListener(ufoListener)

        invitedLayout = findViewById(R.id.invited_layout)
        inviterImgView = findViewById<View>(R.id.inviter_img) as ImageView
        inviterNameView = findViewById<View>(R.id.inviter_name) as TextView
        findViewById<View>(R.id.btn_accept).setOnClickListener(ufoListener)
        findViewById<View>(R.id.btn_decline).setOnClickListener(ufoListener)

        confirmLayout = findViewById(R.id.confirm_layout)
        confirmMessageView = findViewById<View>(R.id.confirm_message) as TextView
        findViewById<View>(R.id.btn_ok).setOnClickListener(ufoListener)
        findViewById<View>(R.id.btn_cancel).setOnClickListener(ufoListener)

        listLayout = findViewById(R.id.list_layout)
        listView = findViewById<View>(R.id.list_view) as ListView

        gsManager = GameSoundManager.getInstance(context)
    }

    private fun showAnimate(alertType: AlertType) {
        this.alertType = alertType
        visibility = View.VISIBLE
        if (ufoLayout!!.visibility == View.GONE) {
            handler2.postDelayed({ animateMoveIn() }, 300)
        } else {
            animateScaleDown(Runnable { animateScaleUp() })
        }
    }

    fun hideAnimate(nextRunnable: Runnable?) {
        animateScaleDown(Runnable { animateMoveOut(nextRunnable) })
    }

    fun hideAnimateFromButton(ufoButtonPosition: Int) {
        hideAnimate(Runnable { ufoButtonListener!!.onClick(ufoButtonPosition) })
    }

    private fun hideAllLayouts() {
        noticeLayout!!.visibility = View.GONE
        scoreLayout!!.visibility = View.GONE
        pause2Layout!!.visibility = View.GONE
        invitedLayout!!.visibility = View.GONE
        confirmLayout!!.visibility = View.GONE
        listLayout!!.visibility = View.GONE
    }

    private fun showSelectedLayout() {
        when (alertType) {
            UfoAlertView.AlertType.NOTICE -> noticeLayout!!.visibility = View.VISIBLE
            UfoAlertView.AlertType.MISSION_END -> noticeLayout!!.visibility = View.VISIBLE
            UfoAlertView.AlertType.SCORE -> scoreLayout!!.visibility = View.VISIBLE
            UfoAlertView.AlertType.INVITED -> invitedLayout!!.visibility = View.VISIBLE
            UfoAlertView.AlertType.PAUSE2 -> pause2Layout!!.visibility = View.VISIBLE
            UfoAlertView.AlertType.CONFIRM -> confirmLayout!!.visibility = View.VISIBLE
            UfoAlertView.AlertType.BEGIN_STAGE -> listLayout!!.visibility = View.VISIBLE

            else -> noticeLayout!!.visibility = View.VISIBLE
        }

        if (AlertType.SCORE == alertType == false && AlertType.MISSION_END == alertType == false) {
            ufoExTouchPane!!.visibility = View.GONE
        }
    }

    private fun animateMoveIn() {
        ufoLayout!!.visibility = View.VISIBLE
        hideAllLayouts()

        val anim = TranslateAnimation((-width / 1.8).toInt().toFloat(), 0f, 0f, 0f)
        anim.duration = DURATION_ANIM.toLong()
        anim.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                ufoSize = ufoLayout!!.width
                animateScaleUp()
            }
        })
        ufoLayout!!.startAnimation(anim)

        gsManager!!.playUfoMove()
    }

    private fun animateMoveOut(nextRunnable: Runnable?) {
        val anim = TranslateAnimation(0f, (-width / 1.8).toInt().toFloat(), 0f, 0f)
        anim.duration = DURATION_ANIM.toLong()
        anim.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                ufoLayout!!.visibility = View.GONE
                visibility = View.GONE
                nextRunnable?.run()
            }
        })
        ufoLayout!!.startAnimation(anim)

        gsManager!!.playUfoMove()
    }

    private fun animateScaleUp() {
        ufoLayout!!.visibility = View.VISIBLE
        hideAllLayouts()

        val anim = ObjectAnimator.ofInt(this, "ufoSize", ufoSize, (width * 0.8).toInt())
        anim.duration = DURATION_ANIM.toLong()
        anim.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                showSelectedLayout()
            }
        })
        anim.start()
    }

    private fun animateScaleDown(nextRunnable: Runnable) {
        ufoLayout!!.visibility = View.VISIBLE
        hideAllLayouts()

        val anim = ObjectAnimator.ofInt(this, "ufoSize", (width * 0.8).toInt(), ufoSize)
        anim.duration = DURATION_ANIM.toLong()
        anim.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                nextRunnable.run()
            }
        })
        anim.start()
    }

    fun setUfoSize(size: Int) {
        val lp = ufoLayout!!.layoutParams as RelativeLayout.LayoutParams
        lp.width = size
        lp.height = size
        ufoLayout!!.layoutParams = lp
    }

    fun showMissionModal(stage: Int, missionResId: Int, clickRunnable: Runnable) {
        noticeTitleView!!.text = context!!.getString(R.string.title_notice_mission_format, stage)
        noticeMessageView!!.visibility = View.VISIBLE
        noticeMessageView!!.setText(missionResId)
        noticePointTitleView!!.visibility = View.GONE
        noticePointView!!.visibility = View.GONE
        this.clickRunnable = clickRunnable

        showAnimate(AlertType.NOTICE)
    }

    fun showCommentModal(titleResId: Int, commentResId: Int, clickRunnable: Runnable) {
        if (titleResId == 0) {
            noticeTitleView!!.setText(R.string.title_notice_comment)
        } else {
            noticeTitleView!!.setText(titleResId)
        }
        noticeMessageView!!.visibility = View.VISIBLE
        noticeMessageView!!.setText(commentResId)
        noticePointTitleView!!.visibility = View.GONE
        noticePointView!!.visibility = View.GONE
        this.clickRunnable = clickRunnable

        showAnimate(AlertType.NOTICE)
    }

    fun showMissionClearModal(stage: Int, stageScore: StageScore, clickRunnable: Runnable) {
        scoreTitleView!!.text = context!!.getString(R.string.title_notice_mission_clear_format, stage)
        baseScoreView!!.text = "" + stageScore.baseScore
        if (stageScore.hasCostMission()) {
            costMissionScoreLayout!!.visibility = View.VISIBLE
            costMissionScoreView!!.text = context!!.getString(R.string.text_mission_score_format, stageScore.remainCosts.toString() + " X " + stageScore.multiplier)
        } else {
            costMissionScoreLayout!!.visibility = View.GONE
        }
        if (stageScore.hasMaterialMission()) {
            materialMissionScoreLayout!!.visibility = View.VISIBLE
            materialMissionScoreView!!.text = context!!.getString(R.string.text_mission_score_format, stageScore.remainMaterials.toString() + " X " + stageScore.multiplier)
        } else {
            materialMissionScoreLayout!!.visibility = View.GONE
        }
        if (stageScore.hasTimeMission()) {
            timeMissionScoreLayout!!.visibility = View.VISIBLE
            timeMissionScoreView!!.text = context!!.getString(R.string.text_mission_score_format, stageScore.remainTime.toString() + " X " + stageScore.multiplier)
        } else {
            timeMissionScoreLayout!!.visibility = View.GONE
        }
        if (stageScore.hasMaterialMission()) {
            bonusMissionLayout!!.visibility = View.VISIBLE
            if (stageScore.isDouble) {
                bonusMissionMultiplierView!!.setText(R.string.text_bonus_mission_success)
            } else {
                bonusMissionMultiplierView!!.setText(R.string.text_bonus_mission_fail)
            }
        } else {
            bonusMissionLayout!!.visibility = View.GONE
        }
        getScoreView!!.text = context!!.getString(R.string.text_score_format, stageScore.score)

        this.clickRunnable = clickRunnable

        showAnimate(AlertType.SCORE)

        setExTouchListener()
    }

    private fun setExTouchListener() {
        handler2.postDelayed({
            ufoExTouchPane!!.visibility = View.VISIBLE
            ufoExTouchPane!!.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> visibility = View.INVISIBLE

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> visibility = View.VISIBLE
                }
                true
            }
        }, 500)
    }

    fun showMissionFailModal(stage: Int, totalScore: Int, clickRunnable: Runnable) {
        noticeTitleView!!.text = context!!.getString(R.string.title_notice_mission_fail_format, stage)
        noticeMessageView!!.visibility = View.GONE
        noticePointTitleView!!.visibility = View.VISIBLE
        noticePointTitleView!!.setText(R.string.text_final_score)
        noticePointView!!.visibility = View.VISIBLE
        noticePointView!!.text = context!!.getString(R.string.text_score_format, totalScore)
        this.clickRunnable = clickRunnable

        showAnimate(AlertType.MISSION_END)

        setExTouchListener()
    }

    fun showGameClearModal(totalScore: Int, clickRunnable: Runnable) {
        noticeTitleView!!.setText(R.string.title_notice_game_clear)
        noticeMessageView!!.visibility = View.GONE
        noticePointTitleView!!.visibility = View.VISIBLE
        noticePointTitleView!!.setText(R.string.text_final_score)
        noticePointView!!.visibility = View.VISIBLE
        noticePointView!!.text = context!!.getString(R.string.text_score_format, totalScore)
        this.clickRunnable = clickRunnable

        showAnimate(AlertType.MISSION_END)

        setExTouchListener()
    }

    fun showGameOverModal(win: Boolean, message: String, winCount: Int, clickRunnable: Runnable) {
        noticeTitleView!!.text = message
        if (win) {
            noticeMessageView!!.visibility = View.GONE
            noticePointTitleView!!.visibility = View.VISIBLE
            noticePointTitleView!!.setText(R.string.text_wins_count)
            noticePointView!!.visibility = View.VISIBLE
            noticePointView!!.text = context!!.getString(R.string.text_wins_count_format, winCount)
        } else {
            noticeMessageView!!.visibility = View.VISIBLE
            noticeMessageView!!.setText(R.string.text_lose_friend_2)
            noticePointTitleView!!.visibility = View.GONE
            noticePointView!!.visibility = View.GONE
        }
        this.clickRunnable = clickRunnable

        showAnimate(AlertType.MISSION_END)

        setExTouchListener()
    }

    fun showPause2Dialog(l: UfoButtonListener) {
        this.ufoButtonListener = l
        alertType = AlertType.PAUSE2
        showAnimate(AlertType.PAUSE2)
    }

    fun showInvitedDialog(imgUri: Uri?, name: String, l: UfoButtonListener) {
        this.ufoButtonListener = l
        alertType = AlertType.INVITED

        val im = ImageManager.create(getContext())
        if (imgUri != null) {
            im.loadImage(inviterImgView!!, imgUri, R.drawable.default_profile_img)
        } else {
            inviterImgView!!.setImageResource(R.drawable.default_profile_img)
        }

        inviterNameView!!.text = name

        showAnimate(AlertType.INVITED)
    }

    fun showConfirmDialog(message: Spanned, l: UfoButtonListener) {
        this.ufoButtonListener = l
        alertType = AlertType.CONFIRM
        confirmMessageView!!.text = message
        showAnimate(AlertType.CONFIRM)
    }

    fun showBeginningStageChooser(adapter: BaseAdapter) {
        this.ufoButtonListener = null
        alertType = AlertType.BEGIN_STAGE

        listView!!.adapter = adapter

        showAnimate(AlertType.BEGIN_STAGE)
    }

    companion object {

        private val DURATION_ANIM = 200
    }

}
