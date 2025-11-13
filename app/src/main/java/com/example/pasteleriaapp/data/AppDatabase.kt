package com.example.pasteleriaapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pasteleriaapp.data.network.CartDao
import com.example.pasteleriaapp.data.network.ProductDao
import com.example.pasteleriaapp.data.network.UsuarioDao
import com.example.pasteleriaapp.model.CartItem
import com.example.pasteleriaapp.model.Product
import com.example.pasteleriaapp.model.Usuario

@Database(entities = [Product::class, Usuario::class, CartItem::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun cartDao(): CartDao

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