package com.jangi.game

import kotlin.random.Random

object AIPlayer {
    fun findBestMove(gameState: GameState, depth: Int): Pair<Position, Position>? {
        val moves = getAllPossibleMoves(gameState)
        if (moves.isEmpty()) return null

        // 승리할 수 있는 수가 있으면 바로 선택
        for ((from, to) in moves) {
            val testState = gameState.copy()
            testState.makeMove(from, to)
            if (testState.isGameOver() && testState.winner == Player.BLUE) {
                return Pair(from, to)
            }
        }

        // 상대의 승리를 막는 수
        val opponentMoves = getAllPossibleMoves(gameState.copy().apply { currentPlayer = currentPlayer.opponent() })
        for ((from, to) in opponentMoves) {
            val testState = gameState.copy()
            testState.currentPlayer = testState.currentPlayer.opponent()
            testState.makeMove(from, to)
            if (testState.isGameOver() && testState.winner == Player.RED) {
                // 이 수를 막아야 함
                val blockingMoves = gameState.getPossibleMoves(to)
                if (blockingMoves.isNotEmpty()) {
                    return Pair(to, blockingMoves.first())
                }
            }
        }

        // 미니맥스 알고리즘으로 최선의 수 찾기
        var bestMove: Pair<Position, Position>? = null
        var bestScore = Int.MIN_VALUE

        for ((from, to) in moves) {
            val testState = gameState.copy()
            testState.makeMove(from, to)
            val score = minimax(testState, depth - 1, false, Int.MIN_VALUE, Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestMove = Pair(from, to)
            }
        }

        return bestMove ?: moves.random()
    }

    private fun minimax(state: GameState, depth: Int, maximizing: Boolean, alpha: Int, beta: Int): Int {
        if (depth == 0 || state.isGameOver()) {
            return evaluate(state)
        }

        if (maximizing) {
            var maxEval = Int.MIN_VALUE
            val moves = getAllPossibleMoves(state)
            for ((from, to) in moves) {
                val testState = state.copy()
                testState.makeMove(from, to)
                val eval = minimax(testState, depth - 1, false, alpha, beta)
                maxEval = maxOf(maxEval, eval)
                val newAlpha = maxOf(alpha, eval)
                if (beta <= newAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            val moves = getAllPossibleMoves(state)
            for ((from, to) in moves) {
                val testState = state.copy()
                testState.makeMove(from, to)
                val eval = minimax(testState, depth - 1, true, alpha, beta)
                minEval = minOf(minEval, eval)
                val newBeta = minOf(beta, eval)
                if (newBeta <= alpha) break
            }
            return minEval
        }
    }

    private fun evaluate(state: GameState): Int {
        if (state.isGameOver()) {
            return if (state.winner == Player.BLUE) 10000 else -10000
        }

        var score = 0
        val pieceValues = mapOf(
            PieceType.KING to 1000,
            PieceType.CHA to 13,
            PieceType.MA to 5,
            PieceType.SANG to 3,
            PieceType.SA to 3,
            PieceType.PO to 7,
            PieceType.JOL to 2
        )

        for (row in 0..9) {
            for (col in 0..8) {
                val piece = state.getPiece(Position(row, col))
                if (piece != null) {
                    val value = pieceValues[piece.type] ?: 0
                    if (piece.player == Player.BLUE) {
                        score += value
                    } else {
                        score -= value
                    }
                }
            }
        }

        return score
    }

    private fun getAllPossibleMoves(state: GameState): List<Pair<Position, Position>> {
        val moves = mutableListOf<Pair<Position, Position>>()
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = state.getPiece(Position(row, col))
                if (piece != null && piece.player == state.currentPlayer) {
                    val possibleMoves = state.getPossibleMoves(Position(row, col))
                    for (move in possibleMoves) {
                        moves.add(Pair(Position(row, col), move))
                    }
                }
            }
        }
        return moves
    }
}
