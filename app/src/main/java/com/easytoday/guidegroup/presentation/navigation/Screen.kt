package com.easytoday.guidegroup.presentation.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object SignUpScreen : Screen("signup_screen")
    object HomeScreen : Screen("home_screen")

    object ChatScreen : Screen("chat_screen/{groupId}?poiId={poiId}&poiName={poiName}") {
        fun createRoute(groupId: String) = "chat_screen/$groupId"
        fun createSharePoiRoute(groupId: String, poiId: String, poiName: String): String {
            val encodedPoiName = URLEncoder.encode(poiName, StandardCharsets.UTF_8.toString())
            return "chat_screen/$groupId?poiId=$poiId&poiName=$encodedPoiName"
        }
        val arguments = listOf(
            navArgument("groupId") { type = NavType.StringType },
            navArgument("poiId") { type = NavType.StringType; nullable = true },
            navArgument("poiName") { type = NavType.StringType; nullable = true }
        )
    }

    object GroupDetailScreen : Screen("group_detail_screen/{groupId}") {
        fun createRoute(groupId: String) = "group_detail_screen/$groupId"
    }

    // CORRECTION : Ajout de lat et lon comme arguments optionnels pour le focus
    object MapScreen : Screen("map_screen/{groupId}?focusOnPoi={focusOnPoi}&lat={lat}&lon={lon}") {
        fun createRoute(groupId: String?) = "map_screen/${groupId ?: "no_group"}"

        // Route pour le focus qui passe maintenant toutes les infos
        fun createFocusPoiRoute(groupId: String, poiId: String, lat: Double, lon: Double) =
            "map_screen/$groupId?focusOnPoi=$poiId&lat=$lat&lon=$lon"

        val arguments = listOf(
            navArgument("groupId") { type = NavType.StringType; nullable = true },
            navArgument("focusOnPoi") { type = NavType.StringType; nullable = true },
            // On définit les nouveaux arguments 
            navArgument("lat") { type = NavType.StringType; nullable = true },
            navArgument("lon") { type = NavType.StringType; nullable = true }
        )
    }
}
