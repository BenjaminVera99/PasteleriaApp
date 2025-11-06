package com.example.pasteleriaapp.data

import com.example.pasteleriaapp.R
import com.example.pasteleriaapp.model.Product

object DataSource {
    val products = listOf(
        Product(
            id = 1,
            name = "Pastel de Chocolate",
            price = 25000,
            imageResId = R.drawable.pastel_chocolate,
            description = "Un clásico irresistible. Bizcocho de chocolate húmedo relleno y cubierto con una rica ganache de chocolate semi-amargo."
        ),
        Product(
            id = 2,
            name = "Cheesecake de Fresa",
            price = 30000,
            imageResId = R.drawable.cheesecake_fresa,
            description = "Cremoso cheesecake sobre una base de galleta, coronado con una generosa capa de mermelada de fresas frescas."
        ),
        Product(
            id = 3,
            name = "Tarta de Manzana",
            price = 20000,
            imageResId = R.drawable.tarta_manzana,
            description = "La tarta casera por excelencia. Finas láminas de manzana sobre una base de hojaldre crujiente con un toque de canela."
        ),
        Product(
            id = 4,
            name = "Galletas con Chispas",
            price = 10000,
            imageResId = R.drawable.galletas_chispas,
            description = "Una docena de galletas recién horneadas, crujientes por fuera y suaves por dentro, cargadas de chispas de chocolate."
        ),
        Product(
            id = 5,
            name = "Cupcakes de Vainilla",
            price = 5000,
            imageResId = R.drawable.cupcakes_vainilla,
            description = "4 cupcakes esponjosos de vainilla con un frosting de crema de mantequilla suave y decoraciones de azúcar."
        ),
        Product(
            id = 6,
            name = "Donas Glaseadas",
            price = 6000,
            imageResId = R.drawable.donas_glaseadas,
            description = "3 porciones de donas tiernas y esponjosas, cubiertas con un glaseado de azúcar clásico que se derrite en la boca."
        )
    )
}