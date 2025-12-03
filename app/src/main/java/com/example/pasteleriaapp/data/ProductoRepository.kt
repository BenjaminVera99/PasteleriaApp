package com.example.pasteleriaapp.data

import ApiService
import android.util.Log
import com.example.pasteleriaapp.data.dao.ProductDao
import com.example.pasteleriaapp.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

 class ProductoRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService
) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val products: Flow<List<Product>> = productDao.getProducts()


    suspend fun refreshProducts() {
        _isLoading.value = true

        try {
            val networkProducts = apiService.getProducts() // Llama a la API

            productDao.clearTable()
            productDao.insertAll(networkProducts)

            Log.d("ProductoRepository", "Productos actualizados desde el servidor.")

        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al cargar desde API: ${e.message}")

            val localCount = productDao.countProducts() // 👈 Este método debe ser añadido al DAO

            if (localCount == 0) {
                Log.e("ProductoRepository", "No hay datos locales. La app no puede cargar productos.")
            }
        } finally {
            _isLoading.value = false
        }
    }
}