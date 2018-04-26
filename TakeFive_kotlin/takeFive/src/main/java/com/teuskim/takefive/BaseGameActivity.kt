package com.teuskim.takefive

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.analytics.tracking.android.EasyTracker
import com.google.analytics.tracking.android.MapBuilder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.Games
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.InvitationCallback
import com.teuskim.takefive.common.BgmManager
import com.teuskim.takefive.common.GameSoundManager
import com.teuskim.takefive.common.IGoogleAnalytics
import com.teuskim.takefive.common.OmokPreference
//import com.teuskim.takefive.iab.IabHelper
//import com.teuskim.takefive.iab.Purchase
import com.teuskim.takefive.view.UfoAlertView
import com.teuskim.takefive.view.UfoAlertView.AlertType

open class BaseGameActivity : FragmentActivity(), IGoogleAnalytics {

    private var bgmManager: BgmManager? = null
    private var pref: OmokPreference? = null
    private var gsManager: GameSoundManager? = null
//    private var iabHelper: IabHelper? = null
    private var inGame: Boolean = false
    private var googleSignInClient: GoogleSignInClient? = null

    protected var ufoAlertView: UfoAlertView? = null
        private set
    private var loadingView: View? = null

    private val invitationCallback = object : InvitationCallback() {
        override fun onInvitationReceived(invitation: Invitation) {
            if (pref!!.isOnInvitedOk) {
                // 초대화면 출력
                recvInvitation = invitation
                showInvitationDialog()
            } else {
                // 바로 거절
                declineInvitation(invitation)
            }
        }

        override fun onInvitationRemoved(invId: String) {
            if (recvInvitation != null && recvInvitation!!.invitationId == invId) {
                clearInvitation()
            }
        }
    }

    protected var recvInvitation: Invitation? = null
        private set

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

    private val isShowInvitationDialog: Boolean
        get() = ufoAlertView != null && ufoAlertView!!.isShown && ufoAlertView!!.alertType == AlertType.INVITED

    protected fun clearInvitation() {
        recvInvitation = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        bgmManager = BgmManager.getInstance(applicationContext)
        pref = OmokPreference.getInstance(applicationContext)
        gsManager = GameSoundManager.getInstance(applicationContext)
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

        //		initIab();  // TODO: 인앱빌링을 너무 예전에 구현해서 현재는 하위호환이 안되어 다시 구현해야 한다.
    }

    private fun initIab() {
//        val pk = "Fill in pk"
//        iabHelper = IabHelper(this, pk)
//        iabHelper!!.enableDebugLogging(false)
//        iabHelper!!.startSetup(IabHelper.OnIabSetupFinishedListener { result ->
//            if (result.isFailure) {
//                return@OnIabSetupFinishedListener
//            }
//            iabHelper!!.queryInventoryAsync(IabHelper.QueryInventoryFinishedListener { result, inv ->
//                if (result.isFailure) {
//                    return@QueryInventoryFinishedListener
//                }
//                val item = inv!!.getPurchase(PREMIUM_ITEM)
//                afterIab(item)
//
//                // 아이템 구매 취소 테스트용
//                /*
//						iabHelper.consumeAsync(inv.getPurchase(PREMIUM_ITEM), new OnConsumeFinishedListener() {
//							@Override public void onConsumeFinished(Purchase purchase, IabResult result) {}
//						});
//						*/
//            })
//        })
    }

//    protected open fun afterIab(item: Purchase?) {
//        if (item != null) {
//            pref!!.isPremium = true
//        }
//    }

    protected fun openBuyItem() {
//        showLoading()
//        iabHelper!!.launchPurchaseFlow(this, PREMIUM_ITEM, RC_BUY_ITEM) { result, info ->
//            hideLoading()
//            if (result.isSuccess) {
//                showToast(R.string.text_success_buy_item)
//                afterIab(info)
//            } else if (result.response == -1005) {
//                showToast(R.string.text_cancel_buy_item)
//            } else {
//                showToast(R.string.text_fail_buy_item)
//            }
//        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.base_game_activity)

        val container = findViewById<View>(R.id.container) as RelativeLayout
        LayoutInflater.from(this).inflate(layoutResID, container)

        loadingView = findViewById(R.id.loading)
        ufoAlertView = findViewById<View>(R.id.ufo_alert) as UfoAlertView
    }

    override fun onStart() {
        super.onStart()
        EasyTracker.getInstance(this).activityStart(this) // google analytics
    }

    @JvmOverloads
    protected fun googleAnalyticsEvent(action: String, label: String, value: Long? = null) {
        EasyTracker.getInstance(this).send(MapBuilder.createEvent(javaClass.getName(), action, label, value).build())
    }

    override fun onStop() {
        super.onStop()
        EasyTracker.getInstance(this).activityStop(this) // google analytics
    }

    override fun onResume() {
        super.onResume()
        refreshBgm()
    }

    protected fun refreshBgm() {
        if (inGame) {
            startBgmForGame()
        } else {
            startBgmForMenu()
        }
    }

    protected fun setInGame(inGame: Boolean) {
        this.inGame = inGame
    }

    protected fun startBgmForMenu() {
        bgmManager!!.startForMenu()
    }

    protected fun startBgmForGame() {
        bgmManager!!.startForGame()
    }

    override fun onPause() {
        pauseBgm()
        super.onPause()
    }

    protected fun pauseBgm() {
        bgmManager!!.pause()
    }

    override fun onBackPressed() {
        if (isShowInvitationDialog == false) {
            super.onBackPressed()
        }
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (iabHelper != null) {
//            try {
//                iabHelper!!.dispose()
//            } catch (e: Exception) {
//            }
//
//        }
//        iabHelper = null
    }

    protected fun showAlert(msg: String) {
        makeSimpleDialog(this, msg).show()
    }

    protected open fun onConnected(googleSignInAccount: GoogleSignInAccount) {
        pref!!.isSignedIn = true
        Games.getInvitationsClient(applicationContext, googleSignInAccount)
                .registerInvitationCallback(invitationCallback)
    }

    protected open fun onDisconnected() {
        pref!!.isSignedIn = false
    }

    protected fun signIn() {
        if (pref!!.isSignedIn) {
            googleSignInClient!!.silentSignIn().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onConnected(task.result)
                } else {
                    onDisconnected()
                }
            }
        } else {
            startActivityForResult(googleSignInClient!!.signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

//        iabHelper!!.handleActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult<ApiException>(ApiException::class.java!!)
                onConnected(account)
            } catch (apiException: ApiException) {
                var message: String? = apiException.message
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.err_please_retry)
                }
                onDisconnected()

                AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show()
            }

        }
    }

    protected fun showDialog(fragment: DialogFragment) {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        fragment.show(ft, "dialog")
    }

    protected fun showInvitationDialog() {
        if (ufoAlertView != null && recvInvitation != null) {
            val p = recvInvitation!!.inviter
            ufoAlertView!!.showInvitedDialog(p.iconImageUri, p.displayName, object : UfoAlertView.UfoButtonListener {
                override fun onClick(position: Int) {
                    when (position) {
                        0 -> if (recvInvitation != null) {
                            moveToAcceptInvited()
                        } else {
                            showToast(R.string.text_sender_cancel)
                        }

                        1 -> if (recvInvitation != null) {
                            declineInvitation(recvInvitation)
                        }
                    }
                }
            })
        }
    }

    protected open fun moveToAcceptInvited() {
        if (recvInvitation != null) {
            val intent = Intent(applicationContext, GameWithFriendActivity::class.java)
            intent.putExtra("invitation_id", recvInvitation!!.invitationId)
            startActivity(intent)
            clearInvitation()
        }
    }

    private fun declineInvitation(invitation: Invitation?) {
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .declineInvitation(invitation!!.invitationId)
    }

    protected fun playButtonClick() {
        gsManager!!.playButtonClick()
    }

    protected fun showToast(resId: Int) {
        Toast.makeText(applicationContext, resId, Toast.LENGTH_SHORT).show()
    }

    protected fun unlockAchievement(achievementResId: Int) {
        if (pref!!.isSignedIn) {
            Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                    .unlock(getString(achievementResId))
        }
    }

    protected fun showLoading() {
        loadingView!!.visibility = View.VISIBLE
    }

    protected fun hideLoading() {
        loadingView!!.visibility = View.GONE
    }

    protected fun isInstalled(packageName: String): Boolean {

        val pm = packageManager
        val installedPkgList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)

        for (a in installedPkgList) {
            if (a.packageName == packageName) {
                return true
            }
        }
        return false
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        overridePendingTransition(0, 0)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    companion object {

        //	protected static final String PREMIUM_ITEM = "android.test.purchased";  // for test
        protected val PREMIUM_ITEM = "premium_item"

        private val RC_SIGN_IN = 101
        private val RC_BUY_ITEM = 102

        internal fun makeSimpleDialog(activity: Activity, text: String): Dialog {
            return AlertDialog.Builder(activity).setMessage(text)
                    .setNeutralButton(android.R.string.ok, null).create()
        }
    }

}
