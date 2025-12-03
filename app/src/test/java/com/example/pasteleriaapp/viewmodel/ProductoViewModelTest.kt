package com.example.pasteleriaapp.viewmodel

import com.example.pasteleriaapp.data.ProductoRepository
import com.example.pasteleriaapp.model.Product
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.just
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope // ⭐ IMPORTANTE: Nuevo Scope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class ProductoViewModel(
    private val repository: ProductoRepository,
    private val dispatcher: CoroutineDispatcher
) : androidx.lifecycle.ViewModel() {

    private val testScope = CoroutineScope(dispatcher)

    private val _productList = MutableStateFlow(emptyList<Product>())
    val productList: kotlinx.coroutines.flow.StateFlow<List<Product>> = _productList.asStateFlow()

    fun fetchProducts() {
        testScope.launch {
            try {
                repository.refreshProducts()
            } catch (e: Exception) {
            }
        }
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest : StringSpec({

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

    "fetchProducts() debe disparar la llamada a repository.refreshProducts()" {
        val mockRepo = mockk<ProductoRepository>()

        coEvery { mockRepo.products } returns MutableStateFlow(emptyList())

        coEvery { mockRepo.refreshProducts() } just Runs

        val dispatcher = StandardTestDispatcher()

        val viewModel = ProductoViewModel(
            repository = mockRepo,
            dispatcher = dispatcher
        )

        runTest(dispatcher) {
            viewModel.fetchProducts()

            advanceUntilIdle()

            coVerify(exactly = 1) { mockRepo.refreshProducts() }
        }
    }
})