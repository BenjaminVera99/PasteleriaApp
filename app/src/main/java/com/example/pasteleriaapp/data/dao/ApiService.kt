// src/main/kotlin/.../dao/ApiService.kt

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

    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Long): Product

    @POST("auth/register")
    suspend fun register(
        @Body registro: RegistroData
    ): Response<MensajeRespuesta>

    // --- Endpoint de Login (Recomendado añadir) ---
    @POST("auth/login")
    suspend fun login(@Body credenciales: InicioSesion): Response<LoginResponse>

    @PUT("auth/update")
    suspend fun updateProfile(
        // Utiliza el DTO que acabamos de crear
        @Body updateData: UpdateData
    ): Response<Map<String, Any>>

}