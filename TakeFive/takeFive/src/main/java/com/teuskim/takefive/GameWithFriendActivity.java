package com.teuskim.takefive;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.teuskim.takefive.common.Cell;
import com.teuskim.takefive.common.CellLocate;
import com.teuskim.takefive.common.Log;
import com.teuskim.takefive.common.MiscUtil;
import com.teuskim.takefive.common.OmokManager;
import com.teuskim.takefive.common.OmokPreference;
import com.teuskim.takefive.common.OmokResult;
import com.teuskim.takefive.common.OnIntResultListener;
import com.teuskim.takefive.view.BoardView;
import com.teuskim.takefive.view.UfoAlertView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameWithFriendActivity extends BaseGameActivity {
	
	private final static int RC_SELECT_PLAYERS = 10000;
	private final static int RC_INVITATION_INBOX = 10001;
	private final static int RC_WAITING_ROOM = 10002;
	private final static int RC_RECORD_AUDIO = 10003;
	
	private final static int DEFAULT_REMAIN_TIME = 60;
	
    private BoardView boardView;
	private ViewController viewCtrl;
	private OmokManager oman;
	private Handler handler;
	private String roomId;
	private RoomConfig roomConfig;
	private ArrayList<Participant> participants;
	private String myId, opponentId, myName, opponentName;
	private Uri myImgUri;
	private byte[] myFirstMsgBuf, opponentFirstMsgBuf;
	private List<Chat> listChat;
	private ChatAdapter adapter;
	private OmokPreference pref;
	private int time;
	private SpeechRecognizer recognizer;
	private boolean isGameWithCom;

	/**
	 * 제한시간의 시간변화
	 */
	private Runnable timeRunnable = new Runnable() {
		
		@Override
		public void run() {
			time--;
			viewCtrl.setRemainTime(time);
			if (time >= 0) {
				handler.postDelayed(timeRunnable, 1000);
			} else {
				gameOver(false);
			}
		}
	};

	@Override
	protected void onConnected(GoogleSignInAccount googleSignInAccount) {
		super.onConnected(googleSignInAccount);
		Log.test("onConnected");

		Games.getPlayersClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.getCurrentPlayer()
				.addOnSuccessListener(new OnSuccessListener<Player>() {
					@Override
					public void onSuccess(Player player) {
						myName = player.getDisplayName();
						myImgUri = player.getIconImageUri();
					}
				});

		String invitationId = getIntent().getStringExtra("invitation_id");
		if (invitationId != null) {
			acceptInviteToRoom(invitationId);
			return;
		}
		showMenus();
//		testUI();  // for test
	}

	@Override
	protected void onDisconnected() {
		super.onDisconnected();
		Log.test("onDisconnected");
		finish();
	}

	// UI 테스트용
	private void testUI() {
		viewCtrl.showGameLayout();
		
		handler.postDelayed(new Runnable() { @Override public void run() { viewCtrl.animateUfoOpponent(true); }}, 1000);
		handler.postDelayed(new Runnable() { @Override public void run() { viewCtrl.animateUfoOpponent(false); }}, 3000);
		handler.postDelayed(new Runnable() { @Override public void run() { viewCtrl.animateUfoMe(true); }}, 5000);
		handler.postDelayed(new Runnable() { @Override public void run() { viewCtrl.animateUfoMe(false); }}, 7000);
	}

	private RoomUpdateCallback roomUpdateCallback = new RoomUpdateCallback() {
		@Override
		public void onRoomCreated(int statusCode, @Nullable Room room) {
			Log.test("onRoomCreated: "+statusCode);
			if (statusCode != GamesCallbackStatusCodes.OK) {
				showGameError();
				return;
			}
			showWaitingRoom(room);
		}

		@Override
		public void onRoomConnected(int statusCode, @Nullable Room room) {
			Log.test("onRoomConnected: "+statusCode);
			if (statusCode != GamesCallbackStatusCodes.OK) {
				showGameError();
				return;
			}
		}

		@Override
		public void onLeftRoom(int statusCode, @NonNull String s) {
			Log.test("onLeftRoom: "+statusCode+" , "+roomId);
			showMenus();
		}

		@Override
		public void onJoinedRoom(int statusCode, @Nullable Room room) {
			Log.test("onJoinedRoom: "+statusCode);
			if (statusCode != GamesCallbackStatusCodes.OK) {
				showGameError();
				return;
			}
			showWaitingRoom(room);
		}
	};
	
	private RoomStatusUpdateCallback roomStatusUpdateCallback = new RoomStatusUpdateCallback() {
		@Override public void onRoomConnecting(@Nullable Room room) {}
		@Override public void onRoomAutoMatching(@Nullable Room room) {}
		@Override public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {}
		@Override public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {}
		@Override public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {}
		@Override public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {}
		@Override public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {}
		@Override public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {}
		@Override public void onP2PConnected(@NonNull String s) {}
		@Override public void onP2PDisconnected(@NonNull String s) {}

		@Override
		public void onDisconnectedFromRoom(@Nullable Room room) {
			Log.test("onDisconnectedFromRoom");
			if (viewCtrl.isGameOver == false) {
				gameOver(true);
			}
		}

		@Override
		public void onConnectedToRoom(@Nullable final Room room) {
			Log.test("onConnectedToRoom");
			// 게임에 연결되었을때
			roomId = room.getRoomId();
			Games.getPlayersClient(GameWithFriendActivity.this, GoogleSignIn.getLastSignedInAccount(GameWithFriendActivity.this))
					.getCurrentPlayerId()
					.addOnSuccessListener(new OnSuccessListener<String>() {
						@Override
						public void onSuccess(String currentPlayerId) {
							myId = room.getParticipantId(currentPlayerId);
							participants = room.getParticipants();
							Uri opponentImgUri = null;
							for (Participant p : participants) {
								if (p.getParticipantId().equals(myId) == false) {
									opponentId = p.getParticipantId();
									opponentName = p.getDisplayName();
									opponentImgUri = p.getIconImageUri();
								}
							}
							viewCtrl.setProfileImgs(opponentImgUri, myImgUri);
							viewCtrl.setProfileNames(myName, opponentName);
						}
					});
		}
	};

	private OnRealTimeMessageReceivedListener rtmReceivedListener = new OnRealTimeMessageReceivedListener() {
		@Override
		public void onRealTimeMessageReceived(@NonNull RealTimeMessage rtm) {
			// 메시지를 받으면 게임뷰 갱신하고 내차례
			Log.test("onRealTimeMessageReceived!");

			byte[] buf = rtm.getMessageData();
			if (buf[0] == (byte)'f') {  // 선정하기데이터. f는 who first
				opponentFirstMsgBuf = buf;
				setWhoFirstView();

			} else if (buf[0] == (byte)'m') {  // 메시지데이터. m은 message
				String msg = opponentName+": "+new String(buf, 1, buf.length-1);
				addChat(msg);

			} else if (buf[0] == (byte)'c') {  // 오목게임데이터. c는 cell list
				boardView.setIsUserTurn(false);
				int row = buf[buf.length-2];
				int col = buf[buf.length-1];
				addOpponentChoice(col, row);
			}
		}
	};

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.game_with_friend_activity);
		listChat = new ArrayList<Chat>();
		adapter = new ChatAdapter();
		viewCtrl = new ViewController();
		boardView = (BoardView) findViewById(R.id.board_view);
		oman = new OmokManager();
		handler = new Handler();
		pref = OmokPreference.getInstance(getApplicationContext());
		initBoardView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		initRecognizer();
		viewCtrl.setMicReady();
	}

	private void initBoardView() {
		boardView.setOnIntResultListener(new OnIntResultListener() {
			
			@Override
			public void onIntResult(int result) {
				if (result == 1) {
					myTurnResult();
				} else if (result == 0) {
					Toast.makeText(getApplicationContext(), R.string.err_unable_to_put, Toast.LENGTH_SHORT).show();
				}
			}
		});
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) boardView.getLayoutParams();
		lp.width = getWindowManager().getDefaultDisplay().getWidth();
		lp.height = lp.width;
		boardView.setLayoutParams(lp);
		
		boardView.reset();
		boardView.setIsLocked(false);
	}
	
	private void startRemainTime() {
		time = DEFAULT_REMAIN_TIME;
		viewCtrl.setRemainTime(time);
		handler.removeCallbacks(timeRunnable);
		handler.postDelayed(timeRunnable, 1000);
	}
	
	private void stopRemainTimeAndWait() {
		handler.removeCallbacks(timeRunnable);
		viewCtrl.setAsWait();
	}
	
	private void myTurnResult() {
		List<Cell> cellList = boardView.getCellList();
		
		if (isGameWithCom) {
			CellLocate lastCellLocate = oman.getCellLocate(cellList.get(cellList.size()-1));
			oman.addSearchArea(lastCellLocate);
		} else {
			sendToOpponent(cellList); // 상대에게 데이터 보내기			
		}
		
		OmokResult omokResult = oman.getOmokResult(cellList);
		
		if (OmokResult.USER_WIN.equals(omokResult)) {
			
			boardView.animateOmokComplete(oman.getLastData());
			
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					gameOver(true);
				}
			}, 4000);
			
		} else {
			opponentTurn();
		}
	}
	
	private void sendToOpponent(List<Cell> clist) {
		byte[] msgBuf = new byte[1+clist.size()*2];
		msgBuf[0] = 'c';
		CellLocate cl;
		int j;
		for (int i=0; i<clist.size(); i++) {
			cl = oman.getCellLocate(clist.get(i));
			j = (i*2)+1;
			msgBuf[j] = (byte)(cl.getRow());
			msgBuf[j+1] = (byte)(cl.getCol());
		}
		Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.sendReliableMessage(msgBuf, roomId, opponentId, null);
	}
	
	private void opponentTurn() {
		// 턴을 상대에게 넘기고 대기, 상대가 실제친구면 onRealTimeMessageReceived 가 호출된다.
		boardView.setIsLocked(true);
		viewCtrl.animateUfo(false);
		boardView.setIsUserTurn(false);
		stopRemainTimeAndWait();
		
		if (isGameWithCom) {
			// 상대가 컴이면 컴의선택을 결정하고 다시 사용자턴으로
			comTurn();
		}
	}
	
	private void comTurn() {
		CellLocate cl = oman.getComsNextLocate(boardView.getCellList());
		addOpponentChoice(cl.getCol(), cl.getRow());
		oman.addSearchArea(cl);
	}
	
	private void addOpponentChoice(int col, int row) {
		boardView.addComChoice(col, row, new Animator.AnimatorListener() {
			
			@Override public void onAnimationStart(Animator animation) {}
			@Override public void onAnimationRepeat(Animator animation) {}
			@Override public void onAnimationCancel(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				boardView.setIsShowGuideLine(false);
				if (OmokResult.COM_WIN.equals(oman.getOmokResult(boardView.getCellList()))) {
					
					boardView.animateOmokComplete(oman.getLastData());
					
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							gameOver(false);
						}
					}, 4000);
					return;
					
				} else if (boardView.needToExpand()) {
					expandBoard();
				}
				boardView.setIsLocked(false);
				boardView.setIsUserTurn(true);
				viewCtrl.animateUfo(true);
				startRemainTime();
			}
		});
	}

	@Override
	protected void onStop() {
		Log.test("onStop!!");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.test("onDestroy!!");
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		
		switch (request) {
		case RC_SELECT_PLAYERS:
			handleSelectPlayersResult(response, data);
			break;
			
		case RC_WAITING_ROOM:
			if (response == Activity.RESULT_OK) {
				startGame(false);
			} else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
				leaveRoom();
			} else if (response == Activity.RESULT_CANCELED) {
				leaveRoom();
			}
			break;
			
		case RC_INVITATION_INBOX:
			handleInvitationInboxResult(response, data);
			break;
		}
	}

	private void inviteFriends() {
		Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.getSelectOpponentsIntent(1, 1)
				.addOnSuccessListener(new OnSuccessListener<Intent>() {
					@Override
					public void onSuccess(Intent intent) {
						startActivityForResult(intent, RC_SELECT_PLAYERS);
					}
				});
	}
	
	private void handleSelectPlayersResult(int response, Intent data) {
		if (response != Activity.RESULT_OK) {
			showMenus();
			return;
		}
		
		final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
		
		Bundle autoMatchCriteria = null;
		int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
		int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
		if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
		}
		
		RoomConfig.Builder rcb = RoomConfig.builder(roomUpdateCallback);
		rcb.addPlayersToInvite(invitees);
		rcb.setOnMessageReceivedListener(rtmReceivedListener);
		rcb.setRoomStatusUpdateCallback(roomStatusUpdateCallback);
		if (autoMatchCriteria != null) {
			rcb.setAutoMatchCriteria(autoMatchCriteria);
		}
		roomConfig = rcb.build();
		Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)).create(roomConfig);
	}
	
	private void seeInvitations() {
		Games.getInvitationsClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.getInvitationInboxIntent()
				.addOnSuccessListener(new OnSuccessListener<Intent>() {
					@Override
					public void onSuccess(Intent intent) {
						startActivityForResult(intent, RC_INVITATION_INBOX);
					}
				});
	}
	
	private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            showMenus();
            return;
        }

        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
        acceptInviteToRoom(inv.getInvitationId());
    }
	
	private void acceptInviteToRoom(String invId) {
		showLoading();
        RoomConfig.Builder rcb = RoomConfig.builder(roomUpdateCallback);
		rcb.setInvitationIdToAccept(invId)
			.setOnMessageReceivedListener(rtmReceivedListener)
			.setRoomStatusUpdateCallback(roomStatusUpdateCallback);
		roomConfig = rcb.build();

		Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)).join(roomConfig);
        clearInvitation();
    }
	
	@Override
	protected void moveToAcceptInvited() {
		acceptInviteToRoom(getInvitation().getInvitationId());
	}

	private void launchGoogleplus() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://plus.google.com/app/basic/people"));
		startActivity(intent);
	}
	
	private void showGameError() {
		showAlert(getString(R.string.err_please_retry));
		showMenus();
	}
	
	private void showWaitingRoom(Room room) {
		final int MIN_PLAYERS = Integer.MAX_VALUE;

		Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.getWaitingRoomIntent(room, MIN_PLAYERS)
				.addOnSuccessListener(new OnSuccessListener<Intent>() {
					@Override
					public void onSuccess(Intent intent) {
						startActivityForResult(intent, RC_WAITING_ROOM);
					}
				});
	}
	
	private void leaveRoom() {
		// 게임에서 벗어나기
		if (roomId != null) {
			Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)).leave(roomConfig, roomId);
            roomId = null;
        } else {
            finish();
        }
	}
	
	private void gameOver(boolean win) {
		if (win) {
			int wins = pref.getWinsCount()+1;
			pref.setWinsCount(wins);
			
			// 리더보드에 반영
			if (pref.isSignedIn()) {
				Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
						.submitScore(getString(R.string.leaderboard_friend_mode), wins);
			}
			
			// 업적 달성
			unlockAchievement(R.string.achievement_3);
			if (wins >= 10) {
				unlockAchievement(R.string.achievement_4);
			}
			if (wins >= 100) {
				unlockAchievement(R.string.achievement_6);
			}
		}
		handler.removeCallbacks(timeRunnable);
		viewCtrl.showGameOverLayout(win, pref.getWinsCount());
	}

	/**
	 * 게임시작하기
	 */
	private void startGame(boolean isGameWithCom) {
		this.isGameWithCom = isGameWithCom;
		
		viewCtrl.showGameLayout();
		
		if (oman.didInit() == false) {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// cellsize을 렌더링 후에 알 수 있기 때문에 여기서 init
					oman.init(BoardView.cellCnt, BoardView.cellCnt, boardView.getCellSize());
					oman.setDifficulty(OmokManager.DIFFICULTY_3);
				}
			});
		}
		
		listChat.add(new Chat(true, getString(R.string.text_game_chat_1)));
		adapter.notifyDataSetChanged();
		
		boardView.setIsLocked(true);
		sendForWhoFirst();
		
		if (isGameWithCom) {
			opponentName = "COM";
			viewCtrl.setProfileNames(myName, opponentName);
			viewCtrl.setProfileImgs(null, myImgUri);
			viewCtrl.profileImgOpponent.setImageResource(R.drawable.profile_img_com);
			setWhoFirstView();
		} else {
			if (opponentFirstMsgBuf != null && viewCtrl.isInitUfos == false) {
				setWhoFirstView();
			}
		}
	}
	
	private void setWhoFirstView() {
		if (myFirstMsgBuf != null && opponentFirstMsgBuf != null) {
			boolean meFirst = false;
			for (int i=1; i<myFirstMsgBuf.length; i++) {
				if (myFirstMsgBuf[i] > opponentFirstMsgBuf[i]) {
					meFirst = true;
					break;
				} else if (myFirstMsgBuf[i] < opponentFirstMsgBuf[i]) {
					meFirst = false;
					break;
				}
			}
			
			if (meFirst) {
				// 채팅영역에 내가 먼저라고 출력하고, 상단 턴정보 갱신한다.
				listChat.add(new Chat(true, getString(R.string.text_game_chat_2, myName)));
				viewCtrl.animateUfo(true);
				boardView.setIsUserTurn(true);
				boardView.setIsLocked(false);
				startRemainTime();
			} else {
				// 채팅영역에 상대가 먼저라고 출력하고, 상단 턴정보 갱신한다.
				listChat.add(new Chat(true, getString(R.string.text_game_chat_2, opponentName)));
				boardView.setIsLocked(true);
				boardView.setIsUserTurn(false);
				
				if (isGameWithCom) {
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							opponentTurn();
						}
					}, 500);
					
				} else {
					viewCtrl.animateUfo(false);
					stopRemainTimeAndWait();
				}
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 게임이 시작되면 누가 선인지 정하기 위한 메시지를 처음에 한번 보내야 한다.
	 */
	private void sendForWhoFirst() {
		myFirstMsgBuf = new byte[11];
		myFirstMsgBuf[0] = 'f';
		Random rand = new Random();
		for (int i=1; i<myFirstMsgBuf.length; i++) {
			myFirstMsgBuf[i] = (byte)rand.nextInt(100);
		}
		
		if (isGameWithCom) {
			opponentFirstMsgBuf = new byte[11];
			opponentFirstMsgBuf[0] = 'f';
			for (int i=1; i<opponentFirstMsgBuf.length; i++) {
				opponentFirstMsgBuf[i] = (byte)rand.nextInt(100);
			}
		} else {
			Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
					.sendReliableMessage(myFirstMsgBuf, roomId, opponentId, null);
		}
	}
	
	private void showMenus() {
		viewCtrl.showMenusLayout();
	}
	
	private void expandBoard() {
		if (oman == null) {
			return;
		}
		oman.expand(BoardView.CELL_CNT_EXPANDED, BoardView.CELL_CNT_EXPANDED);
		boardView.expand();
		oman.setCellSize(boardView.getCellSize());
	}
	
	@Override
	public void onBackPressed() {
		if (viewCtrl.isMicListening()) {
			
			cancelVoiceListening();
			
		} else if (viewCtrl.gameLayout.isShown()) {
			if (viewCtrl.ufoAlertView.isShown()) {
				switch (viewCtrl.ufoAlertView.getAlertType()) {
				case PAUSE2:
					viewCtrl.ufoAlertView.hideAnimate(new Runnable() {
						
						@Override
						public void run() {
							continueGame();
						}
					});
					break;
					
				default: // nothing
				}
			} else {
				viewCtrl.showPauseDialog();
			}
		} else {
			super.onBackPressed();
		}
	}
	
	private void continueGame() {
	}
	
	private void exitGame() {
		handler.removeCallbacks(timeRunnable);
		finish();
	}
	
	/**
	 * 초기상태로 리셋
	 */
	private void reset() {
		boardView.reset();
		oman.reset();
		viewCtrl.isInitUfos = false;
		viewCtrl.isGameOver = false;
		roomId = null;
		participants = null;
		opponentId = null;
		opponentName = null;
		myFirstMsgBuf = null;
		opponentFirstMsgBuf = null;
		listChat.clear();
		isGameWithCom = false;
	}
	
	private void addChat(String msg) {
		listChat.add(new Chat(false, msg));
		adapter.notifyDataSetChanged();
		viewCtrl.listChatView.setSelection(Integer.MAX_VALUE);
	}
	
	private void sendChat(String msg) {
		if (isGameWithCom) {
			if (listChat.size() > 0 && listChat.size()%3 == 0) {
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						String msg = opponentName+": "+getString(R.string.text_coms_talk, myName);
						addChat(msg);
					}
				}, 500);
			}
		} else {
			byte[] tmp = msg.getBytes();
			byte[] msgBuf = new byte[tmp.length+1];
			msgBuf[0] = (byte)'m';
			for (int i=0; i<tmp.length; i++) {
				msgBuf[i+1] = tmp[i];
			}

			Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
					.sendReliableMessage(msgBuf, roomId, opponentId, null);
		}
		
		addChat(getString(R.string.text_me)+": "+msg);
	}
	
	private void initRecognizer() {
		if (recognizer != null && viewCtrl.isMicListening()) {
			cancelVoiceListening();
		}
		
		recognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
	}

	private boolean isReady;
	private void startVoiceListening() {
    	viewCtrl.showLoadingMic();
    	recognizer.setRecognitionListener(new RecognitionListener() {
    		
			@Override
			public void onRmsChanged(float rmsdB) {
				if (isReady) {
					int state = (int)(rmsdB / 3);
					if (state < 0) {
						state = 0;
					} else if (state > 3) {
						state = 3;
					}
					viewCtrl.setSpeechState(state);
				}
			}
			
			@Override
			public void onResults(Bundle results) {
				isReady = false;
				viewCtrl.setMicReady();
				List<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
				Log.test("google search result : "+list);
				if (list != null && list.size() > 0) {
					sendChat(list.get(0));
				}
			}
			
			@Override
			public void onReadyForSpeech(Bundle params) {
				viewCtrl.setMicListening();
	        	isReady = true;
			}
			
			@Override public void onPartialResults(Bundle partialResults) {}
			
			@Override public void onEvent(int eventType, Bundle params) {}
			
			@Override
			public void onError(int error) {
				viewCtrl.setMicReady();
				showToast(R.string.err_voice_recognizer);
			}
			
			@Override
			public void onEndOfSpeech() {
				if (isReady) {
					viewCtrl.showLoadingMic();
				}
			}
			
			@Override public void onBufferReceived(byte[] buffer) {}
			
			@Override public void onBeginningOfSpeech() {}
		});
    	
    	Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizer.startListening(i);
    }
	
	private void cancelVoiceListening() {
		isReady = false;
    	recognizer.cancel();
        viewCtrl.setMicReady();
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == RC_RECORD_AUDIO) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startVoiceListening();
			}
		}
	}


	class Chat {
		boolean isSystemMsg;
		String msg;
		
		public Chat(boolean isSystemMsg, String msg) {
			this.isSystemMsg = isSystemMsg;
			this.msg = msg;
		}
	}
	
	class ChatViewHolder {
		TextView systemMsgView;
		TextView chatMsgView;
	}
	
	class ChatAdapter extends BaseAdapter {
		
		private LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

		@Override
		public int getCount() {
			return listChat.size();
		}

		@Override
		public Chat getItem(int position) {
			return listChat.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ChatViewHolder vh;
			if (convertView == null) {
				vh = new ChatViewHolder();
				convertView = inflater.inflate(R.layout.game_chat_item, null);
				vh.systemMsgView = (TextView) convertView.findViewById(R.id.system_msg);
				vh.chatMsgView = (TextView) convertView.findViewById(R.id.chat_msg);
				convertView.setTag(vh);
			} else {
				vh = (ChatViewHolder) convertView.getTag();
			}
			
			Chat c = getItem(position);
			if (c.isSystemMsg) {
				vh.systemMsgView.setVisibility(View.VISIBLE);
				vh.chatMsgView.setVisibility(View.GONE);
				vh.systemMsgView.setText(c.msg);
			} else {
				vh.systemMsgView.setVisibility(View.GONE);
				vh.chatMsgView.setVisibility(View.VISIBLE);
				vh.chatMsgView.setText(c.msg);
			}
			
			return convertView;
		}
	}
	
	class ViewController {
		private View menusLayout;
		private View btnInviteFriends;
		private View btnSeeInvitations;
		private View btnGameWithCom;
		private View btnLaunchGoogleplus;
		
		private View gameLayout;
		private ImageView profileImgOpponent, profileImgMe;
		private ImageView ufoOpponent, ufoMe;
		private TranslateAnimation ufoOpponentIn, ufoOpponentOut, ufoMeIn, ufoMeOut;
		private TextView opponentProfileNameView, remainTimeView, myProfileNameView;
		private ListView listChatView;
		private View btnRecordStart, btnRecordCancel;
		private ImageView speechStateView;
		private View loadingSpeech;
		
		private UfoAlertView ufoAlertView;
		
		private int dp30 = MiscUtil.convertDpToPx(30, getResources());
		private boolean isInitUfos, isGameOver;
		
		public ViewController() {
			View.OnClickListener listener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					playButtonClick();
					
					switch (v.getId()) {
					case R.id.btn_invite_friends:
						showLoading();
						inviteFriends();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_INVITE_FRIENDS);
						break;
						
					case R.id.btn_see_invitations:
						showLoading();
						seeInvitations();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_SEE_INVITATIONS);
						break;
						
					case R.id.btn_game_with_com:
						startGame(true);
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_GAME_WITH_COM);
						break;
						
					case R.id.btn_launch_googleplus:
						launchGoogleplus();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_LAUNCH_GOOGLEPLUS);
						break;
						
					case R.id.btn_record_start:
						int permissionCheck = ContextCompat.checkSelfPermission(GameWithFriendActivity.this, Manifest.permission.RECORD_AUDIO);
						if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(GameWithFriendActivity.this
									, new String[]{Manifest.permission.RECORD_AUDIO}
									, RC_RECORD_AUDIO);
						} else {
							startVoiceListening();
						}
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_RECORD_START);
						break;
						
					case R.id.btn_record_cancel:
						cancelVoiceListening();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_RECORD_CANCEL);
						break;
						
					case R.id.btn_pause:
						showPauseDialog();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_PAUSE);
						break;
					}
				}
			};
			
			menusLayout = findViewById(R.id.menus_layout);
			btnInviteFriends = findViewById(R.id.btn_invite_friends);
			btnInviteFriends.setOnClickListener(listener);
			btnSeeInvitations = findViewById(R.id.btn_see_invitations);
			btnSeeInvitations.setOnClickListener(listener);
			btnGameWithCom = findViewById(R.id.btn_game_with_com);
			btnGameWithCom.setOnClickListener(listener);
			btnLaunchGoogleplus = findViewById(R.id.btn_launch_googleplus);
			btnLaunchGoogleplus.setOnClickListener(listener);
			
			gameLayout = findViewById(R.id.game_layout);
			profileImgOpponent = (ImageView) findViewById(R.id.profile_img_opponent);
			profileImgMe = (ImageView) findViewById(R.id.profile_img_me);
			ufoOpponent = (ImageView) findViewById(R.id.ufo_opponent);
			ufoMe = (ImageView) findViewById(R.id.ufo_me);
			opponentProfileNameView = (TextView) findViewById(R.id.opponent_profile_name);
			remainTimeView = (TextView) findViewById(R.id.remain_time);
			myProfileNameView = (TextView) findViewById(R.id.my_profile_name);
			listChatView = (ListView) findViewById(R.id.list_chat);
			listChatView.setAdapter(adapter);
			btnRecordStart = findViewById(R.id.btn_record_start);
			btnRecordStart.setOnClickListener(listener);
			btnRecordCancel = findViewById(R.id.btn_record_cancel);
			btnRecordCancel.setOnClickListener(listener);
			speechStateView = (ImageView) findViewById(R.id.speech_state);
			loadingSpeech = findViewById(R.id.loading_speech);
			findViewById(R.id.btn_pause).setOnClickListener(listener);
			
			ufoAlertView = (UfoAlertView) findViewById(R.id.ufo_alert);
		}
		
		public void showGameLayout() {
			hideLoading();
			menusLayout.setVisibility(View.GONE);
			gameLayout.setVisibility(View.VISIBLE);
			
			setInGame(true);
			refreshBgm();
		}
		
		public void showMenusLayout() {
			hideLoading();
			menusLayout.setVisibility(View.VISIBLE);
			gameLayout.setVisibility(View.GONE);
			
			setInGame(false);
			refreshBgm();
		}
		
		public void showGameOverLayout(boolean win, int winCount) {
			hideLoading();
			isGameOver = true;
			
			String message;
			if (win) {
				message = getString(R.string.text_win_friend);
			} else {
				message = getString(R.string.text_lose_friend);
			}
			
			ufoAlertView.showGameOverModal(win, message, winCount, new Runnable() {
				
				@Override
				public void run() {
					ufoAlertView.hideAnimate(new Runnable() {
						
						@Override
						public void run() {
							finish();
						}
					});
				}
			});
		}
		
		public void setProfileImgs(Uri opponent, Uri me) {
			ImageManager im = ImageManager.create(getApplicationContext());
			if (me != null) {
				im.loadImage(profileImgMe, me, R.drawable.default_profile_img);
			} else {
				profileImgMe.setImageResource(R.drawable.default_profile_img);
			}
			if (opponent != null) {
				im.loadImage(profileImgOpponent, opponent, R.drawable.default_profile_img);
			} else {
				profileImgOpponent.setImageResource(R.drawable.default_profile_img);
			}
		}
		
		private void animateUfoOpponent(boolean hasTurn) {
			if (hasTurn) {
				if (ufoOpponentOut == null) {
					ufoOpponentOut = new TranslateAnimation(0, dp30, 0, 0);
					ufoOpponentOut.setDuration(500);
					ufoOpponentOut.setFillAfter(true);
				}
				ufoOpponent.setSelected(true);
				ufoOpponent.startAnimation(ufoOpponentOut);
			} else {
				if (ufoOpponentIn == null) {
					ufoOpponentIn = new TranslateAnimation(dp30, 0, 0, 0);
					ufoOpponentIn.setDuration(500);
					ufoOpponentIn.setFillAfter(true);
				}
				ufoOpponent.setSelected(false);
				ufoOpponent.startAnimation(ufoOpponentIn);
			}
		}
		
		private void animateUfoMe(boolean hasTurn) {
			if (hasTurn) {
				if (ufoMeOut == null) {
					ufoMeOut = new TranslateAnimation(0, -dp30, 0, 0);
					ufoMeOut.setDuration(500);
					ufoMeOut.setFillAfter(true);
				}
				ufoMe.setSelected(true);
				ufoMe.startAnimation(ufoMeOut);
			} else {
				if (ufoMeIn == null) {
					ufoMeIn = new TranslateAnimation(-dp30, 0, 0, 0);
					ufoMeIn.setDuration(500);
					ufoMeIn.setFillAfter(true);
				}
				ufoMe.setSelected(false);
				ufoMe.startAnimation(ufoMeIn);
			}
		}
		
		public void animateUfo(boolean myTurn) {
			animateUfoOpponent(!myTurn);
			animateUfoMe(myTurn);
			isInitUfos = true;
		}
		
		public void showPauseDialog() {
			ufoAlertView.showPause2Dialog(new UfoAlertView.UfoButtonListener() {
				
				@Override
				public void onClick(int position) {
					switch (position) {
					case 0:
						continueGame();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_CONTINUE);
						break;
					case 1:
						exitGame();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_EXIT);
						break;
					}
				}
			});
		}
		
		public void setProfileNames(String me, String opponent) {
			myProfileNameView.setText(getString(R.string.format_my_profile_name, me));
			opponentProfileNameView.setText(opponent);
		}
		
		public void setRemainTime(int remainTime) {
			if (remainTime < 0) {
				remainTime = 0;
			}
			remainTimeView.setText(getString(R.string.format_remain_time, remainTime));
		}
		
		public void setAsWait() {
			remainTimeView.setText(R.string.text_wait);
		}
		
		public void setMicReady() {
			btnRecordStart.setVisibility(View.VISIBLE);
			btnRecordCancel.setVisibility(View.GONE);
			speechStateView.setVisibility(View.INVISIBLE);
			hideLoadingMic();
		}
		
		public void setMicListening() {
			btnRecordStart.setVisibility(View.GONE);
			btnRecordCancel.setVisibility(View.VISIBLE);
			speechStateView.setVisibility(View.VISIBLE);
			hideLoadingMic();
		}
		
		public boolean isMicListening() {
			return btnRecordCancel.isShown();
		}

		public void showLoadingMic() {
			loadingSpeech.setVisibility(View.VISIBLE);
			speechStateView.setVisibility(View.INVISIBLE);
		}
		
		private void hideLoadingMic() {
			loadingSpeech.setVisibility(View.INVISIBLE);
		}
		
		public void setSpeechState(int state) {
			speechStateView.setVisibility(View.VISIBLE);
			
			switch (state) {
			case 1: speechStateView.setImageResource(R.drawable.speech_state_1); break;
			case 2: speechStateView.setImageResource(R.drawable.speech_state_2); break;
			case 3: speechStateView.setImageResource(R.drawable.speech_state_3); break;
			default: speechStateView.setImageResource(R.drawable.speech_state_0); break;
			}
		}
		
	}

}
