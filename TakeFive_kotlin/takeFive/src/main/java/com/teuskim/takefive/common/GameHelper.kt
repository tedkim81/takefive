/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teuskim.takefive.common

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log

//import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesActivityResultCodes
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.Multiplayer
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch

//import com.google.android.gms.plus.Plus;

class GameHelper
/**
 * Construct a GameHelper object, initially tied to the given Activity.
 * After constructing this object, call @link{setup} from the onCreate()
 * method of your Activity.
 *
 * @param clientsToUse the API clients to use (a combination of the CLIENT_* flags,
 * or CLIENT_ALL to mean all clients).
 */
(activity: Activity, clientsToUse: Int) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // configuration done?
    private val mSetupDone = false

    // are we currently connecting?
    /** Returns whether or not we are currently connecting  */
    var isConnecting = false
        private set

    // Are we expecting the result of a resolution flow?
    internal var mExpectingResolution = false

    // was the sign-in flow cancelled when we tried it?
    // if true, we know not to try again automatically.
    internal var mSignInCancelled = false

    /**
     * The Activity we are bound to. We need to keep a reference to the Activity
     * because some games methods require an Activity (a Context won't do). We
     * are careful not to leak these references: we release them on onStop().
     */
    internal var mActivity: Activity? = null

    // app context
    internal var mAppContext: Context? = null

    // the Google API client builder we will use to create GoogleApiClient
    internal var mGoogleApiClientBuilder: GoogleApiClient.Builder? = null

    // Api options to use when adding each API, null for none
    //    GoogleApiClient.ApiOptions mGamesApiOptions = null;
    //    GoogleApiClient.ApiOptions mPlusApiOptions = null;
    //    GoogleApiClient.ApiOptions mAppStateApiOptions = null;

    // Google API client object we manage.
    internal var mGoogleApiClient: GoogleApiClient? = null

    // What clients were requested? (bit flags)
    internal var mRequestedClients = CLIENT_NONE

    // Whether to automatically try to sign in on onStart(). We only set this
    // to true when the sign-in process fails or the user explicitly signs out.
    // We set it back to false when the user initiates the sign in process.
    internal var mConnectOnStart = true

    /*
     * Whether user has specifically requested that the sign-in process begin. If
     * mUserInitiatedSignIn is false, we're in the automatic sign-in attempt that we try once the
     * Activity is started -- if true, then the user has already clicked a "Sign-In" button or
     * something similar
     */
    internal var mUserInitiatedSignIn = false

    // The connection result we got from our last attempt to sign-in.
    internal var mConnectionResult: ConnectionResult? = null

    // The error that happened during sign-in.
    /**
     * Returns the error that happened during the sign-in process, null if no
     * error occurred.
     */
    var signInError: SignInFailureReason? = null
        internal set

    // Should we show error dialog boxes?
    internal var mShowErrorDialogs = true

    // Print debug logs?
    internal var mDebugLog = false

    internal var mHandler: Handler

    /*
     * If we got an invitation when we connected to the games client, it's here. Otherwise, it's
     * null.
     */
    internal var mInvitation: Invitation? = null

    /*
     * If we got turn-based match when we connected to the games client, it's here. Otherwise, it's
     * null.
     */
    internal var mTurnBasedMatch: TurnBasedMatch? = null

    // Listener
    internal var mListener: GameHelperListener? = null
    internal var mMaxAutoSignInAttempts = DEFAULT_MAX_SIGN_IN_ATTEMPTS

    /** Sets the options to pass when setting up the Games API. Call before setup().  */
    //    public void setGamesApiOptions(GoogleApiClient.ApiOptions options) {
    //        doApiOptionsPreCheck();
    //        mGamesApiOptions = options;
    //    }

    /** Sets the options to pass when setting up the AppState API. Call before setup().  */
    //    public void setAppStateApiOptions(GoogleApiClient.ApiOptions options) {
    //        doApiOptionsPreCheck();
    //        mAppStateApiOptions = options;
    //    }

    /** Sets the options to pass when setting up the Plus API. Call before setup().  */
    //    public void setPlusApiOptions(GoogleApiClient.ApiOptions options) {
    //        doApiOptionsPreCheck();
    //        mPlusApiOptions = options;
    //    }

    /**
     * Creates a GoogleApiClient.Builder for use with @link{#setup}. Normally, you do not have
     * to do this; use this method only if you need to make nonstandard setup (e.g. adding
     * extra scopes for other APIs) on the GoogleApiClient.Builder before calling @link{#setup}.
     */
    //    public GoogleApiClient.Builder createApiClientBuilder() {
    //        if (mSetupDone) {
    //            String error = "GameHelper: you called GameHelper.createApiClientBuilder() after " +
    //                    "calling setup. You can only get a client builder BEFORE performing setup.";
    //            logError(error);
    //            throw new IllegalStateException(error);
    //        }
    //
    //        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(mActivity, this, this);
    //
    //        if (0 != (mRequestedClients & CLIENT_GAMES)) {
    //            builder.addApi(Games.API, mGamesApiOptions);
    //            builder.addScope(Games.SCOPE_GAMES);
    //        }
    //
    //        if (0 != (mRequestedClients & CLIENT_PLUS)) {
    //            builder.addApi(Plus.API, mPlusApiOptions);
    //            builder.addScope(Plus.SCOPE_PLUS_LOGIN);
    //        }
    //
    //        if (0 != (mRequestedClients & CLIENT_APPSTATE)) {
    //            builder.addApi(AppStateManager.API, mAppStateApiOptions);
    //            builder.addScope(AppStateManager.SCOPE_APP_STATE);
    //        }
    //
    //        mGoogleApiClientBuilder = builder;
    //        return builder;
    //    }

    /**
     * Performs setup on this GameHelper object. Call this from the onCreate()
     * method of your Activity. This will create the clients and do a few other
     * initialization tasks. Next, call @link{#onStart} from the onStart()
     * method of your Activity.
     *
     * @param listener The listener to be notified of sign-in events.
     */
    //    public void setup(GameHelperListener listener) {
    //        if (mSetupDone) {
    //            String error = "GameHelper: you cannot call GameHelper.setup() more than once!";
    //            logError(error);
    //            throw new IllegalStateException(error);
    //        }
    //        mListener = listener;
    //        debugLog("Setup: requested clients: " + mRequestedClients);
    //
    //        if (mGoogleApiClientBuilder == null) {
    //            // we don't have a builder yet, so create one
    //            createApiClientBuilder();
    //        }
    //
    //        mGoogleApiClient = mGoogleApiClientBuilder.build();
    //        mGoogleApiClientBuilder = null;
    //        mSetupDone = true;
    //    }

    /**
     * Returns the GoogleApiClient object. In order to call this method, you must have
     * called @link{setup}.
     */
    val apiClient: GoogleApiClient
        get() {
            if (mGoogleApiClient == null) {
                throw IllegalStateException("No GoogleApiClient. Did you call setup()?")
            }
            return mGoogleApiClient as GoogleApiClient
        }

    /** Returns whether or not the user is signed in.  */
    val isSignedIn: Boolean
        get() = mGoogleApiClient != null && mGoogleApiClient!!.isConnected

    /**
     * Returns the invitation ID received through an invitation notification.
     * This should be called from your GameHelperListener's
     * @link{GameHelperListener#onSignInSucceeded} method, to check if there's an
     * invitation available. In that case, accept the invitation.
     * @return The id of the invitation, or null if none was received.
     */
    val invitationId: String?
        get() {
            if (!mGoogleApiClient!!.isConnected) {
                Log.w(TAG, "Warning: getInvitationId() should only be called when signed in, " + "that is, after getting onSignInSuceeded()")
            }
            return if (mInvitation == null) null else mInvitation!!.invitationId
        }

    /**
     * Returns the invitation received through an invitation notification.
     * This should be called from your GameHelperListener's
     * @link{GameHelperListener#onSignInSucceeded} method, to check if there's an
     * invitation available. In that case, accept the invitation.
     * @return The invitation, or null if none was received.
     */
    val invitation: Invitation?
        get() {
            if (!mGoogleApiClient!!.isConnected) {
                Log.w(TAG, "Warning: getInvitation() should only be called when signed in, " + "that is, after getting onSignInSuceeded()")
            }
            return mInvitation
        }

    /**
     * Returns the tbmp match received through an invitation notification. This
     * should be called from your GameHelperListener's
     * @link{GameHelperListener#onSignInSucceeded} method, to check if there's a
     * match available.
     * @return The match, or null if none was received.
     */
    val turnBasedMatch: TurnBasedMatch?
        get() {
            if (!mGoogleApiClient!!.isConnected) {
                Log.w(TAG, "Warning: getTurnBasedMatch() should only be called when signed in, " + "that is, after getting onSignInSuceeded()")
            }
            return mTurnBasedMatch
        }

    private val GAMEHELPER_SHARED_PREFS = "GAMEHELPER_SHARED_PREFS"
    private val KEY_SIGN_IN_CANCELLATIONS = "KEY_SIGN_IN_CANCELLATIONS"

    // Return the number of times the user has cancelled the sign-in flow in the life of the app
    internal val signInCancellations: Int
        get() {
            val sp = mAppContext!!.getSharedPreferences(GAMEHELPER_SHARED_PREFS,
                    Context.MODE_PRIVATE)
            return sp.getInt(KEY_SIGN_IN_CANCELLATIONS, 0)
        }

    /** Listener for sign-in success or failure events.  */
    interface GameHelperListener {
        /**
         * Called when sign-in fails. As a result, a "Sign-In" button can be
         * shown to the user; when that button is clicked, call
         * @link{GamesHelper#beginUserInitiatedSignIn}. Note that not all calls to this
         * method mean an error; it may be a result of the fact that automatic
         * sign-in could not proceed because user interaction was required
         * (consent dialogs). So implementations of this method should NOT
         * display an error message unless a call to @link{GamesHelper#hasSignInError}
         * indicates that an error indeed occurred.
         */
        fun onSignInFailed()

        /** Called when sign-in succeeds.  */
        fun onSignInSucceeded()
    }

    init {
        mActivity = activity
        mAppContext = activity.applicationContext
        mRequestedClients = clientsToUse
        mHandler = Handler()
    }

    /**
     * Sets the maximum number of automatic sign-in attempts to be made on application
     * startup. This maximum is over the lifetime of the application (it is stored in
     * a SharedPreferences file). So, for example, if you specify 2, then it means that
     * the user will be prompted to sign in on app startup the first time and, if they
     * cancel, a second time the next time the app starts, and, if they cancel that one,
     * never again. Set to 0 if you do not want the user to be prompted to sign in
     * on application startup.
     */
    fun setMaxAutoSignInAttempts(max: Int) {
        mMaxAutoSignInAttempts = max
    }

    internal fun assertConfigured(operation: String) {
        if (!mSetupDone) {
            val error = "GameHelper error: Operation attempted without setup: " + operation +
                    ". The setup() method must be called before attempting any other operation."
            logError(error)
            throw IllegalStateException(error)
        }
    }

    private fun doApiOptionsPreCheck() {
        if (mGoogleApiClientBuilder != null) {
            val error = "GameHelper: you cannot call set*ApiOptions after the client " +
                    "builder has been created. Call it before calling createApiClientBuilder() " +
                    "or setup()."
            logError(error)
            throw IllegalStateException(error)
        }
    }

    /**
     * Returns whether or not there was a (non-recoverable) error during the
     * sign-in process.
     */
    fun hasSignInError(): Boolean {
        return signInError != null
    }

    // Set whether to show error dialogs or not.
    fun setShowErrorDialogs(show: Boolean) {
        mShowErrorDialogs = show
    }

    /** Call this method from your Activity's onStart().  */
    fun onStart(act: Activity) {
        mActivity = act
        mAppContext = act.applicationContext

        debugLog("onStart")
        assertConfigured("onStart")

        if (mConnectOnStart) {
            if (mGoogleApiClient!!.isConnected) {
                Log.w(TAG, "GameHelper: client was already connected on onStart()")
            } else {
                debugLog("Connecting client.")
                isConnecting = true
                mGoogleApiClient!!.connect()
            }
        } else {
            debugLog("Not attempting to connect becase mConnectOnStart=false")
            debugLog("Instead, reporting a sign-in failure.")
            mHandler.postDelayed({ notifyListener(false) }, 1000)
        }
    }

    /** Call this method from your Activity's onStop().  */
    fun onStop() {
        debugLog("onStop")
        assertConfigured("onStop")
        if (mGoogleApiClient!!.isConnected) {
            debugLog("Disconnecting client due to onStop")
            mGoogleApiClient!!.disconnect()
        } else {
            debugLog("Client already disconnected when we got onStop.")
        }
        isConnecting = false
        mExpectingResolution = false

        // let go of the Activity reference
        mActivity = null
    }

    fun hasInvitation(): Boolean {
        return mInvitation != null
    }

    fun hasTurnBasedMatch(): Boolean {
        return mTurnBasedMatch != null
    }

    fun clearInvitation() {
        mInvitation = null
    }

    fun clearTurnBasedMatch() {
        mTurnBasedMatch = null
    }

    /** Enables debug logging  */
    fun enableDebugLog(enabled: Boolean) {
        mDebugLog = enabled
        if (enabled) {
            debugLog("Debug log enabled.")
        }
    }

    @Deprecated("")
    fun enableDebugLog(enabled: Boolean, tag: String) {
        Log.w(TAG, "GameHelper.enableDebugLog(boolean,String) is deprecated. " + "Use GameHelper.enableDebugLog(boolean)")
        enableDebugLog(enabled)
    }

    /** Sign out and disconnect from the APIs.  */
    //    public void signOut() {
    //        if (!mGoogleApiClient.isConnected()) {
    //            // nothing to do
    //            debugLog("signOut: was already disconnected, ignoring.");
    //            return;
    //        }
    //
    //        // for Plus, "signing out" means clearing the default account and
    //        // then disconnecting
    //        if (0 != (mRequestedClients & CLIENT_PLUS)) {
    //            debugLog("Clearing default account on PlusClient.");
    //            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
    //        }
    //
    //        // For the games client, signing out means calling signOut and disconnecting
    //        if (0 != (mRequestedClients & CLIENT_GAMES)) {
    //            debugLog("Signing out from GamesClient.");
    //            Games.signOut(mGoogleApiClient);
    //        }
    //
    //        // Ready to disconnect
    //        debugLog("Disconnecting client.");
    //        mConnectOnStart = false;
    //        mConnecting = false;
    //        mGoogleApiClient.disconnect();
    //    }


    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback. If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    fun onActivityResult(requestCode: Int, responseCode: Int, intent: Intent) {
        debugLog("onActivityResult: req=" + (if (requestCode == RC_RESOLVE)
            "RC_RESOLVE"
        else
            requestCode.toString()) + ", resp=" +
                GameHelperUtils.activityResponseCodeToString(responseCode))
        if (requestCode != RC_RESOLVE) {
            debugLog("onActivityResult: request code not meant for us. Ignoring.")
            return
        }

        // no longer expecting a resolution
        mExpectingResolution = false

        if (!isConnecting) {
            debugLog("onActivityResult: ignoring because we are not connecting.")
            return
        }

        // We're coming back from an activity that was launched to resolve a
        // connection problem. For example, the sign-in UI.
        if (responseCode == Activity.RESULT_OK) {
            // Ready to try to connect again.
            debugLog("onAR: Resolution was RESULT_OK, so connecting current client again.")
            connect()
        } else if (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            debugLog("onAR: Resolution was RECONNECT_REQUIRED, so reconnecting.")
            connect()
        } else if (responseCode == Activity.RESULT_CANCELED) {
            // User cancelled.
            debugLog("onAR: Got a cancellation result, so disconnecting.")
            mSignInCancelled = true
            mConnectOnStart = false
            mUserInitiatedSignIn = false
            signInError = null // cancelling is not a failure!
            isConnecting = false
            mGoogleApiClient!!.disconnect()

            // increment # of cancellations
            val prevCancellations = signInCancellations
            val newCancellations = incrementSignInCancellations()
            debugLog("onAR: # of cancellations " + prevCancellations + " --> " + newCancellations +
                    ", max " + mMaxAutoSignInAttempts)

            notifyListener(false)
        } else {
            // Whatever the problem we were trying to solve, it was not
            // solved. So give up and show an error message.
            debugLog("onAR: responseCode=" +
                    GameHelperUtils.activityResponseCodeToString(responseCode) + ", so giving up.")
            giveUp(SignInFailureReason(mConnectionResult!!.errorCode, responseCode))
        }
    }

    internal fun notifyListener(success: Boolean) {
        debugLog("Notifying LISTENER of sign-in " + if (success)
            "SUCCESS"
        else if (signInError != null) "FAILURE (error)" else "FAILURE (no error)")
        if (mListener != null) {
            if (success) {
                mListener!!.onSignInSucceeded()
            } else {
                mListener!!.onSignInFailed()
            }
        }
    }

    /**
     * Starts a user-initiated sign-in flow. This should be called when the user
     * clicks on a "Sign In" button. As a result, authentication/consent dialogs
     * may show up. At the end of the process, the GameHelperListener's
     * onSignInSucceeded() or onSignInFailed() methods will be called.
     */
    fun beginUserInitiatedSignIn() {
        debugLog("beginUserInitiatedSignIn: resetting attempt count.")
        resetSignInCancellations()
        mSignInCancelled = false
        mConnectOnStart = true

        if (mGoogleApiClient!!.isConnected) {
            // nothing to do
            logWarn("beginUserInitiatedSignIn() called when already connected. " + "Calling listener directly to notify of success.")
            notifyListener(true)
            return
        } else if (isConnecting) {
            logWarn("beginUserInitiatedSignIn() called when already connecting. " +
                    "Be patient! You can only call this method after you get an " +
                    "onSignInSucceeded() or onSignInFailed() callback. Suggestion: disable " +
                    "the sign-in button on startup and also when it's clicked, and re-enable " +
                    "when you get the callback.")
            // ignore call (listener will get a callback when the connection process finishes)
            return
        }

        debugLog("Starting USER-INITIATED sign-in flow.")

        // indicate that user is actively trying to sign in (so we know to resolve
        // connection problems by showing dialogs)
        mUserInitiatedSignIn = true

        if (mConnectionResult != null) {
            // We have a pending connection result from a previous failure, so
            // start with that.
            debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.")
            isConnecting = true
            resolveConnectionResult()
        } else {
            // We don't have a pending connection result, so start anew.
            debugLog("beginUserInitiatedSignIn: starting new sign-in flow.")
            isConnecting = true
            connect()
        }
    }

    internal fun connect() {
        if (mGoogleApiClient!!.isConnected) {
            debugLog("Already connected.")
            return
        }
        debugLog("Starting connection.")
        isConnecting = true
        mInvitation = null
        mTurnBasedMatch = null
        mGoogleApiClient!!.connect()
    }

    /**
     * Disconnects the API client, then connects again.
     */
    fun reconnectClient() {
        if (!mGoogleApiClient!!.isConnected) {
            Log.w(TAG, "reconnectClient() called when client is not connected.")
            // interpret it as a request to connect
            connect()
        } else {
            debugLog("Reconnecting client.")
            mGoogleApiClient!!.reconnect()
        }
    }

    /** Called when we successfully obtain a connection to a client.  */
    override fun onConnected(connectionHint: Bundle?) {
        debugLog("onConnected: connected!")

        if (connectionHint != null) {
            debugLog("onConnected: connection hint provided. Checking for invite.")
            val inv = connectionHint
                    .getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)
            if (inv != null && inv.invitationId != null) {
                // retrieve and cache the invitation ID
                debugLog("onConnected: connection hint has a room invite!")
                mInvitation = inv
                debugLog("Invitation ID: " + mInvitation!!.invitationId)
            }

            debugLog("onConnected: connection hint provided. Checking for TBMP game.")
            mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH)
        }

        // we're good to go
        succeedSignIn()
    }

    internal fun succeedSignIn() {
        debugLog("succeedSignIn")
        signInError = null
        mConnectOnStart = true
        mUserInitiatedSignIn = false
        isConnecting = false
        notifyListener(true)
    }

    // Increments the counter that indicates how many times the user has cancelled the sign in
    // flow in the life of the application
    internal fun incrementSignInCancellations(): Int {
        val cancellations = signInCancellations
        val editor = mAppContext!!.getSharedPreferences(GAMEHELPER_SHARED_PREFS,
                Context.MODE_PRIVATE).edit()
        editor.putInt(KEY_SIGN_IN_CANCELLATIONS, cancellations + 1)
        editor.commit()
        return cancellations + 1
    }

    // Reset the counter of how many times the user has cancelled the sign-in flow.
    internal fun resetSignInCancellations() {
        val editor = mAppContext!!.getSharedPreferences(GAMEHELPER_SHARED_PREFS,
                Context.MODE_PRIVATE).edit()
        editor.putInt(KEY_SIGN_IN_CANCELLATIONS, 0)
    }

    /** Handles a connection failure.  */
    override fun onConnectionFailed(result: ConnectionResult) {
        // save connection result for later reference
        debugLog("onConnectionFailed")

        mConnectionResult = result
        debugLog("Connection failure:")
        debugLog("   - code: " + GameHelperUtils.errorCodeToString(mConnectionResult!!.errorCode))
        debugLog("   - resolvable: " + mConnectionResult!!.hasResolution())
        debugLog("   - details: " + mConnectionResult!!.toString())

        val cancellations = signInCancellations
        var shouldResolve = false

        if (mUserInitiatedSignIn) {
            debugLog("onConnectionFailed: WILL resolve because user initiated sign-in.")
            shouldResolve = true
        } else if (mSignInCancelled) {
            debugLog("onConnectionFailed WILL NOT resolve (user already cancelled once).")
            shouldResolve = false
        } else if (cancellations < mMaxAutoSignInAttempts) {
            debugLog("onConnectionFailed: WILL resolve because we have below the max# of " +
                    "attempts, " + cancellations + " < " + mMaxAutoSignInAttempts)
            shouldResolve = true
        } else {
            shouldResolve = false
            debugLog("onConnectionFailed: Will NOT resolve; not user-initiated and max attempts " +
                    "reached: " + cancellations + " >= " + mMaxAutoSignInAttempts)
        }

        if (!shouldResolve) {
            // Fail and wait for the user to want to sign in.
            debugLog("onConnectionFailed: since we won't resolve, failing now.")
            mConnectionResult = result
            isConnecting = false
            notifyListener(false)
            return
        }

        debugLog("onConnectionFailed: resolving problem...")

        // Resolve the connection result. This usually means showing a dialog or
        // starting an Activity that will allow the user to give the appropriate
        // consents so that sign-in can be successful.
        resolveConnectionResult()
    }

    /**
     * Attempts to resolve a connection failure. This will usually involve
     * starting a UI flow that lets the user give the appropriate consents
     * necessary for sign-in to work.
     */
    internal fun resolveConnectionResult() {
        // Try to resolve the problem
        if (mExpectingResolution) {
            debugLog("We're already expecting the result of a previous resolution.")
            return
        }

        debugLog("resolveConnectionResult: trying to resolve result: " + mConnectionResult!!)
        if (mConnectionResult!!.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            debugLog("Result has resolution. Starting it.")
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                mExpectingResolution = true
                mConnectionResult!!.startResolutionForResult(mActivity, RC_RESOLVE)
            } catch (e: SendIntentException) {
                // Try connecting again
                debugLog("SendIntentException, so connecting again.")
                connect()
            }

        } else {
            // It's not a problem what we can solve, so give up and show an error.
            debugLog("resolveConnectionResult: result has no resolution. Giving up.")
            giveUp(SignInFailureReason(mConnectionResult!!.errorCode))
        }
    }

    fun disconnect() {
        if (mGoogleApiClient!!.isConnected) {
            debugLog("Disconnecting client.")
            mGoogleApiClient!!.disconnect()
        } else {
            Log.w(TAG, "disconnect() called when client was already disconnected.")
        }
    }

    /**
     * Give up on signing in due to an error. Shows the appropriate error
     * message to the user, using a standard error dialog as appropriate to the
     * cause of the error. That dialog will indicate to the user how the problem
     * can be solved (for example, re-enable Google Play Services, upgrade to a
     * new version, etc).
     */
    internal fun giveUp(reason: SignInFailureReason) {
        mConnectOnStart = false
        disconnect()
        signInError = reason

        if (reason.activityResultCode == GamesActivityResultCodes.RESULT_APP_MISCONFIGURED) {
            // print debug info for the developer
            GameHelperUtils.printMisconfiguredDebugInfo(mAppContext)
        }

        showFailureDialog()
        isConnecting = false
        notifyListener(false)
    }

    /** Called when we are disconnected from the Google API client.  */
    override fun onConnectionSuspended(cause: Int) {
        debugLog("onConnectionSuspended, cause=" + cause)
        disconnect()
        signInError = null
        debugLog("Making extraordinary call to onSignInFailed callback")
        isConnecting = false
        notifyListener(false)
    }

    fun showFailureDialog() {
        if (signInError != null) {
            val errorCode = signInError!!.serviceErrorCode
            val actResp = signInError!!.activityResultCode

            if (mShowErrorDialogs) {
                showFailureDialog(mActivity, actResp, errorCode)
            } else {
                debugLog("Not showing error dialog because mShowErrorDialogs==false. " + "" +
                        "Error was: " + signInError)
            }
        }
    }

    fun makeSimpleDialog(text: String): Dialog? {
        if (mActivity == null) {
            logError("*** makeSimpleDialog failed: no current Activity!")
            return null
        }
        return makeSimpleDialog(mActivity!!, text)
    }

    fun makeSimpleDialog(title: String, text: String): Dialog? {
        if (mActivity == null) {
            logError("*** makeSimpleDialog failed: no current Activity!")
            return null
        }
        return makeSimpleDialog(mActivity!!, title, text)
    }

    internal fun debugLog(message: String) {
        if (mDebugLog) {
            Log.d(TAG, "GameHelper: " + message)
        }
    }

    internal fun logWarn(message: String) {
        Log.w(TAG, "!!! GameHelper WARNING: " + message)
    }

    internal fun logError(message: String) {
        Log.e(TAG, "*** GameHelper ERROR: " + message)
    }

    // Represents the reason for a sign-in failure
    class SignInFailureReason @JvmOverloads constructor(serviceErrorCode: Int, activityResultCode: Int = NO_ACTIVITY_RESULT_CODE) {
        var serviceErrorCode = 0
            internal set
        var activityResultCode = NO_ACTIVITY_RESULT_CODE
            internal set

        init {
            this.serviceErrorCode = serviceErrorCode
            this.activityResultCode = activityResultCode
        }

        override fun toString(): String {
            return "SignInFailureReason(serviceErrorCode:" +
                    GameHelperUtils.errorCodeToString(serviceErrorCode) +
                    if (activityResultCode == NO_ACTIVITY_RESULT_CODE)
                        ")"
                    else
                        ",activityResultCode:" +
                                GameHelperUtils.activityResponseCodeToString(activityResultCode) + ")"
        }

        companion object {
            val NO_ACTIVITY_RESULT_CODE = -100
        }
    }

    // Not recommended for general use. This method forces the "connect on start" flag
    // to a given state. This may be useful when using GameHelper in a non-standard
    // sign-in flow.
    fun setConnectOnStart(connectOnStart: Boolean) {
        debugLog("Forcing mConnectOnStart=" + connectOnStart)
        mConnectOnStart = connectOnStart
    }

    companion object {

        internal val TAG = "GameHelper"

        // Request code we use when invoking other Activities to complete the
        // sign-in flow.
        internal val RC_RESOLVE = 9001

        // Request code when invoking Activities whose result we don't care about.
        internal val RC_UNUSED = 9002

        // Client request flags
        val CLIENT_NONE = 0x00
        val CLIENT_GAMES = 0x01
        val CLIENT_PLUS = 0x02
        val CLIENT_APPSTATE = 0x04
        val CLIENT_ALL = CLIENT_GAMES or CLIENT_PLUS or CLIENT_APPSTATE

        // Should we start the flow to sign the user in automatically on startup? If so, up to
        // how many times in the life of the application?
        internal val DEFAULT_MAX_SIGN_IN_ATTEMPTS = 3

        /** Shows an error dialog that's appropriate for the failure reason.  */
        fun showFailureDialog(activity: Activity?, actResp: Int, errorCode: Int) {
            if (activity == null) {
                Log.e("GameHelper", "*** No Activity. Can't show failure dialog!")
                return
            }
            var errorDialog: Dialog? = null

            when (actResp) {
                GamesActivityResultCodes.RESULT_APP_MISCONFIGURED -> errorDialog = makeSimpleDialog(activity, GameHelperUtils.getString(activity,
                        GameHelperUtils.R_APP_MISCONFIGURED))
                GamesActivityResultCodes.RESULT_SIGN_IN_FAILED -> errorDialog = makeSimpleDialog(activity, GameHelperUtils.getString(activity,
                        GameHelperUtils.R_SIGN_IN_FAILED))
                GamesActivityResultCodes.RESULT_LICENSE_FAILED -> errorDialog = makeSimpleDialog(activity, GameHelperUtils.getString(activity,
                        GameHelperUtils.R_LICENSE_FAILED))
                else -> {
                    // No meaningful Activity response code, so generate default Google
                    // Play services dialog
                    errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity,
                            RC_UNUSED, null)
                    if (errorDialog == null) {
                        // get fallback dialog
                        Log.e("GameHelper", "No standard error dialog available. Making fallback dialog.")
                        errorDialog = makeSimpleDialog(activity,
                                GameHelperUtils.getString(activity, GameHelperUtils.R_UNKNOWN_ERROR)
                                        + " " + GameHelperUtils.errorCodeToString(errorCode))
                    }
                }
            }

            errorDialog.show()
        }

        internal fun makeSimpleDialog(activity: Activity, text: String): Dialog {
            return AlertDialog.Builder(activity).setMessage(text)
                    .setNeutralButton(android.R.string.ok, null).create()
        }

        internal fun makeSimpleDialog(activity: Activity, title: String, text: String): Dialog {
            return AlertDialog.Builder(activity).setMessage(text).setTitle(title)
                    .setNeutralButton(android.R.string.ok, null).create()
        }
    }
}
