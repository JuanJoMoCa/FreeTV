package com.example.freetv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freetv.screens.HomeScreen
import com.example.freetv.screens.PlayerScreen
import com.example.freetv.screens.SettingsScreen
import com.example.freetv.ui.theme.FreeTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreeTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FreeTVAppNavigation()
                }
            }
        }
    }
}

@Composable
fun FreeTVAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToPlayer = { navController.navigate("player") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("player") {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}