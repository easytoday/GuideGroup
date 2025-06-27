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
 * Écran "intelligent" pour l'inscription.
 * Gère la logique et l'état via le ViewModel.
 */
@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val signUpState by authViewModel.signUpState.collectAsState()

    LaunchedEffect(signUpState) {
        when (val result = signUpState) {
            is Result.Success -> {
                Toast.makeText(context, "Inscription réussie!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    popUpTo(Screen.SignUpScreen.route) { inclusive = true }
                }
            }
            is Result.Error -> {
                Toast.makeText(context, "Erreur: ${result.message}", Toast.LENGTH_LONG).show()
                authViewModel.resetAuthStates()
            }
            else -> {}
        }
    }

    SignUpScreenContent(
        isLoading = signUpState is Result.Loading,
        onSignUpClick = { email, password, name, isGuide ->
            authViewModel.signUp(email, password, name, isGuide)
        },
        onNavigateToLogin = {
            navController.popBackStack() // Retourne simplement à l'écran précédent (Login)
        }
    )
}

/**
 * Écran d'affichage "stupide" pour l'inscription.
 * 100% prévisualisable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreenContent(
    isLoading: Boolean,
    onSignUpClick: (email: String, password: String, name: String, isGuide: Boolean) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isGuide by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Inscription", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = if (it.isBlank()) "Le nom est requis" else null },
            label = { Text("Nom") },
            isError = nameError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) "Email invalide" else null },
            label = { Text("Email") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = if (it.length < 6) "6 caractères minimum" else null },
            label = { Text("Mot de passe") },
            isError = passwordError != null,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isGuide, onCheckedChange = { isGuide = it })
            Text("Je suis un guide")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSignUpClick(email, password, name, isGuide) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && emailError == null && passwordError == null && nameError == null && email.isNotBlank() && password.isNotBlank() && name.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("S'inscrire")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Déjà un compte ? Se connecter")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {
    SignUpScreenContent(
        isLoading = false,
        onSignUpClick = { _, _, _, _ -> },
        onNavigateToLogin = {}
    )
}

@Preview(showBackground = true, name = "État de Chargement")
@Composable
fun PreviewSignUpScreenLoading() {
    SignUpScreenContent(
        isLoading = true,
        onSignUpClick = { _, _, _, _ -> },
        onNavigateToLogin = {}
    )
}