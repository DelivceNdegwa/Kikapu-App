package com.delivce.kikapu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.delivce.kikapu.ui.navigation.AppNavGraph
import com.delivce.kikapu.ui.screens.auth.AuthViewModel
import com.delivce.kikapu.ui.theme.KikapuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var deepLinkTripId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkTripId = intent.extractTripId()
        setContent {
            KikapuTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    pendingTripId = deepLinkTripId,
                    onDeepLinkHandled = { deepLinkTripId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkTripId = intent.extractTripId()
    }

    private fun Intent.extractTripId(): String? =
        if (action == Intent.ACTION_VIEW) data?.lastPathSegment else null
}
