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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val state = gameState ?: return

        val width = width.toFloat()
        val height = height.toFloat()
        val cellWidth = width / 9f
        val cellHeight = height / 10f

        // 배경 그리기
        canvas.drawRect(0f, 0f, width, height, boardPaint)

        // 보드 선 그리기
        linePaint.strokeWidth = 2f
        for (i in 0..9) {
            val y = i * cellHeight
            canvas.drawLine(0f, y, width, y, linePaint)
        }
        for (i in 0..8) {
            val x = i * cellWidth
            canvas.drawLine(x, 0f, x, height, linePaint)
        }

        // 궁성 그리기
        linePaint.strokeWidth = 4f
        // 한궁
        canvas.drawLine(3 * cellWidth, 7 * cellHeight, 5 * cellWidth, 7 * cellHeight, linePaint)
        canvas.drawLine(3 * cellWidth, 7 * cellHeight, 3 * cellWidth, 9 * cellHeight, linePaint)
        canvas.drawLine(5 * cellWidth, 7 * cellHeight, 5 * cellWidth, 9 * cellHeight, linePaint)
        canvas.drawLine(3 * cellWidth, 9 * cellHeight, 5 * cellWidth, 9 * cellHeight, linePaint)
        canvas.drawLine(4 * cellWidth, 7 * cellHeight, 4 * cellWidth, 9 * cellHeight, linePaint)

        // 초궁
        canvas.drawLine(3 * cellWidth, 0f, 5 * cellWidth, 0f, linePaint)
        canvas.drawLine(3 * cellWidth, 0f, 3 * cellWidth, 2 * cellHeight, linePaint)
        canvas.drawLine(5 * cellWidth, 0f, 5 * cellWidth, 2 * cellHeight, linePaint)
        canvas.drawLine(3 * cellWidth, 2 * cellHeight, 5 * cellWidth, 2 * cellHeight, linePaint)
        canvas.drawLine(4 * cellWidth, 0f, 4 * cellWidth, 2 * cellHeight, linePaint)

        // 선택된 위치 표시
        selectedPosition?.let { pos ->
            val x = pos.col * cellWidth
            val y = pos.row * cellHeight
            canvas.drawRect(x, y, x + cellWidth, y + cellHeight, selectedPaint)
        }

        // 가능한 이동 위치 표시
        possibleMoves.forEach { pos ->
            val x = pos.col * cellWidth
            val y = pos.row * cellHeight
            canvas.drawRect(x, y, x + cellWidth, y + cellHeight, possiblePaint)
        }

        // 말 그리기
        textPaint.textSize = cellHeight * 0.4f
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = state.getPiece(Position(row, col))
                if (piece != null) {
                    val x = col * cellWidth + cellWidth / 2
                    val y = row * cellHeight + cellHeight / 2

                    // 말 배경 원
                    piecePaint.color = if (piece.player == Player.RED) {
                        ContextCompat.getColor(context, R.color.red_piece)
                    } else {
                        ContextCompat.getColor(context, R.color.blue_piece)
                    }
                    val radius = cellWidth * 0.35f
                    canvas.drawCircle(x, y, radius, piecePaint)

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

        val cellWidth = width / 9f
        val cellHeight = height / 10f

        val col = (event.x / cellWidth).toInt()
        val row = (event.y / cellHeight).toInt()

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
