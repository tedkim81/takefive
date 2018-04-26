package com.teuskim.takefive.common;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.teuskim.takefive.view.BoardView;

public class OmokManager {
	
	private int rowCnt, colCnt, cellSize;
	private int initRowCnt, initColCnt, initCellSize;
	private Set<CellLocate> searchAreas;
	private Random random;
	private OmokLastData lastData;
	private int turnCnt;
	
	public static final int DIFFICULTY_0 = 0;
	public static final int DIFFICULTY_1 = 1;
	public static final int DIFFICULTY_2 = 2;
	public static final int DIFFICULTY_3 = 3;
	public static final int DIFFICULTY_HINT = 100;
	private int difficulty;
	
	private int[][] testMap;  // for test
	public int[][] getTestMap() {
		return testMap;
	}
	
	public OmokManager() {
		searchAreas = new HashSet<CellLocate>();
		random = new Random();
	}
	
	public void init(int rowCnt, int colCnt, int cellSize) {
		this.rowCnt = rowCnt;
		this.initRowCnt = rowCnt;
		this.colCnt = colCnt;
		this.initColCnt = colCnt;
		this.cellSize = cellSize;
		this.initCellSize = cellSize;
		initSearchAreas();
	}
	
	private void initSearchAreas() {
		searchAreas.clear();
		searchAreas.add(new CellLocate(rowCnt/2, colCnt/2));
	}
	
	public boolean didInit() {
		return (cellSize > 0);
	}

	public int getRowCnt() {
		return rowCnt;
	}
	
	public int getColCnt() {
		return colCnt;
	}

	public int getCellSize() {
		return cellSize;
	}
	
	public void setCellSize(int cellSize) {
		this.cellSize = cellSize;
	}
	
	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
	
	public CellState[][] convertToTable(List<Cell> list) {
		CellState[][] table = getEmptyTable(rowCnt, colCnt);
		int col, row;
		for (Cell cell : list) {
			col = cell.getX() / cellSize;
			row = cell.getY() / cellSize;
			if (cell.isUserTurn()) {
				table[row][col] = CellState.USER;
			} else {
				table[row][col] = CellState.COM;
			}
		}
		return table;
	}
	
	public CellLocate getCellLocate(Cell cell) {
		return new CellLocate((cell.getY() / cellSize), (cell.getX() / cellSize));
	}
	
	public static CellState[][] getEmptyTable(int row, int col) {
		CellState[][] table = new CellState[row][col];
		for (int i=0; i<row; i++) {
			for (int j=0; j<col; j++) {
				table[i][j] = CellState.NONE;
			}
		}
		return table;
	}
	
	public OmokResult getOmokResult(CellState[][] table) {
		PatternFounded pf = findPatternsAll(table);
		if (pf.has(Pattern.COM_CCCCC)) {
			return OmokResult.COM_WIN;
		} else if (pf.has(Pattern.USER_UUUUU)) {
			return OmokResult.USER_WIN;
		} else {
			return OmokResult.NONE;
		}
	}

	public OmokResult getOmokResult(List<Cell> list) {
		CellState[][] table = convertToTable(list);
		return getOmokResult(table);
	}
	
	public OmokLastData getLastData() {
		return lastData;
	}
	
	public CellLocate getComsNextLocate(List<Cell> list) {
		turnCnt = list.size() / 2;
		return getNextLocate(convertToTable(list));
	}
	
	public CellLocate getUsersNextLocate(List<Cell> list) {
		int df = difficulty;
		difficulty = DIFFICULTY_HINT;
		CellLocate cl = getNextLocate(convertToTable(list));
		difficulty = df;
		return cl;
	}

	public CellLocate getNextLocate(CellState[][] table) {
		int resultRow = -1;
		int resultCol = -1;
		int resultPoint = -1;
		int row,col;
		
		if (ConstantValues.IS_SHOW_TEST_MAP) {
			testMap = new int[BoardView.cellCnt][BoardView.cellCnt];  // for test
		}
		for (CellLocate cl : getSearchAreas()) {
			row = cl.getRow();
			col = cl.getCol();
			if (CellState.NONE.equals(table[row][col])) {
				int point = getPointIfThere(table, row, col);
				
				if (ConstantValues.IS_SHOW_TEST_MAP) {
					testMap[row][col] = point;  // for test
					if (point > 10) {
						testIfThere(table, row, col);
					}
				}
				
				if (point > resultPoint || (point == resultPoint && random.nextBoolean())) {
					resultRow = row;
					resultCol = col;
					resultPoint = point;
				}
			}
		}
		Log.test("result point: "+resultPoint);
		return new CellLocate(resultRow, resultCol, resultPoint);
	}
	
	// for test
	private void testIfThere(CellState[][] table, int row, int col) {
		Log.test("row:"+row+", col:"+col);
		
		// COM 공격
		table[row][col] = CellState.COM;
		PatternFounded pf = findPatternsInLines(table, row, col, true);
		pf.logPatternsCntMap();
		table[row][col] = CellState.NONE;
		int point = pf.getPoint(true, turnCnt);
		
		// COM 수비
		table[row][col] = CellState.USER;
		pf = findPatternsInLines(table, row, col, false);
		pf.logPatternsCntMap();
		table[row][col] = CellState.NONE;
		point = Math.max(point, pf.getPoint(false, turnCnt));
		
		Log.test("point: "+point);
	}
	
	public int getPointIfThere(CellState[][] table, int row, int col) {
		// COM 공격 / USER 수비
		table[row][col] = CellState.COM;
		PatternFounded pf = findPatternsInLines(table, row, col, true);
		table[row][col] = CellState.NONE;
		int pointIfCom = pf.getPoint(true, turnCnt);
		
		// COM 수비 / USER 공격
		table[row][col] = CellState.USER;
		pf = findPatternsInLines(table, row, col, false);
		table[row][col] = CellState.NONE;
		int pointIfUser = pf.getPoint(false, turnCnt);
		
		int resultPoint;
		if (pointIfCom > 100 && pointIfUser > 100) {
			resultPoint = Math.max(pointIfCom, pointIfUser);
		} else {
			resultPoint = pointIfCom + pointIfUser;
		}
		
		return resultPoint;
	}

	private PatternFounded findPatternsAll(CellState[][] table) {
		int maxCol = colCnt - 1;
		int maxRow = rowCnt - 1;
		PatternFounded pf = new PatternFounded(difficulty);
		
		// 가로
		for (int i=0; i<rowCnt; i++) {
			pf.add(findPatternsInOneLine(table, i, 0, i, maxCol, true, -1, -1));
			pf.add(findPatternsInOneLine(table, i, 0, i, maxCol, false, -1, -1));
		}
		
		// 세로
		for (int i=0; i<colCnt; i++) {
			pf.add(findPatternsInOneLine(table, 0, i, maxRow, i, true, -1, -1));
			pf.add(findPatternsInOneLine(table, 0, i, maxRow, i, false, -1, -1));
		}
		
		// 대각선 (좌상-우하)
		int maxCnt = Math.max(colCnt, rowCnt) - 4;
		for (int i=0; i<maxCnt; i++) {
			// 좌상-우하 대각선의 오른쪽 방향으로
			if (maxCol-i <= maxRow && maxCol-i >= 4) {
				pf.add(findPatternsInOneLine(table, 0, i, maxCol-i, maxCol, true, -1, -1));
				pf.add(findPatternsInOneLine(table, 0, i, maxCol-i, maxCol, false, -1, -1));
			} else if (maxCol-i > maxRow) {
				pf.add(findPatternsInOneLine(table, 0, i, maxRow, maxRow+i, true, -1, -1));
				pf.add(findPatternsInOneLine(table, 0, i, maxRow, maxRow+i, false, -1, -1));
			}
			// 좌상-우하 대각선의 왼쪽 방향으로
			if (maxCol+i <= maxRow) {
				pf.add(findPatternsInOneLine(table, i, 0, maxCol+i, maxCol, true, -1, -1));
				pf.add(findPatternsInOneLine(table, i, 0, maxCol+i, maxCol, false, -1, -1));
			} else if (maxCol+i > maxRow && maxRow-i >= 4) {
				pf.add(findPatternsInOneLine(table, i, 0, maxRow, maxRow-i, true, -1, -1));
				pf.add(findPatternsInOneLine(table, i, 0, maxRow, maxRow-i, false, -1, -1));
			}
		}
		
		// 대각선 (우상-좌하)
		for (int i=0; i<maxCnt; i++) {
			// 우상-좌하 대각선의 왼쪽 방향으로
			if (maxCol-i <= maxRow && maxCol-i >= 4) {
				pf.add(findPatternsInOneLine(table, 0, maxCol-i, maxCol-i, 0, true, -1, -1));
				pf.add(findPatternsInOneLine(table, 0, maxCol-i, maxCol-i, 0, false, -1, -1));
			} else if (maxCol-i > maxRow) {
				pf.add(findPatternsInOneLine(table, 0, maxCol-i, maxRow, maxCol-i-maxRow, true, -1, -1));
				pf.add(findPatternsInOneLine(table, 0, maxCol-i, maxRow, maxCol-i-maxRow, false, -1, -1));
			}
			// 우상-좌하 대각선의 오른쪽 방향으로
			if (maxCol+i <= maxRow) {
				pf.add(findPatternsInOneLine(table, i, maxCol, maxCol+i, 0, true, -1, -1));
				pf.add(findPatternsInOneLine(table, i, maxCol, maxCol+i, 0, false, -1, -1));
			} else if (maxCol+i > maxRow && maxRow-i >= 4) {
				pf.add(findPatternsInOneLine(table, i, maxCol, maxRow, maxCol+i-maxRow, true, -1, -1));
				pf.add(findPatternsInOneLine(table, i, maxCol, maxRow, maxCol+i-maxRow, false, -1, -1));
			}
		}
		return pf;
	}
	
	public PatternFounded findPatternsInLines(CellState[][] table, int row, int col, boolean isCom) {
		int maxRow = rowCnt - 1;
		int maxCol = colCnt - 1;
		PatternFounded pf = new PatternFounded(difficulty);
		
		// 가로
		pf.add(findPatternsInOneLine(table, row, 0, row, maxCol, isCom, row, col));
		
		// 세로
		pf.add(findPatternsInOneLine(table, 0, col, maxRow, col, isCom, row, col));
		
		int scol, srow, ecol, erow;
		
		// 대각선 (좌상-우하)
		if (row > col) {
			scol = 0;
			srow = row-col;
		} else {
			scol = col-row;
			srow = 0;
		}
		if (maxRow-row > maxCol-col) {
			ecol = maxCol;
			erow = row + (maxCol-col);
		} else {
			ecol = col + (maxRow-row);
			erow = maxRow;
		}
		pf.add(findPatternsInOneLine(table, srow, scol, erow, ecol, isCom, row, col));
		
		// 대각선 (우상-좌하)
		if (row > maxCol-col) {
			scol = maxCol;
			srow = row - (maxCol-col);
		} else {
			scol = col + row;
			srow = 0;
		}
		if (maxRow-row > col) {
			ecol = 0;
			erow = row + col;
		} else {
			ecol = col - (maxRow-row);
			erow = maxRow;
		}
		pf.add(findPatternsInOneLine(table, srow, scol, erow, ecol, isCom, row, col));
		
		return pf;
	}

	public PatternFounded findPatternsInOneLine(CellState[][] table, int srow, int scol, int erow, int ecol, boolean isCom, int crow, int ccol) {
		Map<Pattern, CellState[]> patternMap;
		CellState opponent;
		
		if (isCom) {
			patternMap = PatternFounded.getComPatternMap();
			opponent = CellState.USER;
		} else {
			patternMap = PatternFounded.getUserPatternMap();
			opponent = CellState.COM;
		}
		
		int diffCol = ecol>scol ? 1 : (ecol<scol ? (-1) : 0);
		int diffRow = erow>srow ? 1 : (erow<srow ? (-1) : 0);
		PatternFounded pf = new PatternFounded(difficulty);
		
		for (Entry<Pattern, CellState[]> entry : patternMap.entrySet()) {
			Pattern patternName = entry.getKey();
			CellState[] pattern = entry.getValue();
			
			if (opponent.equals(pattern[0])) {
				CellLocate matchStartCL = new CellLocate();
				if (isMatch(table, pattern, true, opponent, srow, scol, diffRow, diffCol, crow, ccol, matchStartCL)) {
					pf.increase(patternName);
					if (Pattern.COM_CCCCC.equals(patternName) || Pattern.USER_UUUUU.equals(patternName)) {
						lastData = new OmokLastData();
						lastData.setPattern(patternName);
						lastData.setResultPositions(matchStartCL.getRow(), matchStartCL.getCol(), diffRow, diffCol);
						lastData.setWin(Pattern.USER_UUUUU.equals(patternName));
					}
				}
			}
			
			int col = scol;
			int row = srow;
			while (col!=ecol || row!=erow) {
				CellLocate matchStartCL = new CellLocate();
				if (isMatch(table, pattern, false, opponent, row, col, diffRow, diffCol, crow, ccol, matchStartCL)) {
					pf.increase(patternName);
					if (Pattern.COM_CCCCC.equals(patternName) || Pattern.USER_UUUUU.equals(patternName)) {
						lastData = new OmokLastData();
						lastData.setPattern(patternName);
						lastData.setResultPositions(matchStartCL.getRow(), matchStartCL.getCol(), diffRow, diffCol);
						lastData.setWin(Pattern.USER_UUUUU.equals(patternName));
					}
				}
				col += diffCol;
				row += diffRow;
			}
		}
		return pf;
	}

	private boolean isMatch(CellState[][] table, CellState[] pattern, boolean patternFirstSkip, CellState opponent, int srow, int scol, int diffRow, int diffCol, int crow, int ccol, CellLocate matchStartCL) {
		boolean isInclude = false;
		for (CellState cs : pattern) {
			if (patternFirstSkip) {
				patternFirstSkip = false;
				continue;
			}
			if (isOutOfBounds(table, srow, scol)) {
				return cs.equals(opponent);  // opponent는 pattern의 처음이나 마지막에만 나온다는 전제가 깔려있다.
			} else if (cs.equals(table[srow][scol])==false) {
				return false;
			} else if (crow==-1 || ccol==-1 || (srow==crow && scol==ccol)) {
				if (isInclude == false) {
					matchStartCL.setRow(srow);
					matchStartCL.setCol(scol);
				}
				isInclude = true;
			}
			srow += diffRow;
			scol += diffCol;
		}
		return isInclude;
	}
	
	private boolean isOutOfBounds(CellState[][] table, int row, int col) {
		if (row < 0 || col < 0 || row >= rowCnt || col >= colCnt) {
			return true;
		}
		return false;
	}

	public void addSearchArea(CellLocate cl) {
		int srow = (cl.getRow()-2)>=0 ? (cl.getRow()-2) : 0;
		int scol = (cl.getCol()-2)>=0 ? (cl.getCol()-2) : 0;
		int erow = (cl.getRow()+2)<rowCnt ? (cl.getRow()+2) : (rowCnt-1);
		int ecol = (cl.getCol()+2)<colCnt ? (cl.getCol()+2) : (colCnt-1);
		
		for (int row=srow; row<=erow; row++) {
			for (int col=scol; col<=ecol; col++) {
				searchAreas.add(new CellLocate(row, col));
			}
		}
	}

	public Set<CellLocate> getSearchAreas() {
		return searchAreas;
	}

	public void reset() {
		initSearchAreas();
		rowCnt = initRowCnt;
		colCnt = initColCnt;
		cellSize = initCellSize;
	}
	
	public void expand(int expandedRowCnt, int expandedColCnt) {
		if (expandedRowCnt < rowCnt || expandedColCnt < colCnt) {
			return;
		}
		
		int incRow = (expandedRowCnt - rowCnt) / 2;
		int incCol = (expandedColCnt - colCnt) / 2;
		for (CellLocate cl : searchAreas) {
			cl.setRow(cl.getRow() + incRow);
			cl.setCol(cl.getCol() + incCol);
		}
		
		if (ConstantValues.IS_SHOW_TEST_MAP) {
			// for test
			int[][] exTestMap = new int[expandedRowCnt][expandedColCnt];
			for (int row=0; row<rowCnt; row++) {
				for (int col=0; col<colCnt; col++) {
					exTestMap[row+incRow][col+incCol] = testMap[row][col];
				}
			}
			testMap = exTestMap;
		}
		
		rowCnt = expandedRowCnt;
		colCnt = expandedColCnt;
	}
	
}
