package com.teuskim.takefive.common

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Resources
import android.util.Log

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.games.GamesActivityResultCodes
import com.teuskim.takefive.R

/**
 * Created by btco on 2/10/14.
 */
internal object GameHelperUtils {
    val R_UNKNOWN_ERROR = 0
    val R_SIGN_IN_FAILED = 1
    val R_APP_MISCONFIGURED = 2
    val R_LICENSE_FAILED = 3

    private val FALLBACK_STRINGS = arrayOf("*Unknown error.", "*Failed to sign in. Please check your network connection and try again.", "*The application is incorrectly configured. Check that the package name and signing certificate match the client ID created in Developer Console. Also, if the application is not yet published, check that the account you are trying to sign in with is listed as a tester account. See logs for more information.", "*License check failed.")

    private val RES_IDS = intArrayOf(R.string.gamehelper_unknown_error, R.string.gamehelper_sign_in_failed, R.string.gamehelper_app_misconfigured, R.string.gamehelper_license_failed)

    fun activityResponseCodeToString(respCode: Int): String {
        when (respCode) {
            Activity.RESULT_OK -> return "RESULT_OK"
            Activity.RESULT_CANCELED -> return "RESULT_CANCELED"
            GamesActivityResultCodes.RESULT_APP_MISCONFIGURED -> return "RESULT_APP_MISCONFIGURED"
            GamesActivityResultCodes.RESULT_LEFT_ROOM -> return "RESULT_LEFT_ROOM"
            GamesActivityResultCodes.RESULT_LICENSE_FAILED -> return "RESULT_LICENSE_FAILED"
            GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED -> return "RESULT_RECONNECT_REQUIRED"
            GamesActivityResultCodes.RESULT_SIGN_IN_FAILED -> return "SIGN_IN_FAILED"
            else -> return respCode.toString()
        }
    }

    fun errorCodeToString(errorCode: Int): String {
        when (errorCode) {
            ConnectionResult.DEVELOPER_ERROR -> return "DEVELOPER_ERROR($errorCode)"
            ConnectionResult.INTERNAL_ERROR -> return "INTERNAL_ERROR($errorCode)"
            ConnectionResult.INVALID_ACCOUNT -> return "INVALID_ACCOUNT($errorCode)"
            ConnectionResult.LICENSE_CHECK_FAILED -> return "LICENSE_CHECK_FAILED($errorCode)"
            ConnectionResult.NETWORK_ERROR -> return "NETWORK_ERROR($errorCode)"
            ConnectionResult.RESOLUTION_REQUIRED -> return "RESOLUTION_REQUIRED($errorCode)"
            ConnectionResult.SERVICE_DISABLED -> return "SERVICE_DISABLED($errorCode)"
            ConnectionResult.SERVICE_INVALID -> return "SERVICE_INVALID($errorCode)"
            ConnectionResult.SERVICE_MISSING -> return "SERVICE_MISSING($errorCode)"
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> return "SERVICE_VERSION_UPDATE_REQUIRED($errorCode)"
            ConnectionResult.SIGN_IN_REQUIRED -> return "SIGN_IN_REQUIRED($errorCode)"
            ConnectionResult.SUCCESS -> return "SUCCESS($errorCode)"
            else -> return "Unknown error code " + errorCode
        }
    }

    fun printMisconfiguredDebugInfo(ctx: Context?) {
        Log.w("GameHelper", "****")
        Log.w("GameHelper", "****")
        Log.w("GameHelper", "**** APP NOT CORRECTLY CONFIGURED TO USE GOOGLE PLAY GAME SERVICES")
        Log.w("GameHelper", "**** This is usually caused by one of these reasons:")
        Log.w("GameHelper", "**** (1) Your package name and certificate fingerprint do not match")
        Log.w("GameHelper", "****     the client ID you registered in Developer Console.")
        Log.w("GameHelper", "**** (2) Your App ID was incorrectly entered.")
        Log.w("GameHelper", "**** (3) Your game settings have not been published and you are ")
        Log.w("GameHelper", "****     trying to log in with an account that is not listed as")
        Log.w("GameHelper", "****     a test account.")
        Log.w("GameHelper", "****")
        if (ctx == null) {
            Log.w("GameHelper", "*** (no Context, so can't print more debug info)")
            return
        }

        Log.w("GameHelper", "**** To help you debug, here is the information about this app")
        Log.w("GameHelper", "**** Package name         : " + ctx.packageName)
        Log.w("GameHelper", "**** Cert SHA1 fingerprint: " + getSHA1CertFingerprint(ctx))
        Log.w("GameHelper", "**** App ID from          : " + getAppIdFromResource(ctx))
        Log.w("GameHelper", "****")
        Log.w("GameHelper", "**** Check that the above information matches your setup in ")
        Log.w("GameHelper", "**** Developer Console. Also, check that you're logging in with the")
        Log.w("GameHelper", "**** right account (it should be listed in the Testers section if")
        Log.w("GameHelper", "**** your project is not yet published).")
        Log.w("GameHelper", "****")
        Log.w("GameHelper", "**** For more information, refer to the troubleshooting guide:")
        Log.w("GameHelper", "****   http://developers.google.com/games/services/android/troubleshooting")
    }

    fun getAppIdFromResource(ctx: Context): String {
        try {
            val res = ctx.resources
            val pkgName = ctx.packageName
            val res_id = res.getIdentifier("googleplay_app_id", "string", pkgName)
            return res.getString(res_id)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return "??? (failed to retrieve APP ID)"
        }

    }

    fun getSHA1CertFingerprint(ctx: Context): String {
        try {
            val sigs = ctx.packageManager.getPackageInfo(
                    ctx.packageName, PackageManager.GET_SIGNATURES).signatures
            if (sigs.size == 0) {
                return "ERROR: NO SIGNATURE."
            } else if (sigs.size > 1) {
                return "ERROR: MULTIPLE SIGNATURES"
            }
            val digest = MessageDigest.getInstance("SHA1").digest(sigs[0].toByteArray())
            val hexString = StringBuilder()
            for (i in digest.indices) {
                if (i > 0) {
                    hexString.append(":")
                }
                byteToString(hexString, digest[i])
            }
            return hexString.toString()

        } catch (ex: PackageManager.NameNotFoundException) {
            ex.printStackTrace()
            return "(ERROR: package not found)"
        } catch (ex: NoSuchAlgorithmException) {
            ex.printStackTrace()
            return "(ERROR: SHA1 algorithm not found)"
        }

    }

    fun byteToString(sb: StringBuilder, b: Byte) {
//        val unsigned_byte = if (b < 0) b + 256 else b
//        val hi = unsigned_byte / 16
//        val lo = unsigned_byte % 16
//        sb.append("0123456789ABCDEF".substring(hi, hi + 1))
//        sb.append("0123456789ABCDEF".substring(lo, lo + 1))
    }

    fun getString(ctx: Context, whichString: Int): String {
        var whichString = whichString
        whichString = if (whichString >= 0 && whichString < RES_IDS.size) whichString else 0
        val resId = RES_IDS[whichString]
        try {
            return ctx.getString(resId)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.w(GameHelper.TAG, "*** GameHelper could not found resource id #" + resId + ". " +
                    "This probably happened because you included it as a stand-alone JAR. " +
                    "BaseGameUtils should be compiled as a LIBRARY PROJECT, so that it can access " +
                    "its resources. Using a fallback string.")
            return FALLBACK_STRINGS[whichString]
        }

    }
}
