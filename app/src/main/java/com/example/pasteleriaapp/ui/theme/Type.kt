package com.example.pasteleriaapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pasteleriaapp.R

// 1. Define the Font Families from the files in res/font
val Pacifico = FontFamily(
    Font(R.font.pacifico_regular, FontWeight.Normal)
)

val Lato = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold)
)

// 2. Create the Typography object, overriding the default styles
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Pacifico,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Pacifico,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)
