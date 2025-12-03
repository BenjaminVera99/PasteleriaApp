package com.example.pasteleriaapp.util

import java.text.NumberFormat
import java.util.Locale


fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    format.maximumFractionDigits = 0
    return format.format(price).replace("COP", "$").trim()
}