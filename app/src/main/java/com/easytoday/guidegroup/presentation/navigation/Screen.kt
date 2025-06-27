package com.easytoday.guidegroup.presentation.navigation

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object SignUpScreen : Screen("signup_screen")
    object HomeScreen : Screen("home_screen")

    object ChatScreen : Screen("chat_screen/{groupId}") {
        fun createRoute(groupId: String) = "chat_screen/$groupId"
    }

    object GroupDetailScreen : Screen("group_detail_screen/{groupId}") {
        fun createRoute(groupId: String) = "group_detail_screen/$groupId"
    }

    // CORRECTION : Ajout du groupId à la route de la carte
    object MapScreen : Screen("map_screen/{groupId}") {
        // Le ? à la fin de {groupId} le rend optionnel si nécessaire
        fun createRoute(groupId: String?) = "map_screen/${groupId ?: "no_group"}"
    }
}