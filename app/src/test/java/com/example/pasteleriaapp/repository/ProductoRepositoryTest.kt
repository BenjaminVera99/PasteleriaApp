package com.example.pasteleriaapp.repository

import ApiService
import com.example.pasteleriaapp.data.ProductoRepository
import com.example.pasteleriaapp.data.dao.ProductDao // Importa tu DAO
import com.example.pasteleriaapp.model.Product
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class ProductoRepositoryTest : StringSpec({

    // Mocks de las dependencias
    val mockApi = mockk<ApiService>()
    // Mockeamos el DAO para verificar que se llame a insertAll (relaxed = true para evitar errores de métodos no definidos)
    val mockDao = mockk<ProductDao>(relaxed = true)

    // ⭐ DATOS SIMULADOS CORREGIDOS (AJUSTAR CAMPOS) ⭐
    // Esta estructura simula los errores de tu consola. DEBES AJUSTAR LOS TIPOS Y NOMBRES
    // DE ACUERDO A TU ARCHIVO Product.kt.
    val fakeProducts = listOf(
        Product(
            // Asumiendo que 'id' es Long, y 'price' es Double, y existen 'code', 'img', 'onSale'
            // SI TUS CAMPOS SON DIFERENTES, AJÚSTALOS AQUÍ EXACTAMENTE COMO ESTÁN EN PRODUCT.KT
            id = 1L, // Long, no String
            code = "TC001",
            category = "Tortas Cuadradas",
            name = "Torta de Chocolate",
            price = 45000.0, // Double, no Int
            img = "/torta-cuadrada-chocolate.png", // Usa el nombre de campo correcto ('img' o 'imageUri')
            onSale = false // Campo que salía como faltante
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
        // 1. Configurar el comportamiento: La API devuelve los productos simulados
        coEvery { mockApi.getProducts() } returns fakeProducts

        // 2. Crear la instancia del Repository con los Mocks
        val repo = ProductoRepository(productDao = mockDao, apiService = mockApi)

        // 3. Ejecutar la función (la que realmente llama a la API)
        runTest {
            repo.refreshProducts()
        }

        // 4. ⭐ VERIFICACIONES CLAVE ⭐

        // Verificar que el DAO borró la tabla (según tu lógica de refresh)
        coVerify(exactly = 1) { mockDao.clearTable() }

        // Verificar que el DAO insertó la lista de productos que vinieron de la API
        coVerify(exactly = 1) { mockDao.insertAll(fakeProducts) }
    }
})