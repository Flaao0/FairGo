package com.example.fairgo.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.fairgo.presentation.theme.FairGoDivider
import com.example.fairgo.presentation.theme.FairGoGreen
import com.example.fairgo.presentation.theme.FairGoTextSecondary
import com.example.fairgo.presentation.theme.FairGoWhite

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(FairGoWhite)
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Вход",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(42.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthFieldLabel("ЭЛЕКТРОННАЯ ПОЧТА")
                AuthOutlinedField(
                    value = email,
                    onValueChange = viewModel::onEmailChanged,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthFieldLabel("ПАРОЛЬ")
                AuthOutlinedField(
                    value = password,
                    onValueChange = viewModel::onPasswordChanged,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = null,
                                tint = FairGoTextSecondary,
                            )
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        PrimaryGreenButton(
            text = "Войти",
            onClick = onSignInSuccess,
        )

        Spacer(Modifier.height(26.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Divider(modifier = Modifier.weight(1f), color = FairGoDivider)
            Text(
                text = "ИЛИ ВОЙДИТЕ С ПОМОЩЬЮ",
                style = MaterialTheme.typography.labelLarge,
                color = FairGoTextSecondary,
            )
            Divider(modifier = Modifier.weight(1f), color = FairGoDivider)
        }

        Spacer(Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SocialPlaceholder()
            SocialPlaceholder()
            SocialPlaceholder()
        }

        Spacer(Modifier.weight(1f))

        val bottomText = buildAnnotatedString {
            withStyle(SpanStyle(color = FairGoTextSecondary)) {
                append("Нет учетной записи? ")
            }
            withStyle(
                SpanStyle(
                    color = FairGoGreen,
                    fontWeight = FontWeight.SemiBold,
                ),
            ) {
                append("Регистрация")
            }
        }

        Text(
            text = bottomText,
            modifier = Modifier
                .padding(bottom = 10.dp)
                .clickable(onClick = onNavigateToSignUp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Transparent, // ignored by annotated string
        )
    }
}

@Composable
private fun SocialPlaceholder() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(Color(0xFFE5E5E5), CircleShape),
    )
}

