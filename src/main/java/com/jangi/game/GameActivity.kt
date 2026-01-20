package com.jangi.game

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jangi.game.databinding.ActivityGameBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var gameBoard: GameBoardView
    private var gameMode: GameMode = GameMode.TOGETHER
    private var gameState: GameState = GameState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameMode = intent.getSerializableExtra("mode") as? GameMode ?: GameMode.TOGETHER

        gameBoard = binding.gameBoard
        gameBoard.setGameState(gameState)
        gameBoard.setOnPieceMoveListener { from, to ->
            handleMove(from, to)
        }

        binding.btnNewGame.setOnClickListener {
            resetGame()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        updateUI()
    }

    private fun handleMove(from: Position, to: Position) {
        if (gameState.isGameOver()) {
            return
        }

        val moveResult = gameState.makeMove(from, to)
        if (moveResult) {
            gameBoard.invalidate()
            updateUI()

            // 게임 종료 확인
            if (gameState.isGameOver()) {
                showGameOverDialog()
            } else if (gameMode == GameMode.ALONE && gameState.currentPlayer == Player.BLUE) {
                // AI 차례
                lifecycleScope.launch {
                    delay(500) // 짧은 딜레이로 자연스러운 느낌
                    makeAIMove()
                }
            }
        } else {
            Toast.makeText(this, "잘못된 이동입니다", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun makeAIMove() {
        val aiMove = AIPlayer.findBestMove(gameState, 3)
        if (aiMove != null) {
            gameState.makeMove(aiMove.first, aiMove.second)
            gameBoard.invalidate()
            updateUI()

            if (gameState.isGameOver()) {
                showGameOverDialog()
            }
        }
    }

    private fun updateUI() {
        val playerName = if (gameState.currentPlayer == Player.RED) {
            getString(R.string.player_red)
        } else {
            getString(R.string.player_blue)
        }
        binding.tvPlayerTurn.text = getString(R.string.player_turn, playerName)
    }

    private fun resetGame() {
        gameState = GameState()
        gameBoard.setGameState(gameState)
        gameBoard.invalidate()
        updateUI()
    }

    private fun showGameOverDialog() {
        val winner = if (gameState.winner == Player.RED) {
            getString(R.string.player_red)
        } else {
            getString(R.string.player_blue)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_over))
            .setMessage(getString(R.string.winner, winner))
            .setPositiveButton("확인") { _, _ ->
                resetGame()
            }
            .setCancelable(false)
            .show()
    }
}
