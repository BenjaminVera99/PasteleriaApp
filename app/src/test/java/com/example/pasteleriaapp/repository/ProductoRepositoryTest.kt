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
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0

        coEvery { mockApi.getProducts() } returns fakeProducts

        val repo = ProductoRepository(productDao = mockDao, apiService = mockApi)

        runTest {
            repo.refreshProducts()
        }

        coVerify(exactly = 1) { mockDao.clearTable() }
        coVerify(exactly = 1) { mockDao.insertAll(fakeProducts) }
    }
})