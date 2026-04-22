package com.example.fairgo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fairgo.presentation.screens.AddCardScreen
import com.example.fairgo.presentation.screens.PromoCodeScreen
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
    startDestination: String = Screen.Welcome.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
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

        // ЭКРАН КАРТЫ
        composable(Screen.Map.route) {
            // Создаем основную ViewModel здесь
            val viewModel: MapViewModel = hiltViewModel()

            MapScreen(
                viewModel = viewModel,
                onNavigateToAddressSelection = {
                    navController.navigate(Screen.AddressSelection.route)
                },
                onNavigateToPayment = {
                    navController.navigate(Screen.Payment.route)
                },
                onNavigateToPromoCode = {
                    navController.navigate(Screen.PromoCode.route)
                }
            )
        }

        composable(Screen.Payment.route) {
            AddCardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ЭКРАН ВЫБОРА АДРЕСА
        composable(Screen.AddressSelection.route) { backStackEntry ->
            // Магия: находим в истории навигации запись экрана Карты
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Map.route)
            }

            // Получаем ТУ ЖЕ САМУЮ ViewModel, которая привязана к экрану карты
            val sharedViewModel: MapViewModel = hiltViewModel(parentEntry)

            AddressSelectionScreen(
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.PromoCode.route) {
            PromoCodeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}