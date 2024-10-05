package com.example.myrecipes.onlinedb

import com.example.myrecipes.model.MealDbResponse
import retrofit2.Call
import retrofit2.http.GET

interface TheMealDB {
    @GET("random.php")
    fun getRandomMeal(): Call<MealDbResponse>
}
