package com.example.freetv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freetv.ui.theme.HomeScreen
import com.example.freetv.ui.theme.FreeTVTheme // Asegúrate de que el nombre coincida con tu Theme

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
            Text("Aquí va el PlayerScreen")
        }


        composable("settings") {
            Text("Aquí van las configuraciones")
        }
    }
}