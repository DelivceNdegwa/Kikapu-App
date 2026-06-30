package com.delivce.kikapu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.delivce.kikapu.ui.screens.auth.AuthViewModel
import com.delivce.kikapu.ui.screens.auth.LoginScreen
import com.delivce.kikapu.ui.screens.auth.SignUpScreen
import com.delivce.kikapu.ui.screens.splash.SplashScreen
import com.delivce.kikapu.ui.KikapuApp

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object MainApp : Screen("main_app")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (authViewModel.currentUser != null) Screen.MainApp.route else Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                val destination = if (authViewModel.currentUser != null) Screen.MainApp.route else Screen.Login.route
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onSignInClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MainApp.route) {
            KikapuApp(onLogout = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.MainApp.route) { inclusive = true }
                }
            })
        }
    }
}
