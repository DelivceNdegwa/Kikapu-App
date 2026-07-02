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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.R
import com.delivce.kikapu.ui.components.NavigationButton
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
fun SignUpScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    var agreedToTerms by remember { mutableStateOf(false) }

    val signUpState by viewModel.signUpUiState.collectAsStateWithLifecycle()
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
                viewModel.onSignUpEvent(SignUpEvent.GoogleSignInClicked(token))
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    // React to successful sign up (session state changing)
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            onSignUpSuccess()
        }
    }

    // React to sign up errors
    LaunchedEffect(signUpState.errorMessage) {
        signUpState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onSignUpEvent(SignUpEvent.ClearError)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        NavigationButton(isBack = true, onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Please register on our Streamline, where you can continue using our service.",
            style = MaterialTheme.typography.bodyMedium,
            color = RetroTheme.TextColor.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        RetroTextField(
            value = signUpState.fullName,
            onValueChange = { viewModel.onSignUpEvent(SignUpEvent.FullNameChanged(it)) },
            placeholder = "Full Name",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RetroTextField(
            value = signUpState.email,
            onValueChange = { viewModel.onSignUpEvent(SignUpEvent.EmailChanged(it)) },
            placeholder = "youremail@example.com",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RetroTextField(
            value = signUpState.password,
            onValueChange = { viewModel.onSignUpEvent(SignUpEvent.PasswordChanged(it)) },
            placeholder = "yourpassword",
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .retroFrame(
                        borderColor = RetroTheme.BorderColor,
                        shadowColor = RetroTheme.ShadowColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        if (agreedToTerms) AppColors.Coral else RetroTheme.BackgroundColor,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (agreedToTerms) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("I agree to ")
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append("privacy policy & terms")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                .clickable(enabled = !signUpState.isLoading) {
                    if (signUpState.fullName.isNotBlank() && signUpState.email.isNotBlank() && signUpState.password.isNotBlank() && agreedToTerms) {
                        viewModel.onSignUpEvent(SignUpEvent.SignUpClicked)
                    } else if (!agreedToTerms) {
                        Toast.makeText(context, "Please agree to terms", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (signUpState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

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

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(
                    style = SpanStyle(
                        color = AppColors.Coral,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Sign in instead")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSignInClick() },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = RetroTheme.TextColor
        )
    }
}
