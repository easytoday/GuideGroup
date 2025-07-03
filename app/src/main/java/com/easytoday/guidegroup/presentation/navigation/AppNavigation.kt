package com.easytoday.guidegroup.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.easytoday.guidegroup.presentation.screens.auth.LoginScreen
import com.easytoday.guidegroup.presentation.screens.auth.SignUpScreen
import com.easytoday.guidegroup.presentation.screens.main.ChatScreen
import com.easytoday.guidegroup.presentation.screens.main.GroupDetailScreen
import com.easytoday.guidegroup.presentation.screens.main.HomeScreen
import com.easytoday.guidegroup.presentation.screens.main.MapScreen

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {

        composable(Screen.LoginScreen.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.SignUpScreen.route) {
            SignUpScreen(navController = navController)
        }

        composable(Screen.HomeScreen.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.GroupDetailScreen.route,
            // CORRECTION : On définit l'argument ici directement comme pour les autres,
            // car il n'est pas défini dans l'objet Screen.
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            requireNotNull(groupId) { "L'ID du groupe ne peut pas être null." }

            GroupDetailScreen(
                navController = navController,
                groupId = groupId
            )
        }

        composable(
            route = Screen.ChatScreen.route,
            arguments = Screen.ChatScreen.arguments
        ) {
            ChatScreen(navController = navController)
        }

        composable(
            route = Screen.MapScreen.route,
            arguments = Screen.MapScreen.arguments
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            MapScreen(navController = navController, groupId = groupId)
        }
    }
}