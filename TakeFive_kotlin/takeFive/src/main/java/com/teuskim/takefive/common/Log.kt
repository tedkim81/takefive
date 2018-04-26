package com.teuskim.takefive.common

import org.json.JSONException
import org.json.JSONObject

import com.teuskim.takefive.BuildConfig

object Log {

    val TAG = "TakeFive"

    fun d(msg: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, msg)
        }
    }

    fun d(msg: String, tr: Throwable) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, msg, tr)
        }
    }

    fun e(msg: String) {
        android.util.Log.e(TAG, msg)
    }

    fun test(msg: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e("TEST", msg)
        }
    }

    fun test(msg: String, tr: Throwable) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e("TEST", msg, tr)
        }
    }

    fun e(msg: String, tr: Throwable) {
        android.util.Log.e(TAG, msg, tr)
    }

    fun i(json: JSONObject) {
        if (BuildConfig.DEBUG) {
            try {
                android.util.Log.i(TAG, json.toString(4))
            } catch (e: JSONException) {
            }

        }
    }

    fun i(msg: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, msg)
        }
    }

    fun i(msg: String, tr: Throwable) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, msg, tr)
        }
    }

    fun v(msg: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(TAG, msg)
        }
    }

    fun v(msg: String, tr: Throwable) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(TAG, msg, tr)
        }
    }

    fun w(msg: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, msg)
        }
    }

    fun w(msg: String, tr: Throwable) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, msg, tr)
        }
    }

}
