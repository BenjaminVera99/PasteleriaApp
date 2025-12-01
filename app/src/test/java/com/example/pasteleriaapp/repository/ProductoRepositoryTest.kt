package com.example.pasteleriaapp.repository

import ApiService
import com.example.pasteleriaapp.data.ProductoRepository
import com.example.pasteleriaapp.data.dao.ProductDao
import com.example.pasteleriaapp.model.Product
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
// ⭐ Importar la clase Log de Android y las funciones de mockk ⭐
import android.util.Log
import io.mockk.mockkStatic
import io.mockk.every
import kotlinx.coroutines.test.runTest

class ProductoRepositoryTest : StringSpec({

    val mockApi = mockk<ApiService>()
    val mockDao = mockk<ProductDao>(relaxed = true)

    // Datos simulados (mantener la estructura que ya te funcionó)
    val fakeProducts = listOf(
        Product(
            id = 1L,
            code = "TC001",
            category = "Tortas Cuadradas",
            name = "Torta de Chocolate",
            price = 45000.0,
            img = "/torta-cuadrada-chocolate.png",
            onSale = false
        ),
        Product(
            id = 2L,
            code = "TC002",
            category = "Pasteles",
            name = "Cheesecake",
            price = 30000.5,
            img = "/cheesecake.png",
            onSale = true
        )
    )

    "refreshProducts() debe obtener de API, borrar Room e insertar los nuevos productos" {
        // ⭐ PASO 1: Mockear la clase Log de Android (es una clase estática) ⭐
        mockkStatic(Log::class)
        // Decirle a MockK que cuando se llame a Log.e, simplemente devuelva 0 (un entero)
        every { Log.e(any(), any()) } returns 0
        // También mockeamos Log.d (que se llama en el success de tu refreshProducts)
        every { Log.d(any(), any()) } returns 0

        // 2. Configurar el comportamiento: La API devuelve los productos simulados (ÉXITO)
        coEvery { mockApi.getProducts() } returns fakeProducts

        // 3. Crear la instancia del Repository con los Mocks
        val repo = ProductoRepository(productDao = mockDao, apiService = mockApi)

        // 4. Ejecutar la función
        runTest {
            repo.refreshProducts()
        }

        // 5. Verificaciones de éxito
        coVerify(exactly = 1) { mockDao.clearTable() }
        coVerify(exactly = 1) { mockDao.insertAll(fakeProducts) }
    }
})