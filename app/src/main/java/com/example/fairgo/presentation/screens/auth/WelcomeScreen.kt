package com.example.fairgo.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fairgo.presentation.theme.FairGoDotInactive
import com.example.fairgo.presentation.theme.FairGoGreen
import com.example.fairgo.presentation.theme.FairGoTextPrimary
import com.example.fairgo.presentation.theme.FairGoTextSecondary
import com.example.fairgo.presentation.theme.FairGoWhite

@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(FairGoWhite)
            .clickable(onClick = onNavigateToSignIn)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F2F2)),
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Установите свое\nместоположение",
            style = MaterialTheme.typography.titleLarge,
            color = FairGoTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Включите общий доступ к\nместоположению, чтобы ваш\nводитель мог видеть, где вы\nнаходитесь",
            style = MaterialTheme.typography.bodyMedium,
            color = FairGoTextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Dot(isActive = true)
            Dot(isActive = false)
            Dot(isActive = false, isHollow = true)
        }
    }
}

@Composable
private fun Dot(
    isActive: Boolean,
    isHollow: Boolean = false,
) {
    val size = 8.dp
    val activeColor = FairGoGreen
    val inactiveColor = FairGoDotInactive

    val modifier = if (isHollow) {
        Modifier
            .size(10.dp)
            .clip(CircleShape)
            .border(width = 1.5.dp, color = activeColor, shape = CircleShape)
    } else {
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isActive) activeColor else inactiveColor)
    }

    Box(modifier = modifier)
}

