package com.delivce.kikapu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.delivce.kikapu.ui.navigation.AppNavGraph
import com.delivce.kikapu.ui.screens.auth.AuthViewModel
import com.delivce.kikapu.ui.theme.KikapuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KikapuTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val navController = rememberNavController()
                
                AppNavGraph(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
