package com.teuskim.takefive.common

import android.content.res.Resources
import com.teuskim.takefive.common.MiscUtil.dpToPxRatio

object MiscUtil {

    private var dpToPxRatio = -1f

    /**
     * DP 값을 PX 값으로 변환
     */
    fun convertDpToPx(dp: Float, r: Resources): Int {
        if (dpToPxRatio < 0) {
            dpToPxRatio = r.displayMetrics.density
        }

        return Math.round(dp * dpToPxRatio)
    }
}
