package com.teuskim.takefive.common

import java.util.HashSet
import java.util.Random

import com.teuskim.takefive.view.BoardView

class OmokManager {

    var rowCnt: Int = 0
        private set
    var colCnt: Int = 0
        private set
    var cellSize: Int = 0
    private var initRowCnt: Int = 0
    private var initColCnt: Int = 0
    private var initCellSize: Int = 0
    private val searchAreas: MutableSet<CellLocate>
    private val random: Random
    var lastData: OmokLastData? = null
        private set
    private var turnCnt: Int = 0
    private var difficulty: Int = 0

    var testMap: Array<IntArray>? = null
        private set  // for test

    init {
        searchAreas = HashSet()
        random = Random()
    }

    fun init(rowCnt: Int, colCnt: Int, cellSize: Int) {
        this.rowCnt = rowCnt
        this.initRowCnt = rowCnt
        this.colCnt = colCnt
        this.initColCnt = colCnt
        this.cellSize = cellSize
        this.initCellSize = cellSize
        initSearchAreas()
    }

    private fun initSearchAreas() {
        searchAreas.clear()
        searchAreas.add(CellLocate(rowCnt / 2, colCnt / 2))
    }

    fun didInit(): Boolean {
        return cellSize > 0
    }

    fun setDifficulty(difficulty: Int) {
        this.difficulty = difficulty
    }

    fun convertToTable(list: List<Cell>): Array<Array<CellState>> {
        val table = getEmptyTable(rowCnt, colCnt)
        var col: Int
        var row: Int
        for (cell in list) {
            col = cell.x / cellSize
            row = cell.y / cellSize
            if (cell.isUserTurn) {
                table[row][col] = CellState.USER
            } else {
                table[row][col] = CellState.COM
            }
        }
        return table
    }

    fun getCellLocate(cell: Cell): CellLocate {
        return CellLocate(cell.y / cellSize, cell.x / cellSize)
    }

    fun getOmokResult(table: Array<Array<CellState>>): OmokResult {
        val pf = findPatternsAll(table)
        return if (pf.has(Pattern.COM_CCCCC)) {
            OmokResult.COM_WIN
        } else if (pf.has(Pattern.USER_UUUUU)) {
            OmokResult.USER_WIN
        } else {
            OmokResult.NONE
        }
    }

    fun getOmokResult(list: List<Cell>): OmokResult {
        val table = convertToTable(list)
        return getOmokResult(table)
    }

    fun getComsNextLocate(list: List<Cell>): CellLocate {
        turnCnt = list.size / 2
        return getNextLocate(convertToTable(list))
    }

    fun getUsersNextLocate(list: List<Cell>): CellLocate {
        val df = difficulty
        difficulty = DIFFICULTY_HINT
        val cl = getNextLocate(convertToTable(list))
        difficulty = df
        return cl
    }

    fun getNextLocate(table: Array<Array<CellState>>): CellLocate {
        var resultRow = -1
        var resultCol = -1
        var resultPoint = -1
        var row: Int
        var col: Int

        if (ConstantValues.IS_SHOW_TEST_MAP) {
            testMap = Array(BoardView.cellCnt) { IntArray(BoardView.cellCnt) }  // for test
        }
        for (cl in getSearchAreas()) {
            row = cl.row
            col = cl.col
            if (CellState.NONE == table[row][col]) {
                val point = getPointIfThere(table, row, col)

                if (ConstantValues.IS_SHOW_TEST_MAP) {
                    testMap!![row][col] = point  // for test
                    if (point > 10) {
                        testIfThere(table, row, col)
                    }
                }

                if (point > resultPoint || point == resultPoint && random.nextBoolean()) {
                    resultRow = row
                    resultCol = col
                    resultPoint = point
                }
            }
        }
        Log.test("result point: " + resultPoint)
        return CellLocate(resultRow, resultCol, resultPoint)
    }

    // for test
    private fun testIfThere(table: Array<Array<CellState>>, row: Int, col: Int) {
        Log.test("row:$row, col:$col")

        // COM 공격
        table[row][col] = CellState.COM
        var pf = findPatternsInLines(table, row, col, true)
        pf.logPatternsCntMap()
        table[row][col] = CellState.NONE
        var point = pf.getPoint(true, turnCnt)

        // COM 수비
        table[row][col] = CellState.USER
        pf = findPatternsInLines(table, row, col, false)
        pf.logPatternsCntMap()
        table[row][col] = CellState.NONE
        point = Math.max(point, pf.getPoint(false, turnCnt))

        Log.test("point: " + point)
    }

    fun getPointIfThere(table: Array<Array<CellState>>, row: Int, col: Int): Int {
        // COM 공격 / USER 수비
        table[row][col] = CellState.COM
        var pf = findPatternsInLines(table, row, col, true)
        table[row][col] = CellState.NONE
        val pointIfCom = pf.getPoint(true, turnCnt)

        // COM 수비 / USER 공격
        table[row][col] = CellState.USER
        pf = findPatternsInLines(table, row, col, false)
        table[row][col] = CellState.NONE
        val pointIfUser = pf.getPoint(false, turnCnt)

        val resultPoint: Int
        if (pointIfCom > 100 && pointIfUser > 100) {
            resultPoint = Math.max(pointIfCom, pointIfUser)
        } else {
            resultPoint = pointIfCom + pointIfUser
        }

        return resultPoint
    }

    private fun findPatternsAll(table: Array<Array<CellState>>): PatternFounded {
        val maxCol = colCnt - 1
        val maxRow = rowCnt - 1
        val pf = PatternFounded(difficulty)

        // 가로
        for (i in 0 until rowCnt) {
            pf.add(findPatternsInOneLine(table, i, 0, i, maxCol, true, -1, -1))
            pf.add(findPatternsInOneLine(table, i, 0, i, maxCol, false, -1, -1))
        }

        // 세로
        for (i in 0 until colCnt) {
            pf.add(findPatternsInOneLine(table, 0, i, maxRow, i, true, -1, -1))
            pf.add(findPatternsInOneLine(table, 0, i, maxRow, i, false, -1, -1))
        }

        // 대각선 (좌상-우하)
        val maxCnt = Math.max(colCnt, rowCnt) - 4
        for (i in 0 until maxCnt) {
            // 좌상-우하 대각선의 오른쪽 방향으로
            if (maxCol - i <= maxRow && maxCol - i >= 4) {
                pf.add(findPatternsInOneLine(table, 0, i, maxCol - i, maxCol, true, -1, -1))
                pf.add(findPatternsInOneLine(table, 0, i, maxCol - i, maxCol, false, -1, -1))
            } else if (maxCol - i > maxRow) {
                pf.add(findPatternsInOneLine(table, 0, i, maxRow, maxRow + i, true, -1, -1))
                pf.add(findPatternsInOneLine(table, 0, i, maxRow, maxRow + i, false, -1, -1))
            }
            // 좌상-우하 대각선의 왼쪽 방향으로
            if (maxCol + i <= maxRow) {
                pf.add(findPatternsInOneLine(table, i, 0, maxCol + i, maxCol, true, -1, -1))
                pf.add(findPatternsInOneLine(table, i, 0, maxCol + i, maxCol, false, -1, -1))
            } else if (maxCol + i > maxRow && maxRow - i >= 4) {
                pf.add(findPatternsInOneLine(table, i, 0, maxRow, maxRow - i, true, -1, -1))
                pf.add(findPatternsInOneLine(table, i, 0, maxRow, maxRow - i, false, -1, -1))
            }
        }

        // 대각선 (우상-좌하)
        for (i in 0 until maxCnt) {
            // 우상-좌하 대각선의 왼쪽 방향으로
            if (maxCol - i <= maxRow && maxCol - i >= 4) {
                pf.add(findPatternsInOneLine(table, 0, maxCol - i, maxCol - i, 0, true, -1, -1))
                pf.add(findPatternsInOneLine(table, 0, maxCol - i, maxCol - i, 0, false, -1, -1))
            } else if (maxCol - i > maxRow) {
                pf.add(findPatternsInOneLine(table, 0, maxCol - i, maxRow, maxCol - i - maxRow, true, -1, -1))
                pf.add(findPatternsInOneLine(table, 0, maxCol - i, maxRow, maxCol - i - maxRow, false, -1, -1))
            }
            // 우상-좌하 대각선의 오른쪽 방향으로
            if (maxCol + i <= maxRow) {
                pf.add(findPatternsInOneLine(table, i, maxCol, maxCol + i, 0, true, -1, -1))
                pf.add(findPatternsInOneLine(table, i, maxCol, maxCol + i, 0, false, -1, -1))
            } else if (maxCol + i > maxRow && maxRow - i >= 4) {
                pf.add(findPatternsInOneLine(table, i, maxCol, maxRow, maxCol + i - maxRow, true, -1, -1))
                pf.add(findPatternsInOneLine(table, i, maxCol, maxRow, maxCol + i - maxRow, false, -1, -1))
            }
        }
        return pf
    }

    fun findPatternsInLines(table: Array<Array<CellState>>, row: Int, col: Int, isCom: Boolean): PatternFounded {
        val maxRow = rowCnt - 1
        val maxCol = colCnt - 1
        val pf = PatternFounded(difficulty)

        // 가로
        pf.add(findPatternsInOneLine(table, row, 0, row, maxCol, isCom, row, col))

        // 세로
        pf.add(findPatternsInOneLine(table, 0, col, maxRow, col, isCom, row, col))

        var scol: Int
        var srow: Int
        var ecol: Int
        var erow: Int

        // 대각선 (좌상-우하)
        if (row > col) {
            scol = 0
            srow = row - col
        } else {
            scol = col - row
            srow = 0
        }
        if (maxRow - row > maxCol - col) {
            ecol = maxCol
            erow = row + (maxCol - col)
        } else {
            ecol = col + (maxRow - row)
            erow = maxRow
        }
        pf.add(findPatternsInOneLine(table, srow, scol, erow, ecol, isCom, row, col))

        // 대각선 (우상-좌하)
        if (row > maxCol - col) {
            scol = maxCol
            srow = row - (maxCol - col)
        } else {
            scol = col + row
            srow = 0
        }
        if (maxRow - row > col) {
            ecol = 0
            erow = row + col
        } else {
            ecol = col - (maxRow - row)
            erow = maxRow
        }
        pf.add(findPatternsInOneLine(table, srow, scol, erow, ecol, isCom, row, col))

        return pf
    }

    fun findPatternsInOneLine(table: Array<Array<CellState>>, srow: Int, scol: Int, erow: Int, ecol: Int, isCom: Boolean, crow: Int, ccol: Int): PatternFounded {
        val patternMap: Map<Pattern, Array<CellState>>?
        val opponent: CellState

        if (isCom) {
            patternMap = PatternFounded.comPatternMap
            opponent = CellState.USER
        } else {
            patternMap = PatternFounded.userPatternMap
            opponent = CellState.COM
        }

        val diffCol = if (ecol > scol) 1 else if (ecol < scol) -1 else 0
        val diffRow = if (erow > srow) 1 else if (erow < srow) -1 else 0
        val pf = PatternFounded(difficulty)

        for ((patternName, pattern) in patternMap!!) {

            if (opponent == pattern[0]) {
                val matchStartCL = CellLocate()
                if (isMatch(table, pattern, true, opponent, srow, scol, diffRow, diffCol, crow, ccol, matchStartCL)) {
                    pf.increase(patternName)
                    if (Pattern.COM_CCCCC == patternName || Pattern.USER_UUUUU == patternName) {
                        lastData = OmokLastData()
                        lastData!!.pattern = patternName
                        lastData!!.setResultPositions(matchStartCL.row, matchStartCL.col, diffRow, diffCol)
                        lastData!!.isWin = Pattern.USER_UUUUU == patternName
                    }
                }
            }

            var col = scol
            var row = srow
            while (col != ecol || row != erow) {
                val matchStartCL = CellLocate()
                if (isMatch(table, pattern, false, opponent, row, col, diffRow, diffCol, crow, ccol, matchStartCL)) {
                    pf.increase(patternName)
                    if (Pattern.COM_CCCCC == patternName || Pattern.USER_UUUUU == patternName) {
                        lastData = OmokLastData()
                        lastData!!.pattern = patternName
                        lastData!!.setResultPositions(matchStartCL.row, matchStartCL.col, diffRow, diffCol)
                        lastData!!.isWin = Pattern.USER_UUUUU == patternName
                    }
                }
                col += diffCol
                row += diffRow
            }
        }
        return pf
    }

    private fun isMatch(table: Array<Array<CellState>>, pattern: Array<CellState>, patternFirstSkip: Boolean, opponent: CellState, srow: Int, scol: Int, diffRow: Int, diffCol: Int, crow: Int, ccol: Int, matchStartCL: CellLocate): Boolean {
        var patternFirstSkip = patternFirstSkip
        var srow = srow
        var scol = scol
        var isInclude = false
        for (cs in pattern) {
            if (patternFirstSkip) {
                patternFirstSkip = false
                continue
            }
            if (isOutOfBounds(table, srow, scol)) {
                return cs == opponent  // opponent는 pattern의 처음이나 마지막에만 나온다는 전제가 깔려있다.
            } else if (cs == table[srow][scol] == false) {
                return false
            } else if (crow == -1 || ccol == -1 || srow == crow && scol == ccol) {
                if (isInclude == false) {
                    matchStartCL.row = srow
                    matchStartCL.col = scol
                }
                isInclude = true
            }
            srow += diffRow
            scol += diffCol
        }
        return isInclude
    }

    private fun isOutOfBounds(table: Array<Array<CellState>>, row: Int, col: Int): Boolean {
        return if (row < 0 || col < 0 || row >= rowCnt || col >= colCnt) {
            true
        } else false
    }

    fun addSearchArea(cl: CellLocate) {
        val srow = if (cl.row - 2 >= 0) cl.row - 2 else 0
        val scol = if (cl.col - 2 >= 0) cl.col - 2 else 0
        val erow = if (cl.row + 2 < rowCnt) cl.row + 2 else rowCnt - 1
        val ecol = if (cl.col + 2 < colCnt) cl.col + 2 else colCnt - 1

        for (row in srow..erow) {
            for (col in scol..ecol) {
                searchAreas.add(CellLocate(row, col))
            }
        }
    }

    fun getSearchAreas(): Set<CellLocate> {
        return searchAreas
    }

    fun reset() {
        initSearchAreas()
        rowCnt = initRowCnt
        colCnt = initColCnt
        cellSize = initCellSize
    }

    fun expand(expandedRowCnt: Int, expandedColCnt: Int) {
        if (expandedRowCnt < rowCnt || expandedColCnt < colCnt) {
            return
        }

        val incRow = (expandedRowCnt - rowCnt) / 2
        val incCol = (expandedColCnt - colCnt) / 2
        for (cl in searchAreas) {
            cl.row = cl.row + incRow
            cl.col = cl.col + incCol
        }

        if (ConstantValues.IS_SHOW_TEST_MAP) {
            // for test
            val exTestMap = Array(expandedRowCnt) { IntArray(expandedColCnt) }
            for (row in 0 until rowCnt) {
                for (col in 0 until colCnt) {
                    exTestMap[row + incRow][col + incCol] = testMap!![row][col]
                }
            }
            testMap = exTestMap
        }

        rowCnt = expandedRowCnt
        colCnt = expandedColCnt
    }

    companion object {

        val DIFFICULTY_0 = 0
        val DIFFICULTY_1 = 1
        val DIFFICULTY_2 = 2
        val DIFFICULTY_3 = 3
        val DIFFICULTY_HINT = 100

        fun getEmptyTable(row: Int, col: Int): Array<Array<CellState>> {
            val table = Array<Array<CellState>>(row) { Array<CellState>(col){ CellState.NONE } }
            return table
        }
    }

}
