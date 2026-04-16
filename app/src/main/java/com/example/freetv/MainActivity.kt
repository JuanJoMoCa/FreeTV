package com.example.freetv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.freetv.screens.*
import com.example.freetv.ui.theme.FreeTVTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
    val sharedTvViewModel: SharedTvViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        
        composable("splash") {
            SplashScreen(
                onSuccess = { 
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = sharedTvViewModel,
                onNavigateToPlayer = { url -> 
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("player/$encodedUrl") 
                },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable(
            route = "player/{streamUrl}",
            arguments = listOf(navArgument("streamUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val streamUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""
            PlayerScreen(
                initialStreamUrl = streamUrl,
                viewModel = sharedTvViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("details/$encodedUrl")
                },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable(
            route = "details/{streamUrl}",
            arguments = listOf(navArgument("streamUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val streamUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""
            ChannelDetailScreen(
                streamUrl = streamUrl,
                viewModel = sharedTvViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = sharedTvViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
