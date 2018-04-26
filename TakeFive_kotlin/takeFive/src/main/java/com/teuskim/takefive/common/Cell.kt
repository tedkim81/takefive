package com.teuskim.takefive.common

import android.graphics.Point

class Cell(point: Point?, val isUserTurn: Boolean) {

    var x: Int = 0
    var y: Int = 0

    init {
        if (point != null) {
            this.x = point.x
            this.y = point.y
        }
    }

}
