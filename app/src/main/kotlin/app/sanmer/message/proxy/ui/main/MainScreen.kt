package app.sanmer.message.proxy.ui.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.sanmer.message.proxy.ui.screen.home.HomeScreen
import app.sanmer.message.proxy.ui.screen.log.LogScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        navController = navController,
        startDestination = Screen.Home()
    ) {
        Screen.Home(navController).addTo(this)
        Screen.Log(navController).addTo(this)
    }
}

sealed class Screen(
    private val route: String,
    private val content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
    private val arguments: List<NamedNavArgument> = emptyList(),
    private val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = { fadeIn() },
    private val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = { fadeOut() },
) {
    fun addTo(builder: NavGraphBuilder) = builder.composable(
        route = this@Screen.route,
        arguments = this@Screen.arguments,
        enterTransition = this@Screen.enterTransition,
        exitTransition = this@Screen.exitTransition,
        content = this@Screen.content
    )

    @Suppress("FunctionName")
    companion object Routes {
        fun Home() = "Home"
        fun Log() = "Log"
    }

    class Home(navController: NavController) : Screen(
        route = Home(),
        content = { HomeScreen(navController = navController) }
    )

    class Log(navController: NavController) : Screen(
        route = Log(),
        content = { LogScreen(navController = navController) }
    )
}