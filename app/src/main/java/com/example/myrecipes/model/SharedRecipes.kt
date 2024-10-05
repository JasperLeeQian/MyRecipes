package com.example.myrecipes.model

data class SharedRecipes (
    val imageUrl: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val ingredients: List<String> = listOf(),
    val steps: List<String> = listOf(),
    val cookingTime: Int = 0 // in minutes
)
