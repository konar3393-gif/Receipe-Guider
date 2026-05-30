package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = SaffronOrange,
    onPrimary = Color.White,
    primaryContainer = SaffronOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = RosemaryGreen,
    onSecondary = Color.White,
    secondaryContainer = RosemaryGreenDark,
    onSecondaryContainer = Color.White,
    tertiary = HoneyGold,
    onTertiary = Color.Black,
    background = BasaltObsidian,
    surface = CardSlate,
    onBackground = DarkText,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF2C3034),
    onSurfaceVariant = Color(0xFFCFD8DC),
    error = ChiliRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronOrange,
    onPrimary = Color.White,
    primaryContainer = SaffronOrangeLight,
    onPrimaryContainer = Color.Black,
    secondary = RosemaryGreen,
    onSecondary = Color.White,
    secondaryContainer = RosemaryGreenLight,
    onSecondaryContainer = Color.Black,
    tertiary = HoneyGoldDark,
    onTertiary = Color.White,
    background = ParchmentVanilla,
    surface = CardCream,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = Color(0xFFF1F1EB),
    onSurfaceVariant = Color(0xFF37474F),
    error = ChiliRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to ensure our signature gourmet colors are always displayed
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
