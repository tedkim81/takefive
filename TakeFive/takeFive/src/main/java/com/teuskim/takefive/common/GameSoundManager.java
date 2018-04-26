package com.teuskim.takefive.common;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.teuskim.takefive.R;

public class GameSoundManager {

	private static GameSoundManager instance;
	private Context context;
	private SoundPool soundPool;
	private AudioManager audioManager;
	private OmokPreference pref;
	
	private int idButtonClick, idBoardTouch, idUfoMove, idCrash, idMissionClear, idMissionFail;
	
	private GameSoundManager(Context context) {
		this.context = context;
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		pref = OmokPreference.getInstance(context);
		loadSounds();
	}
	
	public static GameSoundManager getInstance(Context context) {
		if (instance == null) {
			instance = new GameSoundManager(context);
		}
		return instance;
	}
	
	private void loadSounds() {
		idButtonClick = soundPool.load(context, R.raw.sound_button_click, 1);
		idBoardTouch = soundPool.load(context, R.raw.sound_board_touch, 1);
		idUfoMove = soundPool.load(context, R.raw.sound_ufo_move, 1);
		idCrash = soundPool.load(context, R.raw.sound_crush, 1);
		idMissionClear = soundPool.load(context, R.raw.sound_mission_clear, 1);
		idMissionFail = soundPool.load(context, R.raw.sound_mission_fail, 1);
	}
	
	private void playSound(int id) {
		if (pref.isOnGameSound() == false) {
			return;
		}
		int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundPool.play(id, streamVolume, streamVolume, 1, 0, 1f);
	}
	
	public void playButtonClick() {
		playSound(idButtonClick);
	}
	
	public void playBoardTouch() {
		playSound(idBoardTouch);
	}
	
	public void playUfoMove() {
		playSound(idUfoMove);
	}
	
	public void playCrash() {
		playSound(idCrash);
	}
	
	public void playMissionClear() {
		playSound(idMissionClear);
	}
	
	public void playMissionFail() {
		playSound(idMissionFail);
	}
}
