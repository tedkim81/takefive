package com.teuskim.takefive.common

class CellLocate {
    var row: Int = 0
    var col: Int = 0
    var point: Int = 0

    constructor() {}

    constructor(row: Int, col: Int) {
        this.row = row
        this.col = col
    }

    constructor(row: Int, col: Int, point: Int) : this(row, col) {
        this.point = point
    }

    override fun equals(o: Any?): Boolean {
        val other = o as CellLocate?
        return this.row == other!!.row && this.col == other.col
    }

    override fun hashCode(): Int {
        return row + col * 10000
    }

    override fun toString(): String {
        return "row:$row, col:$col"
    }

}