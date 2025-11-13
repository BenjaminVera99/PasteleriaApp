package com.example.pasteleriaapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pasteleriaapp.data.converters.OrderItemsConverter
import com.example.pasteleriaapp.data.network.CartDao
import com.example.pasteleriaapp.data.network.OrderDao
import com.example.pasteleriaapp.data.network.ProductDao
import com.example.pasteleriaapp.data.network.UsuarioDao
import com.example.pasteleriaapp.model.CartItem
import com.example.pasteleriaapp.model.Order
import com.example.pasteleriaapp.model.Product
import com.example.pasteleriaapp.model.Usuario

@Database(entities = [Product::class, Usuario::class, CartItem::class, Order::class], version = 5, exportSchema = false)
@TypeConverters(OrderItemsConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pasteleria_database"
                )
                .fallbackToDestructiveMigration() // Usamos esto para la simplicidad de la migración
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}