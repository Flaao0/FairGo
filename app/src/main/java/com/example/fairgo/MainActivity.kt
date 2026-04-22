package com.example.fairgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fairgo.data.local.TokenManager
import com.example.fairgo.presentation.navigation.FairGoNavGraph
import com.example.fairgo.presentation.navigation.Screen
import com.example.fairgo.presentation.theme.FairGoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val hasAccessToken = !tokenManager.getAccessToken().isNullOrBlank()
        val startDestination = if (hasAccessToken) Screen.Map.route else Screen.Welcome.route

        setContent {
            FairGoTheme {
                FairGoNavGraph(startDestination = startDestination)
            }
        }
    }
}
