package com.example.simmr.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.simmr.core.designsystem.R

val RethinkSans = FontFamily(
    Font(R.font.rethink_sans_regular, FontWeight.Normal),
    Font(R.font.rethink_sans_medium, FontWeight.Medium),
    Font(R.font.rethink_sans_semi_bold, FontWeight.SemiBold),
    Font(R.font.rethink_sans_bold, FontWeight.Bold),
    Font(R.font.rethink_sans_extra_bold, FontWeight.ExtraBold),
    Font(R.font.rethink_sans_italic, FontWeight.Normal, FontStyle.Italic),
)

/** Named styles that have no one-to-one Material typography role. */
object SimmrTextStyles {
    val LargeTitle = simmrTextStyle(FontWeight.ExtraBold, 34, 41)
    val Title = simmrTextStyle(FontWeight.Bold, 28, 34)
    val Title2 = simmrTextStyle(FontWeight.SemiBold, 22, 28)
    val Title3 = simmrTextStyle(FontWeight.SemiBold, 20, 25)
    val Headline = simmrTextStyle(FontWeight.SemiBold, 17, 22)
    val Body = simmrTextStyle(FontWeight.Normal, 17, 22)
    val BodyMedium = simmrTextStyle(FontWeight.Medium, 17, 22)
    val Callout = simmrTextStyle(FontWeight.Medium, 16, 21)
    val Subheadline = simmrTextStyle(FontWeight.Normal, 15, 20)
    val Footnote = simmrTextStyle(FontWeight.Normal, 14, 18)
    val Caption = simmrTextStyle(FontWeight.Medium, 12, 16)
    val Caption2 = simmrTextStyle(FontWeight.Normal, 11, 14)
    val Button = simmrTextStyle(FontWeight.SemiBold, 17, 22)
    val CookingStepTitle = simmrTextStyle(FontWeight.Bold, 30, 36)
    val CookingInstruction = simmrTextStyle(FontWeight.Medium, 19, 25)
    val TimerDisplay = simmrTextStyle(FontWeight.ExtraBold, 44, 52)
}

val SimmrTypography = Typography(
    displaySmall = SimmrTextStyles.LargeTitle,
    headlineLarge = SimmrTextStyles.Title,
    headlineMedium = SimmrTextStyles.CookingStepTitle,
    headlineSmall = SimmrTextStyles.Title2,
    titleLarge = SimmrTextStyles.Title3,
    titleMedium = SimmrTextStyles.Headline,
    bodyLarge = SimmrTextStyles.Body,
    bodyMedium = SimmrTextStyles.Subheadline,
    bodySmall = SimmrTextStyles.Footnote,
    labelLarge = SimmrTextStyles.Button,
    labelMedium = SimmrTextStyles.Caption,
    labelSmall = SimmrTextStyles.Caption2,
)

private fun simmrTextStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
) = TextStyle(
    fontFamily = RethinkSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)
