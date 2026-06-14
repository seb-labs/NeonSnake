package com.neon.snake.game

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class Position(val x: Int, val y: Int)

data class GameState(
    val snake: List<Position>,
    val food: Position,
    val direction: Direction,
    val score: Int,
    val isGameOver: Boolean,
    val isPaused: Boolean
)

class GameEngine(
    private val boardWidth: Int = 20,
    private val boardHeight: Int = 30,
    private val onStateChange: (GameState) -> Unit
) {
    private var gameJob: Job? = null
    private var currentDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    private var snake = listOf(Position(5, 15), Position(4, 15), Position(3, 15))
    private var food = generateFood()
    private var score = 0
    private var gameSpeed = 200L // ms per tick

    var gameState by mutableStateOf(
        GameState(
            snake = snake,
            food = food,
            direction = currentDirection,
            score = score,
            isGameOver = false,
            isPaused = false
        )
    )
        private set

    fun start(scope: CoroutineScope) {
        gameJob?.cancel()
        gameJob = scope.launch(Dispatchers.Default) {
            while (isActive && !gameState.isGameOver) {
                delay(gameSpeed)
                if (!gameState.isPaused) {
                    tick()
                }
            }
        }
    }

    fun stop() {
        gameJob?.cancel()
    }

    fun changeDirection(newDirection: Direction) {
        // Prevent 180-degree turns
        val opposite = when (newDirection) {
            Direction.UP -> Direction.DOWN
            Direction.DOWN -> Direction.UP
            Direction.LEFT -> Direction.RIGHT
            Direction.RIGHT -> Direction.LEFT
        }
        if (currentDirection != opposite) {
            nextDirection = newDirection
        }
    }

    fun pause() {
        gameState = gameState.copy(isPaused = true)
    }

    fun resume() {
        gameState = gameState.copy(isPaused = false)
    }

    fun restart() {
        gameJob?.cancel()
        currentDirection = Direction.RIGHT
        nextDirection = Direction.RIGHT
        snake = listOf(Position(5, 15), Position(4, 15), Position(3, 15))
        food = generateFood()
        score = 0
        gameSpeed = 200L
        gameState = GameState(
            snake = snake,
            food = food,
            direction = currentDirection,
            score = score,
            isGameOver = false,
            isPaused = false
        )
    }

    private fun tick() {
        currentDirection = nextDirection

        val head = snake.first()
        val newHead = when (currentDirection) {
            Direction.UP -> Position(head.x, head.y - 1)
            Direction.DOWN -> Position(head.x, head.y + 1)
            Direction.LEFT -> Position(head.x - 1, head.y)
            Direction.RIGHT -> Position(head.x + 1, head.y)
        }

        // Check wall collision
        if (newHead.x < 0 || newHead.x >= boardWidth || newHead.y < 0 || newHead.y >= boardHeight) {
            endGame()
            return
        }

        // Check self collision
        if (snake.contains(newHead)) {
            endGame()
            return
        }

        val newSnake = mutableListOf(newHead)
        newSnake.addAll(snake)

        // Check food
        if (newHead == food) {
            score++
            food = generateFood()
            // Speed up every 5 points
            if (score % 5 == 0 && gameSpeed > 80L) {
                gameSpeed -= 15L
            }
        } else {
            newSnake.removeAt(newSnake.size - 1)
        }

        snake = newSnake
        gameState = GameState(
            snake = snake,
            food = food,
            direction = currentDirection,
            score = score,
            isGameOver = false,
            isPaused = gameState.isPaused
        )
        onStateChange(gameState)
    }

    private fun endGame() {
        gameJob?.cancel()
        gameState = gameState.copy(isGameOver = true)
        onStateChange(gameState)
    }

    private fun generateFood(): Position {
        val occupied = snake.toSet()
        var pos: Position
        do {
            pos = Position(
                x = (0 until boardWidth).random(),
                y = (0 until boardHeight).random()
            )
        } while (pos in occupied)
        return pos
    }
}
