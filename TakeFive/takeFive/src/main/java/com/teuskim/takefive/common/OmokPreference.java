package com.teuskim.takefive.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class OmokPreference {

	private static final String KEY_BEST_SCORE = "best_score";
	private static final String KEY_WINS_COUNT = "wins_count";
	private static final String KEY_IS_SIGNED_IN = "is_signed_in";
	private static final String KEY_IS_ON_BGM = "is_on_bgm";
	private static final String KEY_IS_ON_GAME_SOUND = "is_on_game_sound";
	private static final String KEY_IS_ON_INVITED_OK = "is_on_invited_popup";
	private static final String KEY_IS_ON_FIRST_STAGE_HELPER = "is_on_first_stage_helper";
	private static final String KEY_CLICK_YNS = "click_yns";
	private static final String KEY_IS_PREMIUM = "is_premium";
	private static final String KEY_SAYING_INDEX = "saying_index";
	private static final String KEY_SAYING_DAY = "saying_day";
	private static final String KEY_BEGINNING_STAGE_LIMIT = "beginning_stage_limit";
	
	private static OmokPreference instance;
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	
	private OmokPreference(Context context) {
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		editor = pref.edit();
	}
	
	public static OmokPreference getInstance(Context context) {
		if (instance == null) {
			instance = new OmokPreference(context);
		}
		return instance;
	}
	
	public void setBestScore(int score) {
		editor.putInt(KEY_BEST_SCORE, score);
		editor.commit();
	}
	
	public int getBestScore() {
		return pref.getInt(KEY_BEST_SCORE, 0);
	}
	
	public void setWinsCount(int winsCount) {
		editor.putInt(KEY_WINS_COUNT, winsCount);
		editor.commit();
	}
	
	public int getWinsCount() {
		return pref.getInt(KEY_WINS_COUNT, 0);
	}
	
	public void setIsSignedIn(boolean isSignedIn) {
		editor.putBoolean(KEY_IS_SIGNED_IN, isSignedIn);
		editor.commit();
	}
	
	public boolean isSignedIn() {
		return pref.getBoolean(KEY_IS_SIGNED_IN, false);
	}
	
	public void setIsOnBgm(boolean isOnBgm) {
		editor.putBoolean(KEY_IS_ON_BGM, isOnBgm);
		editor.commit();
	}
	
	public boolean isOnBgm() {
		return pref.getBoolean(KEY_IS_ON_BGM, true);
	}
	
	public void setIsOnGameSound(boolean isOnGameSound) {
		editor.putBoolean(KEY_IS_ON_GAME_SOUND, isOnGameSound);
		editor.commit();
	}
	
	public boolean isOnGameSound() {
		return pref.getBoolean(KEY_IS_ON_GAME_SOUND, true);
	}
	
	public void setIsOnInvitedOk(boolean isOnInvitedOk) {
		editor.putBoolean(KEY_IS_ON_INVITED_OK, isOnInvitedOk);
		editor.commit();
	}
	
	public boolean isOnInvitedOk() {
		return pref.getBoolean(KEY_IS_ON_INVITED_OK, true);
	}
	
	public void setIsOnFirstStageHelper(boolean isOnFirstStageHelper) {
		editor.putBoolean(KEY_IS_ON_FIRST_STAGE_HELPER, isOnFirstStageHelper);
		editor.commit();
	}
	
	public boolean isOnFirstStageHelper() {
		return pref.getBoolean(KEY_IS_ON_FIRST_STAGE_HELPER, true);
	}
	
	public void setClickYNS(String clickYNs) {
		editor.putString(KEY_CLICK_YNS, clickYNs);
		editor.commit();
	}
	
	public String getClickYNs() {
		return pref.getString(KEY_CLICK_YNS, ClickYNs.DEFAULT_YNS);
	}
	
	public void setIsPremium(boolean isPremium) {
		editor.putBoolean(KEY_IS_PREMIUM, isPremium);
		editor.commit();
	}
	
	public boolean isPremium() {
		return pref.getBoolean(KEY_IS_PREMIUM, false);
	}
	
	public void setSayingIndex(int sayingIndex) {
		editor.putInt(KEY_SAYING_INDEX, sayingIndex);
		editor.commit();
	}
	
	public int getSayingIndex() {
		return pref.getInt(KEY_SAYING_INDEX, 0);
	}
	
	public void setSayingDay(int sayingDay) {
		editor.putInt(KEY_SAYING_DAY, sayingDay);
		editor.commit();
	}
	
	public int getSayingDay() {
		return pref.getInt(KEY_SAYING_DAY, 0);
	}
	
	public void setBeginningStageLimit(int beginningStageLimit) {
		editor.putInt(KEY_BEGINNING_STAGE_LIMIT, beginningStageLimit);
		editor.commit();
	}
	
	public int getBeginningStageLimit() {
		return pref.getInt(KEY_BEGINNING_STAGE_LIMIT, 1);
	}
	
}