package com.example.myrecipes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myrecipes.model.Recipe
import com.example.myrecipes.localdb.RecipeRepository

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)

    // MutableLiveData for holding the list of recipes
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    // Function to load the recipes
    fun loadRecipes() {
        val recipeList = repository.getRecipesFromJson()
        _recipes.postValue(recipeList)
    }
}
