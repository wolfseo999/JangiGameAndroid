package com.jangi.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class GameBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gameState: GameState? = null
    private var selectedPosition: Position? = null
    private var possibleMoves: List<Position> = emptyList()
    private var onPieceMoveListener: ((Position, Position) -> Unit)? = null
    
    private var boardLeft = 0f
    private var boardTop = 0f
    private var boardWidth = 0f
    private var boardHeight = 0f

    private val boardPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.board_bg)
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.board_line)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val selectedPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.cell_selected)
        style = Paint.Style.FILL
    }

    private val possiblePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.cell_possible)
        style = Paint.Style.FILL
    }

    private val piecePaint = Paint().apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.black)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val pieceNames = mapOf(
        PieceType.KING to "왕",
        PieceType.CHA to "차",
        PieceType.MA to "마",
        PieceType.SANG to "상",
        PieceType.SA to "사",
        PieceType.PO to "포",
        PieceType.JOL to "졸"
    )

    fun setGameState(state: GameState) {
        this.gameState = state
        invalidate()
    }

    fun setOnPieceMoveListener(listener: (Position, Position) -> Unit) {
        this.onPieceMoveListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        
        // 장기판 비율 9:10 (가로:세로)
        val boardRatio = 9f / 10f
        
        // 사용 가능한 공간에서 장기판 크기 계산
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        
        if (availableWidth / availableHeight > boardRatio) {
            // 높이를 기준으로 크기 결정
            boardHeight = availableHeight.toFloat()
            boardWidth = boardHeight * boardRatio
        } else {
            // 너비를 기준으로 크기 결정
            boardWidth = availableWidth.toFloat()
            boardHeight = boardWidth / boardRatio
        }
        
        // 중앙 정렬을 위한 오프셋 계산
        boardLeft = paddingLeft + (availableWidth - boardWidth) / 2f
        boardTop = paddingTop + (availableHeight - boardHeight) / 2f
        
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val state = gameState ?: return

        val cellWidth = boardWidth / 8f
        val cellHeight = boardHeight / 9f

        // 배경 그리기
        canvas.drawRect(boardLeft, boardTop, boardLeft + boardWidth, boardTop + boardHeight, boardPaint)

        // 보드 선 그리기
        linePaint.strokeWidth = 2f
        for (i in 0..9) {
            val y = boardTop + i * cellHeight
            canvas.drawLine(boardLeft, y, boardLeft + boardWidth, y, linePaint)
        }
        for (i in 0..8) {
            val x = boardLeft + i * cellWidth
            canvas.drawLine(x, boardTop, x, boardTop + boardHeight, linePaint)
        }

        // 궁성 그리기
        linePaint.strokeWidth = 3f
        // 한궁 (빨간색 - 하단)
        val hanPalaceLeft = boardLeft + 3 * cellWidth
        val hanPalaceTop = boardTop + 7 * cellHeight
        val hanPalaceRight = boardLeft + 5 * cellWidth
        val hanPalaceBottom = boardTop + 9 * cellHeight
        val hanPalaceCenterX = boardLeft + 4 * cellWidth
        canvas.drawLine(hanPalaceLeft, hanPalaceTop, hanPalaceRight, hanPalaceBottom, linePaint)
        canvas.drawLine(hanPalaceRight, hanPalaceTop, hanPalaceLeft, hanPalaceBottom, linePaint)

        // 초궁 (파란색 - 상단)
        val choPalaceLeft = boardLeft + 3 * cellWidth
        val choPalaceTop = boardTop
        val choPalaceRight = boardLeft + 5 * cellWidth
        val choPalaceBottom = boardTop + 2 * cellHeight
        val choPalaceCenterX = boardLeft + 4 * cellWidth
        canvas.drawLine(choPalaceLeft, choPalaceTop, choPalaceRight, choPalaceBottom, linePaint)
        canvas.drawLine(choPalaceRight, choPalaceTop, choPalaceLeft, choPalaceBottom, linePaint)

        // 선택된 위치 표시
        selectedPosition?.let { pos ->
            val x = boardLeft + pos.col * cellWidth
            val y = boardTop + pos.row * cellHeight
            val radius = minOf(cellWidth, cellHeight) * 0.4f
            canvas.drawCircle(x, y, radius, selectedPaint)
        }

        // 가능한 이동 위치 표시
        possibleMoves.forEach { pos ->
            val x = boardLeft + pos.col * cellWidth
            val y = boardTop + pos.row * cellHeight
            val radius = minOf(cellWidth, cellHeight) * 0.25f
            canvas.drawCircle(x, y, radius, possiblePaint)
        }

        // 말 그리기 (선의 교차점에 위치)
        textPaint.textSize = minOf(cellWidth, cellHeight) * 0.3f
        val pieceRadius = minOf(cellWidth, cellHeight) * 0.35f
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = state.getPiece(Position(row, col))
                if (piece != null) {
                    // 선의 교차점에 말을 배치
                    val x = boardLeft + col * cellWidth
                    val y = boardTop + row * cellHeight

                    // 말 배경 원
                    piecePaint.color = if (piece.player == Player.RED) {
                        ContextCompat.getColor(context, R.color.red_piece)
                    } else {
                        ContextCompat.getColor(context, R.color.blue_piece)
                    }
                    canvas.drawCircle(x, y, pieceRadius, piecePaint)

                    // 말 이름
                    textPaint.color = ContextCompat.getColor(context, R.color.white)
                    val pieceName = pieceNames[piece.type] ?: ""
                    canvas.drawText(pieceName, x, y + textPaint.textSize / 3, textPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return false

        val state = gameState ?: return false
        if (state.isGameOver()) return false

        val cellWidth = boardWidth / 8f
        val cellHeight = boardHeight / 9f

        // 터치 위치를 보드 좌표로 변환
        val touchX = event.x - boardLeft
        val touchY = event.y - boardTop
        
        // 보드 영역 밖이면 무시
        if (touchX < 0 || touchX > boardWidth || touchY < 0 || touchY > boardHeight) {
            return false
        }

        // 터치 위치를 가장 가까운 선의 교차점으로 변환
        val col = (touchX / cellWidth + 0.5f).toInt().coerceIn(0, 8)
        val row = (touchY / cellHeight + 0.5f).toInt().coerceIn(0, 9)

        if (row !in 0..9 || col !in 0..8) return false

        val clickedPos = Position(row, col)

        when {
            selectedPosition == null -> {
                // 말 선택
                val piece = state.getPiece(clickedPos)
                if (piece != null && piece.player == state.currentPlayer) {
                    selectedPosition = clickedPos
                    possibleMoves = state.getPossibleMoves(clickedPos)
                    invalidate()
                }
            }
            selectedPosition == clickedPos -> {
                // 선택 해제
                selectedPosition = null
                possibleMoves = emptyList()
                invalidate()
            }
            possibleMoves.contains(clickedPos) -> {
                // 이동
                onPieceMoveListener?.invoke(selectedPosition!!, clickedPos)
                selectedPosition = null
                possibleMoves = emptyList()
            }
            else -> {
                // 다른 말 선택
                val piece = state.getPiece(clickedPos)
                if (piece != null && piece.player == state.currentPlayer) {
                    selectedPosition = clickedPos
                    possibleMoves = state.getPossibleMoves(clickedPos)
                    invalidate()
                } else {
                    selectedPosition = null
                    possibleMoves = emptyList()
                    invalidate()
                }
            }
        }

        return true
    }
}
