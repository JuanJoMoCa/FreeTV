package com.example.freetv

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.freetv.screens.AddChannelScreen
import com.example.freetv.screens.ChannelDetailScreen
import com.example.freetv.screens.CreateListScreen
import com.example.freetv.screens.HomeScreen
import com.example.freetv.screens.MyListsScreen
import com.example.freetv.screens.PlayerScreen
import com.example.freetv.screens.SettingsScreen
import com.example.freetv.screens.SharedTvViewModel
import com.example.freetv.screens.SplashScreen
import com.example.freetv.ui.theme.FreeTVTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val sharedTvViewModel: SharedTvViewModel by viewModels()

    private var isPlayerScreenVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme by sharedTvViewModel.isDarkTheme.collectAsState()

            FreeTVTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FreeTVAppNavigation(
                        sharedTvViewModel = sharedTvViewModel,
                        onPlayerScreenChanged = { isVisible ->
                            isPlayerScreenVisible = isVisible
                        }
                    )
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (
            isPlayerScreenVisible &&
            canEnterPictureInPictureModeSafely() &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !isInPictureInPictureMode
        ) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()

            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        sharedTvViewModel.setPipMode(isInPictureInPictureMode)
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode) {
                sharedTvViewModel.setPipMode(false)
            }
        } else {
            sharedTvViewModel.setPipMode(false)
        }
    }

    private fun canEnterPictureInPictureModeSafely(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }

        val hasPipFeature = packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )

        if (!hasPipFeature) {
            return false
        }

        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
            Process.myUid(),
            packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_DEFAULT
    }
}

@Composable
fun FreeTVAppNavigation(
    sharedTvViewModel: SharedTvViewModel,
    onPlayerScreenChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        val isPlayerRoute = currentRoute == "player/{streamUrl}"

        onPlayerScreenChanged(isPlayerRoute)

        if (!isPlayerRoute) {
            sharedTvViewModel.setPipMode(false)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = sharedTvViewModel,
                onNavigateToPlayer = { url ->
                    val encodedUrl = URLEncoder.encode(
                        url,
                        StandardCharsets.UTF_8.toString()
                    )

                    navController.navigate("player/$encodedUrl")
                },
                onNavigateToSettings = {
                    sharedTvViewModel.setPipMode(false)
                    navController.navigate("settings")
                },
                onNavigateToAddChannel = {
                    sharedTvViewModel.setPipMode(false)
                    navController.navigate("add_channel")
                },
                onNavigateToMyLists = {
                    sharedTvViewModel.setPipMode(false)
                    navController.navigate("my_lists")
                }
            )
        }

        composable(
            route = "player/{streamUrl}",
            arguments = listOf(
                navArgument("streamUrl") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val streamUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""

            PlayerScreen(
                initialStreamUrl = streamUrl,
                viewModel = sharedTvViewModel,
                onNavigateBack = {
                    sharedTvViewModel.setPipMode(false)

                    navController.navigate("home") {
                        popUpTo("home") {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToDetails = { url ->
                    sharedTvViewModel.setPipMode(false)

                    val encodedUrl = URLEncoder.encode(
                        url,
                        StandardCharsets.UTF_8.toString()
                    )

                    navController.navigate("details/$encodedUrl")
                },
                onNavigateToSettings = {
                    sharedTvViewModel.setPipMode(false)

                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "details/{streamUrl}",
            arguments = listOf(
                navArgument("streamUrl") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val streamUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""

            ChannelDetailScreen(
                streamUrl = streamUrl,
                viewModel = sharedTvViewModel,
                onNavigateBack = {
                    sharedTvViewModel.setPipMode(false)
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = sharedTvViewModel,
                onNavigateBack = {
                    sharedTvViewModel.setPipMode(false)
                    navController.popBackStack()
                }
            )
        }

        composable("add_channel") {
            AddChannelScreen(
                viewModel = sharedTvViewModel,
                onNavigateBack = {
                    sharedTvViewModel.setPipMode(false)
                    navController.popBackStack()
                }
            )
        }

        composable("create_list") {
            CreateListScreen(
                viewModel = sharedTvViewModel,
                onNavigateBack = {
                    sharedTvViewModel.setPipMode(false)
                    navController.popBackStack()
                }
            )
        }

        composable("my_lists") {
            MyListsScreen(
                viewModel = sharedTvViewModel,
                onNavigateBack = {
                    sharedTvViewModel.setPipMode(false)
                    navController.popBackStack()
                },
                onNavigateToCreateList = {
                    sharedTvViewModel.setPipMode(false)
                    navController.navigate("create_list")
                }
            )
        }
    }
}
