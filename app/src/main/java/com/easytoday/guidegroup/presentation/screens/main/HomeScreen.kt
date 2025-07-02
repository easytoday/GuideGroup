package com.easytoday.guidegroup.presentation.screens.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.easytoday.guidegroup.BuildConfig
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.presentation.navigation.Screen
import com.easytoday.guidegroup.presentation.viewmodel.AuthViewModel
import com.easytoday.guidegroup.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val groupsState by homeViewModel.groups.collectAsState()
    val currentUser by homeViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val seedingState by homeViewModel.seedingState.collectAsState()

    LaunchedEffect(seedingState) {
        if (seedingState.isNotBlank()) {
            Toast.makeText(context, seedingState, Toast.LENGTH_SHORT).show()
        }
    }

    HomeScreenContent(
        groupsState = groupsState,
        currentUser = currentUser,
        onGroupClick = { group ->
            navController.navigate(Screen.GroupDetailScreen.createRoute(group.id))
        },
        onLogoutClick = {
            authViewModel.signOut()
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(Screen.HomeScreen.route) { inclusive = true }
            }
        },
        onRetry = {
            homeViewModel.fetchGroups()
            Toast.makeText(context, "Tentative de rechargement...", Toast.LENGTH_SHORT).show()
        },
        onSeedDatabaseClick = {
            homeViewModel.seedDatabase()
        }
    )
}

@Composable
fun HomeScreenContent(
    groupsState: Result<List<Group>>,
    currentUser: User?,
    onGroupClick: (Group) -> Unit,
    onLogoutClick: () -> Unit,
    onRetry: () -> Unit,
    onSeedDatabaseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenue ${currentUser?.username ?: "Invité"} !",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Vos Groupes :",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (groupsState) {
            is Result.Loading, Result.Initial -> {
                CircularProgressIndicator()
                Text("Chargement des groupes...")
            }
            is Result.Success -> {
                val groups = groupsState.data
                if (groups.isEmpty()) {
                    Text("Aucun groupe trouvé. Créez-en un !")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(groups) { group ->
                            GroupItem(group = group, onClick = onGroupClick)
                        }
                    }
                }
            }
            is Result.Error -> {
                Text("Erreur: ${groupsState.message}")
                Button(onClick = onRetry) {
                    Text("Réessayer")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (BuildConfig.DEBUG) {
            Button(onClick = onSeedDatabaseClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                Text("SEED DATABASE (DEBUG)")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = onLogoutClick) {
            Text("Se déconnecter")
        }
    }
}

@Composable
fun GroupItem(group: Group, onClick: (Group) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(group) },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.name, style = MaterialTheme.typography.titleMedium)
            Text(text = group.description, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(text = "Membres: ${group.memberIds.size}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview(showBackground = true, name = "État de Succès")
@Composable
fun PreviewHomeScreenSuccess() {
    val fakeGroups = listOf(
        Group(id = "1", name = "Randonnée Montagne", description = "Exploration des sentiers.", memberIds = listOf("a", "b")),
        Group(id = "2", name = "Visite de Paris", description = "Découverte des monuments.", memberIds = listOf("a", "b", "c"))
    )
    val fakeUser = User(username = "Ala")

    HomeScreenContent(
        groupsState = Result.Success(fakeGroups),
        currentUser = fakeUser,
        onGroupClick = {},
        onLogoutClick = {},
        onRetry = {},
        onSeedDatabaseClick = {}
    )
}

@Preview(showBackground = true, name = "État de Chargement")
@Composable
fun PreviewHomeScreenLoading() {
    HomeScreenContent(
        groupsState = Result.Loading,
        currentUser = User(username = "Ala"),
        onGroupClick = {},
        onLogoutClick = {},
        onRetry = {},
        onSeedDatabaseClick = {}
    )
}

@Preview(showBackground = true, name = "État d'Erreur")
@Composable
fun PreviewHomeScreenError() {
    HomeScreenContent(
        groupsState = Result.Error("Connexion au serveur impossible."),
        currentUser = User(username = "Ala"),
        onGroupClick = {},
        onLogoutClick = {},
        onRetry = {},
        onSeedDatabaseClick = {}
    )
}