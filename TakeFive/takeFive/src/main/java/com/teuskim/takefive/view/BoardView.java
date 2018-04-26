package com.teuskim.takefive.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.teuskim.takefive.R;
import com.teuskim.takefive.common.BgmManager;
import com.teuskim.takefive.common.Cell;
import com.teuskim.takefive.common.ConstantValues;
import com.teuskim.takefive.common.GameSoundManager;
import com.teuskim.takefive.common.Material;
import com.teuskim.takefive.common.MiscUtil;
import com.teuskim.takefive.common.OmokLastData;
import com.teuskim.takefive.common.OnIntResultListener;

public class BoardView extends View {
	
	public static final int CELL_CNT_NORMAL = 12;
	public static final int CELL_CNT_EXPANDED = 18;
	
	private static final int DURATION_MOVE = 500;
	private static final int DURATION_DELAY = 500;
	
	private int[][] testMap;  // for test
	
	public static int cellCnt;
	private List<Cell> cellList;
	private Cell lastCell;
	private Paint userMarkPaint, comMarkPaint, guideLinePaint, guideCirclePaint, textPaint, boardLinePaint;
	private Paint completeTextPaint, hintLinePaint, costPaint;
	private int cellSize, padding;
	private float textWidth;
	private boolean isShowGuideLine;
	private OnIntResultListener onIntResultListener;
	private boolean isLocked;
	private int[][] costsMap;
	private int bottomCost;
	private Material[][] materialsMap;
	private Map<Material, Paint> materialPaintMap;
	private UFO ufoCom, ufoUser;
	private float scaleBefore, scaleNormal;
	private GameSoundManager gsManager;
	private Handler handler;
	private boolean isUserTurn;
	private List<Cell> omokCompleteList;
	private Cell hintCell;
	private Drawable imgHintPosition, markUser, markCom;
	private DrawInfo[][] drawInfoMap;
	private Drawable imgComplete;
	
	private ValueAnimator.AnimatorUpdateListener animUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			invalidate();
		}
	};
	
	public BoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public BoardView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		cellCnt = CELL_CNT_NORMAL;
		setBackgroundColor(0xfffdefb2);
		cellList = new ArrayList<Cell>();
		userMarkPaint = new Paint();
		userMarkPaint.setColor(Color.WHITE);
		comMarkPaint = new Paint();
		comMarkPaint.setColor(Color.BLACK);
		guideLinePaint = new Paint();
		guideCirclePaint = new Paint();
		guideCirclePaint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint();
		textPaint.setColor(0xff766c63);
		isShowGuideLine = false;
		boardLinePaint = new Paint();
		boardLinePaint.setColor(0xff968c83);
		boardLinePaint.setStrokeWidth(MiscUtil.convertDpToPx(1, getResources()));
		completeTextPaint = new Paint();
		completeTextPaint.setColor(Color.WHITE);
		hintLinePaint = new Paint();
		hintLinePaint.setColor(0xcc009999);
		costPaint = new Paint();
		costPaint.setColor(0xff766c63);
		imgHintPosition = getResources().getDrawable(R.drawable.img_hint_position);
		imgHintPosition.setBounds(0, 0, imgHintPosition.getIntrinsicWidth(), imgHintPosition.getIntrinsicHeight());
		markUser = getResources().getDrawable(R.drawable.mark_white);
		markUser.setBounds(0, 0, markUser.getIntrinsicWidth(), markUser.getIntrinsicHeight());
		markCom = getResources().getDrawable(R.drawable.mark_black);
		markCom.setBounds(0, 0, markCom.getIntrinsicWidth(), markCom.getIntrinsicHeight());
		
		ufoCom = new UFO(getResources().getDrawable(R.drawable.ufo_black_big_on)
						, getResources().getDrawable(R.drawable.ufo_black_big_off));
		ufoUser = new UFO(getResources().getDrawable(R.drawable.ufo_white_big_on)
						, getResources().getDrawable(R.drawable.ufo_white_big_off));
		
		onIntResultListener = new OnIntResultListener() {
			@Override public void onIntResult(int result) {}
		};
		
		materialPaintMap = new HashMap<Material, Paint>();
		Paint p1 = new Paint();
		p1.setColor(0xffe8523e);
		materialPaintMap.put(Material.NUM1, p1);
		Paint p2 = new Paint();
		p2.setColor(0xff85d698);
		materialPaintMap.put(Material.NUM2, p2);
		Paint p3 = new Paint();
		p3.setColor(0xff7ecef4);
		materialPaintMap.put(Material.NUM3, p3);
		gsManager = GameSoundManager.getInstance(context);
		handler = new Handler();
		omokCompleteList = new ArrayList<Cell>();
		imgComplete = getResources().getDrawable(R.drawable.img_complete);
		imgComplete.setBounds(0, 0, imgComplete.getIntrinsicWidth(), imgComplete.getIntrinsicHeight());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isLocked) {
			return false;
		}
		
		int x = (int)event.getX();
		int y = (int)event.getY();
		Point point = getAvailablePoint(x, y);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isShowGuideLine = true;
			if (point != null) {
				gsManager.playBoardTouch();
				lastCell = new Cell(point, true);
			} else {
				return false;
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (lastCell == null) {
				return false;
			} else if (point != null) {
				lastCell.setX(point.x);
				lastCell.setY(point.y);
			} else {
				if (cellList.size() > 0) {
					lastCell = cellList.get(cellList.size()-1);
				} else {
					lastCell = null;
				}
				isShowGuideLine = false;
				invalidate();
				return false;
			}
			break;
			
		case MotionEvent.ACTION_UP:
			if (lastCell == null) {
				return false;
			} else if (isAddablePosition(cellList, lastCell)) {
				setIsLocked(true);
				cellList.add(lastCell);
				hintCell = null;
				onIntResultListener.onIntResult(2);
				moveUfo(ufoUser, new Animator.AnimatorListener() {
					@Override public void onAnimationStart(Animator animation) {}
					@Override public void onAnimationRepeat(Animator animation) {}
					@Override public void onAnimationCancel(Animator animation) {}
					
					@Override
					public void onAnimationEnd(Animator animation) {
						setIsShowGuideLine(false);
						onIntResultListener.onIntResult(1);
					}
				});
			} else if (point != null) {
				setIsShowGuideLine(false);
				onIntResultListener.onIntResult(0);
			}
			break;
		}
		invalidate();
		return true;
	}
	
	public void setIsShowGuideLine(boolean isShowGuideLine) {
		this.isShowGuideLine = isShowGuideLine;
	}
	
	public void setIsUserTurn(boolean isUserTurn) {
		this.isUserTurn = isUserTurn;
		ufoUser.setIsOn(isUserTurn);
		ufoCom.setIsOn(!isUserTurn);
	}
	
	private void moveUfo(UFO ufo, Animator.AnimatorListener animListener) {
		AnimatorSet as = new AnimatorSet();
		as.addListener(animListener);
		
		if (ufo.isHidden()) {
			ValueAnimator animScale = ObjectAnimator.ofFloat(ufo, "scale", scaleBefore, scaleNormal);
			animScale.setDuration(DURATION_MOVE);
			animScale.addUpdateListener(animUpdateListener);
			
			as.play(animScale);
			
			ufo.setX(lastCell.getX());
			ufo.setY(lastCell.getY());
			
		} else {
			ValueAnimator animX = ObjectAnimator.ofFloat(ufo, "x", ufo.getX(), lastCell.getX());
			animX.setDuration(DURATION_MOVE);
			animX.addUpdateListener(animUpdateListener);
			
			ValueAnimator animY = ObjectAnimator.ofFloat(ufo, "y", ufo.getY(), lastCell.getY());
			animY.setDuration(DURATION_MOVE);
			
			as.play(animX).with(animY);
		}
		
		as.setInterpolator(new DecelerateInterpolator(3.0f));
		as.start();
		invalidate();
		
		gsManager.playUfoMove();
	}
	
	private boolean isAddablePosition(List<Cell> list, Cell c) {
		for (Cell c2 : list) {
			if (c.getX() == c2.getX() && c.getY() == c2.getY()) {
				return false;
			}
		}
		return true;
	}

	private Point getAvailablePoint(int x, int y) {
		int csize = getCellSize();
		int padding = getPadding();
		return getPoint((int)((x-padding) / csize), (int)((y-padding) / csize));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!ConstantValues.IS_SHOW_TEST_MAP) {
			// 비용맵 또는 재료맵 보기
			drawCosts(canvas);
			drawMaterials(canvas);
		}
		
		// 지나간 마킹들
		drawMarks(canvas);
		
		// 오목판 선들
		drawBoardLines(canvas);
		
		// 터치중일때 위치확인 위한 가이드선 ( ufo보다 전에 그린다. )
		drawGuideCircle(canvas);
		
		// 현재 ufo위치들
		drawUfos(canvas);

		// 훈수 받았을때 표시
		drawHint(canvas);
				
		// 터치중일때 위치확인 위한 가이드선
		drawGuideLine(canvas);
		
		// 오목이 완성되었을때 완성된 오목 표시
		drawOmokComplete(canvas);
		
		if (ConstantValues.IS_SHOW_TEST_MAP) {
			// for test : 컴의 다음수를 결정하기위해 계산된 점수들 보기
			drawTestPoints(canvas);
		}
	}
	
	private void drawCosts(Canvas canvas) {
		if (costsMap == null) {
			return;
		}
		if (drawInfoMap == null) {
			drawInfoMap = new DrawInfo[cellCnt][cellCnt];
			
			int csize = getCellSize();
			float textSize = textPaint.getTextSize();
			int offsetX = (int)((csize-textWidth)/2);
			int offsetY = (int)(textSize+((csize-textSize)/2));
			int x,y,alpha;
			int padding = getPadding();
			for (int row=0; row<cellCnt; row++) {
				for (int col=0; col<cellCnt; col++) {
					x = col*csize+padding;
					y = row*csize+padding;
					alpha = (costsMap[row][col]-bottomCost) * 15;
					costPaint.setAlpha(alpha);
					canvas.drawRect(x, y, x+csize, y+csize, costPaint);
					canvas.drawText(""+costsMap[row][col], x+offsetX, y+offsetY, textPaint);
					
					drawInfoMap[row][col] = new DrawInfo(x, y, x+csize, y+csize, x+offsetX, y+offsetY, alpha);
				}
			}
		} else {
			for (int row=0; row<cellCnt; row++) {
				for (int col=0; col<cellCnt; col++) {
					DrawInfo di = drawInfoMap[row][col];
					costPaint.setAlpha(di.alpha);
					canvas.drawRect(di.left, di.top, di.right, di.bottom, costPaint);
					canvas.drawText(""+costsMap[row][col], di.x, di.y, textPaint);
				}
			}
		}
	}
	
	private void drawMaterials(Canvas canvas) {
		// 재료들 그리기
		if (materialsMap == null) {
			return;
		}
		guideLinePaint.setColor(0xaa999999);
		guideCirclePaint.setColor(0xcc999999);
		if (drawInfoMap == null) {
			drawInfoMap = new DrawInfo[cellCnt][cellCnt];
			
			int csize = getCellSize();
			int offset = getPadding();
			float left, top, right, bottom;
			for (int row=0; row<cellCnt; row++) {
				for (int col=0; col<cellCnt; col++) {
					left = col*csize+offset;
					top = row*csize+offset;
					right = left+csize;
					bottom = top+csize;
					canvas.drawRect(left, top, right, bottom, materialPaintMap.get(materialsMap[row][col]));
					
					drawInfoMap[row][col] = new DrawInfo(left, top, right, bottom);
				}
			}
		} else {
			DrawInfo di;
			for (int row=0; row<cellCnt; row++) {
				for (int col=0; col<cellCnt; col++) {
					di = drawInfoMap[row][col];
					canvas.drawRect(di.left, di.top, di.right, di.bottom, materialPaintMap.get(materialsMap[row][col]));
				}
			}
		}
	}
	
	private void drawBoardLines(Canvas canvas) {
		int padding = getPadding();
		for (int i=padding; i<getWidth(); i+=getCellSize()) {
			canvas.drawLine(i, padding, i, getHeight()-padding, boardLinePaint);
		}
		for (int i=padding; i<getHeight(); i+=getCellSize()) {
			canvas.drawLine(padding, i, getWidth()-padding, i, boardLinePaint);
		}
	}
	
	private void drawMarks(Canvas canvas) {
		int cellSize = getCellSize();
		Cell cell;
		Drawable d;
		for (int i=0; i<cellList.size()-2; i++) {
			cell = cellList.get(i);
			if (cell.isUserTurn()) {
				d = markUser;
			} else {
				d = markCom;
			}
			canvas.save();
			canvas.translate(cell.getX(), cell.getY());
			float scale = cellSize / (float)d.getIntrinsicWidth();
			canvas.scale(scale, scale);
			d.draw(canvas);
			canvas.restore();
		}
	}
	
	private void drawUfos(Canvas canvas) {
		/* 왜 이렇게 했었는지 기억이 나지 않는다. 일단 뒀다가 추후 삭제하자.
		if (lastCell != null) {
			if (lastCell.isUserTurn()) {
				drawUfo(canvas, ufoCom);
				drawUfo(canvas, ufoUser);
			} else {
				drawUfo(canvas, ufoUser);
				drawUfo(canvas, ufoCom);
			}
		}
		*/
		
		if (isUserTurn) {
			drawUfo(canvas, ufoCom);
			drawUfo(canvas, ufoUser);
		} else {
			drawUfo(canvas, ufoUser);
			drawUfo(canvas, ufoCom);
		}
	}
	
	private void drawUfo(Canvas canvas, UFO ufo) {
		float offset = (ufo.getWidth() - getCellSize()) / 2;
		
		if (ufo.isHidden() == false) {
			canvas.save();
			canvas.translate(ufo.getX()-offset, ufo.getY()-offset);
			canvas.scale(ufo.getScale(), ufo.getScale());
			ufo.getD().draw(canvas);
			canvas.restore();
		}
	}
	
	private void drawGuideLine(Canvas canvas) {
		if (isShowGuideLine) {
			int mid = getCellSize()/2;
			int x = lastCell.getX()+mid;
			int y = lastCell.getY()+mid;
			canvas.drawLine(0, y, getWidth(), y, guideLinePaint);
			canvas.drawLine(x, 0, x, getHeight(), guideLinePaint);
		}
	}
	
	private void drawGuideCircle(Canvas canvas) {
		if (isShowGuideLine) {
			int mid = getCellSize()/2;
			int x = lastCell.getX()+mid;
			int y = lastCell.getY()+mid;
			canvas.drawCircle(x, y, getCellSize()*0.5f, guideCirclePaint);
			canvas.drawCircle(x, y, getCellSize()*1.5f, guideCirclePaint);
		}
	}
	
	private void drawOmokComplete(Canvas canvas) {
		if (omokCompleteList.size() > 0) {
			int size = getCellSize();
			float textSize = completeTextPaint.getTextSize();
			int offset = (int)(size*0.33);
			for (int i=0; i<omokCompleteList.size(); i++) {
				Cell c = omokCompleteList.get(i);
				
				canvas.save();
				canvas.translate(c.getX(), c.getY());
				float scale = cellSize / (float)imgComplete.getIntrinsicWidth();
				canvas.scale(scale, scale);
				imgComplete.draw(canvas);
				canvas.restore();
				
				canvas.drawText(""+(i+1), c.getX()+offset, c.getY()+textSize+(int)(offset/4.4), completeTextPaint);
			}
		}
	}
	
	private void drawHint(Canvas canvas) {
		if (hintCell != null) {
			int size = getCellSize();
			
			canvas.save();
			canvas.translate(hintCell.getX(), hintCell.getY());
			float scale = size / (float)imgHintPosition.getIntrinsicWidth();
			canvas.scale(scale, scale);
			imgHintPosition.draw(canvas);
			canvas.restore();
			
			int mid = size/2;
			int cx = hintCell.getX()+mid;
			int cy = hintCell.getY()+mid;
			canvas.drawLine(cx, 0, cx, cy, hintLinePaint);
		}
	}
	
	private void drawTestPoints(Canvas canvas) {
		if (testMap == null) {
			return;
		}
		int size = getCellSize();
		float textSize = textPaint.getTextSize();
		int offset = 5;
		for (int row=0; row<cellCnt; row++) {
			for (int col=0; col<cellCnt; col++) {
				if (testMap[row][col] > 0) {
					canvas.drawText(""+testMap[row][col], col*size+offset, row*size+textSize+offset, textPaint);
				}
			}
		}
	}

	public int getCellSize() {
		if (cellSize == 0) {
			initValues();
		}
		return cellSize;
	}
	
	public int getPadding() {
		if (padding == 0) {
			initValues();
		}
		return padding;
	}
	
	private void initValues() {
		padding = getWidth() / 40;
		cellSize = (getWidth()-(padding*2)) / cellCnt;
		padding = (getWidth() - (cellSize*cellCnt)) / 2;
		textPaint.setTextSize(cellSize/2);
		textWidth = textPaint.measureText("0");
		completeTextPaint.setTextSize(cellSize/1.5f);
		
		float ufoWidth = (float)ufoUser.getD().getIntrinsicWidth();
		float ufoHeight = (float)ufoUser.getD().getIntrinsicHeight();
		
		scaleBefore = 5.0f;
		scaleNormal = cellSize / ufoWidth * 1.4f;
		
		ufoCom.setScale(scaleNormal);
		ufoCom.setWidth(ufoWidth * scaleNormal);
		ufoCom.setHeight(ufoHeight * scaleNormal);
		ufoUser.setScale(scaleNormal);
		ufoUser.setWidth(ufoWidth * scaleNormal);
		ufoUser.setHeight(ufoHeight * scaleNormal);
		
		guideLinePaint.setStrokeWidth(cellSize/10);
		guideCirclePaint.setStrokeWidth(cellSize/2);
		
		hintLinePaint.setStrokeWidth(cellSize/15);
	}

	public void addComChoice(int col, int row, final Animator.AnimatorListener animListener) {
		lastCell = new Cell(getPoint(col, row), false);
		cellList.add(lastCell);
		
		isShowGuideLine = true;
		invalidate();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				moveUfo(ufoCom, animListener);
			}
		}, DURATION_DELAY);
	}
	
	public void showHint(int col, int row) {
		hintCell = new Cell(getPoint(col, row), true);
		invalidate();
	}
	
	public void hideHint() {
		hintCell = null;
		invalidate();
	}
	
	private Point getPoint(int col, int row) {
		if (col >= 0 && col < cellCnt && row >= 0 && row < cellCnt) {
			int csize = getCellSize();
			int padding = getPadding();
			int x = (col * csize) + padding;
			int y = (row * csize) + padding;
			return new Point(x, y);
		}
		return null;
	}

	public void setOnIntResultListener(OnIntResultListener onIntResultListener) {
		this.onIntResultListener = onIntResultListener;
	}
	
	public List<Cell> getCellList() {
		return cellList;
	}
	
	public void setCellList(List<Cell> cellList) {
		this.cellList = cellList;
	}
	
	public boolean goBackOneStep() {
		if (cellList.size() < 2) {
			return false;
		}
		cellList.remove(cellList.size()-1);
		cellList.remove(cellList.size()-1);
		
		if (cellList.size() >= 2) {
			Cell c = cellList.get(cellList.size()-2);
			lastCell = cellList.get(cellList.size()-1);
			ufoUser.setX(c.getX());
			ufoUser.setY(c.getY());
			ufoCom.setX(lastCell.getX());
			ufoCom.setY(lastCell.getY());
		} else {
			lastCell = null;
			ufoUser.init();
			ufoCom.init();
		}
		
		lastData = null;
		omokCompleteList.clear();
		
		invalidate();
		return true;
	}

	public void reset() {
		cellCnt = CELL_CNT_NORMAL;
		cellSize = 0;
		cellList.clear();
		lastCell = null;
		ufoUser.init();
		ufoCom.init();
		omokCompleteList.clear();
		hintCell = null;
		drawInfoMap = null;
		guideLinePaint.setColor(0x55ff0000);
		guideCirclePaint.setColor(0x77ff0000);
		invalidate();
	}
	
	public void expand() {
		int inc = (CELL_CNT_EXPANDED - CELL_CNT_NORMAL) / 2;
		int cellSizeNormal = getCellSize();
		cellCnt = CELL_CNT_EXPANDED;
		initValues();
		
		Point p;
		for (Cell c : cellList) {
			p = getPoint((c.getX()/cellSizeNormal)+inc, (c.getY()/cellSizeNormal)+inc);
			c.setX(p.x);
			c.setY(p.y);
		}
		
		p = getPoint((int)((ufoUser.getX()/cellSizeNormal)+inc), (int)((ufoUser.getY()/cellSizeNormal)+inc));
		ufoUser.setX(p.x);
		ufoUser.setY(p.y);
		
		p = getPoint((int)((ufoCom.getX()/cellSizeNormal)+inc), (int)((ufoCom.getY()/cellSizeNormal)+inc));
		ufoCom.setX(p.x);
		ufoCom.setY(p.y);
		
		invalidate();
	}
	
	public boolean isExpanded() {
		return (cellCnt == CELL_CNT_EXPANDED);
	}
	
	public void setIsLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	public void setCostsMap(int[][] costsMap) {
		this.costsMap = costsMap;
		drawInfoMap = null;
	}
	
	public int[][] getCostsMap() {
		return costsMap;
	}
	
	public void setBottomCost(int bottomCost) {
		this.bottomCost = bottomCost;
	}
	
	public Material[][] getMaterialsMap() {
		return materialsMap;
	}

	public void setMaterialsMap(Material[][] materialsMap) {
		this.materialsMap = materialsMap;
		drawInfoMap = null;
	}

	public void setTestPointsMap(int[][] testMap) {
		this.testMap = testMap;
	}
	
	public boolean needToExpand() {
		return (isExpanded()==false && cellList.size() > 40);
	}
	
	public void animateOmokComplete(OmokLastData lastData) {
		animateOmokCompleteRecur(lastData, 0);
	}
	
	private OmokLastData lastData;
	private void animateOmokCompleteRecur(OmokLastData pLastData, final int num) {
		this.lastData = pLastData;
		if (num >= 5) {
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					if (lastData == null) {
						return;
					}
					if (lastData.isWin()) {
						gsManager.playMissionClear();
					} else {
						gsManager.playMissionFail();
					}
				}
			}, DURATION_DELAY);
			return;
		}
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (lastData == null) {
					return;
				}
				gsManager.playCrash();
				omokCompleteList.add(new Cell(getPoint((lastData.getScol()+(lastData.getDiffCol()*num)), (lastData.getSrow()+(lastData.getDiffRow()*num))), isUserTurn));
				invalidate();
				animateOmokCompleteRecur(lastData, num+1);
			}
		}, DURATION_DELAY);
	}
	
	
	class UFO {
		private Drawable don, doff;
		private float x, y;
		private float scale;
		private float width, height;
		private boolean isOn;
		
		public UFO(Drawable don, Drawable doff) {
			this.don = don;
			this.don.setBounds(0, 0, don.getIntrinsicWidth(), don.getIntrinsicHeight());
			this.doff = doff;
			this.doff.setBounds(0, 0, doff.getIntrinsicWidth(), doff.getIntrinsicHeight());
			init();
		}
		
		public void init() {
			x = Float.MIN_VALUE;
			y = Float.MIN_VALUE;
		}
		
		public boolean isHidden() {
			if (x == Float.MIN_VALUE || y == Float.MIN_VALUE) {
				return true;
			}
			return false;
		}
		
		public void setIsOn(boolean isOn) {
			this.isOn = isOn;
		}
		
		public Drawable getD() {
			if (isOn) {
				return don;
			} else {
				return doff;
			}
		}

		public float getX() {
			if (scale > scaleNormal) {
				return x - (((getD().getIntrinsicWidth()*scale)-width)/2);
			}
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			if (scale > scaleNormal) {
				return y - (((getD().getIntrinsicHeight()*scale)-height)/2);
			}
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}
		
		public void setScale(float scale) {
			this.scale = scale; 
		}
		
		public float getScale() {
			return scale;
		}

		public float getWidth() {
			return width;
		}

		public void setWidth(float width) {
			this.width = width;
		}

		public float getHeight() {
			return height;
		}

		public void setHeight(float height) {
			this.height = height;
		}
		
	}
	
	class DrawInfo {
		float left, top, right, bottom, x, y;
		int alpha;
		
		public DrawInfo(float left, float top, float right, float bottom, float x, float y, int alpha) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.x = x;
			this.y = y;
			this.alpha = alpha;
		}
		
		public DrawInfo(float left, float top, float right, float bottom) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}
	}
	
}
