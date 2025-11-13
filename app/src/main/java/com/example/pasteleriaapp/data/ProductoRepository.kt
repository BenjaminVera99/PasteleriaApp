package com.example.pasteleriaapp.data

import android.app.Application
import android.util.Log
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
     * la llena con los datos locales del DataSource.
     */
    private suspend fun refreshProducts() {
        try {
            // 1. Intenta obtener los productos de la red
            val networkProducts = apiService.getProducts()
            productDao.insertAll(networkProducts)
            Log.d("PastryRepository", "Productos actualizados desde la API.")
        } catch (e: Exception) {
            Log.e("PastryRepository", "Error al actualizar desde la API: ${e.message}")
            // 2. Si la red falla, comprueba si la base de datos está vacía.
            if (products.first().isEmpty()) {
                Log.d("PastryRepository", "La base de datos está vacía. Cargando desde DataSource local.")
                // 3. Si está vacía, la llena con los datos de DataSource.
                productDao.insertAll(DataSource.products)
            }
        }
    }
}