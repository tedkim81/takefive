package com.teuskim.takefive.common;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;

import com.teuskim.takefive.R;

public class BgmManager {

	private static BgmManager instance;
	private MediaPlayer mediaPlayer;
	private OmokPreference pref;
	private Context context;
	private int currentType;
	
	private BgmManager(Context context) {
		this.context = context;
		mediaPlayer = new MediaPlayer();
		pref = OmokPreference.getInstance(context);
	}
	
	public static BgmManager getInstance(Context context) {
		if (instance == null) {
			instance = new BgmManager(context);
		}
		return instance;
	}
	
	public void startForMenu() {
		start(1);
	}
	
	public void startForGame() {
		start(2);
	}
	
	private void start(int type) {
		if (pref.isOnBgm() == false || type == 0) {
			return;
		}
		
		if (currentType != type) {
			currentType = type;
			
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			new AsyncTask<Integer, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Integer... params) {
					try {
						int fileResId;
						if (params[0] == 2) {
							fileResId = R.raw.omok_bgm_game;
						} else {
							fileResId = R.raw.omok_bgm_menu;
						}
						mediaPlayer.reset();
						mediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + fileResId));
						mediaPlayer.prepare();
						mediaPlayer.setLooping(true);
					} catch (Exception e) {
						Log.test("bgm", e);
						return false;
					}
					return true;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					if (result) {
						mediaPlayer.start();
					}
				}
				
			}.execute(type);
		} else {
			if (mediaPlayer.isPlaying() == false) {
				mediaPlayer.start();
			}
		}
	}
	
	public void pause() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}
}
