package com.example.fitnesstracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.R

private val DisplayFont = FontFamily(
    Font(R.font.lexend_variable, FontWeight.W300),
    Font(R.font.lexend_variable, FontWeight.W400),
    Font(R.font.lexend_variable, FontWeight.W500),
    Font(R.font.lexend_variable, FontWeight.W600),
    Font(R.font.lexend_variable, FontWeight.W700)
)

private val BodyFont = FontFamily(
    Font(R.font.noto_sans_variable, FontWeight.W400),
    Font(R.font.noto_sans_variable, FontWeight.W500),
    Font(R.font.noto_sans_variable, FontWeight.W700)
)

private fun displayStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight
) = TextStyle(
    fontFamily = DisplayFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)

private fun bodyStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight
) = TextStyle(
    fontFamily = BodyFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)

val Typography = Typography(
    displayLarge = displayStyle(57, 64, FontWeight.Light),
    displayMedium = displayStyle(45, 52, FontWeight.Medium),
    displaySmall = displayStyle(36, 44, FontWeight.Medium),
    headlineLarge = displayStyle(32, 40, FontWeight.SemiBold),
    headlineMedium = displayStyle(28, 36, FontWeight.SemiBold),
    headlineSmall = displayStyle(24, 32, FontWeight.SemiBold),
    titleLarge = displayStyle(22, 28, FontWeight.SemiBold),
    titleMedium = displayStyle(16, 24, FontWeight.Medium),
    titleSmall = displayStyle(14, 20, FontWeight.Medium),
    bodyLarge = bodyStyle(16, 24, FontWeight.Normal),
    bodyMedium = bodyStyle(14, 20, FontWeight.Normal),
    bodySmall = bodyStyle(12, 16, FontWeight.Normal),
    labelLarge = bodyStyle(14, 20, FontWeight.Medium),
    labelMedium = bodyStyle(12, 16, FontWeight.Medium),
    labelSmall = bodyStyle(11, 16, FontWeight.Medium)
)
