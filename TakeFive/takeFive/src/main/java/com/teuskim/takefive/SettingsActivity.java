package com.teuskim.takefive;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.teuskim.takefive.common.OmokPreference;
import com.teuskim.takefive.iab.Purchase;

public class SettingsActivity extends BaseGameActivity {
	
	private ViewControllaer viewCtrl;
	private OmokPreference pref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		viewCtrl = new ViewControllaer();
		viewCtrl.init();
		pref = OmokPreference.getInstance(getApplicationContext());
		initStates();
	}
	
	private void initStates() {
		viewCtrl.setBgm(pref.isOnBgm());
		viewCtrl.setGameSound(pref.isOnGameSound());
		viewCtrl.setInvitedOk(pref.isOnInvitedOk());
		viewCtrl.setFirstStageHelper(pref.isOnFirstStageHelper());
	}
	
//	@Override
//	protected void afterIab(Purchase item) {
//		super.afterIab(item);
//
//		if (item == null) {
//			viewCtrl.showItemView();
//		} else {
//			viewCtrl.hideItemView();
//		}
//	}

	private void toggleBgm() {
		boolean isOn = !viewCtrl.bgmOn.isShown();
		viewCtrl.setBgm(isOn);
		pref.setIsOnBgm(isOn);
		if (isOn) {
			startBgmForMenu();
			googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_BGM_ON);
		} else {
			pauseBgm();
			googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_BGM_OFF);
		}
	}

	private void toggleGameSound() {
		boolean isOn = !viewCtrl.gameSoundOn.isShown();
		viewCtrl.setGameSound(isOn);
		pref.setIsOnGameSound(isOn);
		googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, isOn ? GA_LABEL_GAME_SOUND_ON : GA_LABEL_GAME_SOUND_OFF);
	}

	private void toggleInvitedOk() {
		boolean isOn = !viewCtrl.invitedOkOn.isShown();
		viewCtrl.setInvitedOk(isOn);
		pref.setIsOnInvitedOk(isOn);
		googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, isOn ? GA_LABEL_INVITED_OK_ON : GA_LABEL_INVITED_OK_OFF);
	}
	
	private void toggleFirstStageHelper() {
		boolean isOn = !viewCtrl.firstStageHelperOn.isShown();
		viewCtrl.setFirstStageHelper(isOn);
		pref.setIsOnFirstStageHelper(isOn);
		googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, isOn ? GA_LABEL_FIRST_STAGE_HELPER_ON : GA_LABEL_FIRST_STAGE_HELPER_OFF);
	}
	

	class ViewControllaer {
		private View bgmOn, bgmOff;
		private View gameSoundOn, gameSoundOff;
		private View invitedOkOn, invitedOkOff;
		private View firstStageHelperOn, firstStageHelperOff;
		private TextView textBuyItemView;
		private View btnBuyItem;
		
		public void init() {
			View.OnClickListener listener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					playButtonClick();
					
					switch (v.getId()) {
					case R.id.btn_bgm:
						toggleBgm();
						break;
					case R.id.btn_game_sound:
						toggleGameSound();
						break;
					case R.id.btn_invited_ok:
						toggleInvitedOk();
						break;
					case R.id.btn_first_stage_helper:
						toggleFirstStageHelper();
						break;
					case R.id.btn_buy_item:
//						openBuyItem();
						googleAnalyticsEvent(GA_ACTION_BUTTON_CLICK, GA_LABEL_BUY_ITEM);
						break;
					}
				}
			};
			findViewById(R.id.btn_bgm).setOnClickListener(listener);
			bgmOn = findViewById(R.id.bgm_on);
			bgmOff = findViewById(R.id.bgm_off);
			findViewById(R.id.btn_game_sound).setOnClickListener(listener);
			gameSoundOn = findViewById(R.id.game_sound_on);
			gameSoundOff = findViewById(R.id.game_sound_off);
			findViewById(R.id.btn_invited_ok).setOnClickListener(listener);
			invitedOkOn = findViewById(R.id.invited_ok_on);
			invitedOkOff = findViewById(R.id.invited_ok_off);
			findViewById(R.id.btn_first_stage_helper).setOnClickListener(listener);
			firstStageHelperOn = findViewById(R.id.first_stage_helper_on);
			firstStageHelperOff = findViewById(R.id.first_stage_helper_off);
			
			textBuyItemView = (TextView) findViewById(R.id.text_buy_item);
			textBuyItemView.setText(Html.fromHtml(getString(R.string.main_message_6)));
			btnBuyItem = findViewById(R.id.btn_buy_item);
			btnBuyItem.setOnClickListener(listener);
			hideItemView();
		}
		
		public void setBgm(boolean isOn) {
			if (isOn) {
				bgmOn.setVisibility(View.VISIBLE);
				bgmOff.setVisibility(View.GONE);
			} else {
				bgmOn.setVisibility(View.GONE);
				bgmOff.setVisibility(View.VISIBLE);
			}
		}
		
		public void setGameSound(boolean isOn) {
			if (isOn) {
				gameSoundOn.setVisibility(View.VISIBLE);
				gameSoundOff.setVisibility(View.GONE);
			} else {
				gameSoundOn.setVisibility(View.GONE);
				gameSoundOff.setVisibility(View.VISIBLE);
			}
		}
		
		public void setInvitedOk(boolean isOn) {
			if (isOn) {
				invitedOkOn.setVisibility(View.VISIBLE);
				invitedOkOff.setVisibility(View.GONE);
			} else {
				invitedOkOn.setVisibility(View.GONE);
				invitedOkOff.setVisibility(View.VISIBLE);
			}
		}
		
		public void setFirstStageHelper(boolean isOn) {
			if (isOn) {
				firstStageHelperOn.setVisibility(View.VISIBLE);
				firstStageHelperOff.setVisibility(View.GONE);
			} else {
				firstStageHelperOn.setVisibility(View.GONE);
				firstStageHelperOff.setVisibility(View.VISIBLE);
			}
		}
		
		public void hideItemView() {
			textBuyItemView.setVisibility(View.GONE);
			btnBuyItem.setVisibility(View.GONE);
		}
		
		public void showItemView() {
			textBuyItemView.setVisibility(View.VISIBLE);
			btnBuyItem.setVisibility(View.VISIBLE);
		}
	}

}
