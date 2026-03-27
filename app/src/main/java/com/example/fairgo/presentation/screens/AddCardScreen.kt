package com.example.fairgo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onBackClick: () -> Unit,
) {
    // Состояния полей ввода
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F5)) // Светло-серый фон
            .systemBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // --- ШАПКА ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBackClick,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Назад",
                    modifier = Modifier.padding(12.dp),
                    tint = Color(0xFF3D4754)
                )
            }

            Text(
                text = "Добавить карту",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color(0xFF3D4754),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 44.dp),
                textAlign = TextAlign.Center
            )
        }

        // --- ИНТЕРАКТИВНАЯ КАРТА ---
        CreditCardPreview(
            cardNumber = cardNumber,
            cardHolder = cardHolder,
            expiryDate = expiryDate,
            cvv = cvv
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- ФОРМА ВВОДА ---
        CustomTextField(
            label = "НОМЕР КАРТЫ",
            value = cardNumber,
            onValueChange = { if (it.length <= 16) cardNumber = it }, // Ограничение 16 цифр
            keyboardType = KeyboardType.Number
        )
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            label = "ИМЯ ВЛАДЕЛЬЦА",
            value = cardHolder,
            onValueChange = { cardHolder = it.uppercase() }, // Всегда заглавные
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                CustomTextField(
                    label = "ДЕЙСТВУЕТ ДО",
                    value = expiryDate,
                    onValueChange = { if (it.length <= 4) expiryDate = it }, // 4 цифры (MMYY)
                    keyboardType = KeyboardType.Number
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                CustomTextField(
                    label = "КОД CVV",
                    value = cvv,
                    onValueChange = { if (it.length <= 3) cvv = it }, // 3 цифры
                    keyboardType = KeyboardType.Number
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- БЛОК СКАНИРОВАНИЯ ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                ActionRow(text = "Сканировать карту")
                HorizontalDivider(color = Color(0xFFE8ECEF), modifier = Modifier.padding(horizontal = 16.dp))
                ActionRow(text = "Добавить face ID")
            }
        }

        // Выталкиваем кнопку "Добавить" в самый низ экрана
        Spacer(modifier = Modifier.weight(1f))

        // --- КНОПКА СОХРАНЕНИЯ ---
        Button(
            onClick = {
                // Возвращаемся назад при клике
                onBackClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(56.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DB546) // FairGo Green
            )
        ) {
            Text(
                text = "Добавить",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color.White
            )
        }
    }
}

// ---------------------------------------------------------------------------
// КОМПОНЕНТЫ ЭКРАНА
// ---------------------------------------------------------------------------

@Composable
fun CreditCardPreview(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cvv: String
) {
    // Форматируем номер карты с пробелами: 1234 5678 XXXX XXXX
    val formattedNumber = buildString {
        for (i in 0 until 16) {
            if (i > 0 && i % 4 == 0) append(" ")
            if (i < cardNumber.length) append(cardNumber[i]) else append("X")
        }
    }

    // Форматируем дату: MM/YY
    val formattedExpiry = buildString {
        if (expiryDate.isNotEmpty()) append(expiryDate.take(2)) else append("MM")
        append("/")
        if (expiryDate.length > 2) append(expiryDate.drop(2).take(2)) else append("YY")
    }

    // Форматируем CVV
    val formattedCvv = cvv.padEnd(3, 'X')

    // Само "тело" карты
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B4452)), // Темно-синий/серый цвет карты
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {

            // Логотип Mastercard кодом (круги пересекаются)
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy((-14).dp)
            ) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEB001B).copy(alpha = 0.9f)))
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF79E1B).copy(alpha = 0.9f)))
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Номер карты
                Text(
                    text = "НОМЕР КАРТЫ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9CA5B4)
                )
                Text(
                    text = formattedNumber,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        fontSize = 20.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Дата
                    Column {
                        Text(
                            text = "МЕСЯЦ/ГОД",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA5B4)
                        )
                        Text(
                            text = formattedExpiry,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    // CVV
                    Column(modifier = Modifier.padding(end = 40.dp)) {
                        Text(
                            text = "CVV",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA5B4)
                        )
                        Text(
                            text = formattedCvv,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF768294), // Делаем CVV чуть более тусклым, как на макете
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Имя владельца
                Text(
                    text = cardHolder.ifEmpty { "ИМЯ ФАМИЛИЯ" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF6B7A8A),
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFDCE2E8),
                focusedBorderColor = Color(0xFF7DB546)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = KeyboardCapitalization.Characters
            ),
            singleLine = true
        )
    }
}

@Composable
private fun ActionRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Действие */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.LightGray, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF3D4754)
        )
    }
}