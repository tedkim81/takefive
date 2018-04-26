package com.teuskim.takefive.common;

import org.json.JSONException;
import org.json.JSONObject;

import com.teuskim.takefive.BuildConfig;

public class Log {
	
	public static final String TAG = "TakeFive";

    public static void d(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void d(String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, msg, tr);
        }
    }

    public static void e(String msg) {
        android.util.Log.e(TAG, msg);
    }
    
    public static void test(String msg) {
    	if (BuildConfig.DEBUG) {
    		android.util.Log.e("TEST", msg);
    	}
    }
    
    public static void test(String msg, Throwable tr) {
    	if (BuildConfig.DEBUG) {
    		android.util.Log.e("TEST", msg, tr);
    	}
    }

    public static void e(String msg, Throwable tr) {
        android.util.Log.e(TAG, msg, tr);
    }

    public static void i(JSONObject json) {
        if (BuildConfig.DEBUG) {
            try {
                android.util.Log.i(TAG, json.toString(4));
            } catch (JSONException e) {
            }
        }
    }

    public static void i(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void i(String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, msg, tr);
        }
    }

    public static void v(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(TAG, msg);
        }
    }

    public static void v(String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(TAG, msg, tr);
        }
    }

    public static void w(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, msg);
        }
    }

    public static void w(String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, msg, tr);
        }
    }

}
