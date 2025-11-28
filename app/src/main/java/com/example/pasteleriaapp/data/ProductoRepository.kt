package com.example.pasteleriaapp.data

import android.app.Application
import android.util.Log
import com.example.pasteleriaapp.data.dao.ProductDao
import com.example.pasteleriaapp.data.dao.RetrofitInstance
import com.example.pasteleriaapp.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductoRepository(application: Application) {

    private val productDao: ProductDao
    private val apiService = RetrofitInstance.api

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Flow de productos desde Room
    val products: Flow<List<Product>>

    init {
        val database = AppDatabase.getDatabase(application)
        productDao = database.productDao()
        products = productDao.getProducts()

        // Primera carga automática
        CoroutineScope(Dispatchers.IO).launch {
            refreshProducts()
        }
    }

    // Refrescar datos desde la API y guardarlos en Room
    suspend fun refreshProducts() {
        _isLoading.value = true

        try {
            val networkProducts = apiService.getProducts()

            // Sobrescribe completamente la BD con datos del servidor
            productDao.clearTable()
            productDao.insertAll(networkProducts)

            Log.d("ProductoRepository", "Productos actualizados desde el servidor.")

        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al cargar desde API: ${e.message}")

            // Si falla la API y NO hay datos en Room
            if (products.first().isEmpty()) {
                Log.e("ProductoRepository", "No hay datos locales. La app no puede cargar productos.")
            }
        } finally {
            _isLoading.value = false
        }
    }
}
