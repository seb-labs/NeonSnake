package com.neon.snake.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.neon.snake.game.GameEngine
import com.neon.snake.ui.components.GameBoard
import com.neon.snake.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun GameScreen(onGameOver: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    val engine = remember {
        GameEngine(
            boardWidth = 20,
            boardHeight = 30,
            onStateChange = {}
        )
    }

    var gameOverFired by remember { mutableStateOf(false) }

    // Observe game over
    LaunchedEffect(engine.gameState.isGameOver) {
        if (engine.gameState.isGameOver && !gameOverFired) {
            gameOverFired = true
            onGameOver(engine.gameState.score)
        }
    }

    // Lifecycle management
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!engine.gameState.isGameOver) {
                        engine.resume()
                        engine.start(scope)
                    }
                }
                Lifecycle.Event.ON_PAUSE -> engine.pause()
                Lifecycle.Event.ON_DESTROY -> engine.stop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            engine.stop()
        }
    }

    // Start game on first composition
    LaunchedEffect(Unit) {
        engine.restart()
        engine.start(scope)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        GameBoard(
            gameState = engine.gameState,
            onDirectionChange = { direction ->
                engine.changeDirection(direction)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
