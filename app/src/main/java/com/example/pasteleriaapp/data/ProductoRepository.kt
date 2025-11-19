package com.example.pasteleriaapp.data

import android.app.Application
import android.util.Log
import com.example.pasteleriaapp.R
import com.example.pasteleriaapp.data.network.ProductDao
import com.example.pasteleriaapp.data.network.RetrofitInstance
import com.example.pasteleriaapp.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductoRepository(application: Application) {

    private val productDao: ProductDao
    private val apiService = RetrofitInstance.api

    // La fuente de verdad para los productos es la base de datos.
    val products: Flow<List<Product>>

    init {
        val database = AppDatabase.getDatabase(application)
        productDao = database.productDao()
        products = productDao.getProducts()

        // Inicia la actualización de datos desde la red en segundo plano.
        CoroutineScope(Dispatchers.IO).launch {
            refreshProducts()
        }
    }

    /**
     * Intenta actualizar los productos desde la API. Si falla, y la base de datos está vacía,
     * la llena con los datos locales de respaldo.
     */
    private suspend fun refreshProducts() {
        try {
            val networkProducts = apiService.getProducts()
            productDao.insertAll(networkProducts)
            Log.d("ProductoRepository", "Productos actualizados desde la API.")
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al actualizar desde la API: ${e.message}")
            if (products.first().isEmpty()) {
                Log.d("ProductoRepository", "La base de datos está vacía. Cargando desde el respaldo local.")
                productDao.insertAll(backupProducts)
            }
        }
    }
}

// Lista de productos de respaldo, ahora interna al repositorio.
private val backupProducts = listOf(
    Product(
        id = 1,
        name = "Pastel de Chocolate",
        price = 25.0,
        imageResId = R.drawable.pastel_chocolate,
        description = "Un clásico irresistible. Bizcocho de chocolate húmedo relleno y cubierto con una rica ganache de chocolate semi-amargo."
    ),
    Product(
        id = 2,
        name = "Cheesecake de Fresa",
        price = 30.0,
        imageResId = R.drawable.cheesecake_fresa,
        description = "Cremoso cheesecake sobre una base de galleta, coronado con una generosa capa de mermelada de fresas frescas."
    ),
    Product(
        id = 3,
        name = "Tarta de Manzana",
        price = 20.0,
        imageResId = R.drawable.tarta_manzana,
        description = "La tarta casera por excelencia. Finas láminas de manzana sobre una base de hojaldre crujiente con un toque de canela."
    ),
    Product(
        id = 4,
        name = "Galletas con Chispas",
        price = 10.0,
        imageResId = R.drawable.galletas_chispas,
        description = "Una docena de galletas recién horneadas, crujientes por fuera y suaves por dentro, cargadas de chispas de chocolate."
    ),
    Product(
        id = 5,
        name = "Cupcakes de Vainilla",
        price = 5.0,
        imageResId = R.drawable.cupcakes_vainilla,
        description = "4 cupcakes esponjosos de vainilla con un frosting de crema de mantequilla suave y decoraciones de azúcar."
    ),
    Product(
        id = 6,
        name = "Donas Glaseadas",
        price = 6.0,
        imageResId = R.drawable.donas_glaseadas,
        description = "3 porciones de donas tiernas y esponjosas, cubiertas con un glaseado de azúcar clásico que se derrite en la boca."
    ),
    Product(
        id = 7,
        name = "Pastel de Zanahoria",
        price = 28.0,
        imageResId = R.drawable.pastel_zanahoria,
        description = "Un bizcocho especiado y húmedo con zanahoria rallada y nueces, cubierto con un delicioso frosting de queso crema."
    ),
    Product(
        id = 8,
        name = "Tiramisú Clásico",
        price = 32.0,
        imageResId = R.drawable.tiramisu,
        description = "Capas de bizcochos de soletilla empapados en café y licor, alternadas con una crema suave de mascarpone y cacao en polvo."
    ),
    Product(
        id = 9,
        name = "Rollos de canela",
        price = 15.0,
        imageResId = R.drawable.rollos_de_canela,
        description = "Sabrosos Rollos de canela perfectos para una tarde de sabor inigualable."
    ),
    Product(
        id = 10,
        name = "Tarta Tres Leches",
        price = 27.0,
        imageResId = R.drawable.tres_leches,
        description = "Bizcocho esponjoso bañado en una mezcla de tres tipos de leche, cubierto con merengue suave y un toque de canela."
    )
)
