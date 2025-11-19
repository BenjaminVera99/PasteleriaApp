package com.example.pasteleriaapp.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formatea un valor Double a una cadena de texto con formato de moneda, 
 * sin decimales y con el símbolo de peso.
 * Ejemplo: 25000.0 -> $25.000
 */
fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    format.maximumFractionDigits = 0
    return format.format(price).replace("COP", "$").trim()
}