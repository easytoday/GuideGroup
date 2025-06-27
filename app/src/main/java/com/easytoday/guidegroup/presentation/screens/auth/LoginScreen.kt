package com.easytoday.guidegroup.presentation.screens.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.presentation.navigation.Screen
import com.easytoday.guidegroup.presentation.viewmodel.AuthViewModel

/**
 * Écran "intelligent" pour la connexion.
 * Gère la logique et l'état via le ViewModel.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val signInState by authViewModel.signInState.collectAsState()

    // Effet pour gérer le résultat de la connexion
    LaunchedEffect(signInState) {
        when (val result = signInState) {
            is Result.Success -> {
                Toast.makeText(context, "Connexion réussie!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }
            is Result.Error -> {
                Toast.makeText(context, "Erreur: ${result.message}", Toast.LENGTH_LONG).show()
                authViewModel.resetAuthStates()
            }
            else -> { /* No action for Loading or Initial */ }
        }
    }

    LoginScreenContent(
        isLoading = signInState is Result.Loading,
        onLoginClick = { email, password ->
            authViewModel.signIn(email, password)
        },
        onNavigateToSignUp = {
            navController.navigate(Screen.SignUpScreen.route)
        }
    )
}

/**
 * Écran d'affichage "stupide" pour la connexion.
 * Ne contient que l'UI et est 100% prévisualisable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    onLoginClick: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connexion", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (it.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(it).matches()) "Email invalide" else null
            },
            label = { Text("Email") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if (it.length < 6) "Le mot de passe doit faire au moins 6 caractères" else null
            },
            label = { Text("Mot de passe") },
            isError = passwordError != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (emailError == null && passwordError == null && email.isNotBlank() && password.isNotBlank()) {
                    onLoginClick(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && emailError == null && passwordError == null && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Se connecter")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignUp) {
            Text("Pas encore de compte ? S'inscrire")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    // On prévisualise le composant "stupide" en lui passant des valeurs factices.
    LoginScreenContent(
        isLoading = false,
        onLoginClick = { _, _ -> },
        onNavigateToSignUp = {}
    )
}

@Preview(showBackground = true, name = "État de Chargement")
@Composable
fun PreviewLoginScreenLoading() {
    LoginScreenContent(
        isLoading = true,
        onLoginClick = { _, _ -> },
        onNavigateToSignUp = {}
    )
}