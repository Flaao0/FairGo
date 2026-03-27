package com.example.fairgo.presentation.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fairgo.presentation.theme.FairGoGreen
import com.example.fairgo.presentation.theme.FairGoTextPrimary
import com.example.fairgo.presentation.theme.FairGoTextSecondary
import com.example.fairgo.presentation.theme.FairGoWhite

@Composable
fun AddressSelectionScreen(
    viewModel: MapViewModel,
    onBack: () -> Unit,
) {
    val fromAddress by viewModel.fromAddress.collectAsState()
    val toAddress by viewModel.toAddress.collectAsState()
    val recent by viewModel.recentAddresses.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .systemBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
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

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FairGoWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(FairGoGreen),
                    )
                    Spacer(
                        modifier = Modifier
                            .height(26.dp)
                            .padding(vertical = 2.dp)
                            .background(Color(0xFF9AA3B0))
                            .fillMaxWidth(),
                    )
                    Box(
                        modifier = Modifier
                            .size(0.dp),
                    )
                    Text("▼", color = Color(0xFF9AA3B0), style = MaterialTheme.typography.labelLarge)
                }

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = fromAddress,
                        onValueChange = viewModel::onFromAddressChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                    )
                    Divider(color = Color(0xFFE2E6EA))
                    OutlinedTextField(
                        value = toAddress,
                        onValueChange = viewModel::onToAddressChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Старозав", color = FairGoTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))
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
                Divider(color = Color(0xFFDDE3E8))
            }
        }
    }
}

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

