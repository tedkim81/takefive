package com.teuskim.takefive.common;

import android.content.res.Resources;

public class MiscUtil {

	private static float dpToPxRatio = -1;

	/**
	 * DP 값을 PX 값으로 변환
	 */
	public static int convertDpToPx(float dp, Resources r) {
		if (dpToPxRatio < 0) {
			dpToPxRatio = r.getDisplayMetrics().density;
		}

		int px = Math.round(dp * dpToPxRatio);
		return px;
	}
}
