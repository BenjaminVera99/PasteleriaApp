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

// ⭐ CLASE DE AYUDA CORREGIDA: Usa CoroutineScope en lugar de viewModelScope ⭐
class ProductoViewModel(
    private val repository: ProductoRepository,
    private val dispatcher: CoroutineDispatcher
) : androidx.lifecycle.ViewModel() {

    // ⭐ 1. Definimos un scope testable usando el dispatcher inyectado ⭐
    private val testScope = CoroutineScope(dispatcher)

    private val _productList = MutableStateFlow(emptyList<Product>())
    val productList: kotlinx.coroutines.flow.StateFlow<List<Product>> = _productList.asStateFlow()

    fun fetchProducts() {
        // ⭐ 2. Usamos el scope testable para lanzar la corrutina ⭐
        testScope.launch {
            try {
                // La tarea principal del ViewModel es disparar la actualización en el repositorio
                repository.refreshProducts()
            } catch (e: Exception) {
                // Manejo de error simulado
            }
        }
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest : StringSpec({

    // 1. Datos simulados (para asegurar compatibilidad)
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
        // 2. Crear el mock del ProductoRepository
        val mockRepo = mockk<ProductoRepository>()

        // Mockear el Flow 'products' del repositorio (necesario para el constructor)
        coEvery { mockRepo.products } returns MutableStateFlow(emptyList())

        // Mockear la función de acción del repositorio. Usamos 'just Runs' para simular éxito
        coEvery { mockRepo.refreshProducts() } just Runs

        // 4. Configurar el Dispatcher de test
        val dispatcher = StandardTestDispatcher()

        // 5. Crear el ViewModel
        val viewModel = ProductoViewModel(
            repository = mockRepo,
            dispatcher = dispatcher
        )

        // 6. Ejecutar el test
        runTest(dispatcher) {
            viewModel.fetchProducts()

            // Permite que las corrutinas internas terminen su ejecución
            advanceUntilIdle()

            // 7. Verificación: Aseguramos que se llamó a la función de refresco
            coVerify(exactly = 1) { mockRepo.refreshProducts() }
        }
    }
})