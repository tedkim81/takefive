package com.teuskim.takefive;

import java.util.Calendar;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.teuskim.takefive.common.ClickYNs;
import com.teuskim.takefive.common.MiscUtil;
import com.teuskim.takefive.common.OmokPreference;
import com.teuskim.takefive.iab.Purchase;
import com.teuskim.takefive.view.UfoAlertView;

public class MainActivity extends BaseGameActivity {
	
	private final int RC_UNUSED = 5001;
	
	private ViewController viewCtrl;
	private OmokPreference pref;
	private ClickYNs clickYNs;
	private Handler handler = new Handler();
	private String sayingForShare;
	private int beginningStageLimit;
	private int beginningStage;
	
	private enum MainMessageType {
		MISSION_GAME, ACHIEVEMENT, RANKING, SHARE, BUY_ITEM, SAYING
	}
	private MainMessageType mainMessageType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		viewCtrl = new ViewController();
		pref = OmokPreference.getInstance(getApplicationContext());
		clickYNs = new ClickYNs(getApplicationContext());
		initGoogle();
	}
	
	private void initGoogle() {
		if (pref.isSignedIn()) {
			signIn();
		}
	}
	
//	@Override
//	protected void afterIab(Purchase item) {
//		super.afterIab(item);
//
//		if (item != null) {
//			viewCtrl.showMainMessage(getMainMessage(false));
//		}
//	}

	@Override
	protected void onStart() {
		super.onStart();
		viewCtrl.setBestScore(pref.getBestScore());
		viewCtrl.setWinsCount(pref.getWinsCount());
		beginningStageLimit = pref.getBeginningStageLimit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		viewCtrl.adView.resume();
		viewCtrl.showMainMessage(getMainMessage(true));
	}

	@Override
	protected void onPause() {
		viewCtrl.adView.pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		viewCtrl.adView.destroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (getUfoAlertView().isShown() && UfoAlertView.AlertType.BEGIN_STAGE.equals(getUfoAlertView().getAlertType())) {
			getUfoAlertView().hideAnimate(null);
		} else {
			super.onBackPressed();
		}
	}

	private void startGame() {
		if (pref.getBeginningStageLimit() > 1) {
			getUfoAlertView().showBeginningStageChooser(new StageChooserAdapter(new Runnable() {
				
				@Override
				public void run() {
					Intent intent = new Intent(getApplicationContext(), GameActivity.class);
					intent.putExtra("beginning_stage", beginningStage);
					startActivity(intent);
				}
			}));
		} else {
			Intent intent = new Intent(getApplicationContext(), GameActivity.class);
			startActivity(intent);
		}
	}
	
	private void moveToLeaderboard() {
		if (pref.isSignedIn()) {
			Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
					.getAllLeaderboardsIntent()
					.addOnSuccessListener(new OnSuccessListener<Intent>() {
						@Override
						public void onSuccess(Intent intent) {
							startActivityForResult(intent, RC_UNUSED);
						}
					}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					// do nothing
				}
			});
		} else {
			signIn();
		}
	}
	
	private void submitScoreAtFirst() {
		if (pref.isSignedIn() == false) {
			int bestScore = pref.getBestScore();
			if (bestScore > 0) {
				Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
						.submitScore(getString(R.string.leaderboard_mission_mode), bestScore);
			}
		}
	}
	
	private void moveToSettings() {
		Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
		startActivity(intent);
	}
	
	private void moveToShare() {
		Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
		startActivity(intent);
	}
	
	private void moveToInfo() {
		Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
		startActivity(intent);
	}
	
	private void moveToAchievement() {
		if (pref.isSignedIn()) {
			Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
					.getAchievementsIntent()
					.addOnSuccessListener(new OnSuccessListener<Intent>() {
						@Override
						public void onSuccess(Intent intent) {
							startActivityForResult(intent, RC_UNUSED);
						}
					}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					// do nothing
				}
			});
		} else {
			signIn();
		}
	}
	
	private void moveToGameWithFriend() {
		if (pref.isSignedIn()) {
			startActivity(new Intent(getApplicationContext(), GameWithFriendActivity.class));
		} else {
			signIn();
		}
	}
	
	private Spanned getMainMessage(boolean isShownAd) {
		Spanned msg;

		ScrollView.LayoutParams params = (ScrollView.LayoutParams) viewCtrl.mainMessageView.getLayoutParams();
		params.gravity = Gravity.CENTER_VERTICAL;
		
		viewCtrl.adViewLayout.setVisibility(View.GONE);
		
		if (clickYNs.didClickNone()) {
			// 아무버튼도 누르지 않았을때, "안녕하세요, 미션오목입니다. 처음이시죠? 미션게임을 해보세요~!"
			msg = Html.fromHtml(getString(R.string.main_message_1));
			mainMessageType = MainMessageType.MISSION_GAME;
			
		} else if (clickYNs.didClickMissionGame() && clickYNs.didClickAchievement()==false) {
			// 미션게임버튼을 누른적 있고 업적버튼 누른적 없을때, "안녕하세요, 미션오목입니다. 이제 업적을 하나하나 수행해 보세요~!"
			msg = Html.fromHtml(getString(R.string.main_message_2));
			mainMessageType = MainMessageType.ACHIEVEMENT;
			
		} else if (clickYNs.didClickMissionGame() && clickYNs.didClickRanking()==false) {
			// 미션게임버튼을 누른적 있고 랭킹버튼을 누른적 없을때, "안녕하세요, 김태우님. 지금 점수가 몇등인지 궁금하지 않으세요? 랭킹을 확인해 보세요~!"
			msg = Html.fromHtml(getString(R.string.main_message_3));
			mainMessageType = MainMessageType.RANKING;
			
		} else if (clickYNs.didClickShare()==false) {
			// 공유버튼을 누른적 없을때, "안녕하세요, 감태우님. 친구들에게 미션오목을 공유하고 함께 즐겨보세요~!"
			msg = Html.fromHtml(getString(R.string.main_message_4));
			mainMessageType = MainMessageType.SHARE;
			
		} else if (pref.isPremium() || true) {  // TODO: 예전에 구현한 인앱빌링이 하위호환이 되지 않아서 일단 아이템 구매유도를 숨긴다.
			// 아이템 구매자이면 명언 보여주기
			msg = Html.fromHtml(getFamousSaying());
			params.gravity = Gravity.TOP;
			mainMessageType = MainMessageType.SAYING;
			
		} else if (isShownAd==false) {
			// 광고가 보이지 않는 경우, 아이템 구매유도 문구
			msg = Html.fromHtml(getString(R.string.main_message_6));
			mainMessageType = MainMessageType.BUY_ITEM;
			
		} else {
			// 기타 다른 경우, 아이템 구매유도 문구
			msg = Html.fromHtml(getString(R.string.main_message_5));
			viewCtrl.adViewLayout.setVisibility(View.VISIBLE);
			params.gravity = Gravity.TOP;
			mainMessageType = MainMessageType.BUY_ITEM;
		}
		viewCtrl.mainMessageView.setLayoutParams(params);
		
		return msg;
	}
	
	private String getFamousSaying() {
		String[] arr = getResources().getStringArray(R.array.sayings);
		int sayingIndex = pref.getSayingIndex();
		int sayingDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		if (sayingDay != pref.getSayingDay()) {
			sayingIndex++;
			pref.setSayingDay(sayingDay);
		}
		if (sayingIndex >= arr.length) {
			sayingIndex = 0;
		}
		pref.setSayingIndex(sayingIndex);
		sayingForShare = getString(R.string.title_today_saying_for_share)+"\n"+arr[sayingIndex];
		return getString(R.string.title_today_saying)+arr[sayingIndex];
	}
	
	private void onClickMainMessage() {
		switch (mainMessageType) {
		case MISSION_GAME:
			startGame();
			clickYNs.clickMissionGame();
			googleAnalyticsEvent(GA_ACTION_MAIN_MESSAGE_CLICK, GA_LABEL_MISSION_GAME);
			break;
			
		case ACHIEVEMENT:
			moveToAchievement();
			clickYNs.clickAchievement();
			googleAnalyticsEvent(GA_ACTION_MAIN_MESSAGE_CLICK, GA_LABEL_ACHIEVEMENT);
			break;
			
		case RANKING:
			moveToLeaderboard();
			clickYNs.clickRanking();
			googleAnalyticsEvent(GA_ACTION_MAIN_MESSAGE_CLICK, GA_LABEL_LEADERBOARD);
			break;
			
		case SHARE:
			moveToShare();
			clickYNs.clickShare();
			googleAnalyticsEvent(GA_ACTION_MAIN_MESSAGE_CLICK, GA_LABEL_SHARE);
			break;
			
		case BUY_ITEM:
//			openBuyItem();
			googleAnalyticsEvent(GA_ACTION_MAIN_MESSAGE_CLICK, GA_LABEL_BUY_ITEM);
			break;
			
		case SAYING:
			shareSaying();
			googleAnalyticsEvent(GA_ACTION_MAIN_MESSAGE_CLICK, GA_LABEL_SAYING);
			break;
		}
	}
	
	private void shareSaying() {
		if (TextUtils.isEmpty(sayingForShare)) {
			return;
		}
		sayingForShare = sayingForShare.replace("<br/>", "\n");
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_TEXT, sayingForShare);
		i.setType("text/plain");
		Intent i2 = Intent.createChooser(i, getString(R.string.title_saying_share_dialog));
		startActivity(i2);
	}
	
	
	class StageChooserAdapter extends BaseAdapter {
		
		private View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playButtonClick();
				beginningStage = ((Integer) v.getTag()) + 1;
				if (beginningStage <= beginningStageLimit) {
					getUfoAlertView().hideAnimate(nextRunnable);
					googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_BEGINNING_STAGE, (long)beginningStage);
				} else {
					showToast(R.string.text_cannot_beginning_stage);
				}
			}
		};
		
		private Runnable nextRunnable;
		
		private ColorStateList csl;
		
		public StageChooserAdapter(Runnable nextRunnable) {
			int[][] states = new int[][] {
				new int[] {android.R.attr.state_pressed},
				new int[] {}
			};
			int[] colors = new int[] {
					0x80766c63, 0xff766c63 
			};
			csl = new ColorStateList(states, colors);
			
			this.nextRunnable = nextRunnable;
		}

		@Override
		public int getCount() {
			return 10;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v;
			if (convertView == null) {
				v = new TextView(getApplicationContext());
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
				v.setTextSize(25);
				int padding = MiscUtil.convertDpToPx(15, getResources());
				v.setPadding(0, padding, 0, padding);
				v.setGravity(Gravity.CENTER_HORIZONTAL);
				v.setLayoutParams(params);
			} else {
				v = (TextView) convertView;
			}
			
			v.setText(getString(R.string.title_stage_format, (position+1)));
			v.setTag(position);
			v.setOnClickListener(listener);
			
			if (position+1 <= beginningStageLimit) {
				v.setTextColor(csl);
			} else {
				v.setTextColor(0x80766c63);
			}
			
			return v;
		}
		
	}
	
	class ViewController {
		
		private TextView bestScoreView, winsCountView;
		private TextView mainMessageView;
		private View adViewLayout;
		private AdView adView;
		
		public ViewController() {
			View.OnClickListener listener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					playButtonClick();
					
					switch (v.getId()) {
					case R.id.btn_start_mission_game:
						startGame();
						clickYNs.clickMissionGame();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_MISSION_GAME);
						break;
						
					case R.id.btn_achievement:
						moveToAchievement();
						clickYNs.clickAchievement();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_ACHIEVEMENT);
						break;
						
					case R.id.btn_game_with_friends:
						moveToGameWithFriend();
						clickYNs.clickFriendGame();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_MATCHUP);
						break;
						
					case R.id.btn_leader_board:
						moveToLeaderboard();
						clickYNs.clickRanking();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_LEADERBOARD);
						break;
						
					case R.id.btn_settings:
						moveToSettings();
						clickYNs.clickSettings();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_SETTINGS);
						break;
						
					case R.id.btn_share:
						moveToShare();
						clickYNs.clickShare();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_SHARE);
						break;
						
					case R.id.main_message:
						onClickMainMessage();
						break;
						
					case R.id.btn_info:
						moveToInfo();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_INFO);
						break;
						
					/*
					case R.id.btn_logout:
						signOut();
						pref.setIsSignedIn(false);
						break;
					*/
					}
				}
			};
			
			bestScoreView = (TextView) findViewById(R.id.best_score);
			winsCountView = (TextView) findViewById(R.id.wins_count);
			findViewById(R.id.btn_start_mission_game).setOnClickListener(listener);
			findViewById(R.id.btn_achievement).setOnClickListener(listener);
			findViewById(R.id.btn_game_with_friends).setOnClickListener(listener);
			findViewById(R.id.btn_leader_board).setOnClickListener(listener);
			findViewById(R.id.btn_settings).setOnClickListener(listener);
			findViewById(R.id.btn_share).setOnClickListener(listener);
			findViewById(R.id.btn_info).setOnClickListener(listener);
			
			mainMessageView = (TextView) findViewById(R.id.main_message);
			mainMessageView.setOnClickListener(listener);
			initAdView();
		}
		
		private void initAdView() {
			adViewLayout = findViewById(R.id.ad_view_layout);
			adView = (AdView) findViewById(R.id.ad_view);
			adView.setAdListener(new AdListener() {

				@Override
			    public void onAdLoaded() {}

			    @Override
			    public void onAdFailedToLoad(int errorCode) {
			    	adViewLayout.setVisibility(View.GONE);
			    	showMainMessage(getMainMessage(false));
			    }

			    @Override
			    public void onAdOpened() {}

			    @Override
			    public void onAdClosed() {}

			    @Override
			    public void onAdLeftApplication() {}
			});
			adView.loadAd(new AdRequest.Builder().build());
			
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					TextView adTextView = (TextView) findViewById(R.id.ad_text);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) adTextView.getLayoutParams();
					lp.height = adView.getHeight();
					adTextView.setLayoutParams(lp);
				}
			}, 500);
		}
		
		public void setBestScore(int score) {
			bestScoreView.setText(getString(R.string.text_score_format, score));
		}
		
		public void setWinsCount(int winsCount) {
			winsCountView.setText(getString(R.string.text_wins_count_format, winsCount));
		}
		
		public void showMainMessage(Spanned msg) {
			mainMessageView.setText(msg);
		}
		
	}

}
