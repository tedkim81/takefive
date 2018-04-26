package com.teuskim.takefive.common;

public class OmokLastData {

	private Pattern pattern;
	private int srow, scol, diffRow, diffCol;
	private boolean isWin;
	
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public void setResultPositions(int srow, int scol, int diffRow, int diffCol) {
		this.srow = srow;
		this.scol = scol;
		this.diffRow = diffRow;
		this.diffCol = diffCol;
	}
	
	public int getSrow() {
		return srow;
	}
	
	public int getScol() {
		return scol;
	}
	
	public int getDiffRow() {
		return diffRow;
	}

	public int getDiffCol() {
		return diffCol;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}
	
}