package com.teuskim.takefive.common;

import android.content.Context;

public class ClickYNs {

	public static final String DEFAULT_YNS = "NNNNNN";
	
	private static final int POSITION_MISSIONGAME = 0;
	private static final int POSITION_ACHIEVEMENT = 1;
	private static final int POSITION_RANKING = 2;
	private static final int POSITION_SETTINGS = 3;
	private static final int POSITION_SHARE = 4;
	private static final int POSITION_FRIENDGAME = 5;
	
	private OmokPreference pref;
	
	public ClickYNs(Context context) {
		pref = OmokPreference.getInstance(context);
	}
	
	private void clickButton(int position) {
		String yns = pref.getClickYNs();
		String result = yns.substring(0, position) + "Y" + yns.substring(position+1, yns.length());
		pref.setClickYNS(result);
	}
	
	public void clickMissionGame() {
		clickButton(POSITION_MISSIONGAME);
	}
	
	public void clickAchievement() {
		clickButton(POSITION_ACHIEVEMENT);
	}
	
	public void clickRanking() {
		clickButton(POSITION_RANKING);
	}
	
	public void clickSettings() {
		clickButton(POSITION_SETTINGS);
	}
	
	public void clickShare() {
		clickButton(POSITION_SHARE);
	}
	
	public void clickFriendGame() {
		clickButton(POSITION_FRIENDGAME);
	}
	
	private boolean didClickButton(int position) {
		String yns = pref.getClickYNs();
		return (yns.charAt(position) == 'Y');
	}
	
	public boolean didClickMissionGame() {
		return didClickButton(POSITION_MISSIONGAME);
	}
	
	public boolean didClickAchievement() {
		return didClickButton(POSITION_ACHIEVEMENT);
	}
	
	public boolean didClickRanking() {
		return didClickButton(POSITION_RANKING);
	}
	
	public boolean didClickSettings() {
		return didClickButton(POSITION_SETTINGS);
	}
	
	public boolean didClickShare() {
		return didClickButton(POSITION_SHARE);
	}
	
	public boolean didClickFriendGame() {
		return didClickButton(POSITION_FRIENDGAME);
	}
	
	public boolean didClickNone() {
		return (DEFAULT_YNS.equals(pref.getClickYNs()));
	}
}
