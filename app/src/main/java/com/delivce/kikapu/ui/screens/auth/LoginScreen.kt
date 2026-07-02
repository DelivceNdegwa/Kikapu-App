package com.delivce.kikapu.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.R
import com.delivce.kikapu.ui.components.RetroTextField
import com.delivce.kikapu.ui.components.SocialButton
import com.delivce.kikapu.ui.components.SocialIconPlaceholder
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    val loginState by viewModel.loginUiState.collectAsStateWithLifecycle()
    val authState by viewModel.authUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { token ->
                viewModel.onLoginEvent(LoginEvent.GoogleSignInClicked(token))
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // React to successful login (session state changing)
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            onLoginSuccess()
        }
    }

    // React to login errors
    LaunchedEffect(loginState.errorMessage) {
        loginState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onLoginEvent(LoginEvent.ClearError)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Please enter your details to continue using our service.",
            style = MaterialTheme.typography.bodyMedium,
            color = RetroTheme.TextColor.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        RetroTextField(
            value = loginState.email,
            onValueChange = { viewModel.onLoginEvent(LoginEvent.EmailChanged(it)) },
            placeholder = "youremail@example.com",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        RetroTextField(
            value = loginState.password,
            onValueChange = { viewModel.onLoginEvent(LoginEvent.PasswordChanged(it)) },
            placeholder = "yourpassword",
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Forgot password?",
            modifier = Modifier.align(Alignment.End).clickable { /* TODO */ },
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Coral,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .retroFrame(
                    borderColor = RetroTheme.BorderColor,
                    shadowColor = RetroTheme.ShadowColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(AppColors.Coral, RoundedCornerShape(12.dp))
                .clickable(enabled = !loginState.isLoading) {
                    if (loginState.email.isNotBlank() && loginState.password.isNotBlank()) {
                        viewModel.onLoginEvent(LoginEvent.LoginClicked)
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (loginState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = RetroTheme.BorderColor.copy(alpha = 0.25f)
            )
            Text(
                text = "  Or  ",
                style = MaterialTheme.typography.bodySmall,
                color = RetroTheme.TextColor.copy(alpha = 0.4f)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = RetroTheme.BorderColor.copy(alpha = 0.25f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialButton(
                modifier = Modifier.weight(1f),
                icon = { SocialIconPlaceholder("G", Color(0xFFEA4335)) },
                onClick = {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            )
            SocialButton(
                modifier = Modifier.weight(1f),
                icon = { SocialIconPlaceholder("f", Color(0xFF1877F2)) },
                onClick = { /* Facebook Login Not Implemented */ }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = buildAnnotatedString {
                append("Don't have an account? ")
                withStyle(
                    style = SpanStyle(
                        color = AppColors.Coral,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Sign up instead")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSignUpClick() },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = RetroTheme.TextColor
        )
    }
}