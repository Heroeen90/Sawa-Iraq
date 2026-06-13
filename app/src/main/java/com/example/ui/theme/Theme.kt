package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SawaPrimary,
    secondary = SawaSecondary,
    tertiary = SawaTertiary,
    background = SlateBg,
    surface = SlateCard,
    onPrimary = SlateBg,
    onSecondary = SlateBg,
    onBackground = SlateTextOnDark,
    onSurface = SlateTextOnDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SawaPrimaryDark,
    secondary = SawaSecondary,
    tertiary = SawaTertiary,
    background = WarmLightBg,
    surface = WarmLightCard,
    onPrimary = WarmLightBg,
    onSecondary = DarkTextOnLight,
    onBackground = DarkTextOnLight,
    onSurface = DarkTextOnLight
  )

@Composable
fun SawaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic system scheme to enforce the custom premium branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
