package com.teuskim.takefive;

import java.util.List;
import java.util.Random;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.teuskim.takefive.common.Cell;
import com.teuskim.takefive.common.CellLocate;
import com.teuskim.takefive.common.ConstantValues;
import com.teuskim.takefive.common.Material;
import com.teuskim.takefive.common.OmokLastData;
import com.teuskim.takefive.common.OmokManager;
import com.teuskim.takefive.common.OmokPreference;
import com.teuskim.takefive.common.OmokResult;
import com.teuskim.takefive.common.OnIntResultListener;
import com.teuskim.takefive.common.StageScore;
import com.teuskim.takefive.view.BoardView;
import com.teuskim.takefive.view.UfoAlertView;

public class GameActivity extends BaseGameActivity {
	
	private static final int MIN_STAGE = 1;
	private static final int MAX_STAGE = 10;
	private static final int UNIT_TIME = 1500;
	
	private BoardView boardView;
	private ViewController viewCtrl;
	private OmokManager oman;
	private OmokPreference pref;
	private int stage;
	private int totalScore;
	private int goBackToCount;
	private int beginningStageLimit;
	private Handler handler = new Handler();
	private Mission mission;
	private Runnable afterSignIn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_activity);
		viewCtrl = new ViewController();
		boardView = (BoardView) findViewById(R.id.board_view);
		oman = new OmokManager();
		pref = OmokPreference.getInstance(getApplicationContext());
		beginningStageLimit = pref.getBeginningStageLimit();
		mission = new Mission();
		initGoogle();
		initBoardView();
		setInGame(true);
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

	private void initGoogle() {
		if (pref.isSignedIn()) {
			signIn();
		}
	}
	
	private void initBoardView() {
		boardView.setOnIntResultListener(new OnIntResultListener() {
			
			@Override
			public void onIntResult(int result) {
				if (result == 1) {  // 성공
					userTurnResult();
				} else if (result == 2) {  // 미션값 변경
					viewCtrl.recommendHereView.setVisibility(View.INVISIBLE);
					beforeUserTurnEnd();
				} else {  // 실패
					showToast(getString(R.string.err_unable_to_put));
				}
			}
		});
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) boardView.getLayoutParams();
		lp.width = getWindowManager().getDefaultDisplay().getWidth();
		lp.height = lp.width;
		boardView.setLayoutParams(lp);
		
		int beginningStage = getIntent().getIntExtra("beginning_stage", 1);
		gameReset(beginningStage, true);
//		gameReset(3, true);  // for test
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && oman.didInit() == false) {
			oman.init(BoardView.cellCnt, BoardView.cellCnt, boardView.getCellSize());  // cellsize을 렌더링 후에 알 수 있기 때문에 여기서 init
		}
	}
	
	private void beforeUserTurnEnd() {
		List<Cell> cellList = boardView.getCellList();
		CellLocate lastCellLocate = oman.getCellLocate(cellList.get(cellList.size()-1));
		mission.beforeUserTurnEnd(lastCellLocate.getRow(), lastCellLocate.getCol());
	}
	
	private boolean isUserWin() {
		return (OmokResult.USER_WIN.equals(oman.getOmokResult(boardView.getCellList())));
	}

	private void userTurnResult() {
		List<Cell> cellList = boardView.getCellList();
		CellLocate lastCellLocate = oman.getCellLocate(cellList.get(cellList.size()-1));
		oman.addSearchArea(lastCellLocate);
		
		boolean missionContinue = mission.afterUserTurn(lastCellLocate.getRow(), lastCellLocate.getCol());
		OmokResult omokResult = oman.getOmokResult(cellList);
		
		if (OmokResult.USER_WIN.equals(omokResult)) {
			
			boardView.animateOmokComplete(oman.getLastData());
			
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mission.missionClear();
				}
			}, 4000);
			
		} else if (missionContinue == false) {
			mission.missionFail();
		} else {
			comTurn();
		}
	}
	
	private Runnable missionFailRunnable = new Runnable() {
		
		@Override
		public void run() {
			mission.missionFail();
		}
	};
	
	private boolean isComWin(){
		return (OmokResult.COM_WIN.equals(oman.getOmokResult(boardView.getCellList())));
	}
	
	private void comTurn() {
		boardView.setIsUserTurn(false);
		
		CellLocate cl = oman.getComsNextLocate(boardView.getCellList());
		
		if (ConstantValues.IS_SHOW_TEST_MAP) {
			boardView.setTestPointsMap(oman.getTestMap());
		}
		
		boardView.addComChoice(cl.getCol(), cl.getRow(), new Animator.AnimatorListener() {
			@Override public void onAnimationStart(Animator animation) {}
			@Override public void onAnimationRepeat(Animator animation) {}
			@Override public void onAnimationCancel(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				boardView.setIsShowGuideLine(false);
				if (isComWin()) {
					
					boardView.animateOmokComplete(oman.getLastData());
					
					handler.postDelayed(missionFailRunnable, 4000);
					return;
					
				} else if (boardView.needToExpand()) {
					expandBoard();
				}
				
				readyUserTurn();
			}
		});
		oman.addSearchArea(cl);
	}
	
	private void readyUserTurn() {
		boardView.setIsUserTurn(true);
		showHint();
		boardView.setIsLocked(false);
		mission.startTime();
	}
	
	private void gameClear() {
		boardView.setIsLocked(true);
		viewCtrl.showGameClearModal();
		updateBestScore();
	}
	
	private void gameReset(int stage, boolean first) {
		if (first) {
			totalScore = 0;
			goBackToCount = pref.isPremium() ? 10 : 2;
		}
		viewCtrl.setGoBackToCount(goBackToCount);
		
		boardView.reset();
		boardView.setIsLocked(false);
		
		if (ConstantValues.IS_SHOW_TEST_MAP) {
			boardView.setTestPointsMap(new int[BoardView.cellCnt][BoardView.cellCnt]);
		}
		
		if (oman != null) {
			oman.reset();
		}
		
		boardView.setIsUserTurn(true);
		
		setStageInfo(stage, first);
		mission.initMission(stage);
		
		if (stage > beginningStageLimit) {
			pref.setBeginningStageLimit(stage);
		}
	}
	
	private void setStageInfo(int stage, boolean first) {
		this.stage = stage;
		viewCtrl.setStage(stage);
		
		switch (stage) {
		case 1:
			oman.setDifficulty(OmokManager.DIFFICULTY_0);
			viewCtrl.setStageMission(R.string.stage_mission_1_short);
			viewCtrl.showMissionModal(R.string.stage_mission_1, false);
			break;
			
		case 2:
			oman.setDifficulty(OmokManager.DIFFICULTY_1);
			viewCtrl.setStageMission(R.string.stage_mission_2_short);
			if (first) {
				viewCtrl.showMissionModal(R.string.stage_mission_2, false);
			} else {
				viewCtrl.showCommentModal(R.string.stage_mission_2_comment, new Runnable() {
					
					@Override
					public void run() {
						viewCtrl.showMissionModal(R.string.stage_mission_2, false);
					}
				});
			}
			break;
			
		case 3:
			oman.setDifficulty(OmokManager.DIFFICULTY_1);
			viewCtrl.setStageMission(R.string.stage_mission_3_short);
			viewCtrl.showMissionModal(R.string.stage_mission_3, true);
			break;
			
		case 4:
			oman.setDifficulty(OmokManager.DIFFICULTY_2);
			viewCtrl.setStageMission(R.string.stage_mission_4_short);
			if (first) {
				viewCtrl.showMissionModal(R.string.stage_mission_4, false);
			} else {
				viewCtrl.showCommentModal(R.string.stage_mission_4_comment, new Runnable() {
					
					@Override
					public void run() {
						viewCtrl.showMissionModal(R.string.stage_mission_4, false);
					}
				});
			}
			break;
			
		case 5:
			oman.setDifficulty(OmokManager.DIFFICULTY_2);
			viewCtrl.setStageMission(R.string.stage_mission_5_short);
			viewCtrl.showMissionModal(R.string.stage_mission_5, false);
			break;
			
		case 6:
			oman.setDifficulty(OmokManager.DIFFICULTY_2);
			viewCtrl.setStageMission(R.string.stage_mission_6_short);
			viewCtrl.showMissionModal(R.string.stage_mission_6, false);
			break;
			
		case 7:
			oman.setDifficulty(OmokManager.DIFFICULTY_2);
			viewCtrl.setStageMission(R.string.stage_mission_7_short);
			viewCtrl.showMissionModal(R.string.stage_mission_7, true);
			break;
			
		case 8:
			oman.setDifficulty(OmokManager.DIFFICULTY_3);
			viewCtrl.setStageMission(R.string.stage_mission_8_short);
			if (first) {
				viewCtrl.showMissionModal(R.string.stage_mission_8, false);
			} else {
				viewCtrl.showCommentModal(R.string.stage_mission_8_comment, new Runnable() {
					
					@Override
					public void run() {
						viewCtrl.showMissionModal(R.string.stage_mission_8, false);
					}
				});
			}
			break;
			
		case 9:
			oman.setDifficulty(OmokManager.DIFFICULTY_3);
			viewCtrl.setStageMission(R.string.stage_mission_9_short);
			viewCtrl.showMissionModal(R.string.stage_mission_9, false);
			break;
			
		case 10:
			oman.setDifficulty(OmokManager.DIFFICULTY_3);
			viewCtrl.setStageMission(R.string.stage_mission_10_short);
			viewCtrl.showMissionModal(R.string.stage_mission_10, true);
			break;
		}
	}
	
	private void showToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	private void continueGame() {
		if (isComWin()) {
			handler.post(missionFailRunnable);
		} else {
			mission.startTime();
		}
	}
	
	private void showExitConfirm() {
		mission.cancelTime();
		handler.removeCallbacks(missionFailRunnable);
		
		Spanned message = Html.fromHtml(getString(R.string.text_confirm_exit));
		getUfoAlertView().showConfirmDialog(message, new UfoAlertView.UfoButtonListener() {
			
			@Override
			public void onClick(int position) {
				switch (position) {
				case 0:
					exitGame();
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_EXIT);
					break;
				case 1:
					/* do nothing */
					break;
				}
			}
		});
	}
	
	private void exitGame() {
		mission.cancelTime();
		finish();
	}
	
	@Override
	public void onBackPressed() {
		if (getUfoAlertView().isShown()) {
			switch (getUfoAlertView().getAlertType()) {
			case CONFIRM:
				getUfoAlertView().hideAnimate(new Runnable() {
					
					@Override
					public void run() {
						continueGame();
					}
				});
				break;
				
			default: // nothing
			}
		} else {
//			pauseGame();
			
			showExitConfirm();
		}
	}

	private void goBackTo() {
		if (isUserWin()) {
			showToast(R.string.text_go_back_to_fail);
			return;
		}
		if (goBackToCount > 0) {
			if (boardView.goBackOneStep()) {
				showToast(R.string.text_go_back_to_complete);
				goBackToCount--;
				readyUserTurn();
				viewCtrl.setGoBackToCount(goBackToCount);
			} else {
				showToast(R.string.text_go_back_to_fail);
				continueGame();
			}
		} else {
			if (pref.isPremium()) {
				showToast(R.string.text_go_back_to_no_item_2);
			} else {
				showToast(R.string.text_go_back_to_no_item_1);
			}
			continueGame();
		}
	}
	
	private void showHint() {
		if (stage == MIN_STAGE && pref.isOnFirstStageHelper()) {
			TranslateAnimation anim = new TranslateAnimation(0, 0, viewCtrl.recommendHereView.getHeight(), 0);
			anim.setDuration(500);
			anim.setAnimationListener(new AnimationListener() {
				
				@Override public void onAnimationStart(Animation animation) {}
				@Override public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					CellLocate cl = oman.getUsersNextLocate(boardView.getCellList());
					boardView.showHint(cl.getCol(), cl.getRow());
				}
			});
			viewCtrl.recommendHereView.setVisibility(View.VISIBLE);
			viewCtrl.recommendHereView.startAnimation(anim);
		}
	}
	
	private void expandBoard() {
		if (oman == null) {
			return;
		}
		oman.expand(BoardView.CELL_CNT_EXPANDED, BoardView.CELL_CNT_EXPANDED);
		boardView.setTestPointsMap(oman.getTestMap());
		mission.onExpand();
		boardView.expand();
		oman.setCellSize(boardView.getCellSize());
	}
	
	private void moveToMain() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}
	
	private void updateBestScore() {
		if (totalScore > pref.getBestScore()) {
			pref.setBestScore(totalScore);
			if (pref.isSignedIn()) {
//				Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_mission_mode), totalScore);
				Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
						.submitScore(getString(R.string.leaderboard_mission_mode), totalScore);
			}
		}
	}
	
	
	class Mission {
		
		private int cost;
		private int material1Cnt, material2Cnt, material3Cnt;
		private int time;
		private int[][] costsMapNormal, costsMapExpanded;
		private Material[][] materialsMapNormal, materialsMapExpanded;
		private boolean isOnCostMission, isOnMaterialMission, isOnTimeMission;
		private boolean isComFirst;
		
		public void initMission(int stage) {
			switch (stage) {
			case 2:
				initCostMission(99);
				initMaterialMission(0, 0, 0);
				initTimeMission(0);
				isComFirst = false;
				break;
				
			case 3:
				initCostMission(0);
				initMaterialMission(20, 20, 20);
				initTimeMission(0);
				isComFirst = false;
				break;
				
			case 5:
				initCostMission(0);
				initMaterialMission(0, 0, 0);
				initTimeMission(99);
				isComFirst = false;
				break;
				
			case 6:
				initCostMission(80);
				initMaterialMission(0, 0, 0);
				initTimeMission(99);
				isComFirst = false;
				break;
				
			case 7:
				initCostMission(0);
				initMaterialMission(20, 15, 15);
				initTimeMission(99);
				isComFirst = false;
				break;
				
			case 8:
				initCostMission(0);
				initMaterialMission(0, 0, 0);
				initTimeMission(60);
				isComFirst = true;
				break;
				
			case 9:
				initCostMission(60);
				initMaterialMission(0, 0, 0);
				initTimeMission(60);
				isComFirst = true;
				break;
				
			case 10:
				initCostMission(0);
				initMaterialMission(15, 10, 7);
				initTimeMission(60);
				isComFirst = true;
				break;
			
			default:
				initCostMission(0);
				initMaterialMission(0, 0, 0);
				initTimeMission(0);
				isComFirst = false;
				break;
			}
		}
		
		private void initCostMission(int cost) {
			this.cost = cost;
			if (cost > 0) {
				this.isOnCostMission = true;
				generateCostsMap();
				boardView.setCostsMap(costsMapNormal);
			} else {
				this.isOnCostMission = false;
				boardView.setCostsMap(null);
			}
			viewCtrl.setEnableCostMission(isOnCostMission);
			viewCtrl.setRemainCost(cost);
		}
		
		private void initMaterialMission(int material1Cnt, int material2Cnt, int material3Cnt) {
			this.material1Cnt = material1Cnt;
			this.material2Cnt = material2Cnt;
			this.material3Cnt = material3Cnt;
			if (material1Cnt > 0 || material2Cnt > 0 || material3Cnt > 0) {
				this.isOnMaterialMission = true;
				generateMaterialsMap();
				boardView.setMaterialsMap(materialsMapNormal);
			} else {
				this.isOnMaterialMission = false;
				boardView.setMaterialsMap(null);
			}
			viewCtrl.setEnableMaterialMission(isOnMaterialMission);
			viewCtrl.setRemainMaterial1(material1Cnt);
			viewCtrl.setRemainMaterial2(material2Cnt);
			viewCtrl.setRemainMaterial3(material3Cnt);
		}
		
		private void initTimeMission(int time) {
			this.time = time;
			this.isOnTimeMission = (time > 0);
			viewCtrl.setEnableTimeMission(isOnTimeMission);
			viewCtrl.setRemainTime(time);
		}
		
		public void beforeGameStart() {
			if (isComFirst) {
				boardView.setIsUserTurn(false);
				boardView.setIsLocked(true);
				
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						comTurn();
					}
				}, 1000);
			}
		}
		
		/**
		 * 사용자가 터치업한 직후에 미션 관련 데이터를 눈에 띄게하기 위해 색 변화를 준다.
		 */
		public void beforeUserTurnEnd(int row, int col) {
			if (isOnCostMission) {
				viewCtrl.readyRemainCost();
			}
			if (isOnMaterialMission) {
				Material usedMat = boardView.getMaterialsMap()[row][col];
				switch (usedMat) {
				case NUM1: viewCtrl.readyRemainMaterial1(); break;
				case NUM2: viewCtrl.readyRemainMaterial2(); break;
				case NUM3: viewCtrl.readyRemainMaterial3(); break;
				}
			}
		}
		
		/**
		 * 사용자턴 후에 미션 관련 데이터 처리
		 * @return missionContinue 미션이 끝나지않고 지속되고 있는지 여부
		 */
		public boolean afterUserTurn(int row, int col) {
			boolean costMissionContinue = true;
			if (isOnCostMission) {
				int usedCost = boardView.getCostsMap()[row][col];
				cost -= usedCost;
				viewCtrl.setRemainCost(cost);
				costMissionContinue = (cost >= 0);
			}
			
			boolean materialMissionContinue = true;
			if (isOnMaterialMission) {
				Material usedMat = boardView.getMaterialsMap()[row][col];
				switch (usedMat) {
				case NUM1: material1Cnt--; viewCtrl.setRemainMaterial1(material1Cnt); break;
				case NUM2: material2Cnt--; viewCtrl.setRemainMaterial2(material2Cnt); break;
				case NUM3: material3Cnt--; viewCtrl.setRemainMaterial3(material3Cnt); break;
				}
				materialMissionContinue = (material1Cnt >= 0 && material2Cnt >= 0 && material3Cnt >= 0);
			}
			
			return (costMissionContinue && materialMissionContinue);
		}
		
		/**
		 * 제한시간미션의 시간변화
		 */
		private Runnable timeRunnable = new Runnable() {
			
			@Override
			public void run() {
				time--;
				viewCtrl.setRemainTime(time);
				if (time >= 0) {
					handler.postDelayed(timeRunnable, UNIT_TIME);
				} else {
					missionFail();
				}
			}
		};
		public void startTime() {
			if (isOnTimeMission) {
				handler.removeCallbacks(timeRunnable);
				handler.postDelayed(timeRunnable, UNIT_TIME);
			}
		}
		
		public void cancelTime() {
			handler.removeCallbacks(timeRunnable);
		}
		
		private void generateCostsMap() {
			costsMapExpanded = new int[BoardView.CELL_CNT_EXPANDED][BoardView.CELL_CNT_EXPANDED];
			if (stage <= 2) {
				generateCostsMap(1, 5, 1, 15);
				
			} else if (stage <= 6) {
				generateCostsMap(2, 7, 3, 20);
				
			} else {
				generateCostsMap(3, 9, 4, 20);
			}
			
			costsMapNormal = new int[BoardView.CELL_CNT_NORMAL][BoardView.CELL_CNT_NORMAL];
			int offset = (BoardView.CELL_CNT_EXPANDED - BoardView.CELL_CNT_NORMAL) / 2;
			for (int row=0; row<BoardView.CELL_CNT_NORMAL; row++) {
				for (int col=0; col<BoardView.CELL_CNT_NORMAL; col++) {
					costsMapNormal[row][col] = costsMapExpanded[row+offset][col+offset];
				}
			}
		}
		
		private void generateCostsMap(int bottomCost, int maxCost, int randNum, int surCnt) {
			boardView.setBottomCost(bottomCost);
			for (int row=0; row<BoardView.CELL_CNT_EXPANDED; row++) {
				for (int col=0; col<BoardView.CELL_CNT_EXPANDED; col++) {
					costsMapExpanded[row][col] = bottomCost;
				}
			}
			Random rand = new Random();
			int[][] surrounds = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,0},{0,1},{1,-1},{1,0},{1,1}};
			for (int i=0; i<surCnt; i++) {
				int row = rand.nextInt(BoardView.CELL_CNT_EXPANDED);
				int col = rand.nextInt(BoardView.CELL_CNT_EXPANDED);
				costsMapExpanded[row][col] = maxCost - rand.nextInt(randNum);
				int num = costsMapExpanded[row][col] - 2;
				for (int j=0; j<surrounds.length; j++) {
					int surRow = row+surrounds[j][0];
					int surCol = col+surrounds[j][1];
					if (surRow >= 0 && surRow < BoardView.CELL_CNT_EXPANDED 
							&& surCol >= 0 && surCol < BoardView.CELL_CNT_EXPANDED
							&& costsMapExpanded[surRow][surCol] < num) {
						
						costsMapExpanded[surRow][surCol] = num;
					}
				}
			}
		}
		
		private void generateMaterialsMap() {
			materialsMapExpanded = new Material[BoardView.CELL_CNT_EXPANDED][BoardView.CELL_CNT_EXPANDED];
			for (int row=0; row<BoardView.CELL_CNT_EXPANDED; row++) {
				for (int col=0; col<BoardView.CELL_CNT_EXPANDED; col++) {
					materialsMapExpanded[row][col] = Material.NUM1;
				}
			}
			Random rand = new Random();
			int row, col, diffCol;
			Material nextM;
			for (int i=0; i<14; i++) {
				if (i%2 == 0) {
					nextM = Material.NUM2;
				} else {
					nextM = Material.NUM3;
				}
				
				switch (rand.nextInt(3)) {
				case 0:
					row = rand.nextInt(BoardView.CELL_CNT_EXPANDED);
					for (int j=0; j<BoardView.CELL_CNT_EXPANDED; j++) {
						materialsMapExpanded[row][j] = nextM;
					}
					break;
				case 1:
					col = rand.nextInt(BoardView.CELL_CNT_EXPANDED);
					for (int j=0; j<BoardView.CELL_CNT_EXPANDED; j++) {
						materialsMapExpanded[j][col] = nextM;
					}
					break;
				default:
					col = rand.nextInt(BoardView.CELL_CNT_EXPANDED);
					if (rand.nextBoolean()) {
						diffCol = 1;
					} else {
						diffCol = -1;
					}
					row = 0;
					while (row<BoardView.CELL_CNT_EXPANDED && col>=0 && col<BoardView.CELL_CNT_EXPANDED) {
						materialsMapExpanded[row][col] = nextM;
						row++;
						col += diffCol;
					}
					break;
				}
			}
			
			materialsMapNormal = new Material[BoardView.CELL_CNT_NORMAL][BoardView.CELL_CNT_NORMAL];
			int offset = (BoardView.CELL_CNT_EXPANDED - BoardView.CELL_CNT_NORMAL) / 2;
			for (int row2=0; row2<BoardView.CELL_CNT_NORMAL; row2++) {
				for (int col2=0; col2<BoardView.CELL_CNT_NORMAL; col2++) {
					materialsMapNormal[row2][col2] = materialsMapExpanded[row2+offset][col2+offset];
				}
			}
		}
		
		public void missionClear() {
			boardView.setIsLocked(true);
			handler.removeCallbacks(timeRunnable);
			
			StageScore stageScore = mission.getMissionClearPoint(stage);
			totalScore += stageScore.getScore();
			
			viewCtrl.showMissionClearModal(stageScore, new Runnable() {
				
				@Override
				public void run() {
					if (stage == MAX_STAGE) {
						gameClear();
					} else {
						gameReset(stage+1, false);
					}
				}
			});
			
			// 업적 달성
			if (stage == 1) {
				unlockAchievement(R.string.achievement_1);
			}
			if (totalScore >= 2000) {
				unlockAchievement(R.string.achievement_2);
			}
			if (stage == 5) {
				unlockAchievement(R.string.achievement_5);
			}
			if (stage == MAX_STAGE) {
				unlockAchievement(R.string.achievement_7);
			}
		}
		
		public void missionFail() {
			boardView.setIsLocked(true);
			handler.removeCallbacks(timeRunnable);
			
			viewCtrl.showMissionFailModal();
			updateBestScore();
		}
		
		public void onExpand() {
			if (boardView.getCostsMap() != null) {
				boardView.setCostsMap(costsMapExpanded);
			}
			if (boardView.getMaterialsMap() != null) {
				boardView.setMaterialsMap(materialsMapExpanded);
			}
		}
		
		public StageScore getMissionClearPoint(int stage) {
			int baseScore = 100;
			boolean hasCostMission = false;
			int remainCosts = 0;
			boolean hasMaterialMission = false;
			int remainMaterials = 0;
			boolean hasTimeMission = false;
			int remainTime = 0;
			int multiplier = 10;
			boolean isDouble = false;
			
			switch (stage) {
			case 2: 
				baseScore = 200;
				hasCostMission = true;
				remainCosts = cost;
				break;
			case 3: 
				baseScore = 300;
				hasMaterialMission = true;
				remainMaterials = material1Cnt + material2Cnt + material3Cnt;
				isDouble = isWinBySameMaterials();
				break;
			case 4: 
				baseScore = 400; 
				break;
			case 5: 
				baseScore = 500;
				hasTimeMission = true;
				remainTime = time;
				break;
			case 6: 
				baseScore = 600;
				hasCostMission = true;
				remainCosts = cost;
				hasTimeMission = true;
				remainTime = time;
				break;
			case 7: 
				baseScore = 700;
				hasMaterialMission = true;
				remainMaterials = material1Cnt + material2Cnt + material3Cnt;
				hasTimeMission = true;
				remainTime = time;
				isDouble = isWinBySameMaterials();
				break;
			case 8: 
				baseScore = 800;
				hasTimeMission = true;
				remainTime = time;
				multiplier = 100;
				break;
			case 9: 
				baseScore = 900;
				hasCostMission = true;
				remainCosts = cost;
				hasTimeMission = true;
				remainTime = time;
				multiplier = 100;
				break;
			case 10: 
				baseScore = 1000;
				hasMaterialMission = true;
				remainMaterials = material1Cnt + material2Cnt + material3Cnt;
				hasTimeMission = true;
				remainTime = time;
				multiplier = 100;
				break;
			}
			return new StageScore(baseScore, hasCostMission, remainCosts, hasMaterialMission, remainMaterials
								, hasTimeMission, remainTime, multiplier, isDouble);
		}
		
		/**
		 * 같은 재료로 연속된 다섯지역을 점령했는지 여부
		 * 재료미션이 있을때만 호출되어야 한다.
		 */
		private boolean isWinBySameMaterials() {
			OmokLastData lastData = oman.getLastData();
			Material[][] mat;
			if (boardView.isExpanded()) {
				mat = materialsMapExpanded;
			} else {
				mat = materialsMapNormal;
			}
			int row = lastData.getSrow();
			int col = lastData.getScol();
			Material m = mat[row][col];
			for (int i=0; i<4; i++) {
				row += lastData.getDiffRow();
				col += lastData.getDiffCol();
				if (m.equals(mat[row][col]) == false) {
					return false;
				}
			}
			return true;
		}
	}
	
	class ViewController {
		private TextView goBackToCountView;
		private TextView stageView, stageMissionView;
		private TextView costView, materialNum1, materialNum2, materialNum3, remainTimeView;
		private View costMask, materialNumMask, remainTimeMask;
		private View recommendHereView;
		
		public ViewController() {
			View.OnClickListener listener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					playButtonClick();
					
					switch (v.getId()) {
					case R.id.btn_go_back_to:
						goBackTo();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_GO_BACK);
						break;
						
					case R.id.btn_exit:
						showExitConfirm();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_CONFIRM_EXIT);
						break;
						
					case R.id.btn_close_recommend:
						showConfirmDialog();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_CLOSE_RECOMMEND);
						break;
					}
				}
			};
			
			findViewById(R.id.btn_go_back_to).setOnClickListener(listener);
			findViewById(R.id.btn_exit).setOnClickListener(listener);
			goBackToCountView = (TextView) findViewById(R.id.go_back_to_count);
			stageView = (TextView) findViewById(R.id.stage);
			stageMissionView = (TextView) findViewById(R.id.stage_mission);
			costView = (TextView) findViewById(R.id.cost);
			costMask = findViewById(R.id.cost_mask);
			materialNum1 = (TextView) findViewById(R.id.material_num_1);
			materialNum2 = (TextView) findViewById(R.id.material_num_2);
			materialNum3 = (TextView) findViewById(R.id.material_num_3);
			materialNumMask = findViewById(R.id.material_num_mask);
			remainTimeView = (TextView) findViewById(R.id.remain_time);
			remainTimeMask = findViewById(R.id.remain_time_mask);
			recommendHereView = findViewById(R.id.recommend_here);
			findViewById(R.id.btn_close_recommend).setOnClickListener(listener);
		}
		
		public void setStage(int stage) {
			stageView.setText(getString(R.string.title_stage_format, stage));
		}
		
		public void setStageMission(int stageMission) {
			stageMissionView.setText(stageMission);
		}
		
		public void setEnableCostMission(boolean enable) {
			costMask.setVisibility(enable ? View.GONE : View.VISIBLE);
		}
		
		public void readyRemainCost() {
			costView.setTextColor(0xffff5555);
			costView.setBackgroundResource(R.drawable.bg_number_flash);
		}
		
		public void setRemainCost(int cost) {
			if (cost >= 0) {
				costView.setText(""+cost);
			} else {
				costView.setText("0");
			}
			costView.setTextColor(0xffffffff);
			costView.setBackgroundColor(0x00000000);
		}
		
		public void setEnableMaterialMission(boolean enable) {
			materialNumMask.setVisibility(enable ? View.GONE : View.VISIBLE);
		}
		
		public void readyRemainMaterial1() {
			materialNum1.setTextColor(0xffff5555);
			materialNum1.setBackgroundResource(R.drawable.bg_number_flash);
		}
		
		public void setRemainMaterial1(int material1Cnt) {
			if (material1Cnt >= 0) {
				materialNum1.setText(""+material1Cnt);
			} else {
				materialNum1.setText("0");
			}
			materialNum1.setTextColor(0xffffffff);
			materialNum1.setBackgroundColor(0x00000000);
		}
		
		public void readyRemainMaterial2() {
			materialNum2.setTextColor(0xffff5555);
			materialNum2.setBackgroundResource(R.drawable.bg_number_flash);
		}
		
		public void setRemainMaterial2(int material2Cnt) {
			if (material2Cnt >= 0) {
				materialNum2.setText(""+material2Cnt);
			} else {
				materialNum2.setText("0");
			}
			materialNum2.setTextColor(0xffffffff);
			materialNum2.setBackgroundColor(0x00000000);
		}
		
		public void readyRemainMaterial3() {
			materialNum3.setTextColor(0xffff5555);
			materialNum3.setBackgroundResource(R.drawable.bg_number_flash);
		}
		
		public void setRemainMaterial3(int material3Cnt) {
			if (material3Cnt >= 0) {
				materialNum3.setText(""+material3Cnt);
			} else {
				materialNum3.setText("0");
			}
			materialNum3.setTextColor(0xffffffff);
			materialNum3.setBackgroundColor(0x00000000);
		}
		
		public void setEnableTimeMission(boolean enable) {
			remainTimeMask.setVisibility(enable ? View.GONE : View.VISIBLE);
		}
		
		public void setRemainTime(int time) {
			if (time >= 0) {
				remainTimeView.setText(""+time);
			} else {
				remainTimeView.setText("0");
			}
			if (time <= 30) {
				remainTimeView.setTextColor(0xffff5555);
				remainTimeView.setBackgroundResource(R.drawable.bg_number_flash);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						remainTimeView.setTextColor(0xffffffff);
						remainTimeView.setBackgroundColor(0x00000000);
					}
				}, 500);
			}
		}
		
		public void showMissionModal(int missionResId, final boolean showBonusMission) {
			getUfoAlertView().showMissionModal(stage, missionResId, new Runnable() {
				
				@Override
				public void run() {
					if (showBonusMission) {
						// 보너스 미션 보여주기
						getUfoAlertView().showCommentModal(R.string.title_bonus_mission, R.string.stage_material_sub_mission, new Runnable() {
							
							@Override
							public void run() {
								mission.beforeGameStart();  // 여기가 게임시작 직전
								getUfoAlertView().hideAnimate(null);
							}
						});
					} else {
						mission.beforeGameStart();  // 여기가 게임시작 직전
						getUfoAlertView().hideAnimate(new Runnable() {
							
							@Override
							public void run() {
								showHint();
							}
						});
					}
				}
			});
		}
		
		public void showCommentModal(int commentResId, Runnable nextRunnable) {
			getUfoAlertView().showCommentModal(0, commentResId, nextRunnable);
		}
		
		public void showMissionClearModal(StageScore stageScore, Runnable nextRunnable) {
			getUfoAlertView().showMissionClearModal(stage, stageScore, nextRunnable);
		}
		
		public void showMissionFailModal() {
			getUfoAlertView().showMissionFailModal(stage, totalScore, getMoveToMainRunnable());
		}
		
		public void showGameClearModal() {
			getUfoAlertView().showGameClearModal(totalScore, getMoveToMainRunnable());
		}
		
		private Runnable getMoveToMainRunnable() {
			return new Runnable() {
				
				@Override
				public void run() {
					getUfoAlertView().hideAnimate(new Runnable() {
						
						@Override
						public void run() {
							moveToMain();
						}
					});
				}
			};
		}
		
		public void showConfirmDialog() {
			Spanned message = Html.fromHtml(getString(R.string.text_confirm_close_message));
			getUfoAlertView().showConfirmDialog(message, new UfoAlertView.UfoButtonListener() {
				
				@Override
				public void onClick(int position) {
					switch (position) {
					case 0:
						hideHint();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_HIDE_HINT);
						break;
					case 1:
						/* do nothing */
						break;
					}
				}
			});
		}
		
		private void hideHint() {
			recommendHereView.setVisibility(View.GONE);
			boardView.hideHint();
			pref.setIsOnFirstStageHelper(false);
		}
		
		public void setGoBackToCount(int cnt) {
			goBackToCountView.setText(getString(R.string.text_go_back_to_count, cnt));
		}
	}

}
