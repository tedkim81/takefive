package com.teuskim.takefive

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesActivityResultCodes
import com.google.android.gms.games.GamesCallbackStatusCodes
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.Multiplayer
import com.google.android.gms.games.multiplayer.Participant
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener
import com.google.android.gms.games.multiplayer.realtime.Room
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback
import com.teuskim.takefive.common.*
import com.teuskim.takefive.view.BoardView
import com.teuskim.takefive.view.UfoAlertView

import java.util.ArrayList
import java.util.Random

class GameWithFriendActivity : BaseGameActivity() {

    private var boardView: BoardView? = null
    private var viewCtrl: ViewController? = null
    private var oman: OmokManager? = null
    private var handler: Handler? = null
    private var roomId: String? = null
    private var roomConfig: RoomConfig? = null
    private var participants: ArrayList<Participant>? = null
    private var myId: String? = null
    private var opponentId: String? = null
    private var myName: String? = null
    private var opponentName: String? = null
    private var myImgUri: Uri? = null
    private var myFirstMsgBuf: ByteArray? = null
    private var opponentFirstMsgBuf: ByteArray? = null
    private var listChat: MutableList<Chat>? = null
    private var adapter: ChatAdapter? = null
    private var pref: OmokPreference? = null
    private var time: Int = 0
    private var recognizer: SpeechRecognizer? = null
    private var isGameWithCom: Boolean = false

    /**
     * 제한시간의 시간변화
     */
    private val timeRunnable = object : Runnable {

        override fun run() {
            time--
            viewCtrl!!.setRemainTime(time)
            if (time >= 0) {
                handler!!.postDelayed(this, 1000)
            } else {
                gameOver(false)
            }
        }
    }

    private val roomUpdateCallback = object : RoomUpdateCallback() {
        override fun onRoomCreated(statusCode: Int, room: Room?) {
            Log.test("onRoomCreated: " + statusCode)
            if (statusCode != GamesCallbackStatusCodes.OK) {
                showGameError()
                return
            }
            showWaitingRoom(room)
        }

        override fun onRoomConnected(statusCode: Int, room: Room?) {
            Log.test("onRoomConnected: " + statusCode)
            if (statusCode != GamesCallbackStatusCodes.OK) {
                showGameError()
                return
            }
        }

        override fun onLeftRoom(statusCode: Int, s: String) {
            Log.test("onLeftRoom: $statusCode , $roomId")
            showMenus()
        }

        override fun onJoinedRoom(statusCode: Int, room: Room?) {
            Log.test("onJoinedRoom: " + statusCode)
            if (statusCode != GamesCallbackStatusCodes.OK) {
                showGameError()
                return
            }
            showWaitingRoom(room)
        }
    }

    private val roomStatusUpdateCallback = object : RoomStatusUpdateCallback() {
        override fun onRoomConnecting(room: Room?) {}
        override fun onRoomAutoMatching(room: Room?) {}
        override fun onPeerInvitedToRoom(room: Room?, list: List<String>) {}
        override fun onPeerDeclined(room: Room?, list: List<String>) {}
        override fun onPeerJoined(room: Room?, list: List<String>) {}
        override fun onPeerLeft(room: Room?, list: List<String>) {}
        override fun onPeersConnected(room: Room?, list: List<String>) {}
        override fun onPeersDisconnected(room: Room?, list: List<String>) {}
        override fun onP2PConnected(s: String) {}
        override fun onP2PDisconnected(s: String) {}

        override fun onDisconnectedFromRoom(room: Room?) {
            Log.test("onDisconnectedFromRoom")
            if (viewCtrl!!.isGameOver == false) {
                gameOver(true)
            }
        }

        override fun onConnectedToRoom(room: Room?) {
            Log.test("onConnectedToRoom")
            // 게임에 연결되었을때
            roomId = room!!.roomId
            Games.getPlayersClient(this@GameWithFriendActivity, GoogleSignIn.getLastSignedInAccount(this@GameWithFriendActivity)!!)
                    .currentPlayerId
                    .addOnSuccessListener { currentPlayerId ->
                        myId = room.getParticipantId(currentPlayerId)
                        participants = room.participants
                        var opponentImgUri: Uri? = null
                        for (p in participants!!) {
                            if (p.participantId == myId == false) {
                                opponentId = p.participantId
                                opponentName = p.displayName
                                opponentImgUri = p.iconImageUri
                            }
                        }
                        viewCtrl!!.setProfileImgs(opponentImgUri, myImgUri)
                        viewCtrl!!.setProfileNames(myName, opponentName)
                    }
        }
    }

    private val rtmReceivedListener = OnRealTimeMessageReceivedListener { rtm ->
        // 메시지를 받으면 게임뷰 갱신하고 내차례
        Log.test("onRealTimeMessageReceived!")

        val buf = rtm.messageData
        if (buf[0] == 'f'.toByte()) {  // 선정하기데이터. f는 who first
            opponentFirstMsgBuf = buf
            setWhoFirstView()

        } else if (buf[0] == 'm'.toByte()) {  // 메시지데이터. m은 message
            val msg = opponentName + ": " + String(buf, 1, buf.size - 1)
            addChat(msg)

        } else if (buf[0] == 'c'.toByte()) {  // 오목게임데이터. c는 cell list
            boardView!!.setIsUserTurn(false)
            val row = buf[buf.size - 2].toInt()
            val col = buf[buf.size - 1].toInt()
            addOpponentChoice(col, row)
        }
    }

    private var isReady: Boolean = false

    override fun onConnected(googleSignInAccount: GoogleSignInAccount) {
        super.onConnected(googleSignInAccount)
        Log.test("onConnected")

        Games.getPlayersClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .currentPlayer
                .addOnSuccessListener { player ->
                    myName = player.displayName
                    myImgUri = player.iconImageUri
                }

        val invitationId = intent.getStringExtra("invitation_id")
        if (invitationId != null) {
            acceptInviteToRoom(invitationId)
            return
        }
        showMenus()
        //		testUI();  // for test
    }

    override fun onDisconnected() {
        super.onDisconnected()
        Log.test("onDisconnected")
        finish()
    }

    // UI 테스트용
    private fun testUI() {
        viewCtrl!!.showGameLayout()

        handler!!.postDelayed({ viewCtrl!!.animateUfoOpponent(true) }, 1000)
        handler!!.postDelayed({ viewCtrl!!.animateUfoOpponent(false) }, 3000)
        handler!!.postDelayed({ viewCtrl!!.animateUfoMe(true) }, 5000)
        handler!!.postDelayed({ viewCtrl!!.animateUfoMe(false) }, 7000)
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.game_with_friend_activity)
        listChat = ArrayList()
        adapter = ChatAdapter()
        viewCtrl = ViewController()
        boardView = findViewById<View>(R.id.board_view) as BoardView
        oman = OmokManager()
        handler = Handler()
        pref = OmokPreference.getInstance(applicationContext)
        initBoardView()
    }

    override fun onStart() {
        super.onStart()
        initRecognizer()
        viewCtrl!!.setMicReady()
    }

    private fun initBoardView() {
        boardView!!.setOnIntResultListener(object: OnIntResultListener {
            override fun onIntResult(result: Int) {
                if (result == 1) {
                    myTurnResult()
                } else if (result == 0) {
                    Toast.makeText(applicationContext, R.string.err_unable_to_put, Toast.LENGTH_SHORT).show()
                }
            }
        })
        val lp = boardView!!.layoutParams as LinearLayout.LayoutParams
        lp.width = windowManager.defaultDisplay.width
        lp.height = lp.width
        boardView!!.layoutParams = lp

        boardView!!.reset()
        boardView!!.setIsLocked(false)
    }

    private fun startRemainTime() {
        time = DEFAULT_REMAIN_TIME
        viewCtrl!!.setRemainTime(time)
        handler!!.removeCallbacks(timeRunnable)
        handler!!.postDelayed(timeRunnable, 1000)
    }

    private fun stopRemainTimeAndWait() {
        handler!!.removeCallbacks(timeRunnable)
        viewCtrl!!.setAsWait()
    }

    private fun myTurnResult() {
        val cellList = boardView!!.cellList

        if (isGameWithCom) {
            val lastCellLocate = oman!!.getCellLocate(cellList!![cellList.size - 1])
            oman!!.addSearchArea(lastCellLocate)
        } else {
            sendToOpponent(cellList) // 상대에게 데이터 보내기
        }

        val omokResult = oman!!.getOmokResult(cellList!!)

        if (OmokResult.USER_WIN == omokResult) {

            boardView!!.animateOmokComplete(oman!!.lastData!!)

            handler!!.postDelayed({ gameOver(true) }, 4000)

        } else {
            opponentTurn()
        }
    }

    private fun sendToOpponent(clist: List<Cell>?) {
        val msgBuf = ByteArray(1 + clist!!.size * 2)
        msgBuf[0] = 'c'.toByte()
        var cl: CellLocate
        var j: Int
        for (i in clist.indices) {
            cl = oman!!.getCellLocate(clist[i])
            j = i * 2 + 1
            msgBuf[j] = cl.row.toByte()
            msgBuf[j + 1] = cl.col.toByte()
        }
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .sendReliableMessage(msgBuf, roomId!!, opponentId!!, null)
    }

    private fun opponentTurn() {
        // 턴을 상대에게 넘기고 대기, 상대가 실제친구면 onRealTimeMessageReceived 가 호출된다.
        boardView!!.setIsLocked(true)
        viewCtrl!!.animateUfo(false)
        boardView!!.setIsUserTurn(false)
        stopRemainTimeAndWait()

        if (isGameWithCom) {
            // 상대가 컴이면 컴의선택을 결정하고 다시 사용자턴으로
            comTurn()
        }
    }

    private fun comTurn() {
        val cl = oman!!.getComsNextLocate(boardView!!.cellList!!)
        addOpponentChoice(cl.col, cl.row)
        oman!!.addSearchArea(cl)
    }

    private fun addOpponentChoice(col: Int, row: Int) {
        boardView!!.addComChoice(col, row, object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                boardView!!.setIsShowGuideLine(false)
                if (OmokResult.COM_WIN == oman!!.getOmokResult(boardView!!.cellList!!)) {

                    boardView!!.animateOmokComplete(oman!!.lastData!!)

                    handler!!.postDelayed({ gameOver(false) }, 4000)
                    return

                } else if (boardView!!.needToExpand()) {
                    expandBoard()
                }
                boardView!!.setIsLocked(false)
                boardView!!.setIsUserTurn(true)
                viewCtrl!!.animateUfo(true)
                startRemainTime()
            }
        })
    }

    override fun onStop() {
        Log.test("onStop!!")
        super.onStop()
    }

    override fun onDestroy() {
        Log.test("onDestroy!!")
        super.onDestroy()
    }

    override fun onActivityResult(request: Int, response: Int, data: Intent?) {
        super.onActivityResult(request, response, data)

        when (request) {
            RC_SELECT_PLAYERS -> handleSelectPlayersResult(response, data!!)

            RC_WAITING_ROOM -> if (response == Activity.RESULT_OK) {
                startGame(false)
            } else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                leaveRoom()
            } else if (response == Activity.RESULT_CANCELED) {
                leaveRoom()
            }

            RC_INVITATION_INBOX -> handleInvitationInboxResult(response, data!!)
        }
    }

    private fun inviteFriends() {
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .getSelectOpponentsIntent(1, 1)
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_SELECT_PLAYERS) }
    }

    private fun handleSelectPlayersResult(response: Int, data: Intent) {
        if (response != Activity.RESULT_OK) {
            showMenus()
            return
        }

        val invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)

        var autoMatchCriteria: Bundle? = null
        val minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0)
        val maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0)
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0)
        }

        val rcb = RoomConfig.builder(roomUpdateCallback)
        rcb.addPlayersToInvite(invitees)
        rcb.setOnMessageReceivedListener(rtmReceivedListener)
        rcb.setRoomStatusUpdateCallback(roomStatusUpdateCallback)
        if (autoMatchCriteria != null) {
            rcb.setAutoMatchCriteria(autoMatchCriteria)
        }
        roomConfig = rcb.build()
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!).create(roomConfig!!)
    }

    private fun seeInvitations() {
        Games.getInvitationsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .invitationInboxIntent
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_INVITATION_INBOX) }
    }

    private fun handleInvitationInboxResult(response: Int, data: Intent) {
        if (response != Activity.RESULT_OK) {
            showMenus()
            return
        }

        val inv = data.extras!!.getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)
        acceptInviteToRoom(inv!!.invitationId)
    }

    private fun acceptInviteToRoom(invId: String) {
        showLoading()
        val rcb = RoomConfig.builder(roomUpdateCallback)
        rcb.setInvitationIdToAccept(invId)
                .setOnMessageReceivedListener(rtmReceivedListener)
                .setRoomStatusUpdateCallback(roomStatusUpdateCallback)
        roomConfig = rcb.build()

        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!).join(roomConfig!!)
        clearInvitation()
    }

    override fun moveToAcceptInvited() {
        acceptInviteToRoom(recvInvitation!!.invitationId)
    }

    private fun launchGoogleplus() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("http://plus.google.com/app/basic/people")
        startActivity(intent)
    }

    private fun showGameError() {
        showAlert(getString(R.string.err_please_retry))
        showMenus()
    }

    private fun showWaitingRoom(room: Room?) {
        val MIN_PLAYERS = Integer.MAX_VALUE

        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .getWaitingRoomIntent(room!!, MIN_PLAYERS)
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_WAITING_ROOM) }
    }

    private fun leaveRoom() {
        // 게임에서 벗어나기
        if (roomId != null) {
            Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!).leave(roomConfig!!, roomId!!)
            roomId = null
        } else {
            finish()
        }
    }

    private fun gameOver(win: Boolean) {
        if (win) {
            val wins = pref!!.winsCount + 1
            pref!!.winsCount = wins

            // 리더보드에 반영
            if (pref!!.isSignedIn) {
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                        .submitScore(getString(R.string.leaderboard_friend_mode), wins.toLong())
            }

            // 업적 달성
            unlockAchievement(R.string.achievement_3)
            if (wins >= 10) {
                unlockAchievement(R.string.achievement_4)
            }
            if (wins >= 100) {
                unlockAchievement(R.string.achievement_6)
            }
        }
        handler!!.removeCallbacks(timeRunnable)
        viewCtrl!!.showGameOverLayout(win, pref!!.winsCount)
    }

    /**
     * 게임시작하기
     */
    private fun startGame(isGameWithCom: Boolean) {
        this.isGameWithCom = isGameWithCom

        viewCtrl!!.showGameLayout()

        if (oman!!.didInit() == false) {
            handler!!.post {
                // cellsize을 렌더링 후에 알 수 있기 때문에 여기서 init
                oman!!.init(BoardView.cellCnt, BoardView.cellCnt, boardView!!.getCellSize())
                oman!!.setDifficulty(OmokManager.DIFFICULTY_3)
            }
        }

        listChat!!.add(Chat(true, getString(R.string.text_game_chat_1)))
        adapter!!.notifyDataSetChanged()

        boardView!!.setIsLocked(true)
        sendForWhoFirst()

        if (isGameWithCom) {
            opponentName = "COM"
            viewCtrl!!.setProfileNames(myName, opponentName)
            viewCtrl!!.setProfileImgs(null, myImgUri)
            viewCtrl!!.profileImgOpponent.setImageResource(R.drawable.profile_img_com)
            setWhoFirstView()
        } else {
            if (opponentFirstMsgBuf != null && viewCtrl!!.isInitUfos == false) {
                setWhoFirstView()
            }
        }
    }

    private fun setWhoFirstView() {
        if (myFirstMsgBuf != null && opponentFirstMsgBuf != null) {
            var meFirst = false
            for (i in 1 until myFirstMsgBuf!!.size) {
                if (myFirstMsgBuf!![i] > opponentFirstMsgBuf!![i]) {
                    meFirst = true
                    break
                } else if (myFirstMsgBuf!![i] < opponentFirstMsgBuf!![i]) {
                    meFirst = false
                    break
                }
            }

            if (meFirst) {
                // 채팅영역에 내가 먼저라고 출력하고, 상단 턴정보 갱신한다.
                listChat!!.add(Chat(true, getString(R.string.text_game_chat_2, myName)))
                viewCtrl!!.animateUfo(true)
                boardView!!.setIsUserTurn(true)
                boardView!!.setIsLocked(false)
                startRemainTime()
            } else {
                // 채팅영역에 상대가 먼저라고 출력하고, 상단 턴정보 갱신한다.
                listChat!!.add(Chat(true, getString(R.string.text_game_chat_2, opponentName)))
                boardView!!.setIsLocked(true)
                boardView!!.setIsUserTurn(false)

                if (isGameWithCom) {
                    handler!!.postDelayed({ opponentTurn() }, 500)

                } else {
                    viewCtrl!!.animateUfo(false)
                    stopRemainTimeAndWait()
                }
            }
            adapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 게임이 시작되면 누가 선인지 정하기 위한 메시지를 처음에 한번 보내야 한다.
     */
    private fun sendForWhoFirst() {
        myFirstMsgBuf = ByteArray(11)
        myFirstMsgBuf!![0] = 'f'.toByte()
        val rand = Random()
        for (i in 1 until myFirstMsgBuf!!.size) {
            myFirstMsgBuf!![i] = rand.nextInt(100).toByte()
        }

        if (isGameWithCom) {
            opponentFirstMsgBuf = ByteArray(11)
            opponentFirstMsgBuf!![0] = 'f'.toByte()
            for (i in 1 until opponentFirstMsgBuf!!.size) {
                opponentFirstMsgBuf!![i] = rand.nextInt(100).toByte()
            }
        } else {
            Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                    .sendReliableMessage(myFirstMsgBuf!!, roomId!!, opponentId!!, null)
        }
    }

    private fun showMenus() {
        viewCtrl!!.showMenusLayout()
    }

    private fun expandBoard() {
        if (oman == null) {
            return
        }
        oman!!.expand(BoardView.CELL_CNT_EXPANDED, BoardView.CELL_CNT_EXPANDED)
        boardView!!.expand()
        oman!!.cellSize = boardView!!.getCellSize()
    }

    override fun onBackPressed() {
        if (viewCtrl!!.isMicListening) {

            cancelVoiceListening()

        } else if (viewCtrl!!.gameLayout.isShown) {
            if (viewCtrl!!.ufoAlertView.isShown) {
                when (viewCtrl!!.ufoAlertView.alertType) {
                    UfoAlertView.AlertType.PAUSE2 -> viewCtrl!!.ufoAlertView.hideAnimate(object: Runnable {
                        override fun run() {
                            continueGame()
                        }
                    })
                }// nothing
            } else {
                viewCtrl!!.showPauseDialog()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun continueGame() {}

    private fun exitGame() {
        handler!!.removeCallbacks(timeRunnable)
        finish()
    }

    /**
     * 초기상태로 리셋
     */
    private fun reset() {
        boardView!!.reset()
        oman!!.reset()
        viewCtrl!!.isInitUfos = false
        viewCtrl!!.isGameOver = false
        roomId = null
        participants = null
        opponentId = null
        opponentName = null
        myFirstMsgBuf = null
        opponentFirstMsgBuf = null
        listChat!!.clear()
        isGameWithCom = false
    }

    private fun addChat(msg: String) {
        listChat!!.add(Chat(false, msg))
        adapter!!.notifyDataSetChanged()
        viewCtrl!!.listChatView.setSelection(Integer.MAX_VALUE)
    }

    private fun sendChat(msg: String) {
        if (isGameWithCom) {
            if (listChat!!.size > 0 && listChat!!.size % 3 == 0) {
                handler!!.postDelayed({
                    val msg = opponentName + ": " + getString(R.string.text_coms_talk, myName)
                    addChat(msg)
                }, 500)
            }
        } else {
            val tmp = msg.toByteArray()
            val msgBuf = ByteArray(tmp.size + 1)
            msgBuf[0] = 'm'.toByte()
            for (i in tmp.indices) {
                msgBuf[i + 1] = tmp[i]
            }

            Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                    .sendReliableMessage(msgBuf, roomId!!, opponentId!!, null)
        }

        addChat(getString(R.string.text_me) + ": " + msg)
    }

    private fun initRecognizer() {
        if (recognizer != null && viewCtrl!!.isMicListening) {
            cancelVoiceListening()
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
    }

    private fun startVoiceListening() {
        viewCtrl!!.showLoadingMic()
        recognizer!!.setRecognitionListener(object : RecognitionListener {

            override fun onRmsChanged(rmsdB: Float) {
                if (isReady) {
                    var state = (rmsdB / 3).toInt()
                    if (state < 0) {
                        state = 0
                    } else if (state > 3) {
                        state = 3
                    }
                    viewCtrl!!.setSpeechState(state)
                }
            }

            override fun onResults(results: Bundle) {
                isReady = false
                viewCtrl!!.setMicReady()
                val list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.test("google search result : " + list!!)
                if (list != null && list.size > 0) {
                    sendChat(list[0])
                }
            }

            override fun onReadyForSpeech(params: Bundle) {
                viewCtrl!!.setMicListening()
                isReady = true
            }

            override fun onPartialResults(partialResults: Bundle) {}

            override fun onEvent(eventType: Int, params: Bundle) {}

            override fun onError(error: Int) {
                viewCtrl!!.setMicReady()
                showToast(R.string.err_voice_recognizer)
            }

            override fun onEndOfSpeech() {
                if (isReady) {
                    viewCtrl!!.showLoadingMic()
                }
            }

            override fun onBufferReceived(buffer: ByteArray) {}

            override fun onBeginningOfSpeech() {}
        })

        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        recognizer!!.startListening(i)
    }

    private fun cancelVoiceListening() {
        isReady = false
        recognizer!!.cancel()
        viewCtrl!!.setMicReady()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_RECORD_AUDIO) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceListening()
            }
        }
    }


    internal inner class Chat(var isSystemMsg: Boolean, var msg: String)

    internal inner class ChatViewHolder {
        var systemMsgView: TextView? = null
        var chatMsgView: TextView? = null
    }

    internal inner class ChatAdapter : BaseAdapter() {

        private val inflater = LayoutInflater.from(applicationContext)

        override fun getCount(): Int {
            return listChat!!.size
        }

        override fun getItem(position: Int): Chat {
            return listChat!![position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val vh: ChatViewHolder
            if (convertView == null) {
                vh = ChatViewHolder()
                convertView = inflater.inflate(R.layout.game_chat_item, null)
                vh.systemMsgView = convertView!!.findViewById<View>(R.id.system_msg) as TextView
                vh.chatMsgView = convertView.findViewById<View>(R.id.chat_msg) as TextView
                convertView.tag = vh
            } else {
                vh = convertView.tag as ChatViewHolder
            }

            val c = getItem(position)
            if (c.isSystemMsg) {
                vh.systemMsgView!!.visibility = View.VISIBLE
                vh.chatMsgView!!.visibility = View.GONE
                vh.systemMsgView!!.text = c.msg
            } else {
                vh.systemMsgView!!.visibility = View.GONE
                vh.chatMsgView!!.visibility = View.VISIBLE
                vh.chatMsgView!!.text = c.msg
            }

            return convertView
        }
    }

    internal inner class ViewController {
        private val menusLayout: View
        private val btnInviteFriends: View
        private val btnSeeInvitations: View
        private val btnGameWithCom: View
        private val btnLaunchGoogleplus: View

        val gameLayout: View
        val profileImgOpponent: ImageView
        private val profileImgMe: ImageView
        private val ufoOpponent: ImageView
        private val ufoMe: ImageView
        private var ufoOpponentIn: TranslateAnimation? = null
        private var ufoOpponentOut: TranslateAnimation? = null
        private var ufoMeIn: TranslateAnimation? = null
        private var ufoMeOut: TranslateAnimation? = null
        private val opponentProfileNameView: TextView
        private val remainTimeView: TextView
        private val myProfileNameView: TextView
        val listChatView: ListView
        private val btnRecordStart: View
        private val btnRecordCancel: View
        private val speechStateView: ImageView
        private val loadingSpeech: View

        val ufoAlertView: UfoAlertView

        private val dp30 = MiscUtil.convertDpToPx(30f, resources)
        var isInitUfos: Boolean = false
        var isGameOver: Boolean = false

        val isMicListening: Boolean
            get() = btnRecordCancel.isShown

        init {
            val listener = View.OnClickListener { v ->
                playButtonClick()

                when (v.id) {
                    R.id.btn_invite_friends -> {
                        showLoading()
                        inviteFriends()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_INVITE_FRIENDS)
                    }

                    R.id.btn_see_invitations -> {
                        showLoading()
                        seeInvitations()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_SEE_INVITATIONS)
                    }

                    R.id.btn_game_with_com -> {
                        startGame(true)
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_GAME_WITH_COM)
                    }

                    R.id.btn_launch_googleplus -> {
                        launchGoogleplus()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_LAUNCH_GOOGLEPLUS)
                    }

                    R.id.btn_record_start -> {
                        val permissionCheck = ContextCompat.checkSelfPermission(this@GameWithFriendActivity, Manifest.permission.RECORD_AUDIO)
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this@GameWithFriendActivity, arrayOf(Manifest.permission.RECORD_AUDIO), RC_RECORD_AUDIO)
                        } else {
                            startVoiceListening()
                        }
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_RECORD_START)
                    }

                    R.id.btn_record_cancel -> {
                        cancelVoiceListening()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_RECORD_CANCEL)
                    }

                    R.id.btn_pause -> {
                        showPauseDialog()
                        googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_PAUSE)
                    }
                }
            }

            menusLayout = findViewById(R.id.menus_layout)
            btnInviteFriends = findViewById(R.id.btn_invite_friends)
            btnInviteFriends.setOnClickListener(listener)
            btnSeeInvitations = findViewById(R.id.btn_see_invitations)
            btnSeeInvitations.setOnClickListener(listener)
            btnGameWithCom = findViewById(R.id.btn_game_with_com)
            btnGameWithCom.setOnClickListener(listener)
            btnLaunchGoogleplus = findViewById(R.id.btn_launch_googleplus)
            btnLaunchGoogleplus.setOnClickListener(listener)

            gameLayout = findViewById(R.id.game_layout)
            profileImgOpponent = findViewById<View>(R.id.profile_img_opponent) as ImageView
            profileImgMe = findViewById<View>(R.id.profile_img_me) as ImageView
            ufoOpponent = findViewById<View>(R.id.ufo_opponent) as ImageView
            ufoMe = findViewById<View>(R.id.ufo_me) as ImageView
            opponentProfileNameView = findViewById<View>(R.id.opponent_profile_name) as TextView
            remainTimeView = findViewById<View>(R.id.remain_time) as TextView
            myProfileNameView = findViewById<View>(R.id.my_profile_name) as TextView
            listChatView = findViewById<View>(R.id.list_chat) as ListView
            listChatView.adapter = adapter
            btnRecordStart = findViewById(R.id.btn_record_start)
            btnRecordStart.setOnClickListener(listener)
            btnRecordCancel = findViewById(R.id.btn_record_cancel)
            btnRecordCancel.setOnClickListener(listener)
            speechStateView = findViewById<View>(R.id.speech_state) as ImageView
            loadingSpeech = findViewById(R.id.loading_speech)
            findViewById<View>(R.id.btn_pause).setOnClickListener(listener)

            ufoAlertView = findViewById<View>(R.id.ufo_alert) as UfoAlertView
        }

        fun showGameLayout() {
            hideLoading()
            menusLayout.visibility = View.GONE
            gameLayout.visibility = View.VISIBLE

            setInGame(true)
            refreshBgm()
        }

        fun showMenusLayout() {
            hideLoading()
            menusLayout.visibility = View.VISIBLE
            gameLayout.visibility = View.GONE

            setInGame(false)
            refreshBgm()
        }

        fun showGameOverLayout(win: Boolean, winCount: Int) {
            hideLoading()
            isGameOver = true

            val message: String
            if (win) {
                message = getString(R.string.text_win_friend)
            } else {
                message = getString(R.string.text_lose_friend)
            }

            ufoAlertView.showGameOverModal(win, message, winCount, object: Runnable {
                override fun run() {
                    ufoAlertView.hideAnimate(object: Runnable {
                        override fun run() {
                            finish()
                        }
                    })
                }
            })
        }

        fun setProfileImgs(opponent: Uri?, me: Uri?) {
            val im = ImageManager.create(applicationContext)
            if (me != null) {
                im.loadImage(profileImgMe, me, R.drawable.default_profile_img)
            } else {
                profileImgMe.setImageResource(R.drawable.default_profile_img)
            }
            if (opponent != null) {
                im.loadImage(profileImgOpponent, opponent, R.drawable.default_profile_img)
            } else {
                profileImgOpponent.setImageResource(R.drawable.default_profile_img)
            }
        }

        fun animateUfoOpponent(hasTurn: Boolean) {
            if (hasTurn) {
                if (ufoOpponentOut == null) {
                    ufoOpponentOut = TranslateAnimation(0f, dp30.toFloat(), 0f, 0f)
                    ufoOpponentOut!!.duration = 500
                    ufoOpponentOut!!.fillAfter = true
                }
                ufoOpponent.isSelected = true
                ufoOpponent.startAnimation(ufoOpponentOut)
            } else {
                if (ufoOpponentIn == null) {
                    ufoOpponentIn = TranslateAnimation(dp30.toFloat(), 0f, 0f, 0f)
                    ufoOpponentIn!!.duration = 500
                    ufoOpponentIn!!.fillAfter = true
                }
                ufoOpponent.isSelected = false
                ufoOpponent.startAnimation(ufoOpponentIn)
            }
        }

        fun animateUfoMe(hasTurn: Boolean) {
            if (hasTurn) {
                if (ufoMeOut == null) {
                    ufoMeOut = TranslateAnimation(0f, (-dp30).toFloat(), 0f, 0f)
                    ufoMeOut!!.duration = 500
                    ufoMeOut!!.fillAfter = true
                }
                ufoMe.isSelected = true
                ufoMe.startAnimation(ufoMeOut)
            } else {
                if (ufoMeIn == null) {
                    ufoMeIn = TranslateAnimation((-dp30).toFloat(), 0f, 0f, 0f)
                    ufoMeIn!!.duration = 500
                    ufoMeIn!!.fillAfter = true
                }
                ufoMe.isSelected = false
                ufoMe.startAnimation(ufoMeIn)
            }
        }

        fun animateUfo(myTurn: Boolean) {
            animateUfoOpponent(!myTurn)
            animateUfoMe(myTurn)
            isInitUfos = true
        }

        fun showPauseDialog() {
            ufoAlertView.showPause2Dialog(object: UfoAlertView.UfoButtonListener {
                override fun onClick(position: Int) {
                    when (position) {
                        0 -> {
                            continueGame()
                            googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_CONTINUE)
                        }
                        1 -> {
                            exitGame()
                            googleAnalyticsEvent(IGoogleAnalytics.GA_ACTION_BUTTON_CLICK, IGoogleAnalytics.GA_LABEL_EXIT)
                        }
                    }
                }
            })
        }

        fun setProfileNames(me: String?, opponent: String?) {
            myProfileNameView.text = getString(R.string.format_my_profile_name, me)
            opponentProfileNameView.text = opponent
        }

        fun setRemainTime(remainTime: Int) {
            var remainTime = remainTime
            if (remainTime < 0) {
                remainTime = 0
            }
            remainTimeView.text = getString(R.string.format_remain_time, remainTime)
        }

        fun setAsWait() {
            remainTimeView.setText(R.string.text_wait)
        }

        fun setMicReady() {
            btnRecordStart.visibility = View.VISIBLE
            btnRecordCancel.visibility = View.GONE
            speechStateView.visibility = View.INVISIBLE
            hideLoadingMic()
        }

        fun setMicListening() {
            btnRecordStart.visibility = View.GONE
            btnRecordCancel.visibility = View.VISIBLE
            speechStateView.visibility = View.VISIBLE
            hideLoadingMic()
        }

        fun showLoadingMic() {
            loadingSpeech.visibility = View.VISIBLE
            speechStateView.visibility = View.INVISIBLE
        }

        private fun hideLoadingMic() {
            loadingSpeech.visibility = View.INVISIBLE
        }

        fun setSpeechState(state: Int) {
            speechStateView.visibility = View.VISIBLE

            when (state) {
                1 -> speechStateView.setImageResource(R.drawable.speech_state_1)
                2 -> speechStateView.setImageResource(R.drawable.speech_state_2)
                3 -> speechStateView.setImageResource(R.drawable.speech_state_3)
                else -> speechStateView.setImageResource(R.drawable.speech_state_0)
            }
        }

    }

    companion object {

        private val RC_SELECT_PLAYERS = 10000
        private val RC_INVITATION_INBOX = 10001
        private val RC_WAITING_ROOM = 10002
        private val RC_RECORD_AUDIO = 10003

        private val DEFAULT_REMAIN_TIME = 60
    }

}
