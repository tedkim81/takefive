package com.teuskim.takefive.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import java.util.ArrayList
import java.util.HashMap

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

import com.teuskim.takefive.R
import com.teuskim.takefive.common.Cell
import com.teuskim.takefive.common.ConstantValues
import com.teuskim.takefive.common.GameSoundManager
import com.teuskim.takefive.common.Material
import com.teuskim.takefive.common.MiscUtil
import com.teuskim.takefive.common.OmokLastData
import com.teuskim.takefive.common.OnIntResultListener

class BoardView : View {

    private var testMap: Array<IntArray>? = null  // for test
    var cellList: MutableList<Cell>? = null
    private var lastCell: Cell? = null
    private var userMarkPaint: Paint? = null
    private var comMarkPaint: Paint? = null
    private var guideLinePaint: Paint? = null
    private var guideCirclePaint: Paint? = null
    private var textPaint: Paint? = null
    private var boardLinePaint: Paint? = null
    private var completeTextPaint: Paint? = null
    private var hintLinePaint: Paint? = null
    private var costPaint: Paint? = null
    private var cellSize: Int = 0
    private var padding: Int = 0
    private var textWidth: Float = 0.toFloat()
    private var isShowGuideLine: Boolean = false
    private var onIntResultListener: OnIntResultListener? = null
    private var isLocked: Boolean = false
    var costsMap: Array<IntArray>? = null
        set(costsMap) {
            field = costsMap
            drawInfoMap = null
        }
    private var bottomCost: Int = 0
    var materialsMap: Array<Array<Material?>>? = null
        set(materialsMap) {
            field = materialsMap
            drawInfoMap = null
        }
    private var materialPaintMap: MutableMap<Material, Paint>? = null
    private var ufoCom: UFO? = null
    private var ufoUser: UFO? = null
    private var scaleBefore: Float = 0.toFloat()
    private var scaleNormal: Float = 0.toFloat()
    private var gsManager: GameSoundManager? = null
    private var isUserTurn: Boolean = false
    private var omokCompleteList: MutableList<Cell>? = null
    private var hintCell: Cell? = null
    private var imgHintPosition: Drawable? = null
    private var markUser: Drawable? = null
    private var markCom: Drawable? = null
    private var drawInfoMap: Array<Array<DrawInfo?>>? = null
    private var imgComplete: Drawable? = null

    private val animUpdateListener = ValueAnimator.AnimatorUpdateListener { invalidate() }

    val isExpanded: Boolean
        get() = cellCnt == CELL_CNT_EXPANDED

    private var lastData: OmokLastData? = null

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        cellCnt = CELL_CNT_NORMAL
        setBackgroundColor(-0x2104e)
        cellList = ArrayList()
        userMarkPaint = Paint()
        userMarkPaint!!.color = Color.WHITE
        comMarkPaint = Paint()
        comMarkPaint!!.color = Color.BLACK
        guideLinePaint = Paint()
        guideCirclePaint = Paint()
        guideCirclePaint!!.style = Paint.Style.STROKE
        textPaint = Paint()
        textPaint!!.color = -0x89939d
        isShowGuideLine = false
        boardLinePaint = Paint()
        boardLinePaint!!.color = -0x69737d
        boardLinePaint!!.strokeWidth = MiscUtil.convertDpToPx(1f, resources).toFloat()
        completeTextPaint = Paint()
        completeTextPaint!!.color = Color.WHITE
        hintLinePaint = Paint()
        hintLinePaint!!.color = -0x33ff6667
        costPaint = Paint()
        costPaint!!.color = -0x89939d
        imgHintPosition = resources.getDrawable(R.drawable.img_hint_position)
        imgHintPosition!!.setBounds(0, 0, imgHintPosition!!.intrinsicWidth, imgHintPosition!!.intrinsicHeight)
        markUser = resources.getDrawable(R.drawable.mark_white)
        markUser!!.setBounds(0, 0, markUser!!.intrinsicWidth, markUser!!.intrinsicHeight)
        markCom = resources.getDrawable(R.drawable.mark_black)
        markCom!!.setBounds(0, 0, markCom!!.intrinsicWidth, markCom!!.intrinsicHeight)

        ufoCom = UFO(resources.getDrawable(R.drawable.ufo_black_big_on), resources.getDrawable(R.drawable.ufo_black_big_off))
        ufoUser = UFO(resources.getDrawable(R.drawable.ufo_white_big_on), resources.getDrawable(R.drawable.ufo_white_big_off))

        onIntResultListener = object: OnIntResultListener {
            override fun onIntResult(result: Int) {}
        }

        materialPaintMap = HashMap()
        val p1 = Paint()
        p1.color = -0x17adc2
        materialPaintMap!![Material.NUM1] = p1
        val p2 = Paint()
        p2.color = -0x7a2968
        materialPaintMap!![Material.NUM2] = p2
        val p3 = Paint()
        p3.color = -0x81310c
        materialPaintMap!![Material.NUM3] = p3
        gsManager = GameSoundManager.getInstance(context)
        omokCompleteList = ArrayList()
        imgComplete = resources.getDrawable(R.drawable.img_complete)
        imgComplete!!.setBounds(0, 0, imgComplete!!.intrinsicWidth, imgComplete!!.intrinsicHeight)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLocked) {
            return false
        }

        val x = event.x.toInt()
        val y = event.y.toInt()
        val point = getAvailablePoint(x, y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isShowGuideLine = true
                if (point != null) {
                    gsManager!!.playBoardTouch()
                    lastCell = Cell(point, true)
                } else {
                    return false
                }
            }

            MotionEvent.ACTION_MOVE -> if (lastCell == null) {
                return false
            } else if (point != null) {
                lastCell!!.x = point.x
                lastCell!!.y = point.y
            } else {
                if (cellList!!.size > 0) {
                    lastCell = cellList!![cellList!!.size - 1]
                } else {
                    lastCell = null
                }
                isShowGuideLine = false
                invalidate()
                return false
            }

            MotionEvent.ACTION_UP -> if (lastCell == null) {
                return false
            } else if (isAddablePosition(cellList, lastCell!!)) {
                setIsLocked(true)
                cellList!!.add(lastCell!!)
                hintCell = null
                onIntResultListener!!.onIntResult(2)
                moveUfo(ufoUser, object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        setIsShowGuideLine(false)
                        onIntResultListener!!.onIntResult(1)
                    }
                })
            } else if (point != null) {
                setIsShowGuideLine(false)
                onIntResultListener!!.onIntResult(0)
            }
        }
        invalidate()
        return true
    }

    fun setIsShowGuideLine(isShowGuideLine: Boolean) {
        this.isShowGuideLine = isShowGuideLine
    }

    fun setIsUserTurn(isUserTurn: Boolean) {
        this.isUserTurn = isUserTurn
        ufoUser!!.setIsOn(isUserTurn)
        ufoCom!!.setIsOn(!isUserTurn)
    }

    private fun moveUfo(ufo: UFO?, animListener: Animator.AnimatorListener) {
        val `as` = AnimatorSet()
        `as`.addListener(animListener)

        if (ufo!!.isHidden) {
            val animScale = ObjectAnimator.ofFloat(ufo, "scale", scaleBefore, scaleNormal)
            animScale.duration = DURATION_MOVE.toLong()
            animScale.addUpdateListener(animUpdateListener)

            `as`.play(animScale)

            ufo.setX(lastCell!!.x.toFloat())
            ufo.setY(lastCell!!.y.toFloat())

        } else {
            val animX = ObjectAnimator.ofFloat(ufo, "x", ufo.getX(), lastCell!!.x.toFloat())
            animX.setDuration(DURATION_MOVE.toLong())
            animX.addUpdateListener(animUpdateListener)

            val animY = ObjectAnimator.ofFloat(ufo, "y", ufo.getY(), lastCell!!.y.toFloat())
            animY.setDuration(DURATION_MOVE.toLong())

            `as`.play(animX)!!.with(animY)
        }

        `as`.setInterpolator(DecelerateInterpolator(3.0f))
        `as`.start()
        invalidate()

        gsManager!!.playUfoMove()
    }

    private fun isAddablePosition(list: List<Cell>?, c: Cell): Boolean {
        for (c2 in list!!) {
            if (c.x == c2.x && c.y == c2.y) {
                return false
            }
        }
        return true
    }

    private fun getAvailablePoint(x: Int, y: Int): Point? {
        val csize = getCellSize()
        val padding = getPadding()
        return getPoint((x - padding) / csize, (y - padding) / csize)
    }

    override fun onDraw(canvas: Canvas) {
        if (!ConstantValues.IS_SHOW_TEST_MAP) {
            // 비용맵 또는 재료맵 보기
            drawCosts(canvas)
            drawMaterials(canvas)
        }

        // 지나간 마킹들
        drawMarks(canvas)

        // 오목판 선들
        drawBoardLines(canvas)

        // 터치중일때 위치확인 위한 가이드선 ( ufo보다 전에 그린다. )
        drawGuideCircle(canvas)

        // 현재 ufo위치들
        drawUfos(canvas)

        // 훈수 받았을때 표시
        drawHint(canvas)

        // 터치중일때 위치확인 위한 가이드선
        drawGuideLine(canvas)

        // 오목이 완성되었을때 완성된 오목 표시
        drawOmokComplete(canvas)

        if (ConstantValues.IS_SHOW_TEST_MAP) {
            // for test : 컴의 다음수를 결정하기위해 계산된 점수들 보기
            drawTestPoints(canvas)
        }
    }

    private fun drawCosts(canvas: Canvas) {
        if (this.costsMap == null) {
            return
        }
        if (drawInfoMap == null) {
            drawInfoMap = Array(cellCnt) { arrayOfNulls<DrawInfo?>(cellCnt) }

            val csize = getCellSize()
            val textSize = textPaint!!.textSize
            val offsetX = ((csize - textWidth) / 2).toInt()
            val offsetY = (textSize + (csize - textSize) / 2).toInt()
            var x: Int
            var y: Int
            var alpha: Int
            val padding = getPadding()
            for (row in 0 until cellCnt) {
                for (col in 0 until cellCnt) {
                    x = col * csize + padding
                    y = row * csize + padding
                    alpha = (this.costsMap!![row][col] - bottomCost) * 15
                    costPaint!!.alpha = alpha
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + csize).toFloat(), (y + csize).toFloat(), costPaint!!)
                    canvas.drawText("" + this.costsMap!![row][col], (x + offsetX).toFloat(), (y + offsetY).toFloat(), textPaint!!)

                    drawInfoMap!![row][col] = DrawInfo(x.toFloat(), y.toFloat(), (x + csize).toFloat(), (y + csize).toFloat(), (x + offsetX).toFloat(), (y + offsetY).toFloat(), alpha)
                }
            }
        } else {
            for (row in 0 until cellCnt) {
                for (col in 0 until cellCnt) {
                    val di = drawInfoMap!![row][col]
                    if (di != null) {
                        costPaint!!.alpha = di.alpha
                        canvas.drawRect(di.left, di.top, di.right, di.bottom, costPaint!!)
                        canvas.drawText("" + this.costsMap!![row][col], di.x, di.y, textPaint!!)
                    }
                }
            }
        }
    }

    private fun drawMaterials(canvas: Canvas) {
        // 재료들 그리기
        if (this.materialsMap == null) {
            return
        }
        guideLinePaint!!.color = -0x55666667
        guideCirclePaint!!.color = -0x33666667
        if (drawInfoMap == null) {
            drawInfoMap = Array(cellCnt) { arrayOfNulls<DrawInfo?>(cellCnt) }

            val csize = getCellSize()
            val offset = getPadding()
            var left: Float
            var top: Float
            var right: Float
            var bottom: Float
            for (row in 0 until cellCnt) {
                for (col in 0 until cellCnt) {
                    left = (col * csize + offset).toFloat()
                    top = (row * csize + offset).toFloat()
                    right = left + csize
                    bottom = top + csize
                    canvas.drawRect(left, top, right, bottom, materialPaintMap!![this.materialsMap!![row][col]])

                    drawInfoMap!![row][col] = DrawInfo(left, top, right, bottom)
                }
            }
        } else {
            var di: DrawInfo
            for (row in 0 until cellCnt) {
                for (col in 0 until cellCnt) {
                    di = drawInfoMap!![row][col]!!
                    canvas.drawRect(di.left, di.top, di.right, di.bottom, materialPaintMap!![this.materialsMap!![row][col]])
                }
            }
        }
    }

    private fun drawBoardLines(canvas: Canvas) {
        val padding = getPadding()
        run {
            var i = padding
            while (i < width) {
                canvas.drawLine(i.toFloat(), padding.toFloat(), i.toFloat(), (height - padding).toFloat(), boardLinePaint!!)
                i += getCellSize()
            }
        }
        var i = padding
        while (i < height) {
            canvas.drawLine(padding.toFloat(), i.toFloat(), (width - padding).toFloat(), i.toFloat(), boardLinePaint!!)
            i += getCellSize()
        }
    }

    private fun drawMarks(canvas: Canvas) {
        val cellSize = getCellSize()
        var cell: Cell
        var d: Drawable
        for (i in 0 until cellList!!.size - 2) {
            cell = cellList!![i]
            if (cell.isUserTurn) {
                d = markUser!!
            } else {
                d = markCom!!
            }
            canvas.save()
            canvas.translate(cell.x.toFloat(), cell.y.toFloat())
            val scale = cellSize / d.intrinsicWidth.toFloat()
            canvas.scale(scale, scale)
            d.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawUfos(canvas: Canvas) {
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
            drawUfo(canvas, ufoCom)
            drawUfo(canvas, ufoUser)
        } else {
            drawUfo(canvas, ufoUser)
            drawUfo(canvas, ufoCom)
        }
    }

    private fun drawUfo(canvas: Canvas, ufo: UFO?) {
        val offset = (ufo!!.width - getCellSize()) / 2

        if (ufo.isHidden == false) {
            canvas.save()
            canvas.translate(ufo.getX() - offset, ufo.getY() - offset)
            canvas.scale(ufo.scale, ufo.scale)
            ufo.d.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawGuideLine(canvas: Canvas) {
        if (isShowGuideLine) {
            val mid = getCellSize() / 2
            val x = lastCell!!.x + mid
            val y = lastCell!!.y + mid
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), guideLinePaint!!)
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), guideLinePaint!!)
        }
    }

    private fun drawGuideCircle(canvas: Canvas) {
        if (isShowGuideLine) {
            val mid = getCellSize() / 2
            val x = lastCell!!.x + mid
            val y = lastCell!!.y + mid
            canvas.drawCircle(x.toFloat(), y.toFloat(), getCellSize() * 0.5f, guideCirclePaint!!)
            canvas.drawCircle(x.toFloat(), y.toFloat(), getCellSize() * 1.5f, guideCirclePaint!!)
        }
    }

    private fun drawOmokComplete(canvas: Canvas) {
        if (omokCompleteList!!.size > 0) {
            val size = getCellSize()
            val textSize = completeTextPaint!!.textSize
            val offset = (size * 0.33).toInt()
            for (i in omokCompleteList!!.indices) {
                val c = omokCompleteList!![i]

                canvas.save()
                canvas.translate(c.x.toFloat(), c.y.toFloat())
                val scale = cellSize / imgComplete!!.intrinsicWidth.toFloat()
                canvas.scale(scale, scale)
                imgComplete!!.draw(canvas)
                canvas.restore()

                canvas.drawText("" + (i + 1), (c.x + offset).toFloat(), c.y.toFloat() + textSize + (offset / 4.4).toInt().toFloat(), completeTextPaint!!)
            }
        }
    }

    private fun drawHint(canvas: Canvas) {
        if (hintCell != null) {
            val size = getCellSize()

            canvas.save()
            canvas.translate(hintCell!!.x.toFloat(), hintCell!!.y.toFloat())
            val scale = size / imgHintPosition!!.intrinsicWidth.toFloat()
            canvas.scale(scale, scale)
            imgHintPosition!!.draw(canvas)
            canvas.restore()

            val mid = size / 2
            val cx = hintCell!!.x + mid
            val cy = hintCell!!.y + mid
            canvas.drawLine(cx.toFloat(), 0f, cx.toFloat(), cy.toFloat(), hintLinePaint!!)
        }
    }

    private fun drawTestPoints(canvas: Canvas) {
        if (testMap == null) {
            return
        }
        val size = getCellSize()
        val textSize = textPaint!!.textSize
        val offset = 5
        for (row in 0 until cellCnt) {
            for (col in 0 until cellCnt) {
                if (testMap!![row][col] > 0) {
                    canvas.drawText("" + testMap!![row][col], (col * size + offset).toFloat(), (row * size).toFloat() + textSize + offset.toFloat(), textPaint!!)
                }
            }
        }
    }

    fun getCellSize(): Int {
        if (cellSize == 0) {
            initValues()
        }
        return cellSize
    }

    fun getPadding(): Int {
        if (padding == 0) {
            initValues()
        }
        return padding
    }

    private fun initValues() {
        padding = width / 40
        cellSize = (width - padding * 2) / cellCnt
        padding = (width - cellSize * cellCnt) / 2
        textPaint!!.textSize = (cellSize / 2).toFloat()
        textWidth = textPaint!!.measureText("0")
        completeTextPaint!!.textSize = cellSize / 1.5f

        val ufoWidth = ufoUser!!.d.intrinsicWidth.toFloat()
        val ufoHeight = ufoUser!!.d.intrinsicHeight.toFloat()

        scaleBefore = 5.0f
        scaleNormal = cellSize / ufoWidth * 1.4f

        ufoCom!!.scale = scaleNormal
        ufoCom!!.width = ufoWidth * scaleNormal
        ufoCom!!.height = ufoHeight * scaleNormal
        ufoUser!!.scale = scaleNormal
        ufoUser!!.width = ufoWidth * scaleNormal
        ufoUser!!.height = ufoHeight * scaleNormal

        guideLinePaint!!.strokeWidth = (cellSize / 10).toFloat()
        guideCirclePaint!!.strokeWidth = (cellSize / 2).toFloat()

        hintLinePaint!!.strokeWidth = (cellSize / 15).toFloat()
    }

    fun addComChoice(col: Int, row: Int, animListener: Animator.AnimatorListener) {
        lastCell = Cell(getPoint(col, row), false)
        cellList!!.add(lastCell!!)

        isShowGuideLine = true
        invalidate()
        handler!!.postDelayed({ moveUfo(ufoCom, animListener) }, DURATION_DELAY.toLong())
    }

    fun showHint(col: Int, row: Int) {
        hintCell = Cell(getPoint(col, row), true)
        invalidate()
    }

    fun hideHint() {
        hintCell = null
        invalidate()
    }

    private fun getPoint(col: Int, row: Int): Point? {
        if (col >= 0 && col < cellCnt && row >= 0 && row < cellCnt) {
            val csize = getCellSize()
            val padding = getPadding()
            val x = col * csize + padding
            val y = row * csize + padding
            return Point(x, y)
        }
        return null
    }

    fun setOnIntResultListener(onIntResultListener: OnIntResultListener) {
        this.onIntResultListener = onIntResultListener
    }

//    fun getCellList(): List<Cell>? {
//        return cellList
//    }

//    fun setCellList(cellList: MutableList<Cell>) {
//        this.cellList = cellList
//    }

    fun goBackOneStep(): Boolean {
        if (cellList!!.size < 2) {
            return false
        }
        cellList!!.removeAt(cellList!!.size - 1)
        cellList!!.removeAt(cellList!!.size - 1)

        if (cellList!!.size >= 2) {
            val c = cellList!![cellList!!.size - 2]
            lastCell = cellList!![cellList!!.size - 1]
            ufoUser!!.setX(c.x.toFloat())
            ufoUser!!.setY(c.y.toFloat())
            ufoCom!!.setX(lastCell!!.x.toFloat())
            ufoCom!!.setY(lastCell!!.y.toFloat())
        } else {
            lastCell = null
            ufoUser!!.init()
            ufoCom!!.init()
        }

        lastData = null
        omokCompleteList!!.clear()

        invalidate()
        return true
    }

    fun reset() {
        cellCnt = CELL_CNT_NORMAL
        cellSize = 0
        cellList!!.clear()
        lastCell = null
        ufoUser!!.init()
        ufoCom!!.init()
        omokCompleteList!!.clear()
        hintCell = null
        drawInfoMap = null
        guideLinePaint!!.color = 0x55ff0000
        guideCirclePaint!!.color = 0x77ff0000
        invalidate()
    }

    fun expand() {
        val inc = (CELL_CNT_EXPANDED - CELL_CNT_NORMAL) / 2
        val cellSizeNormal = getCellSize()
        cellCnt = CELL_CNT_EXPANDED
        initValues()

        var p: Point?
        for (c in cellList!!) {
            p = getPoint(c.x / cellSizeNormal + inc, c.y / cellSizeNormal + inc)
            c.x = p!!.x
            c.y = p.y
        }

        p = getPoint((ufoUser!!.getX() / cellSizeNormal + inc).toInt(), (ufoUser!!.getY() / cellSizeNormal + inc).toInt())
        ufoUser!!.setX(p!!.x.toFloat())
        ufoUser!!.setY(p.y.toFloat())

        p = getPoint((ufoCom!!.getX() / cellSizeNormal + inc).toInt(), (ufoCom!!.getY() / cellSizeNormal + inc).toInt())
        ufoCom!!.setX(p!!.x.toFloat())
        ufoCom!!.setY(p.y.toFloat())

        invalidate()
    }

    fun setIsLocked(isLocked: Boolean) {
        this.isLocked = isLocked
    }

    fun setBottomCost(bottomCost: Int) {
        this.bottomCost = bottomCost
    }

    fun setTestPointsMap(testMap: Array<IntArray>) {
        this.testMap = testMap
    }

    fun needToExpand(): Boolean {
        return isExpanded == false && cellList!!.size > 40
    }

    fun animateOmokComplete(lastData: OmokLastData) {
        animateOmokCompleteRecur(lastData, 0)
    }

    private fun animateOmokCompleteRecur(pLastData: OmokLastData?, num: Int) {
        this.lastData = pLastData
        if (num >= 5) {
            handler!!.postDelayed(Runnable {
                if (lastData == null) {
                    return@Runnable
                }
                if (lastData!!.isWin) {
                    gsManager!!.playMissionClear()
                } else {
                    gsManager!!.playMissionFail()
                }
            }, DURATION_DELAY.toLong())
            return
        }
        handler!!.postDelayed(Runnable {
            if (lastData == null) {
                return@Runnable
            }
            gsManager!!.playCrash()
            omokCompleteList!!.add(Cell(getPoint(lastData!!.scol + lastData!!.diffCol * num, lastData!!.srow + lastData!!.diffRow * num), isUserTurn))
            invalidate()
            animateOmokCompleteRecur(lastData, num + 1)
        }, DURATION_DELAY.toLong())
    }


    internal inner class UFO(private val don: Drawable, private val doff: Drawable) {
        private var x: Float = 0.toFloat()
        private var y: Float = 0.toFloat()
        var scale: Float = 0.toFloat()
        var width: Float = 0.toFloat()
        var height: Float = 0.toFloat()
        private var isOn: Boolean = false

        val isHidden: Boolean
            get() = if (x == java.lang.Float.MIN_VALUE || y == java.lang.Float.MIN_VALUE) {
                true
            } else false

        val d: Drawable
            get() = if (isOn) {
                don
            } else {
                doff
            }

        init {
            this.don.setBounds(0, 0, don.intrinsicWidth, don.intrinsicHeight)
            this.doff.setBounds(0, 0, doff.intrinsicWidth, doff.intrinsicHeight)
            init()
        }

        fun init() {
            x = java.lang.Float.MIN_VALUE
            y = java.lang.Float.MIN_VALUE
        }

        fun setIsOn(isOn: Boolean) {
            this.isOn = isOn
        }

        fun getX(): Float {
            return if (scale > scaleNormal) {
                x - (d.intrinsicWidth * scale - width) / 2
            } else x
        }

        fun setX(x: Float) {
            this.x = x
        }

        fun getY(): Float {
            return if (scale > scaleNormal) {
                y - (d.intrinsicHeight * scale - height) / 2
            } else y
        }

        fun setY(y: Float) {
            this.y = y
        }

    }

    internal inner class DrawInfo {
        var left: Float = 0.toFloat()
        var top: Float = 0.toFloat()
        var right: Float = 0.toFloat()
        var bottom: Float = 0.toFloat()
        var x: Float = 0.toFloat()
        var y: Float = 0.toFloat()
        var alpha: Int = 0

        constructor(left: Float, top: Float, right: Float, bottom: Float, x: Float, y: Float, alpha: Int) {
            this.left = left
            this.top = top
            this.right = right
            this.bottom = bottom
            this.x = x
            this.y = y
            this.alpha = alpha
        }

        constructor(left: Float, top: Float, right: Float, bottom: Float) {
            this.left = left
            this.top = top
            this.right = right
            this.bottom = bottom
        }
    }

    companion object {

        val CELL_CNT_NORMAL = 12
        val CELL_CNT_EXPANDED = 18

        private val DURATION_MOVE = 500
        private val DURATION_DELAY = 500

        var cellCnt: Int = 0
    }

}
