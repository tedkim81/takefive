package com.teuskim.takefive

import android.animation.Animator
import java.util.Random

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import com.teuskim.takefive.common.*
import com.teuskim.takefive.view.BoardView
import com.teuskim.takefive.view.UfoAlertView

class GameActivity : BaseGameActivity() {

    private var boardView: BoardView? = null
    private var viewCtrl: ViewController? = null
    private var oman: OmokManager? = null
    private var pref: OmokPreference? = null
    private var stage: Int = 0
    private var totalScore: Int = 0
    private var goBackToCount: Int = 0
    private var beginningStageLimit: Int = 0
    private val handler = Handler()
    private var mission: Mission? = null
    private val afterSignIn: Runnable? = null

    private val isUserWin: Boolean
        get() = OmokResult.USER_WIN == oman!!.getOmokResult(boardView!!.cellList!!)

    private val missionFailRunnable = Runnable { mission!!.missionFail() }

    private val isComWin: Boolean
        get() = OmokResult.COM_WIN == oman!!.getOmokResult(boardView!!.cellList!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)
        viewCtrl = ViewController()
        boardView = findViewById<View>(R.id.board_view) as BoardView
        oman = OmokManager()
        pref = OmokPreference.getInstance(applicationContext)
        beginningStageLimit = pref!!.beginningStageLimit
        mission = Mission()
        initGoogle()
        initBoardView()
        setInGame(true)
    }

    //	@Override
    //	public void onSignInSucceeded() {
    //		super.onSignInSucceeded();
    //		if (afterSignIn != null) {
    //			afterSignIn.run();
    //		}
    //	}
    //
    //	@Override
    //	public void onSignInFailed() {
    //		super.onSignInFailed();
    //	}

    private fun initGoogle() {
        if (pref!!.isSignedIn) {
            signIn()
        }
    }

    private fun initBoardView() {
        boardView!!.setOnIntResultListener(object: OnIntResultListener {
            override fun onIntResult(result: Int) {
                if (result == 1) {  // 성공
                    userTurnResult()
                } else if (result == 2) {  // 미션값 변경
                    viewCtrl!!.recommendHereView.visibility = View.INVISIBLE
                    beforeUserTurnEnd()
                } else {  // 실패
                    showToast(getString(R.string.err_unable_to_put))
                }
            }
        })
        val lp = boardView!!.layoutParams as LinearLayout.LayoutParams
        lp.width = windowManager.defaultDisplay.width
        lp.height = lp.width
        boardView!!.layoutParams = lp

        val beginningStage = intent.getIntExtra("beginning_stage", 1)
        gameReset(beginningStage, true)
        //		gameReset(3, true);  // for test
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && oman!!.didInit() == false) {
            oman!!.init(BoardView.cellCnt, BoardView.cellCnt, boardView!!.getCellSize())  // cellsize을 렌더링 후에 알 수 있기 때문에 여기서 init
        }
    }

    private fun beforeUserTurnEnd() {
        val cellList = boardView!!.cellList
        val lastCellLocate = oman!!.getCellLocate(cellList!![cellList.size - 1])
        mission!!.beforeUserTurnEnd(lastCellLocate.row, lastCellLocate.col)
    }

    private fun userTurnResult() {
        val cellList = boardView!!.cellList
        val lastCellLocate = oman!!.getCellLocate(cellList!![cellList.size - 1])
        oman!!.addSearchArea(lastCellLocate)

        val missionContinue = mission!!.afterUserTurn(lastCellLocate.row, lastCellLocate.col)
        val omokResult = oman!!.getOmokResult(cellList)

        if (OmokResult.USER_WIN == omokResult) {

            boardView!!.animateOmokComplete(oman!!.lastData!!)

            handler.postDelayed({ mission!!.missionClear() }, 4000)

        } else if (missionContinue == false) {
            mission!!.missionFail()
        } else {
            comTurn()
        }
    }

    private fun comTurn() {
        boardView!!.setIsUserTurn(false)

        val cl = oman!!.getComsNextLocate(boardView!!.cellList!!)

        if (ConstantValues.IS_SHOW_TEST_MAP) {
            boardView!!.setTestPointsMap(oman!!.testMap!!)
        }

        boardView!!.addComChoice(cl.col, cl.row, object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                boardView!!.setIsShowGuideLine(false)
                if (isComWin) {

                    boardView!!.animateOmokComplete(oman!!.lastData!!)

                    handler.postDelayed(missionFailRunnable, 4000)
                    return

                } else if (boardView!!.needToExpand()) {
                    expandBoard()
                }

                readyUserTurn()
            }
        })
        oman!!.addSearchArea(cl)
    }

    private fun readyUserTurn() {
        boardView!!.setIsUserTurn(true)
        showHint()
        boardView!!.setIsLocked(false)
        mission!!.startTime()
    }

    private fun gameClear() {
        boardView!!.setIsLocked(true)
        viewCtrl!!.showGameClearModal()
        updateBestScore()
    }

    private fun gameReset(stage: Int, first: Boolean) {
        if (first) {
            totalScore = 0
            goBackToCount = if (pref!!.isPremium) 10 else 2
        }
        viewCtrl!!.setGoBackToCount(goBackToCount)

        boardView!!.reset()
        boardView!!.setIsLocked(false)

        if (ConstantValues.IS_SHOW_TEST_MAP) {
            boardView!!.setTestPointsMap(Array(BoardView.cellCnt) { IntArray(BoardView.cellCnt) })
        }

        if (oman != null) {
            oman!!.reset()
        }

        boardView!!.setIsUserTurn(true)

        setStageInfo(stage, first)
        mission!!.initMission(stage)

        if (stage > beginningStageLimit) {
            pref!!.beginningStageLimit = stage
        }
    }

    private fun setStageInfo(stage: Int, first: Boolean) {
        this.stage = stage
        viewCtrl!!.setStage(stage)

        when (stage) {
            1 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_0)
                viewCtrl!!.setStageMission(R.string.stage_mission_1_short)
                viewCtrl!!.showMissionModal(R.string.stage_mission_1, false)
            }

            2 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_1)
                viewCtrl!!.setStageMission(R.string.stage_mission_2_short)
                if (first) {
                    viewCtrl!!.showMissionModal(R.string.stage_mission_2, false)
                } else {
                    viewCtrl!!.showCommentModal(R.string.stage_mission_2_comment, Runnable { viewCtrl!!.showMissionModal(R.string.stage_mission_2, false) })
                }
            }

            3 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_1)
                viewCtrl!!.setStageMission(R.string.stage_mission_3_short)
                viewCtrl!!.showMissionModal(R.string.stage_mission_3, true)
            }

            4 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_2)
                viewCtrl!!.setStageMission(R.string.stage_mission_4_short)
                if (first) {
                    viewCtrl!!.showMissionModal(R.string.stage_mission_4, false)
                } else {
                    viewCtrl!!.showCommentModal(R.string.stage_mission_4_comment, Runnable { viewCtrl!!.showMissionModal(R.string.stage_mission_4, false) })
                }
            }

            5 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_2)
                viewCtrl!!.setStageMission(R.string.stage_mission_5_short)
                viewCtrl!!.showMissionModal(R.string.stage_mission_5, false)
            }

            6 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_2)
                viewCtrl!!.setStageMission(R.string.stage_mission_6_short)
                viewCtrl!!.showMissionModal(R.string.stage_mission_6, false)
            }

            7 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_2)
                viewCtrl!!.setStageMission(R.string.stage_mission_7_short)
                viewCtrl!!.showMissionModal(R.string.stage_mission_7, true)
            }

            8 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_3)
                viewCtrl!!.setStageMission(R.string.stage_mission_8_short)
                if (first) {
                    viewCtrl!!.showMissionModal(R.string.stage_mission_8, false)
                } else {
                    viewCtrl!!.showCommentModal(R.string.stage_mission_8_comment, Runnable { viewCtrl!!.showMissionModal(R.string.stage_mission_8, false) })
                }
            }

            9 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_3)
                viewCtrl!!.setStageMission(R.string.stage_mission_9_short)
                viewCtrl!!.showMissionModal(R.string.stage_mission_9, false)
            }

            10 -> {
                oman!!.setDifficulty(OmokManager.DIFFICULTY_3)
                viewCtrl!!.setStageMission(R.string.stage_mission_10_short)
                viewCtrl!!.showMissionModal(R.string.stage_mission_10, true)
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    private fun continueGame() {
        if (isComWin) {
            handler.post(missionFailRunnable)
        } else {
            mission!!.startTime()
        }
    }

    private fun showExitConfirm() {
        mission!!.cancelTime()
        handler.removeCallbacks(missionFailRunnable)

        val message = Html.fromHtml(getString(R.string.text_confirm_exit))
        ufoAlertView!!.showConfirmDialog(message, object: UfoAlertView.UfoButtonListener {
            override fun onClick(position: Int) {
                when (position) {
                    0 -> {
                        exitGame()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_EXIT)
                    }
                    1 -> {
                    }
                }/* do nothing */
            }
        })
    }

    private fun exitGame() {
        mission!!.cancelTime()
        finish()
    }

    override fun onBackPressed() {
        if (ufoAlertView!!.isShown) {
            when (ufoAlertView!!.alertType) {
                UfoAlertView.AlertType.CONFIRM -> ufoAlertView!!.hideAnimate(object: Runnable {
                    override fun run() {
                        continueGame()
                    }
                })
            }// nothing
        } else {
            //			pauseGame();

            showExitConfirm()
        }
    }

    private fun goBackTo() {
        if (isUserWin) {
            showToast(R.string.text_go_back_to_fail)
            return
        }
        if (goBackToCount > 0) {
            if (boardView!!.goBackOneStep()) {
                showToast(R.string.text_go_back_to_complete)
                goBackToCount--
                readyUserTurn()
                viewCtrl!!.setGoBackToCount(goBackToCount)
            } else {
                showToast(R.string.text_go_back_to_fail)
                continueGame()
            }
        } else {
            if (pref!!.isPremium) {
                showToast(R.string.text_go_back_to_no_item_2)
            } else {
                showToast(R.string.text_go_back_to_no_item_1)
            }
            continueGame()
        }
    }

    private fun showHint() {
        if (stage == MIN_STAGE && pref!!.isOnFirstStageHelper) {
            val anim = TranslateAnimation(0f, 0f, viewCtrl!!.recommendHereView.height.toFloat(), 0f)
            anim.duration = 500
            anim.setAnimationListener(object : AnimationListener {

                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    val cl = oman!!.getUsersNextLocate(boardView!!.cellList!!)
                    boardView!!.showHint(cl.col, cl.row)
                }
            })
            viewCtrl!!.recommendHereView.visibility = View.VISIBLE
            viewCtrl!!.recommendHereView.startAnimation(anim)
        }
    }

    private fun expandBoard() {
        if (oman == null) {
            return
        }
        oman!!.expand(BoardView.CELL_CNT_EXPANDED, BoardView.CELL_CNT_EXPANDED)
        boardView!!.setTestPointsMap(oman!!.testMap!!)
        mission!!.onExpand()
        boardView!!.expand()
        oman!!.cellSize = boardView!!.getCellSize()
    }

    private fun moveToMain() {
        val i = Intent(applicationContext, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finish()
    }

    private fun updateBestScore() {
        if (totalScore > pref!!.bestScore) {
            pref!!.bestScore = totalScore
            if (pref!!.isSignedIn) {
                //				Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_mission_mode), totalScore);
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                        .submitScore(getString(R.string.leaderboard_mission_mode), totalScore.toLong())
            }
        }
    }


    internal inner class Mission {

        private var cost: Int = 0
        private var material1Cnt: Int = 0
        private var material2Cnt: Int = 0
        private var material3Cnt: Int = 0
        private var time: Int = 0
        private var costsMapNormal: Array<IntArray>? = null
        private var costsMapExpanded: Array<IntArray>? = null
        private var materialsMapNormal: Array<Array<Material?>>? = null
        private var materialsMapExpanded: Array<Array<Material?>>? = null
        private var isOnCostMission: Boolean = false
        private var isOnMaterialMission: Boolean = false
        private var isOnTimeMission: Boolean = false
        private var isComFirst: Boolean = false

        /**
         * 제한시간미션의 시간변화
         */
        private val timeRunnable = object : Runnable {

            override fun run() {
                time--
                viewCtrl!!.setRemainTime(time)
                if (time >= 0) {
                    handler.postDelayed(this, UNIT_TIME.toLong())
                } else {
                    missionFail()
                }
            }
        }

        /**
         * 같은 재료로 연속된 다섯지역을 점령했는지 여부
         * 재료미션이 있을때만 호출되어야 한다.
         */
        private val isWinBySameMaterials: Boolean
            get() {
                val lastData = oman!!.lastData
                val mat: Array<Array<Material?>>?
                if (boardView!!.isExpanded) {
                    mat = materialsMapExpanded
                } else {
                    mat = materialsMapNormal
                }
                var row = lastData!!.srow
                var col = lastData.scol
                val m = mat!![row][col]
                for (i in 0..3) {
                    row += lastData.diffRow
                    col += lastData.diffCol
                    if (m == mat[row][col] == false) {
                        return false
                    }
                }
                return true
            }

        fun initMission(stage: Int) {
            when (stage) {
                2 -> {
                    initCostMission(99)
                    initMaterialMission(0, 0, 0)
                    initTimeMission(0)
                    isComFirst = false
                }

                3 -> {
                    initCostMission(0)
                    initMaterialMission(20, 20, 20)
                    initTimeMission(0)
                    isComFirst = false
                }

                5 -> {
                    initCostMission(0)
                    initMaterialMission(0, 0, 0)
                    initTimeMission(99)
                    isComFirst = false
                }

                6 -> {
                    initCostMission(80)
                    initMaterialMission(0, 0, 0)
                    initTimeMission(99)
                    isComFirst = false
                }

                7 -> {
                    initCostMission(0)
                    initMaterialMission(20, 15, 15)
                    initTimeMission(99)
                    isComFirst = false
                }

                8 -> {
                    initCostMission(0)
                    initMaterialMission(0, 0, 0)
                    initTimeMission(60)
                    isComFirst = true
                }

                9 -> {
                    initCostMission(60)
                    initMaterialMission(0, 0, 0)
                    initTimeMission(60)
                    isComFirst = true
                }

                10 -> {
                    initCostMission(0)
                    initMaterialMission(15, 10, 7)
                    initTimeMission(60)
                    isComFirst = true
                }

                else -> {
                    initCostMission(0)
                    initMaterialMission(0, 0, 0)
                    initTimeMission(0)
                    isComFirst = false
                }
            }
        }

        private fun initCostMission(cost: Int) {
            this.cost = cost
            if (cost > 0) {
                this.isOnCostMission = true
                generateCostsMap()
                boardView!!.costsMap = costsMapNormal
            } else {
                this.isOnCostMission = false
                boardView!!.costsMap = null
            }
            viewCtrl!!.setEnableCostMission(isOnCostMission)
            viewCtrl!!.setRemainCost(cost)
        }

        private fun initMaterialMission(material1Cnt: Int, material2Cnt: Int, material3Cnt: Int) {
            this.material1Cnt = material1Cnt
            this.material2Cnt = material2Cnt
            this.material3Cnt = material3Cnt
            if (material1Cnt > 0 || material2Cnt > 0 || material3Cnt > 0) {
                this.isOnMaterialMission = true
                generateMaterialsMap()
                boardView!!.materialsMap = materialsMapNormal
            } else {
                this.isOnMaterialMission = false
                boardView!!.materialsMap = null
            }
            viewCtrl!!.setEnableMaterialMission(isOnMaterialMission)
            viewCtrl!!.setRemainMaterial1(material1Cnt)
            viewCtrl!!.setRemainMaterial2(material2Cnt)
            viewCtrl!!.setRemainMaterial3(material3Cnt)
        }

        private fun initTimeMission(time: Int) {
            this.time = time
            this.isOnTimeMission = time > 0
            viewCtrl!!.setEnableTimeMission(isOnTimeMission)
            viewCtrl!!.setRemainTime(time)
        }

        fun beforeGameStart() {
            if (isComFirst) {
                boardView!!.setIsUserTurn(false)
                boardView!!.setIsLocked(true)

                handler.postDelayed({ comTurn() }, 1000)
            }
        }

        /**
         * 사용자가 터치업한 직후에 미션 관련 데이터를 눈에 띄게하기 위해 색 변화를 준다.
         */
        fun beforeUserTurnEnd(row: Int, col: Int) {
            if (isOnCostMission) {
                viewCtrl!!.readyRemainCost()
            }
            if (isOnMaterialMission) {
                val usedMat = boardView!!.materialsMap!![row][col]
                when (usedMat) {
                    Material.NUM1 -> viewCtrl!!.readyRemainMaterial1()
                    Material.NUM2 -> viewCtrl!!.readyRemainMaterial2()
                    Material.NUM3 -> viewCtrl!!.readyRemainMaterial3()
                }
            }
        }

        /**
         * 사용자턴 후에 미션 관련 데이터 처리
         * @return missionContinue 미션이 끝나지않고 지속되고 있는지 여부
         */
        fun afterUserTurn(row: Int, col: Int): Boolean {
            var costMissionContinue = true
            if (isOnCostMission) {
                val usedCost = boardView!!.costsMap!![row][col]
                cost -= usedCost
                viewCtrl!!.setRemainCost(cost)
                costMissionContinue = cost >= 0
            }

            var materialMissionContinue = true
            if (isOnMaterialMission) {
                val usedMat = boardView!!.materialsMap!![row][col]
                when (usedMat) {
                    Material.NUM1 -> {
                        material1Cnt--
                        viewCtrl!!.setRemainMaterial1(material1Cnt)
                    }
                    Material.NUM2 -> {
                        material2Cnt--
                        viewCtrl!!.setRemainMaterial2(material2Cnt)
                    }
                    Material.NUM3 -> {
                        material3Cnt--
                        viewCtrl!!.setRemainMaterial3(material3Cnt)
                    }
                }
                materialMissionContinue = material1Cnt >= 0 && material2Cnt >= 0 && material3Cnt >= 0
            }

            return costMissionContinue && materialMissionContinue
        }

        fun startTime() {
            if (isOnTimeMission) {
                handler.removeCallbacks(timeRunnable)
                handler.postDelayed(timeRunnable, UNIT_TIME.toLong())
            }
        }

        fun cancelTime() {
            handler.removeCallbacks(timeRunnable)
        }

        private fun generateCostsMap() {
            costsMapExpanded = Array(BoardView.CELL_CNT_EXPANDED) { IntArray(BoardView.CELL_CNT_EXPANDED) }
            if (stage <= 2) {
                generateCostsMap(1, 5, 1, 15)

            } else if (stage <= 6) {
                generateCostsMap(2, 7, 3, 20)

            } else {
                generateCostsMap(3, 9, 4, 20)
            }

            costsMapNormal = Array(BoardView.CELL_CNT_NORMAL) { IntArray(BoardView.CELL_CNT_NORMAL) }
            val offset = (BoardView.CELL_CNT_EXPANDED - BoardView.CELL_CNT_NORMAL) / 2
            for (row in 0 until BoardView.CELL_CNT_NORMAL) {
                for (col in 0 until BoardView.CELL_CNT_NORMAL) {
                    costsMapNormal!![row][col] = costsMapExpanded!![row + offset][col + offset]
                }
            }
        }

        private fun generateCostsMap(bottomCost: Int, maxCost: Int, randNum: Int, surCnt: Int) {
            boardView!!.setBottomCost(bottomCost)
            for (row in 0 until BoardView.CELL_CNT_EXPANDED) {
                for (col in 0 until BoardView.CELL_CNT_EXPANDED) {
                    costsMapExpanded!![row][col] = bottomCost
                }
            }
            val rand = Random()
            val surrounds = arrayOf(intArrayOf(-1, -1), intArrayOf(-1, 0), intArrayOf(-1, 1), intArrayOf(0, -1), intArrayOf(0, 0), intArrayOf(0, 1), intArrayOf(1, -1), intArrayOf(1, 0), intArrayOf(1, 1))
            for (i in 0 until surCnt) {
                val row = rand.nextInt(BoardView.CELL_CNT_EXPANDED)
                val col = rand.nextInt(BoardView.CELL_CNT_EXPANDED)
                costsMapExpanded!![row][col] = maxCost - rand.nextInt(randNum)
                val num = costsMapExpanded!![row][col] - 2
                for (j in surrounds.indices) {
                    val surRow = row + surrounds[j][0]
                    val surCol = col + surrounds[j][1]
                    if (surRow >= 0 && surRow < BoardView.CELL_CNT_EXPANDED
                            && surCol >= 0 && surCol < BoardView.CELL_CNT_EXPANDED
                            && costsMapExpanded!![surRow][surCol] < num) {

                        costsMapExpanded!![surRow][surCol] = num
                    }
                }
            }
        }

        private fun generateMaterialsMap() {
            materialsMapExpanded = Array(BoardView.CELL_CNT_EXPANDED) { arrayOfNulls<Material?>(BoardView.CELL_CNT_EXPANDED) }
            for (row in 0 until BoardView.CELL_CNT_EXPANDED) {
                for (col in 0 until BoardView.CELL_CNT_EXPANDED) {
                    materialsMapExpanded!![row][col] = Material.NUM1
                }
            }
            val rand = Random()
            var row: Int
            var col: Int
            var diffCol: Int
            var nextM: Material
            for (i in 0..13) {
                if (i % 2 == 0) {
                    nextM = Material.NUM2
                } else {
                    nextM = Material.NUM3
                }

                when (rand.nextInt(3)) {
                    0 -> {
                        row = rand.nextInt(BoardView.CELL_CNT_EXPANDED)
                        for (j in 0 until BoardView.CELL_CNT_EXPANDED) {
                            materialsMapExpanded!![row][j] = nextM
                        }
                    }
                    1 -> {
                        col = rand.nextInt(BoardView.CELL_CNT_EXPANDED)
                        for (j in 0 until BoardView.CELL_CNT_EXPANDED) {
                            materialsMapExpanded!![j][col] = nextM
                        }
                    }
                    else -> {
                        col = rand.nextInt(BoardView.CELL_CNT_EXPANDED)
                        if (rand.nextBoolean()) {
                            diffCol = 1
                        } else {
                            diffCol = -1
                        }
                        row = 0
                        while (row < BoardView.CELL_CNT_EXPANDED && col >= 0 && col < BoardView.CELL_CNT_EXPANDED) {
                            materialsMapExpanded!![row][col] = nextM
                            row++
                            col += diffCol
                        }
                    }
                }
            }

            materialsMapNormal = Array(BoardView.CELL_CNT_NORMAL) { arrayOfNulls<Material?>(BoardView.CELL_CNT_NORMAL) }
            val offset = (BoardView.CELL_CNT_EXPANDED - BoardView.CELL_CNT_NORMAL) / 2
            for (row2 in 0 until BoardView.CELL_CNT_NORMAL) {
                for (col2 in 0 until BoardView.CELL_CNT_NORMAL) {
                    materialsMapNormal!![row2][col2] = materialsMapExpanded!![row2 + offset][col2 + offset]
                }
            }
        }

        fun missionClear() {
            boardView!!.setIsLocked(true)
            handler.removeCallbacks(timeRunnable)

            val stageScore = mission!!.getMissionClearPoint(stage)
            totalScore += stageScore.score

            viewCtrl!!.showMissionClearModal(stageScore, Runnable {
                if (stage == MAX_STAGE) {
                    gameClear()
                } else {
                    gameReset(stage + 1, false)
                }
            })

            // 업적 달성
            if (stage == 1) {
                unlockAchievement(R.string.achievement_1)
            }
            if (totalScore >= 2000) {
                unlockAchievement(R.string.achievement_2)
            }
            if (stage == 5) {
                unlockAchievement(R.string.achievement_5)
            }
            if (stage == MAX_STAGE) {
                unlockAchievement(R.string.achievement_7)
            }
        }

        fun missionFail() {
            boardView!!.setIsLocked(true)
            handler.removeCallbacks(timeRunnable)

            viewCtrl!!.showMissionFailModal()
            updateBestScore()
        }

        fun onExpand() {
            if (boardView!!.costsMap != null) {
                boardView!!.costsMap = costsMapExpanded
            }
            if (boardView!!.materialsMap != null) {
                boardView!!.materialsMap = materialsMapExpanded
            }
        }

        fun getMissionClearPoint(stage: Int): StageScore {
            var baseScore = 100
            var hasCostMission = false
            var remainCosts = 0
            var hasMaterialMission = false
            var remainMaterials = 0
            var hasTimeMission = false
            var remainTime = 0
            var multiplier = 10
            var isDouble = false

            when (stage) {
                2 -> {
                    baseScore = 200
                    hasCostMission = true
                    remainCosts = cost
                }
                3 -> {
                    baseScore = 300
                    hasMaterialMission = true
                    remainMaterials = material1Cnt + material2Cnt + material3Cnt
                    isDouble = isWinBySameMaterials
                }
                4 -> baseScore = 400
                5 -> {
                    baseScore = 500
                    hasTimeMission = true
                    remainTime = time
                }
                6 -> {
                    baseScore = 600
                    hasCostMission = true
                    remainCosts = cost
                    hasTimeMission = true
                    remainTime = time
                }
                7 -> {
                    baseScore = 700
                    hasMaterialMission = true
                    remainMaterials = material1Cnt + material2Cnt + material3Cnt
                    hasTimeMission = true
                    remainTime = time
                    isDouble = isWinBySameMaterials
                }
                8 -> {
                    baseScore = 800
                    hasTimeMission = true
                    remainTime = time
                    multiplier = 100
                }
                9 -> {
                    baseScore = 900
                    hasCostMission = true
                    remainCosts = cost
                    hasTimeMission = true
                    remainTime = time
                    multiplier = 100
                }
                10 -> {
                    baseScore = 1000
                    hasMaterialMission = true
                    remainMaterials = material1Cnt + material2Cnt + material3Cnt
                    hasTimeMission = true
                    remainTime = time
                    multiplier = 100
                }
            }
            return StageScore(baseScore, hasCostMission, remainCosts, hasMaterialMission, remainMaterials, hasTimeMission, remainTime, multiplier, isDouble)
        }
    }

    internal inner class ViewController {
        private val goBackToCountView: TextView
        private val stageView: TextView
        private val stageMissionView: TextView
        private val costView: TextView
        private val materialNum1: TextView
        private val materialNum2: TextView
        private val materialNum3: TextView
        private val remainTimeView: TextView
        private val costMask: View
        private val materialNumMask: View
        private val remainTimeMask: View
        val recommendHereView: View

        private val moveToMainRunnable: Runnable
            get() = Runnable { ufoAlertView!!.hideAnimate(object: Runnable {
                override fun run() {
                    moveToMain()
                }
            }) }

        init {
            val listener = View.OnClickListener { v ->
                playButtonClick()

                when (v.id) {
                    R.id.btn_go_back_to -> {
                        goBackTo()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_GO_BACK)
                    }

                    R.id.btn_exit -> {
                        showExitConfirm()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_CONFIRM_EXIT)
                    }

                    R.id.btn_close_recommend -> {
                        showConfirmDialog()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_CLOSE_RECOMMEND)
                    }
                }
            }

            findViewById<View>(R.id.btn_go_back_to).setOnClickListener(listener)
            findViewById<View>(R.id.btn_exit).setOnClickListener(listener)
            goBackToCountView = findViewById<View>(R.id.go_back_to_count) as TextView
            stageView = findViewById<View>(R.id.stage) as TextView
            stageMissionView = findViewById<View>(R.id.stage_mission) as TextView
            costView = findViewById<View>(R.id.cost) as TextView
            costMask = findViewById(R.id.cost_mask)
            materialNum1 = findViewById<View>(R.id.material_num_1) as TextView
            materialNum2 = findViewById<View>(R.id.material_num_2) as TextView
            materialNum3 = findViewById<View>(R.id.material_num_3) as TextView
            materialNumMask = findViewById(R.id.material_num_mask)
            remainTimeView = findViewById<View>(R.id.remain_time) as TextView
            remainTimeMask = findViewById(R.id.remain_time_mask)
            recommendHereView = findViewById(R.id.recommend_here)
            findViewById<View>(R.id.btn_close_recommend).setOnClickListener(listener)
        }

        fun setStage(stage: Int) {
            stageView.text = getString(R.string.title_stage_format, stage)
        }

        fun setStageMission(stageMission: Int) {
            stageMissionView.setText(stageMission)
        }

        fun setEnableCostMission(enable: Boolean) {
            costMask.visibility = if (enable) View.GONE else View.VISIBLE
        }

        fun readyRemainCost() {
            costView.setTextColor(-0xaaab)
            costView.setBackgroundResource(R.drawable.bg_number_flash)
        }

        fun setRemainCost(cost: Int) {
            if (cost >= 0) {
                costView.text = "" + cost
            } else {
                costView.text = "0"
            }
            costView.setTextColor(-0x1)
            costView.setBackgroundColor(0x00000000)
        }

        fun setEnableMaterialMission(enable: Boolean) {
            materialNumMask.visibility = if (enable) View.GONE else View.VISIBLE
        }

        fun readyRemainMaterial1() {
            materialNum1.setTextColor(-0xaaab)
            materialNum1.setBackgroundResource(R.drawable.bg_number_flash)
        }

        fun setRemainMaterial1(material1Cnt: Int) {
            if (material1Cnt >= 0) {
                materialNum1.text = "" + material1Cnt
            } else {
                materialNum1.text = "0"
            }
            materialNum1.setTextColor(-0x1)
            materialNum1.setBackgroundColor(0x00000000)
        }

        fun readyRemainMaterial2() {
            materialNum2.setTextColor(-0xaaab)
            materialNum2.setBackgroundResource(R.drawable.bg_number_flash)
        }

        fun setRemainMaterial2(material2Cnt: Int) {
            if (material2Cnt >= 0) {
                materialNum2.text = "" + material2Cnt
            } else {
                materialNum2.text = "0"
            }
            materialNum2.setTextColor(-0x1)
            materialNum2.setBackgroundColor(0x00000000)
        }

        fun readyRemainMaterial3() {
            materialNum3.setTextColor(-0xaaab)
            materialNum3.setBackgroundResource(R.drawable.bg_number_flash)
        }

        fun setRemainMaterial3(material3Cnt: Int) {
            if (material3Cnt >= 0) {
                materialNum3.text = "" + material3Cnt
            } else {
                materialNum3.text = "0"
            }
            materialNum3.setTextColor(-0x1)
            materialNum3.setBackgroundColor(0x00000000)
        }

        fun setEnableTimeMission(enable: Boolean) {
            remainTimeMask.visibility = if (enable) View.GONE else View.VISIBLE
        }

        fun setRemainTime(time: Int) {
            if (time >= 0) {
                remainTimeView.text = "" + time
            } else {
                remainTimeView.text = "0"
            }
            if (time <= 30) {
                remainTimeView.setTextColor(-0xaaab)
                remainTimeView.setBackgroundResource(R.drawable.bg_number_flash)
                handler.postDelayed({
                    remainTimeView.setTextColor(-0x1)
                    remainTimeView.setBackgroundColor(0x00000000)
                }, 500)
            }
        }

        fun showMissionModal(missionResId: Int, showBonusMission: Boolean) {
            ufoAlertView!!.showMissionModal(stage, missionResId, object: Runnable {
                override fun run() {
                    if (showBonusMission) {
                        // 보너스 미션 보여주기
                        ufoAlertView!!.showCommentModal(R.string.title_bonus_mission, R.string.stage_material_sub_mission, object: Runnable {
                            override fun run() {
                                mission!!.beforeGameStart()  // 여기가 게임시작 직전
                                ufoAlertView!!.hideAnimate(null)
                            }
                        })
                    } else {
                        mission!!.beforeGameStart()  // 여기가 게임시작 직전
                        ufoAlertView!!.hideAnimate(object: Runnable {
                            override fun run() {
                                showHint()
                            }
                        })
                    }
                }
            })
        }

        fun showCommentModal(commentResId: Int, nextRunnable: Runnable) {
            ufoAlertView!!.showCommentModal(0, commentResId, nextRunnable)
        }

        fun showMissionClearModal(stageScore: StageScore, nextRunnable: Runnable) {
            ufoAlertView!!.showMissionClearModal(stage, stageScore, nextRunnable)
        }

        fun showMissionFailModal() {
            ufoAlertView!!.showMissionFailModal(stage, totalScore, moveToMainRunnable)
        }

        fun showGameClearModal() {
            ufoAlertView!!.showGameClearModal(totalScore, moveToMainRunnable)
        }

        fun showConfirmDialog() {
            val message = Html.fromHtml(getString(R.string.text_confirm_close_message))
            ufoAlertView!!.showConfirmDialog(message, object: UfoAlertView.UfoButtonListener {
                override fun onClick(position: Int) {
                    when (position) {
                        0 -> {
                            hideHint()
                            googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_HIDE_HINT)
                        }
                        1 -> {
                        }
                    }/* do nothing */
                }
            })
        }

        private fun hideHint() {
            recommendHereView.visibility = View.GONE
            boardView!!.hideHint()
            pref!!.isOnFirstStageHelper = false
        }

        fun setGoBackToCount(cnt: Int) {
            goBackToCountView.text = getString(R.string.text_go_back_to_count, cnt)
        }
    }

    companion object {

        private val MIN_STAGE = 1
        private val MAX_STAGE = 10
        private val UNIT_TIME = 1500
    }

}
