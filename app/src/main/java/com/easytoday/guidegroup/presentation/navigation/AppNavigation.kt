package com.easytoday.guidegroup.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
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

/**
 * Définit le graphique de navigation de l'application.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    // Les ViewModels sont maintenant gérés directement dans chaque écran "intelligent"
    // via hiltViewModel(), il n'est plus nécessaire de les déclarer ici.

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
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            requireNotNull(groupId) { "L'ID du groupe ne peut pas être null." }

            // CORRECTION : On appelle GroupDetailScreen sans les ViewModels en paramètres,
            // car il les obtient lui-même avec hiltViewModel().
            GroupDetailScreen(
                navController = navController,
                groupId = groupId
            )
        }

        composable(
            route = Screen.ChatScreen.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Le ChatScreen obtient son ViewModel (et donc son groupId) via le SavedStateHandle injecté par Hilt.
            // Il n'est donc pas nécessaire de passer le groupId en paramètre ici.
            ChatScreen(navController = navController)
        }

        composable(
            // NOTE : La route pour MapScreen n'a pas de groupId dans votre Screen.kt,
            // mais l'implémentation de MapScreen s'attend à le recevoir.
            // Nous allons corriger la route pour être cohérent.
            route = "map_screen/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            MapScreen(navController = navController, groupId = groupId)
        }
    }
}