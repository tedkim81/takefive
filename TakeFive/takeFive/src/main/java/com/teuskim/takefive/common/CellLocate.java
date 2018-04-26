package com.teuskim.takefive.common;

public class CellLocate {
	private int row, col;
	private int point;
	
	public CellLocate() {}
	
	public CellLocate(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public CellLocate(int row, int col, int point) {
		this(row, col);
		this.point = point;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	@Override
	public boolean equals(Object o) {
		CellLocate other = (CellLocate) o;
		return (this.row==other.row && this.col==other.col);
	}

	@Override
	public int hashCode() {
		return row+(col*10000);
	}

	@Override
	public String toString() {
		return "row:"+row+", col:"+col;
	}
	
}