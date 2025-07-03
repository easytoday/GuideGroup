package com.easytoday.guidegroup.presentation.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.presentation.navigation.Screen
import com.easytoday.guidegroup.presentation.viewmodel.MapViewModel

/**
 * Écran "intelligent" pour les détails du groupe.
 * Note : J'utilise le MapViewModel car il a déjà la logique pour charger un groupe par ID.
 * Dans une application plus grande, vous pourriez créer un GroupDetailViewModel dédié.
 */
@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String, // L'ID du groupe est non-nullable ici
    viewModel: MapViewModel = hiltViewModel()
) {
    // Demander au ViewModel de charger le groupe pour cet ID
    LaunchedEffect(key1 = groupId) {
        viewModel.setGroupId(groupId)
    }

    // Collecter l'état du groupe depuis le ViewModel
    val group by viewModel.currentGroup.collectAsState()

    GroupDetailScreenContent(
        group = group,
        onNavigateToChat = {
            navController.navigate(Screen.ChatScreen.createRoute(groupId))
        },
        onNavigateToMap = {
            // Assurez-vous que votre MapScreen peut aussi recevoir le groupId
            // Pour l'instant, je vais supposer qu'elle le peut.
            // navController.navigate(Screen.MapScreen.createRoute(groupId))
            // Si la route est simple :
            // On utilise la fonction createRoute pour insérer le VRAI groupId
            navController.navigate(Screen.MapScreen.createRoute(groupId))
            //navController.navigate(Screen.MapScreen.route)
        },
        onNavigateBack = {
            navController.popBackStack()
        }
    )
}

/**
 * Écran d'affichage "stupide" pour les détails du groupe.
 * 100% prévisualisable.
 */
@Composable
fun GroupDetailScreenContent(
    group: Group?,
    onNavigateToChat: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (group == null) {
            // État de chargement
            CircularProgressIndicator()
            Text("Chargement des détails du groupe...", modifier = Modifier.padding(top = 8.dp))
        } else {
            // État de succès
            Text(text = "Détails du Groupe", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nom : ${group.name}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Description : ${group.description}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Membres : ${group.memberIds.size}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Créateur ID : ${group.creatorId}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onNavigateToChat) {
                Text("Aller au Chat")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToMap) {
                Text("Voir sur la Carte")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onNavigateBack) {
            Text("Retour")
        }
    }
}

@Preview(showBackground = true, name = "État de Succès")
@Composable
fun PreviewGroupDetailScreenSuccess() {
    val fakeGroup = Group(
        id = "preview123",
        name = "Visite du Louvre",
        description = "Un groupe pour explorer le musée du Louvre et ses alentours.",
        creatorId = "user_guide_1",
        memberIds = listOf("user1", "user2", "user3", "user4")
    )
    GroupDetailScreenContent(
        group = fakeGroup,
        onNavigateToChat = {},
        onNavigateToMap = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, name = "État de Chargement")
@Composable
fun PreviewGroupDetailScreenLoading() {
    GroupDetailScreenContent(
        group = null, // En passant null, on affiche l'état de chargement
        onNavigateToChat = {},
        onNavigateToMap = {},
        onNavigateBack = {}
    )
}