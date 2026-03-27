package com.example.fairgo.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fairgo.presentation.theme.FairGoFieldContainer
import com.example.fairgo.presentation.theme.FairGoGreen
import com.example.fairgo.presentation.theme.FairGoTextPrimary

@Composable
internal fun AuthFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = FairGoTextPrimary.copy(alpha = 0.75f),
    )
}

@Composable
internal fun AuthOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(FairGoFieldContainer, RoundedCornerShape(18.dp)),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FairGoFieldContainer,
            unfocusedContainerColor = FairGoFieldContainer,
            disabledContainerColor = FairGoFieldContainer,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = FairGoTextPrimary,
            unfocusedTextColor = FairGoTextPrimary,
            cursorColor = FairGoGreen,
        ),
    )
}

@Composable
internal fun PrimaryGreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FairGoGreen,
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

