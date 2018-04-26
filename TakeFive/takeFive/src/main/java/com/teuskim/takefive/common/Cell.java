package com.teuskim.takefive.common;

import android.graphics.Point;

public class Cell {

	private int x, y;
	private boolean isUserTurn;
	
	public Cell(Point point, boolean isUserTurn) {
		this.x = point.x;
		this.y = point.y;
		this.isUserTurn = isUserTurn;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public boolean isUserTurn() {
		return isUserTurn;
	}

}
