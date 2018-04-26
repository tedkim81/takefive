package com.teuskim.takefive.common

class OmokLastData {

    var pattern: Pattern? = null
    var srow: Int = 0
        private set
    var scol: Int = 0
        private set
    var diffRow: Int = 0
        private set
    var diffCol: Int = 0
        private set
    var isWin: Boolean = false

    fun setResultPositions(srow: Int, scol: Int, diffRow: Int, diffCol: Int) {
        this.srow = srow
        this.scol = scol
        this.diffRow = diffRow
        this.diffCol = diffCol
    }

}