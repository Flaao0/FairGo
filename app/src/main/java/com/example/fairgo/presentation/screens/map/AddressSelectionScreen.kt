package com.example.fairgo.presentation.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairgo.presentation.theme.FairGoGreen
import com.example.fairgo.presentation.theme.FairGoTextPrimary
import com.example.fairgo.presentation.theme.FairGoTextSecondary
import com.example.fairgo.presentation.theme.FairGoWhite
import com.yandex.mapkit.search.SuggestItem

@Composable
fun AddressSelectionScreen(
    viewModel: MapViewModel,
    onBack: () -> Unit,
) {
    val fromAddress by viewModel.fromAddress.collectAsState()
    val toAddress by viewModel.toAddress.collectAsState()
    val suggestions by viewModel.addressSuggestions.collectAsState()
    val showSuggestions = toAddress.isNotBlank()
    val toAddressFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        toAddressFocusRequester.requestFocus()
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF7F7F7))) {
        // Заглушка вместо карты сверху
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFE8EAED))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Surface(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape),
                    shape = CircleShape,
                    color = FairGoWhite
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Назад",
                            modifier = Modifier.size(28.dp),
                            tint = Color.DarkGray
                        )
                    }
                }

                Text(
                    text = "Выберите адрес",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = FairGoTextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // Карточка ввода адресов
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FairGoWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD8DDE1))
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RouteIndicator(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .size(width = 18.dp, height = 80.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            TextField(
                                value = fromAddress,
                                onValueChange = viewModel::onFromAddressChanged,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Откуда", color = FairGoTextSecondary) },
                                colors = transparentTextFieldColors(),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F4F5))
                            TextField(
                                value = toAddress,
                                onValueChange = viewModel::onToAddressChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(toAddressFocusRequester),
                                singleLine = true,
                                placeholder = { Text("Куда", color = FairGoTextSecondary) },
                                colors = transparentTextFieldColors(),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Кнопка на карту
            Row(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = FairGoGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Посмотреть на карте",
                    color = FairGoGreen,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(Modifier.height(32.dp))

            if (showSuggestions) {
                // Список подсказок при вводе в поле "Куда"
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)) {
                    Text(
                        text = "ПОДСКАЗКИ",
                        color = Color(0xFF98A7B5),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(suggestions) { item ->
                            SuggestionRow(
                                item = item,
                                onClick = {
                                    viewModel.onAddressSelected(item)
                                    onBack()
                                }
                            )
                            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F4F5))
                        }
                    }
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
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(FairGoGreen))
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
                .width(1.dp)
                .background(Color(0xFFB8C1CB))
        )
        Icon(
            painter = rememberVectorPainter(
                image = ImageVector.Builder(
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
                }.build()
            ),
            contentDescription = null,
            tint = Color.Unspecified
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
)

@Composable
private fun SuggestionRow(item: SuggestItem, onClick: () -> Unit) {
    val title = item.title.text
    val subtitle = item.subtitle?.text.orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFD7DEE4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = FairGoWhite,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = FairGoTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = FairGoTextSecondary
            )
        }
    }
}