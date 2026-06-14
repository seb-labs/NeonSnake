package com.neon.snake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neon.snake.ui.theme.NeonSnakeTheme
import com.neon.snake.ui.screens.GameScreen
import com.neon.snake.ui.screens.MenuScreen
import com.neon.snake.ui.screens.GameOverScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeonSnakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SnakeApp()
                }
            }
        }
    }
}

@Composable
fun SnakeApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(onStartGame = { navController.navigate("game") })
        }
        composable("game") {
            GameScreen(
                onGameOver = { score ->
                    navController.navigate("gameover/$score") {
                        popUpTo("menu") { inclusive = false }
                    }
                }
            )
        }
        composable("gameover/{score}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            GameOverScreen(
                score = score,
                onRestart = {
                    navController.navigate("game") {
                        popUpTo("menu") { inclusive = false }
                    }
                },
                onMenu = {
                    navController.navigate("menu") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SnakeAppPreview() {
    NeonSnakeTheme {
        SnakeApp()
    }
}
