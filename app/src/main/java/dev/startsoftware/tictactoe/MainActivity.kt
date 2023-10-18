package com.example.tictactoe

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.app.AlertDialog
import android.content.DialogInterface


class TicTacToeAI {
    private var depth: Int = 5 // Default depth

    fun setDepth(newDepth: Int) {
        depth = newDepth
    }
    fun getDepth(): Int {
        return depth
    }
    fun getBestMove(board: Array<CharArray>): Pair<Int, Int> {
        var bestScore = Int.MIN_VALUE
        var bestMove = Pair(-1, -1)

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == ' ') {
                    board[i][j] = 'O'
                    val score = minimax(board, depth, false)
                    board[i][j] = ' '

                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(i, j)
                    }
                }
            }
        }

        return bestMove
    }
    private fun minimax(board: Array<CharArray>, depth: Int, isMaximizing: Boolean): Int {
        val result = checkWinner(board)
        if (result != 0) {
            return when {
                result == 1 -> -10 + depth  // Adjusted scoring for maximizing player (AI)
                result == 2 -> 10 - depth   // Adjusted scoring for minimizing player (Human)
                else -> 0
            }
        }

        if (isMaximizing) {
            var maxScore = Int.MIN_VALUE
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    if (board[i][j] == ' ') {
                        board[i][j] = 'O'
                        val score = minimax(board, depth + 1, false)
                        board[i][j] = ' '
                        maxScore = maxOf(maxScore, score)
                    }
                }
            }
            return maxScore
        } else {
            var minScore = Int.MAX_VALUE
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    if (board[i][j] == ' ') {
                        board[i][j] = 'X'
                        val score = minimax(board, depth + 1, true)
                        board[i][j] = ' '
                        minScore = minOf(minScore, score)
                    }
                }
            }
            return minScore
        }
    }


    private fun checkWinner(board: Array<CharArray>): Int {
        val winningPositions = arrayOf(
            arrayOf(0, 1, 2), arrayOf(3, 4, 5), arrayOf(6, 7, 8),
            arrayOf(0, 3, 6), arrayOf(1, 4, 7), arrayOf(2, 5, 8),
            arrayOf(0, 4, 8), arrayOf(2, 4, 6)
        )

        for (position in winningPositions) {
            val (a, b, c) = position
            if (board[a / 3][a % 3] == 'X' && board[b / 3][b % 3] == 'X' && board[c / 3][c % 3] == 'X') {
                return 1
            }
            if (board[a / 3][a % 3] == 'O' && board[b / 3][b % 3] == 'O' && board[c / 3][c % 3] == 'O') {
                return 2
            }
        }

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == ' ') {
                    return 0
                }
            }
        }

        return -1
    }
}

class MainActivity : AppCompatActivity() {
    private var isAgainstComputer = true
    private var turn = 1
    private var gameover = false
    private var player1Score = 0
    private var player2Score = 0
    private val ticTacToeAI = TicTacToeAI()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cells = arrayOf(
            R.id.tx_a1, R.id.tx_a2, R.id.tx_a3,
            R.id.tx_a4, R.id.tx_a5, R.id.tx_a6,
            R.id.tx_a7, R.id.tx_a8, R.id.tx_a9
        )

        for (cell in cells) {
            val item = findViewById<TextView>(cell)
            item.setOnClickListener { processClickEvent(cell) }
        }

        findViewById<TextView>(R.id.btn_play_again).setOnClickListener {
            resetGame()
        }


        findViewById<TextView>(R.id.btn_play_with_computer).setOnClickListener {
            showDifficultyDialog()
        }

        findViewById<TextView>(R.id.btn_play_with_player).setOnClickListener {
            isAgainstComputer = false
            resetGame()
        }
    }

    private fun showDifficultyDialog() {
        val difficultyLevels = arrayOf("Easy", "Medium", "Hard")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose difficulty level")
            .setItems(difficultyLevels) { dialogInterface: DialogInterface, which: Int ->
                // User clicked on a difficulty level
                when (which) {
                    0 -> setAIDifficulty(0)  // Easy: set depth to 0
                    1 -> setAIDifficulty(3)  // Medium: set depth to 3
                    2 -> setAIDifficulty(5)  // Hard: set depth to 5
                }
            }
        builder.show()
    }

    private fun setAIDifficulty(depth: Int) {
        ticTacToeAI.setDepth(depth)  // Use ticTacToeAI to set the depth
        isAgainstComputer = true
        resetGame()
    }

    private fun resetGame() {
        val cells = arrayOf(
            R.id.tx_a1, R.id.tx_a2, R.id.tx_a3,
            R.id.tx_a4, R.id.tx_a5, R.id.tx_a6,
            R.id.tx_a7, R.id.tx_a8, R.id.tx_a9
        )
        for (cell in cells) {
            findViewById<TextView>(cell).text = ""
            findViewById<TextView>(cell).setBackgroundColor(Color.WHITE)
        }

        turn = 1
        gameover = false

        findViewById<TextView>(R.id.tx_turn).text = "Turn of Player: 1"

        if (isAgainstComputer && turn == 2) {
            playComputerTurn()
        }
    }

    private fun updateScores() {
        findViewById<TextView>(R.id.player1_score).text = "Player 1 Score: $player1Score"
        findViewById<TextView>(R.id.player2_score).text = "Player 2 Score: $player2Score"
    }

    private fun playComputerTurn() {
        val board = getBoardState()

        // Based on the game difficulty, choose the appropriate move selection algorithm
        val bestMove = when {
            ticTacToeAI.getDepth() == 0 -> getRandomMove(board) // Random move for "easy" mode
            ticTacToeAI.getDepth() == 3 -> getStrategicMove(board) // Strategic move for "medium" mode
            else -> ticTacToeAI.getBestMove(board) // Minimax for "hard" mode
        }

        val cellId = mapBoardPositionToCellId(bestMove.first, bestMove.second)
        processClickEvent(cellId)
    }

    private fun getRandomMove(board: Array<CharArray>): Pair<Int, Int> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == ' ') {
                    emptyCells.add(Pair(i, j))
                }
            }
        }

        return emptyCells.random()
    }

    private fun getStrategicMove(board: Array<CharArray>): Pair<Int, Int> {
        // Implement a strategic move selection algorithm for "medium" difficulty
        // For simplicity, let's choose the center if available, else choose a random empty cell.
        if (board[1][1] == ' ') {
            return Pair(1, 1) // Choose the center
        } else {
            return getRandomMove(board) // Choose a random empty cell
        }
    }

    private fun mapBoardPositionToCellId(row: Int, col: Int): Int {
        return when (row) {
            0 -> when (col) {
                0 -> R.id.tx_a1
                1 -> R.id.tx_a2
                else -> R.id.tx_a3
            }
            1 -> when (col) {
                0 -> R.id.tx_a4
                1 -> R.id.tx_a5
                else -> R.id.tx_a6
            }
            else -> when (col) {
                0 -> R.id.tx_a7
                1 -> R.id.tx_a8
                else -> R.id.tx_a9
            }
        }
    }
    private fun getBoardState(): Array<CharArray> {
        val board = Array(3) { CharArray(3) }

        val cells = arrayOf(
            R.id.tx_a1, R.id.tx_a2, R.id.tx_a3,
            R.id.tx_a4, R.id.tx_a5, R.id.tx_a6,
            R.id.tx_a7, R.id.tx_a8, R.id.tx_a9
        )

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val cellId = cells[i * 3 + j]
                val cellValue = findViewById<TextView>(cellId).text.toString()
                board[i][j] = if (cellValue.isEmpty()) ' ' else cellValue[0]
            }
        }

        return board
    }

    fun processClickEvent(cellId: Int) {
        if (gameover) return

        val existingValue: String = findViewById<TextView>(cellId).text.toString()
        if (existingValue.isNotEmpty()) {
            // Vibrate for error (clicking on a filled square)
            vibrate()
            return
        }

        if (turn == 1) {
            findViewById<TextView>(cellId).text = "X"
            findViewById<TextView>(cellId).setTextColor(Color.parseColor("#ff0000"))
        } else {
            findViewById<TextView>(cellId).text = "O"
            findViewById<TextView>(cellId).setTextColor(Color.parseColor("#00ff00"))
        }

        val win = checkWin()
        if (win) {
            if (turn == 1) {
                player1Score++
            } else {
                player2Score++
            }
            updateScores()
            findViewById<TextView>(R.id.tx_turn).text = "Congrats to Player $turn"
            gameover = true
            // Vibrate on win
            vibrateOnWin()
            // Show win message
            showWinMessage("Player $turn wins!")
            return
        }

        val isTie = checkTie()
        if (isTie) {
            vibrate()
            showWinMessage("It's a tie!")
            gameover = true
            return
        }

        turn = if (turn == 1) 2 else 1
        findViewById<TextView>(R.id.tx_turn).text = "Turn of Player: $turn"

        if (isAgainstComputer && turn == 2 && !gameover) {
            playComputerTurn()
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }

    private fun vibrateOnWin() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(400)
        }
    }

    private fun showWinMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val possible_wins = arrayOf(
        arrayOf(R.id.tx_a1, R.id.tx_a2, R.id.tx_a3),
        arrayOf(R.id.tx_a4, R.id.tx_a5, R.id.tx_a6),
        arrayOf(R.id.tx_a7, R.id.tx_a8, R.id.tx_a9),

        arrayOf(R.id.tx_a1, R.id.tx_a5, R.id.tx_a9),
        arrayOf(R.id.tx_a7, R.id.tx_a5, R.id.tx_a3),

        arrayOf(R.id.tx_a1, R.id.tx_a4, R.id.tx_a7),
        arrayOf(R.id.tx_a2, R.id.tx_a5, R.id.tx_a8),
        arrayOf(R.id.tx_a3, R.id.tx_a6, R.id.tx_a9)
    )

    private fun checkWin(): Boolean {
        for (possible in possible_wins) {
            var seqStr = ""
            for (cellId in possible) {
                val existingValue: String = findViewById<TextView>(cellId).text.toString()
                if (existingValue.isEmpty()) break
                seqStr += existingValue
            }
            if (seqStr == "OOO" || seqStr == "XXX") {
                for (cellId in possible) {
                    findViewById<TextView>(cellId).setBackgroundColor(Color.parseColor("#FF33F3"))
                }
                return true
            }
        }
        return false
    }

    private fun checkTie(): Boolean {
        val cells = arrayOf(
            R.id.tx_a1, R.id.tx_a2, R.id.tx_a3,
            R.id.tx_a4, R.id.tx_a5, R.id.tx_a6,
            R.id.tx_a7, R.id.tx_a8, R.id.tx_a9
        )

        var isTie = true

        for (cell in cells) {
            val cellView = findViewById<TextView>(cell)
            if (cellView.text.isEmpty()) {
                isTie = false
                break
            }
        }

        // If it's a tie, vibrate and show a message
        if (isTie) {
            vibrate()
            showWinMessage("It's a tie!")
            gameover = true
        }

        return isTie
    }
}
