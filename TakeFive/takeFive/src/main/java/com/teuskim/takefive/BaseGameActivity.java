package com.teuskim.takefive;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.teuskim.takefive.common.BgmManager;
import com.teuskim.takefive.common.GameSoundManager;
import com.teuskim.takefive.common.IGoogleAnalytics;
import com.teuskim.takefive.common.OmokPreference;
//import com.teuskim.takefive.iab.IabHelper;
//import com.teuskim.takefive.iab.IabResult;
//import com.teuskim.takefive.iab.Inventory;
//import com.teuskim.takefive.iab.Purchase;
import com.teuskim.takefive.view.UfoAlertView;
import com.teuskim.takefive.view.UfoAlertView.AlertType;

import java.util.List;

public class BaseGameActivity extends FragmentActivity implements IGoogleAnalytics {
	
//	protected static final String PREMIUM_ITEM = "android.test.purchased";  // for test
	protected static final String PREMIUM_ITEM = "premium_item";
	
	private static final int RC_SIGN_IN = 101;
	private static final int RC_BUY_ITEM = 102;

	private BgmManager bgmManager;
	private OmokPreference pref;
	private GameSoundManager gsManager;
//	private IabHelper iabHelper;
	private boolean inGame;
	private GoogleSignInClient googleSignInClient;
	
	private UfoAlertView ufoAlertView;
	private View loadingView;

	private InvitationCallback invitationCallback = new InvitationCallback() {
		@Override
		public void onInvitationReceived(@NonNull Invitation invitation) {
			if (pref.isOnInvitedOk()) {
				// 초대화면 출력
				recvInvitation = invitation;
				showInvitationDialog();
			} else {
				// 바로 거절
				declineInvitation(invitation);
			}
		}

		@Override
		public void onInvitationRemoved(@NonNull String invId) {
			if (getInvitation() != null && getInvitation().getInvitationId().equals(invId)) {
				clearInvitation();
			}
		}
	};
	
	protected void clearInvitation() {
		recvInvitation = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		bgmManager = BgmManager.getInstance(getApplicationContext());
		pref = OmokPreference.getInstance(getApplicationContext());
		gsManager = GameSoundManager.getInstance(getApplicationContext());
		googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
		
//		initIab();  // TODO: 인앱빌링을 너무 예전에 구현해서 현재는 하위호환이 안되어 다시 구현해야 한다.
	}
	
//	private void initIab() {
//		String pk = "Fill in pk";
//		iabHelper = new IabHelper(this, pk);
//		iabHelper.enableDebugLogging(false);
//		iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//
//			@Override
//			public void onIabSetupFinished(IabResult result) {
//				if (result.isFailure()) {
//					return;
//				}
//				iabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
//
//					@Override
//					public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//						if (result.isFailure()) {
//							return;
//						}
//						Purchase item = inv.getPurchase(PREMIUM_ITEM);
//						afterIab(item);
//
//						// 아이템 구매 취소 테스트용
//						/*
//						iabHelper.consumeAsync(inv.getPurchase(PREMIUM_ITEM), new OnConsumeFinishedListener() {
//							@Override public void onConsumeFinished(Purchase purchase, IabResult result) {}
//						});
//						*/
//					}
//				});
//			}
//		});
//	}
	
//	protected void afterIab(Purchase item) {
//		if (item != null) {
//			pref.setIsPremium(true);
//		}
//	}
	
//	protected void openBuyItem() {
//		showLoading();
//		iabHelper.launchPurchaseFlow(this, PREMIUM_ITEM, RC_BUY_ITEM, new IabHelper.OnIabPurchaseFinishedListener() {
//
//			@Override
//			public void onIabPurchaseFinished(IabResult result, Purchase info) {
//				hideLoading();
//				if (result.isSuccess()) {
//					showToast(R.string.text_success_buy_item);
//					afterIab(info);
//				} else if (result.getResponse() == -1005) {
//					showToast(R.string.text_cancel_buy_item);
//				} else {
//					showToast(R.string.text_fail_buy_item);
//				}
//			}
//		});
//	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.base_game_activity);
		
		RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
		LayoutInflater.from(this).inflate(layoutResID, container);
		
		loadingView = findViewById(R.id.loading);
		ufoAlertView = (UfoAlertView) findViewById(R.id.ufo_alert);
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); // google analytics
	}
	
	protected void googleAnalyticsEvent(String action, String label) {
		googleAnalyticsEvent(action, label, null);
	}
	
	protected void googleAnalyticsEvent(String action, String label, Long value) {
		EasyTracker.getInstance(this).send(MapBuilder.createEvent(getClass().getName(), action, label, value).build());
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // google analytics
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshBgm();
	}
	
	protected void refreshBgm() {
		if (inGame) {
			startBgmForGame();
		} else {
			startBgmForMenu();
		}
	}
	
	protected void setInGame(boolean inGame) {
		this.inGame = inGame;
	}
	
	protected void startBgmForMenu() {
		bgmManager.startForMenu();
	}
	
	protected void startBgmForGame() {
		bgmManager.startForGame();
	}
	
	@Override
	protected void onPause() {
		pauseBgm();
		super.onPause();
	}
	
	protected void pauseBgm() {
		bgmManager.pause();
	}

	@Override
	public void onBackPressed() {
		if (isShowInvitationDialog() == false) {
			super.onBackPressed();
		}
		overridePendingTransition(0, 0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		if (iabHelper != null) {
//			try {
//				iabHelper.dispose();
//			} catch (Exception e) {}
//		}
//		iabHelper = null;
	}

	static Dialog makeSimpleDialog(Activity activity, String text) {
		return (new AlertDialog.Builder(activity)).setMessage(text)
				.setNeutralButton(android.R.string.ok, null).create();
	}
	
	protected void showAlert(String msg) {
		makeSimpleDialog(this, msg).show();
	}

	protected void onConnected(GoogleSignInAccount googleSignInAccount) {
		pref.setIsSignedIn(true);
		Games.getInvitationsClient(getApplicationContext(), googleSignInAccount)
				.registerInvitationCallback(invitationCallback);
	}

	protected void onDisconnected() {
		pref.setIsSignedIn(false);
	}
	
	protected void signIn() {
		if (pref.isSignedIn()) {
			googleSignInClient.silentSignIn().addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
				@Override
				public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
					if (task.isSuccessful()) {
						onConnected(task.getResult());
					} else {
						onDisconnected();
					}
				}
			});
		} else {
			startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
		}
	}
	
	private Invitation recvInvitation;
	protected Invitation getInvitation() {
		return recvInvitation;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

//		iabHelper.handleActivityResult(requestCode, resultCode, data);

		if (requestCode == RC_SIGN_IN) {
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try {
				GoogleSignInAccount account = task.getResult(ApiException.class);
				onConnected(account);
			} catch (ApiException apiException) {
				String message = apiException.getMessage();
				if (message == null || message.isEmpty()) {
					message = getString(R.string.err_please_retry);
				}
				onDisconnected();

				new AlertDialog.Builder(this)
						.setMessage(message)
						.setNeutralButton(android.R.string.ok, null)
						.show();
			}
		}
	}
	
	protected void showDialog(DialogFragment fragment) {
	    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);

	    fragment.show(ft, "dialog");
	}
	
	protected void showInvitationDialog() {
		if (ufoAlertView != null && getInvitation() != null) {
			Participant p = getInvitation().getInviter();
			ufoAlertView.showInvitedDialog(p.getIconImageUri(), p.getDisplayName(), new UfoAlertView.UfoButtonListener() {
				
				@Override
				public void onClick(int position) {
					switch (position) {
					case 0:
						if (getInvitation() != null) {
							moveToAcceptInvited();
						} else {
							showToast(R.string.text_sender_cancel);
						}
						break;
					
					case 1:
						if (getInvitation() != null) {
							declineInvitation(getInvitation());
						}
						break;
					}
				}
			});
		}
	}
	
	// for test
	/*
	protected void showInvitationDialogTest() {
		if (ufoAlertView != null) {
			ufoAlertView.showInvitedDialog(null, "TEST", new UfoAlertView.UfoButtonListener() {
				
				@Override
				public void onClick(int position) {
					playButtonClick();
				}
			});
		}
	}
	*/
	
	private boolean isShowInvitationDialog() {
		return (ufoAlertView != null && ufoAlertView.isShown() && ufoAlertView.getAlertType() == AlertType.INVITED);
	}
	
	protected void moveToAcceptInvited() {
		if (getInvitation() != null) {
			Intent intent = new Intent(getApplicationContext(), GameWithFriendActivity.class);
			intent.putExtra("invitation_id", getInvitation().getInvitationId());
			startActivity(intent);
			clearInvitation();
		}
	}
	
	private void declineInvitation(Invitation invitation) {
		Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.declineInvitation(invitation.getInvitationId());
	}
	
	protected void playButtonClick() {
		gsManager.playButtonClick();
	}
	
	protected void showToast(int resId) {
		Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
	}
	
	protected void unlockAchievement(int achievementResId) {
		if (pref.isSignedIn()) {
			Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
					.unlock(getString(achievementResId));
		}
	}
	
	protected void showLoading() {
		loadingView.setVisibility(View.VISIBLE);
	}
	
	protected void hideLoading() {
		loadingView.setVisibility(View.GONE);
	}
	
	protected boolean isInstalled(String packageName) {

		PackageManager pm = getPackageManager();
		List<ApplicationInfo> installedPkgList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

		for (ApplicationInfo a : installedPkgList) {
			if (a.packageName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}
	
	protected UfoAlertView getUfoAlertView() {
		return ufoAlertView;
	}
	
	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(0, 0);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		overridePendingTransition(0, 0);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}
	
}
