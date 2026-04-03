package com.example.fairgo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PromoCodeScreen(
    onBackClick: () -> Unit,
) {
    var promoCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Полностью белый фон
            .systemBarsPadding()
            .imePadding() // МАГИЯ: двигает контент вверх, когда вылезает клавиатура
            .padding(horizontal = 20.dp)
    ) {
        // --- ШАПКА ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 32.dp),
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
                text = "Add promocode", // Взял текст из макета
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF3D4754),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 44.dp), // Баланс для центрирования
                textAlign = TextAlign.Center
            )
        }

        // --- ЦЕНТРАЛЬНАЯ ЧАСТЬ С ВВОДОМ ---
        Column(
            modifier = Modifier
                .weight(1f) // Занимает всё свободное место, центрируя поле
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = promoCode,
                onValueChange = { promoCode = it.uppercase() }, // Всегда заглавные
                textStyle = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF7DB546), // Наш зеленый цвет
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                cursorBrush = SolidColor(Color(0xFF7DB546)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (promoCode.isEmpty()) {
                                // Плейсхолдер, пока ничего не введено
                                Text(
                                    text = "ПРОМОКОД",
                                    style = TextStyle(
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE8ECEF),
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 2.sp
                                    )
                                )
                            }
                            innerTextField() // Сам вводимый текст
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Серая полоска под текстом
                        HorizontalDivider(
                            color = Color(0xFFE8ECEF),
                            thickness = 2.dp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            )
        }

        // --- КНОПКА ПРИМЕНИТЬ ---
        Button(
            onClick = {
                // TODO: Отправка промокода на сервер
                onBackClick() // Возвращаемся обратно
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DB546)
            ),
            // Опционально: кнопка неактивна (серая), если поле пустое
            enabled = promoCode.isNotBlank()
        ) {
            Text(
                text = "Применить",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color.White
            )
        }
    }
}