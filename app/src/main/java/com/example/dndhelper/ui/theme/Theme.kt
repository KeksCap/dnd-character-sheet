package com.example.dndhelper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Перечисление доступных тем
enum class AppTheme {
    DEFAULT,
    PARCHMENT,
    INFERNAL
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// --- Схема ПЕРГАМЕНТ ---
private val ParchmentColorScheme = lightColorScheme(
    primary = ParchmentPrimary,
    background = ParchmentBackground,
    surface = ParchmentSurface,
    onPrimary = ParchmentOnPrimary,
    onBackground = ParchmentOnBackground,
    onSurface = ParchmentOnBackground,
    primaryContainer = ParchmentSurface,
    onPrimaryContainer = ParchmentOnBackground
)

// --- Схема АДСКАЯ ---
private val InfernalColorScheme = darkColorScheme(
    primary = InfernalPrimary,
    background = InfernalBackground,
    surface = InfernalSurface,
    onPrimary = InfernalOnPrimary,
    onBackground = InfernalOnBackground,
    onSurface = InfernalOnBackground,
    primaryContainer = Color(0xFF330000), // Еще более темный красный
    onPrimaryContainer = Color(0xFFFFEBEE)
)

@Composable
fun DnDHelperTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    if (appTheme == AppTheme.DEFAULT) {
        // ВОЗВРАЩАЕМ КАК БЫЛО: Пустой MaterialTheme использует стандартные настройки библиотеки
        MaterialTheme(content = content)
    } else {
        // Кастомные темы
        val colorScheme = when (appTheme) {
            AppTheme.PARCHMENT -> ParchmentColorScheme
            AppTheme.INFERNAL -> InfernalColorScheme
            else -> if (darkTheme) DarkColorScheme else LightColorScheme
        }
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}