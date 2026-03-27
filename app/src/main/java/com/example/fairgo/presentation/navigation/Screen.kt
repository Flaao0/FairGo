package com.example.fairgo.presentation.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Map : Screen("map")
    data object AddressSelection : Screen("address_selection")
}

