// Archivo: ApiService.kt MODIFICADO

import com.example.pasteleriaapp.data.dao.UpdateData
import com.example.pasteleriaapp.model.InicioSesion
import com.example.pasteleriaapp.model.LoginResponse
import com.example.pasteleriaapp.model.MensajeRespuesta
import com.example.pasteleriaapp.model.Product
import com.example.pasteleriaapp.model.RegistroData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.PUT

interface ApiService {

    // Rutas de Productos (Ahora: /api/products)
    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Long): Product

    // Rutas de Autenticación (Ahora: /api/auth/register)
    // Spring Boot necesita la URL completa, incluso si la BASE_URL ya tiene /api/.
    // Por eso ajustamos el @POST para que sea solo "auth/register"
    @POST("auth/register")
    suspend fun register(
        @Body registro: RegistroData
    ): Response<MensajeRespuesta>

    @POST("auth/login")
    suspend fun login(@Body credenciales: InicioSesion): Response<LoginResponse>

    @PUT("auth/update")
    suspend fun updateProfile(
        @Body updateData: UpdateData
    ): Response<Map<String, Any>>

    // Nota: Las demás rutas (como /api/cart) seguirían el mismo patrón.
}