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

    object MapScreen : Screen("map_screen/{groupId}?focusOnPoi={focusOnPoi}") {
        fun createRoute(groupId: String?) = "map_screen/${groupId ?: "no_group"}"
        fun createFocusPoiRoute(groupId: String, poiId: String) = "map_screen/$groupId?focusOnPoi=$poiId"
        val arguments = listOf(
            navArgument("groupId") { type = NavType.StringType; nullable = true },
            navArgument("focusOnPoi") { type = NavType.StringType; nullable = true }
        )
    }
}