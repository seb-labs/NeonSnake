package com.neon.snake.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.snake.game.Direction
import com.neon.snake.game.GameState
import com.neon.snake.game.Position
import com.neon.snake.ui.theme.*
import kotlin.math.abs

@Composable
fun GameBoard(
    gameState: GameState,
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Pulsing animation for food
    val infiniteTransition = rememberInfiniteTransition(label = "food_pulse")
    val foodPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "food_pulse"
    )

    // Score glow animation
    val scoreGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "score_glow"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCORE",
                    color = NeonCyan.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${gameState.score}",
                    color = NeonGreen.copy(alpha = scoreGlow),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }
        }

        // Game board
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkBackground)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val (dx, dy) = dragAmount
                        if (abs(dx) > abs(dy)) {
                            if (dx > 0) onDirectionChange(Direction.RIGHT)
                            else onDirectionChange(Direction.LEFT)
                        } else {
                            if (dy > 0) onDirectionChange(Direction.DOWN)
                            else onDirectionChange(Direction.UP)
                        }
                    }
                }
        ) {
            val boardWidthDp = maxWidth
            val boardHeightDp = maxHeight
            val cellSizePx = with(density) {
                minOf(
                    boardWidthDp.toPx() / 20f,
                    boardHeightDp.toPx() / 30f
                )
            }
            val cellSizeDp = with(density) { cellSizePx.toDp() }
            val totalBoardWidth = cellSizeDp * 20
            val totalBoardHeight = cellSizeDp * 30

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(totalBoardWidth, totalBoardHeight)
                ) {
                    val cellSize = cellSizePx

                    // Draw grid
                    drawGrid(cellSize)

                    // Draw food with pulse
                    drawFood(gameState.food, cellSize, foodPulse)

                    // Draw snake
                    drawSnake(gameState.snake, cellSize)
                }
            }
        }

        // Pause indicator
        if (gameState.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏸ PAUSED",
                    color = NeonYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(cellSize: Float) {
    val gridColor = GridLine
    // Vertical lines
    for (i in 0..20) {
        drawLine(
            color = gridColor,
            start = Offset(i * cellSize, 0f),
            end = Offset(i * cellSize, size.height),
            strokeWidth = 0.5f
        )
    }
    // Horizontal lines
    for (i in 0..30) {
        drawLine(
            color = gridColor,
            start = Offset(0f, i * cellSize),
            end = Offset(size.width, i * cellSize),
            strokeWidth = 0.5f
        )
    }
}

private fun DrawScope.drawSnake(snake: List<Position>, cellSize: Float) {
    snake.forEachIndexed { index, pos ->
        val isHead = index == 0
        val progress = index.toFloat() / snake.size

        val color = if (isHead) {
            SnakeHead
        } else {
            // Gradient from body to tail
            val greenVal = (230f - (230f - 200f) * progress).toInt().coerceIn(0, 255)
            val blueVal = (118f - (118f - 83f) * progress).toInt().coerceIn(0, 255)
            Color(
                red = 0,
                green = greenVal,
                blue = blueVal
            )
        }

        val padding = if (isHead) 1f else 2f
        val cornerRadius = if (isHead) cellSize * 0.3f else cellSize * 0.2f

        // Glow effect for head
        if (isHead) {
            drawRoundRect(
                color = SnakeHead.copy(alpha = 0.3f),
                topLeft = Offset(
                    pos.x * cellSize - 2f,
                    pos.y * cellSize - 2f
                ),
                size = Size(cellSize + 4f, cellSize + 4f),
                cornerRadius = CornerRadius(cornerRadius + 2f)
            )
        }

        // Main body segment
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0.7f)),
                center = Offset(
                    pos.x * cellSize + cellSize / 2,
                    pos.y * cellSize + cellSize / 2
                ),
                radius = cellSize / 2
            ),
            topLeft = Offset(
                pos.x * cellSize + padding,
                pos.y * cellSize + padding
            ),
            size = Size(cellSize - padding * 2, cellSize - padding * 2),
            cornerRadius = CornerRadius(cornerRadius)
        )

        // Eyes on head
        if (isHead) {
            val eyeSize = cellSize * 0.12f
            val eyeOffset = cellSize * 0.2f
            drawCircle(
                color = Color.Black,
                radius = eyeSize,
                center = Offset(
                    pos.x * cellSize + cellSize / 2 - eyeOffset,
                    pos.y * cellSize + cellSize / 2 - eyeOffset
                )
            )
            drawCircle(
                color = Color.Black,
                radius = eyeSize,
                center = Offset(
                    pos.x * cellSize + cellSize / 2 + eyeOffset,
                    pos.y * cellSize + cellSize / 2 - eyeOffset
                )
            )
        }
    }
}

private fun DrawScope.drawFood(food: Position, cellSize: Float, pulse: Float) {
    val centerX = food.x * cellSize + cellSize / 2
    val centerY = food.y * cellSize + cellSize / 2
    val radius = (cellSize / 2 - 2f) * pulse

    // Outer glow
    drawCircle(
        color = FoodColor.copy(alpha = 0.2f),
        radius = radius * 1.8f,
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = FoodColor.copy(alpha = 0.3f),
        radius = radius * 1.4f,
        center = Offset(centerX, centerY)
    )
    // Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(FoodColor, FoodColor.copy(alpha = 0.5f)),
            center = Offset(centerX, centerY),
            radius = radius
        ),
        radius = radius,
        center = Offset(centerX, centerY)
    )
}
