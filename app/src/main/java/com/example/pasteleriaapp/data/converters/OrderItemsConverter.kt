package com.example.pasteleriaapp.data.converters

import androidx.room.TypeConverter
import com.example.pasteleriaapp.ui.model.UiCartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OrderItemsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromUiCartItemList(items: List<UiCartItem>?): String? {
        return items?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toUiCartItemList(json: String?): List<UiCartItem>? {
        return json?.let {
            val listType = object : TypeToken<List<UiCartItem>>() {}.type
            gson.fromJson(it, listType)
        }
    }
}