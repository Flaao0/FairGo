package com.example.fairgo.presentation.screens.map

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.example.fairgo.presentation.theme.FairGoGreen
import com.example.fairgo.presentation.theme.FairGoTextPrimary
import com.example.fairgo.presentation.theme.FairGoTextSecondary
import com.example.fairgo.presentation.theme.FairGoWhite

@SuppressLint("RememberInComposition")
@Composable
fun AddressSelectionScreen(
    viewModel: MapViewModel,
    onBack: () -> Unit,
) {
    val fromAddress by viewModel.fromAddress.collectAsState()
    val toAddress by viewModel.toAddress.collectAsState()
    val recent by viewModel.recentAddresses.collectAsState()
    val toAddressFocusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        toAddressFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .systemBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FairGoWhite),
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Выберите адрес",
                style = MaterialTheme.typography.titleLarge,
                color = FairGoTextPrimary,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(44.dp))
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RouteIndicator(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(width = 18.dp, height = 74.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FairGoWhite)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                TextField(
                    value = fromAddress,
                    onValueChange = viewModel::onFromAddressChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Откуда") },
                    colors = transparentTextFieldColors(),
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color(0xFFDDE3E8))
                TextField(
                    value = toAddress,
                    onValueChange = viewModel::onToAddressChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(toAddressFocusRequester),
                    singleLine = true,
                    placeholder = { Text("Куда") },
                    colors = transparentTextFieldColors(),
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = FairGoGreen,
            )
            Text(
                text = "Посмотреть на карте",
                color = FairGoGreen,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        val showRecent = toAddress.isNotBlank() && recent.isNotEmpty()
        if (showRecent) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "НЕДАВНИЕ",
                color = Color(0xFF98A7B5),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(recent) { item ->
                    AddressRow(item = item)
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = Color(0xFFDDE3E8)
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(FairGoGreen),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
                .size(width = 1.dp, height = 36.dp)
                .background(Color(0xFFB8C1CB)),
        )
        Icon(
            painter = rememberVectorPainter(
                androidx.compose.ui.graphics.vector.ImageVector.Builder(
                    name = "triangle",
                    defaultWidth = 10.dp,
                    defaultHeight = 8.dp,
                    viewportWidth = 10f,
                    viewportHeight = 8f,
                ).apply {
                    path(fill = SolidColor(Color(0xFF8E98A5))) {
                        moveTo(0f, 0f)
                        lineTo(10f, 0f)
                        lineTo(5f, 8f)
                        close()
                    }
                }.build(),
            ),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
)

@Composable
private fun AddressRow(item: AddressItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFD7DEE4)),
            contentAlignment = Alignment.Center,
        ) {
            Text("•", color = Color.White)
        }
        Column {
            Text(text = item.street, style = MaterialTheme.typography.bodyLarge, color = FairGoTextPrimary)
            Text(text = item.city, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9EB0BF))
        }
    }
}

