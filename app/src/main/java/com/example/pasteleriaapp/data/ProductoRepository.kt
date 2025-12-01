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

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Flow de productos desde Room
    val products: Flow<List<Product>> = productDao.getProducts()


    // Refrescar datos desde la API y guardarlos en Room
    suspend fun refreshProducts() {
        _isLoading.value = true

        try {
            val networkProducts = apiService.getProducts() // Llama a la API

            productDao.clearTable() // Asegúrate de que este método sea 'suspend'
            productDao.insertAll(networkProducts) // Asegúrate de que este método sea 'suspend'

            Log.d("ProductoRepository", "Productos actualizados desde el servidor.")

        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al cargar desde API: ${e.message}")

            // 🔑 CAMBIO 3: Usamos productDao.countProducts() para verificar si la BD está vacía.
            // Esto es más limpio y no requiere manejar el Flow.
            val localCount = productDao.countProducts() // 👈 Este método debe ser añadido al DAO

            // Si falla la API y NO hay datos en Room
            if (localCount == 0) {
                Log.e("ProductoRepository", "No hay datos locales. La app no puede cargar productos.")
                // Aquí podrías notificar al ViewModel con un StateFlow de errores si lo deseas.
            }
        } finally {
            _isLoading.value = false
        }
    }
}