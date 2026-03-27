package com.example.fairgo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fairgo.presentation.screens.AddCardScreen
import com.example.fairgo.presentation.screens.auth.AuthViewModel
import com.example.fairgo.presentation.screens.auth.SignInScreen
import com.example.fairgo.presentation.screens.auth.SignUpScreen
import com.example.fairgo.presentation.screens.auth.WelcomeScreen
import com.example.fairgo.presentation.screens.map.AddressSelectionScreen
import com.example.fairgo.presentation.screens.map.MapScreen
import com.example.fairgo.presentation.screens.map.MapViewModel

@Composable
fun FairGoNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
            )
        }

        composable(Screen.SignIn.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            SignInScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onSignInSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.SignUp.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            SignUpScreen(
                viewModel = viewModel,
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route) {
                        launchSingleTop = true
                    }
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                viewModel = hiltViewModel(),
                onNavigateToAddressSelection = { /* ... твой код ... */ },
                onNavigateToPayment = {
                    navController.navigate(Screen.Payment.route)
                }
            )
        }

        composable(Screen.Payment.route) { // Убедись, что создал объект Payment в своем классе Screen
            AddCardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddressSelection.route) {
            val viewModel: MapViewModel = hiltViewModel()
            AddressSelectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

