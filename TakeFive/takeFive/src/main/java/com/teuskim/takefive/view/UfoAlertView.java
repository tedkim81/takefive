package com.teuskim.takefive.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.teuskim.takefive.R;
import com.teuskim.takefive.common.GameSoundManager;
import com.teuskim.takefive.common.StageScore;

public class UfoAlertView extends RelativeLayout {
	
	private Context context;
	private View ufoExTouchPane;
	private View ufoLayout;
	
	// 알림
	private View noticeLayout;
	private TextView noticeTitleView, noticeMessageView, noticePointTitleView, noticePointView;
	
	// 점수
	private View scoreLayout;
	private TextView scoreTitleView;
	private TextView baseScoreView;
	private View costMissionScoreLayout, materialMissionScoreLayout, timeMissionScoreLayout;
	private TextView costMissionScoreView, materialMissionScoreView, timeMissionScoreView;
	private View bonusMissionLayout;
	private TextView bonusMissionMultiplierView;
	private TextView getScoreView;
	
	// 친구와대결에서 일시정지
	private View pause2Layout;
	
	// 초대받았을때
	private View invitedLayout;
	private ImageView inviterImgView;
	private TextView inviterNameView;
	
	// 컨펌(확인/취소버튼)
	private View confirmLayout;
	private TextView confirmMessageView;
	
	// 리스트뷰
	private View listLayout;
	private ListView listView;
	
	private Runnable clickRunnable;
	private Handler handler = new Handler();
	private int ufoSize;
	private GameSoundManager gsManager;
	
	public static enum AlertType {
		NOTICE, SCORE, MISSION_END, PAUSE2, INVITED, CONFIRM, BEGIN_STAGE
	}
	private AlertType alertType;
	
	private static final int DURATION_ANIM = 200;
	
	public static interface UfoButtonListener {
		public void onClick(int position);
	}
	private UfoButtonListener ufoButtonListener;

	public UfoAlertView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public UfoAlertView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public UfoAlertView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.ufo_alert_view, this);
		setVisibility(GONE);
		
		ufoExTouchPane = findViewById(R.id.ufo_ex_touch_pane);
		ufoLayout = findViewById(R.id.ufo_layout);
		ufoLayout.setVisibility(GONE);
		ufoLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gsManager.playButtonClick();
				
				if (clickRunnable != null) {
					clickRunnable.run();
				}
			}
		});
		noticeLayout = findViewById(R.id.notice_layout);
		noticeTitleView = (TextView) findViewById(R.id.notice_title);
		noticeMessageView = (TextView) findViewById(R.id.notice_message);
		noticePointTitleView = (TextView) findViewById(R.id.notice_point_title);
		noticePointView = (TextView) findViewById(R.id.notice_point);
		
		scoreLayout = findViewById(R.id.score_layout);
		scoreTitleView = (TextView) findViewById(R.id.score_title);
		baseScoreView = (TextView) findViewById(R.id.base_score);
		costMissionScoreLayout = findViewById(R.id.cost_mission_score_layout);
		costMissionScoreView = (TextView) findViewById(R.id.cost_mission_score);
		materialMissionScoreLayout = findViewById(R.id.material_mission_score_layout);
		materialMissionScoreView = (TextView) findViewById(R.id.material_mission_score);
		timeMissionScoreLayout = findViewById(R.id.time_mission_score_layout);
		timeMissionScoreView = (TextView) findViewById(R.id.time_mission_score);
		bonusMissionLayout = findViewById(R.id.bonus_mission_layout);
		bonusMissionMultiplierView = (TextView) findViewById(R.id.bonus_mission_multiplier);
		getScoreView = (TextView) findViewById(R.id.get_score);
		
		OnClickListener ufoListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (ufoButtonListener == null) {
					return;
				}
				
				gsManager.playButtonClick();
				
				switch (v.getId()) {
				case R.id.btn_continue2:	hideAnimateFromButton(0);	break;
				case R.id.btn_withdraw:		hideAnimateFromButton(1);	break;
				case R.id.btn_accept:		hideAnimateFromButton(0);	break;
				case R.id.btn_decline:		hideAnimateFromButton(1);	break;
				case R.id.btn_ok:			hideAnimateFromButton(0);	break;
				case R.id.btn_cancel:		hideAnimateFromButton(1);	break;
				}
			}
		};
		
		pause2Layout = findViewById(R.id.pause2_layout);
		findViewById(R.id.btn_continue2).setOnClickListener(ufoListener);
		findViewById(R.id.btn_withdraw).setOnClickListener(ufoListener);
		
		invitedLayout = findViewById(R.id.invited_layout);
		inviterImgView = (ImageView) findViewById(R.id.inviter_img);
		inviterNameView = (TextView) findViewById(R.id.inviter_name);
		findViewById(R.id.btn_accept).setOnClickListener(ufoListener);
		findViewById(R.id.btn_decline).setOnClickListener(ufoListener);
		
		confirmLayout = findViewById(R.id.confirm_layout);
		confirmMessageView = (TextView) findViewById(R.id.confirm_message);
		findViewById(R.id.btn_ok).setOnClickListener(ufoListener);
		findViewById(R.id.btn_cancel).setOnClickListener(ufoListener);
		
		listLayout = findViewById(R.id.list_layout);
		listView = (ListView) findViewById(R.id.list_view);
		
		gsManager = GameSoundManager.getInstance(context);
	}
	
	private void showAnimate(AlertType alertType) {
		this.alertType = alertType;
		setVisibility(VISIBLE);
		if (ufoLayout.getVisibility() == GONE) {
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					animateMoveIn();
				}
			}, 300);
		} else {
			animateScaleDown(new Runnable() {
				
				@Override
				public void run() {
					animateScaleUp();
				}
			});
		}
	}
	
	public void hideAnimate(final Runnable nextRunnable) {
		animateScaleDown(new Runnable() {
			
			@Override
			public void run() {
				animateMoveOut(nextRunnable);
			}
		});
	}
	
	public void hideAnimateFromButton(final int ufoButtonPosition) {
		hideAnimate(new Runnable() {
			
			@Override
			public void run() {
				ufoButtonListener.onClick(ufoButtonPosition);
			}
		});
	}
	
	private void hideAllLayouts() {
		noticeLayout.setVisibility(GONE);
		scoreLayout.setVisibility(GONE);
		pause2Layout.setVisibility(GONE);
		invitedLayout.setVisibility(GONE);
		confirmLayout.setVisibility(GONE);
		listLayout.setVisibility(GONE);
	}
	
	private void showSelectedLayout() {
		switch(alertType) {
		case NOTICE:		noticeLayout.setVisibility(VISIBLE);	break;
		case MISSION_END:	noticeLayout.setVisibility(VISIBLE);	break;
		case SCORE:			scoreLayout.setVisibility(VISIBLE);		break;
		case INVITED:		invitedLayout.setVisibility(VISIBLE);	break;
		case PAUSE2:		pause2Layout.setVisibility(VISIBLE);	break;
		case CONFIRM:		confirmLayout.setVisibility(VISIBLE);	break;
		case BEGIN_STAGE:	listLayout.setVisibility(VISIBLE);		break;
		
		default:			noticeLayout.setVisibility(VISIBLE);	break;
		}
		
		if (AlertType.SCORE.equals(alertType) == false 
				&& AlertType.MISSION_END.equals(alertType) == false) {
			ufoExTouchPane.setVisibility(GONE);
		}
	}
	
	private void animateMoveIn() {
		ufoLayout.setVisibility(VISIBLE);
		hideAllLayouts();
		
		TranslateAnimation anim = new TranslateAnimation((int)(-getWidth()/1.8), 0, 0, 0);
		anim.setDuration(DURATION_ANIM);
		anim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				ufoSize = ufoLayout.getWidth();
				animateScaleUp();
			}
		});
		ufoLayout.startAnimation(anim);
		
		gsManager.playUfoMove();
	}
	
	private void animateMoveOut(final Runnable nextRunnable) {
		TranslateAnimation anim = new TranslateAnimation(0, (int)(-getWidth()/1.8), 0, 0);
		anim.setDuration(DURATION_ANIM);
		anim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				ufoLayout.setVisibility(GONE);
				setVisibility(GONE);
				if (nextRunnable != null) {
					nextRunnable.run();
				}
			}
		});
		ufoLayout.startAnimation(anim);
		
		gsManager.playUfoMove();
	}
	
	private void animateScaleUp() {
		ufoLayout.setVisibility(VISIBLE);
		hideAllLayouts();
		
		ValueAnimator anim = ObjectAnimator.ofInt(this, "ufoSize", ufoSize, (int)(getWidth()*0.8));
		anim.setDuration(DURATION_ANIM);
		anim.addListener(new Animator.AnimatorListener() {
			
			@Override public void onAnimationStart(Animator animation) {}
			@Override public void onAnimationRepeat(Animator animation) {}
			@Override public void onAnimationCancel(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				showSelectedLayout();
			}
		});
		anim.start();
	}
	
	private void animateScaleDown(final Runnable nextRunnable) {
		ufoLayout.setVisibility(VISIBLE);
		hideAllLayouts();
		
		ValueAnimator anim = ObjectAnimator.ofInt(this, "ufoSize", (int)(getWidth()*0.8), ufoSize);
		anim.setDuration(DURATION_ANIM);
		anim.addListener(new Animator.AnimatorListener() {
			
			@Override public void onAnimationStart(Animator animation) {}
			@Override public void onAnimationRepeat(Animator animation) {}
			@Override public void onAnimationCancel(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				nextRunnable.run();
			}
		});
		anim.start();
	}
	
	public void setUfoSize(int size) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ufoLayout.getLayoutParams();
		lp.width = size;
		lp.height = size;
		ufoLayout.setLayoutParams(lp);
	}
	
	public void showMissionModal(int stage, int missionResId, final Runnable clickRunnable) {
		noticeTitleView.setText(context.getString(R.string.title_notice_mission_format, stage));
		noticeMessageView.setVisibility(VISIBLE);
		noticeMessageView.setText(missionResId);
		noticePointTitleView.setVisibility(GONE);
		noticePointView.setVisibility(GONE);
		this.clickRunnable = clickRunnable;
		
		showAnimate(AlertType.NOTICE);
	}
	
	public void showCommentModal(int titleResId, int commentResId, final Runnable clickRunnable) {
		if (titleResId == 0) {
			noticeTitleView.setText(R.string.title_notice_comment);
		} else {
			noticeTitleView.setText(titleResId);
		}
		noticeMessageView.setVisibility(VISIBLE);
		noticeMessageView.setText(commentResId);
		noticePointTitleView.setVisibility(GONE);
		noticePointView.setVisibility(GONE);
		this.clickRunnable = clickRunnable;
		
		showAnimate(AlertType.NOTICE);
	}
	
	public void showMissionClearModal(int stage, StageScore stageScore, final Runnable clickRunnable) {
		scoreTitleView.setText(context.getString(R.string.title_notice_mission_clear_format, stage));
		baseScoreView.setText(""+stageScore.getBaseScore());
		if (stageScore.hasCostMission()) {
			costMissionScoreLayout.setVisibility(VISIBLE);
			costMissionScoreView.setText(context.getString(R.string.text_mission_score_format, stageScore.getRemainCosts()+" X "+stageScore.getMultiplier()));
		} else {
			costMissionScoreLayout.setVisibility(GONE);
		}
		if (stageScore.hasMaterialMission()) {
			materialMissionScoreLayout.setVisibility(VISIBLE);
			materialMissionScoreView.setText(context.getString(R.string.text_mission_score_format, stageScore.getRemainMaterials()+" X "+stageScore.getMultiplier()));
		} else {
			materialMissionScoreLayout.setVisibility(GONE);
		}
		if (stageScore.hasTimeMission()) {
			timeMissionScoreLayout.setVisibility(VISIBLE);
			timeMissionScoreView.setText(context.getString(R.string.text_mission_score_format, stageScore.getRemainTime()+" X "+stageScore.getMultiplier()));
		} else {
			timeMissionScoreLayout.setVisibility(GONE);
		}
		if (stageScore.hasMaterialMission()) {
			bonusMissionLayout.setVisibility(VISIBLE);
			if (stageScore.isDouble()) {
				bonusMissionMultiplierView.setText(R.string.text_bonus_mission_success);
			} else {
				bonusMissionMultiplierView.setText(R.string.text_bonus_mission_fail);
			}
		} else {
			bonusMissionLayout.setVisibility(GONE);
		}
		getScoreView.setText(context.getString(R.string.text_score_format, stageScore.getScore()));
		
		this.clickRunnable = clickRunnable;
		
		showAnimate(AlertType.SCORE);
		
		setExTouchListener();
	}
	
	private void setExTouchListener() {
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				ufoExTouchPane.setVisibility(VISIBLE);
				ufoExTouchPane.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							setVisibility(View.INVISIBLE);
							break;
							
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
							setVisibility(View.VISIBLE);
							break;
						}
						return true;
					}
				});
			}
		}, 500);
	}
	
	public void showMissionFailModal(int stage, int totalScore, final Runnable clickRunnable) {
		noticeTitleView.setText(context.getString(R.string.title_notice_mission_fail_format, stage));
		noticeMessageView.setVisibility(GONE);
		noticePointTitleView.setVisibility(VISIBLE);
		noticePointTitleView.setText(R.string.text_final_score);
		noticePointView.setVisibility(VISIBLE);
		noticePointView.setText(context.getString(R.string.text_score_format, totalScore));
		this.clickRunnable = clickRunnable;
		
		showAnimate(AlertType.MISSION_END);
		
		setExTouchListener();
	}
	
	public void showGameClearModal(int totalScore, final Runnable clickRunnable) {
		noticeTitleView.setText(R.string.title_notice_game_clear);
		noticeMessageView.setVisibility(GONE);
		noticePointTitleView.setVisibility(VISIBLE);
		noticePointTitleView.setText(R.string.text_final_score);
		noticePointView.setVisibility(VISIBLE);
		noticePointView.setText(context.getString(R.string.text_score_format, totalScore));
		this.clickRunnable = clickRunnable;
		
		showAnimate(AlertType.MISSION_END);
		
		setExTouchListener();
	}
	
	public void showGameOverModal(boolean win, String message, int winCount, final Runnable clickRunnable) {
		noticeTitleView.setText(message);
		if (win) {
			noticeMessageView.setVisibility(GONE);
			noticePointTitleView.setVisibility(VISIBLE);
			noticePointTitleView.setText(R.string.text_wins_count);
			noticePointView.setVisibility(VISIBLE);
			noticePointView.setText(context.getString(R.string.text_wins_count_format, winCount));
		} else {
			noticeMessageView.setVisibility(VISIBLE);
			noticeMessageView.setText(R.string.text_lose_friend_2);
			noticePointTitleView.setVisibility(GONE);
			noticePointView.setVisibility(GONE);
		}
		this.clickRunnable = clickRunnable;
		
		showAnimate(AlertType.MISSION_END);
		
		setExTouchListener();
	}
	
	public void showPause2Dialog(UfoButtonListener l) {
		this.ufoButtonListener = l;
		alertType = AlertType.PAUSE2;
		showAnimate(AlertType.PAUSE2);
	}
	
	public void showInvitedDialog(Uri imgUri, String name, UfoButtonListener l) {
		this.ufoButtonListener = l;
		alertType = AlertType.INVITED;
		
		ImageManager im = ImageManager.create(getContext());
		if (imgUri != null) {
			im.loadImage(inviterImgView, imgUri, R.drawable.default_profile_img);
		} else {
			inviterImgView.setImageResource(R.drawable.default_profile_img);
		}
		
		inviterNameView.setText(name);
		
		showAnimate(AlertType.INVITED);
	}
	
	public void showConfirmDialog(Spanned message, UfoButtonListener l) {
		this.ufoButtonListener = l;
		alertType = AlertType.CONFIRM;
		confirmMessageView.setText(message);
		showAnimate(AlertType.CONFIRM);
	}
	
	public void showBeginningStageChooser(BaseAdapter adapter) {
		this.ufoButtonListener = null;
		alertType = AlertType.BEGIN_STAGE;
		
		listView.setAdapter(adapter);
		
		showAnimate(AlertType.BEGIN_STAGE);
	}
	
	public AlertType getAlertType() {
		return alertType;
	}
	
}
