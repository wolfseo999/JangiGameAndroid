package com.jangi.game

data class Position(val row: Int, val col: Int) {
    fun isValid(): Boolean {
        return row in 0..9 && col in 0..8
    }
}

enum class Player {
    RED, BLUE
}

enum class PieceType(val displayName: String) {
    KING("왕"),
    CHA("차"),
    MA("마"),
    SANG("상"),
    SA("사"),
    PO("포"),
    JOL("졸")
}

data class Piece(
    val type: PieceType,
    val player: Player,
    val isCaptured: Boolean = false
)

class GameState {
    internal val board = Array(10) { Array<Piece?>(9) { null } }
    var currentPlayer: Player = Player.RED
    var winner: Player? = null

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        // 초(파란색) 말 배치
        board[0][0] = Piece(PieceType.CHA, Player.BLUE)
        board[0][1] = Piece(PieceType.MA, Player.BLUE)
        board[0][2] = Piece(PieceType.SANG, Player.BLUE)
        board[0][3] = Piece(PieceType.SA, Player.BLUE)
        board[0][4] = Piece(PieceType.KING, Player.BLUE)
        board[0][5] = Piece(PieceType.SA, Player.BLUE)
        board[0][6] = Piece(PieceType.SANG, Player.BLUE)
        board[0][7] = Piece(PieceType.MA, Player.BLUE)
        board[0][8] = Piece(PieceType.CHA, Player.BLUE)
        board[2][1] = Piece(PieceType.PO, Player.BLUE)
        board[2][7] = Piece(PieceType.PO, Player.BLUE)
        board[3][0] = Piece(PieceType.JOL, Player.BLUE)
        board[3][2] = Piece(PieceType.JOL, Player.BLUE)
        board[3][4] = Piece(PieceType.JOL, Player.BLUE)
        board[3][6] = Piece(PieceType.JOL, Player.BLUE)
        board[3][8] = Piece(PieceType.JOL, Player.BLUE)

        // 한(빨간색) 말 배치
        board[9][0] = Piece(PieceType.CHA, Player.RED)
        board[9][1] = Piece(PieceType.MA, Player.RED)
        board[9][2] = Piece(PieceType.SANG, Player.RED)
        board[9][3] = Piece(PieceType.SA, Player.RED)
        board[9][4] = Piece(PieceType.KING, Player.RED)
        board[9][5] = Piece(PieceType.SA, Player.RED)
        board[9][6] = Piece(PieceType.SANG, Player.RED)
        board[9][7] = Piece(PieceType.MA, Player.RED)
        board[9][8] = Piece(PieceType.CHA, Player.RED)
        board[7][1] = Piece(PieceType.PO, Player.RED)
        board[7][7] = Piece(PieceType.PO, Player.RED)
        board[6][0] = Piece(PieceType.JOL, Player.RED)
        board[6][2] = Piece(PieceType.JOL, Player.RED)
        board[6][4] = Piece(PieceType.JOL, Player.RED)
        board[6][6] = Piece(PieceType.JOL, Player.RED)
        board[6][8] = Piece(PieceType.JOL, Player.RED)
    }

    fun getPiece(pos: Position): Piece? = board[pos.row][pos.col]

    fun getPossibleMoves(from: Position): List<Position> {
        val piece = getPiece(from) ?: return emptyList()
        if (piece.player != currentPlayer) return emptyList()

        return when (piece.type) {
            PieceType.KING -> getKingMoves(from, piece.player)
            PieceType.CHA -> getChaMoves(from, piece.player)
            PieceType.MA -> getMaMoves(from, piece.player)
            PieceType.SANG -> getSangMoves(from, piece.player)
            PieceType.SA -> getSaMoves(from, piece.player)
            PieceType.PO -> getPoMoves(from, piece.player)
            PieceType.JOL -> getJolMoves(from, piece.player)
        }.filter { isValidMove(from, it) }
    }

    private fun isValidMove(from: Position, to: Position): Boolean {
        if (!to.isValid()) return false
        val piece = getPiece(from) ?: return false
        val targetPiece = getPiece(to)

        // 같은 편 말을 잡을 수 없음
        if (targetPiece != null && targetPiece.player == piece.player) {
            return false
        }

        // 임시로 이동해보고 왕이 잡히는지 확인
        val tempPiece = board[to.row][to.col]
        board[to.row][to.col] = board[from.row][from.col]
        board[from.row][from.col] = null

        val kingPos = findKing(piece.player)
        val isInCheck = kingPos != null && isPositionUnderAttack(kingPos, piece.player.opponent())

        // 원래대로 복구
        board[from.row][from.col] = board[to.row][to.col]
        board[to.row][to.col] = tempPiece

        return !isInCheck
    }

    private fun findKing(player: Player): Position? {
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = board[row][col]
                if (piece?.type == PieceType.KING && piece.player == player) {
                    return Position(row, col)
                }
            }
        }
        return null
    }

    private fun isPositionUnderAttack(pos: Position, attacker: Player): Boolean {
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = board[row][col]
                if (piece != null && piece.player == attacker) {
                    val moves = when (piece.type) {
                        PieceType.KING -> getKingMoves(Position(row, col), attacker)
                        PieceType.CHA -> getChaMoves(Position(row, col), attacker)
                        PieceType.MA -> getMaMoves(Position(row, col), attacker)
                        PieceType.SANG -> getSangMoves(Position(row, col), attacker)
                        PieceType.SA -> getSaMoves(Position(row, col), attacker)
                        PieceType.PO -> getPoMoves(Position(row, col), attacker)
                        PieceType.JOL -> getJolMoves(Position(row, col), attacker)
                    }
                    if (moves.contains(pos)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun getKingMoves(pos: Position, player: Player): List<Position> {
        val moves = mutableListOf<Position>()
        val palaceRows = if (player == Player.RED) 7..9 else 0..2
        val palaceCols = 3..5

        val directions = listOf(
            Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1),
            Position(-1, -1), Position(-1, 1), Position(1, -1), Position(1, 1)
        )

        for (dir in directions) {
            val newRow = pos.row + dir.row
            val newCol = pos.col + dir.col
            if (newRow in palaceRows && newCol in palaceCols) {
                moves.add(Position(newRow, newCol))
            }
        }

        return moves
    }

    private fun getChaMoves(pos: Position, player: Player): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(
            Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1)
        )

        for (dir in directions) {
            var newRow = pos.row + dir.row
            var newCol = pos.col + dir.col
            while (Position(newRow, newCol).isValid()) {
                moves.add(Position(newRow, newCol))
                if (getPiece(Position(newRow, newCol)) != null) break
                newRow += dir.row
                newCol += dir.col
            }
        }

        return moves
    }

    private fun getMaMoves(pos: Position, player: Player): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(
            Position(-2, -1), Position(-2, 1), Position(2, -1), Position(2, 1),
            Position(-1, -2), Position(-1, 2), Position(1, -2), Position(1, 2)
        )
        val blocks = listOf(
            Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1),
            Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1)
        )

        directions.forEachIndexed { index, dir ->
            val blockPos = Position(pos.row + blocks[index].row, pos.col + blocks[index].col)
            if (blockPos.isValid() && getPiece(blockPos) == null) {
                val newPos = Position(pos.row + dir.row, pos.col + dir.col)
                if (newPos.isValid()) {
                    moves.add(newPos)
                }
            }
        }

        return moves
    }

    private fun getSangMoves(pos: Position, player: Player): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(
            Position(-3, -2), Position(-3, 2), Position(3, -2), Position(3, 2),
            Position(-2, -3), Position(-2, 3), Position(2, -3), Position(2, 3)
        )
        val blocks = listOf(
            Position(-1, -1), Position(-1, 1), Position(1, -1), Position(1, 1),
            Position(-1, -1), Position(-1, 1), Position(1, -1), Position(1, 1)
        )

        directions.forEachIndexed { index, dir ->
            val blockPos = Position(pos.row + blocks[index].row, pos.col + blocks[index].col)
            if (blockPos.isValid() && getPiece(blockPos) == null) {
                val newPos = Position(pos.row + dir.row, pos.col + dir.col)
                if (newPos.isValid()) {
                    moves.add(newPos)
                }
            }
        }

        return moves
    }

    private fun getSaMoves(pos: Position, player: Player): List<Position> {
        val moves = mutableListOf<Position>()
        val palaceRows = if (player == Player.RED) 7..9 else 0..2
        val palaceCols = 3..5

        val directions = listOf(
            Position(-1, -1), Position(-1, 1), Position(1, -1), Position(1, 1)
        )

        for (dir in directions) {
            val newRow = pos.row + dir.row
            val newCol = pos.col + dir.col
            if (newRow in palaceRows && newCol in palaceCols) {
                moves.add(Position(newRow, newCol))
            }
        }

        return moves
    }

    private fun getPoMoves(pos: Position, player: Player): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(
            Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1)
        )

        for (dir in directions) {
            var newRow = pos.row + dir.row
            var newCol = pos.col + dir.col
            var pieceCount = 0

            while (Position(newRow, newCol).isValid()) {
                val piece = getPiece(Position(newRow, newCol))
                if (piece != null) {
                    pieceCount++
                    if (pieceCount == 2) {
                        moves.add(Position(newRow, newCol))
                        break
                    }
                } else if (pieceCount == 0) {
                    // 포를 뛰어넘을 말이 없으면 이동 불가
                }
                newRow += dir.row
                newCol += dir.col
            }
        }

        return moves
    }

    private fun getJolMoves(pos: Position, player: Player): List<Position> {
        val moves = mutableListOf<Position>()
        val forward = if (player == Player.RED) -1 else 1
        val palaceRows = if (player == Player.RED) 7..9 else 0..2
        val palaceCols = 3..5

        // 앞으로 한 칸
        val forwardPos = Position(pos.row + forward, pos.col)
        if (forwardPos.isValid()) {
            moves.add(forwardPos)
        }

        // 궁 안에서는 좌우로도 이동 가능
        if (pos.row in palaceRows && pos.col in palaceCols) {
            moves.add(Position(pos.row, pos.col - 1))
            moves.add(Position(pos.row, pos.col + 1))
        }

        return moves.filter { it.isValid() }
    }

    fun makeMove(from: Position, to: Position): Boolean {
        val piece = getPiece(from) ?: return false
        if (piece.player != currentPlayer) return false

        val possibleMoves = getPossibleMoves(from)
        if (!possibleMoves.contains(to)) return false

        board[to.row][to.col] = piece
        board[from.row][from.col] = null

        // 승패 확인
        checkGameOver()

        // 차례 변경
        currentPlayer = currentPlayer.opponent()

        return true
    }

    private fun checkGameOver() {
        val redKing = findKing(Player.RED)
        val blueKing = findKing(Player.BLUE)

        if (redKing == null) {
            winner = Player.BLUE
        } else if (blueKing == null) {
            winner = Player.RED
        }
    }

    fun isGameOver(): Boolean = winner != null

    fun copy(): GameState {
        val newState = GameState()
        // 보드 복사
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = board[row][col]
                newState.board[row][col] = piece
            }
        }
        // 상태 복사
        newState.currentPlayer = currentPlayer
        newState.winner = winner
        return newState
    }
}

fun Player.opponent(): Player = if (this == Player.RED) Player.BLUE else Player.RED
