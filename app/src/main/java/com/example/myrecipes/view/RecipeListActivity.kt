package com.example.myrecipes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipes.adapters.RecipeAdapter
import com.example.myrecipes.model.Recipe
import com.example.myrecipes.model.RecipeTypes
import com.example.myrecipes.viewmodel.RecipeViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class RecipeListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeTypeSpinner: Spinner
    private lateinit var addRecipeButton: Button

    private val viewModel: RecipeViewModel by viewModels()

    private var allRecipes: MutableList<Recipe> = mutableListOf()
    private var recipeTypeNames: MutableList<String> = mutableListOf("Show All")

    // Register a launcher for RecipeDetailActivity to handle result
    private val recipeDetailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // Handle updated recipe
            val updatedRecipe = result.data?.getSerializableExtra("UPDATED_RECIPE") as? Recipe
            if (updatedRecipe != null) {
                updateRecipeInList(updatedRecipe)
            }

            // Handle deleted recipe
            val deletedRecipeId = result.data?.getIntExtra("DELETE_RECIPE_ID", -1) ?: -1
            if (deletedRecipeId != -1) {
                deleteRecipeFromList(deletedRecipeId)
            }
        }
    }

    // Register a launcher for AddRecipeActivity
    private val addRecipeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val newRecipe = Recipe(
                    id = allRecipes.size + 1,
                    name = data.getStringExtra("NEW_RECIPE_NAME") ?: "",
                    type = data.getStringExtra("NEW_RECIPE_TYPE") ?: "",
                    ingredients = data.getStringArrayListExtra("NEW_RECIPE_INGREDIENTS") ?: listOf(),
                    steps = data.getStringArrayListExtra("NEW_RECIPE_STEPS") ?: listOf(),
                    imageUrl = data.getStringExtra("NEW_RECIPE_IMAGE_URL") // Corrected key for image file name
                )
                allRecipes.add(newRecipe)
                saveRecipesToInternalStorage(allRecipes)
                updateRecipeListBasedOnSelection()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        // Initialize RecyclerView and Spinner
        recyclerView = findViewById(R.id.recipe_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recipeTypeSpinner = findViewById(R.id.recipe_type_spinner)
        addRecipeButton = findViewById(R.id.add_recipe_button)

        // Setup button click for adding a new recipe
        addRecipeButton.setOnClickListener {
            val filteredRecipeTypes = recipeTypeNames.filter { it != "Show All" }
            val intent = Intent(this, AddRecipeActivity::class.java)
            intent.putStringArrayListExtra("RECIPE_TYPES", ArrayList(filteredRecipeTypes))
            addRecipeLauncher.launch(intent)
        }

        // Observe recipe data and update the list when data changes
        viewModel.recipes.observe(this, Observer { recipes ->
            allRecipes.clear()
            allRecipes.addAll(recipes)
            updateRecipeListBasedOnSelection()
        })

        // Load initial data
        loadRecipes()
        setupRecipeTypeSpinner()
    }

    private fun setupRecipeTypeSpinner() {
        val recipeTypes: List<RecipeTypes> = loadRecipeTypesFromAssets()

        // Add recipe types to the spinner
        recipeTypeNames.addAll(recipeTypes.map { recipeType: RecipeTypes -> recipeType.type })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTypeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recipeTypeSpinner.adapter = adapter

        // Handle selection of a recipe type in the spinner
        recipeTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateRecipeListBasedOnSelection()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    // Update the recipe list based on the selected recipe type
    private fun updateRecipeListBasedOnSelection() {
        val selectedType = recipeTypeSpinner.selectedItem?.toString() ?: "Show All"
        val filteredRecipes = if (selectedType == "Show All") {
            allRecipes
        } else {
            allRecipes.filter { it.type == selectedType }
        }

        // Set the adapter for RecyclerView
        recyclerView.adapter = RecipeAdapter(filteredRecipes, this).apply {
            setOnItemClickListener { recipe ->
                val intent = Intent(this@RecipeListActivity, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_DETAIL", recipe)
                recipeDetailLauncher.launch(intent)
            }
        }
    }

    // Load recipes either from internal storage or assets (on first run)

    private fun loadRecipes() {
        val recipesFile = File(filesDir, "recipes.json")
        if (recipesFile.exists()) {
            val json = recipesFile.bufferedReader().use { it.readText() }

            // Correct way to deserialize a List<Recipe>
            val recipeListType = object : TypeToken<List<Recipe>>() {}.type
            allRecipes = Gson().fromJson(json, recipeListType)
        } else {
            allRecipes = loadRecipesFromAssets().toMutableList()
            saveRecipesToInternalStorage(allRecipes)
        }
        updateRecipeListBasedOnSelection()
    }

    // Save the list of recipes to internal storage
    private fun saveRecipesToInternalStorage(recipes: List<Recipe>) {
        val file = File(filesDir, "recipes.json")
        val json = Gson().toJson(recipes)
        file.writeText(json)
    }

    // Update the recipe in the list and save to local storage
    private fun updateRecipeInList(updatedRecipe: Recipe) {
        val index = allRecipes.indexOfFirst { it.id == updatedRecipe.id }
        if (index != -1) {
            allRecipes[index] = updatedRecipe
            saveRecipesToInternalStorage(allRecipes) // Save the updated list to internal storage
            updateRecipeListBasedOnSelection() // Refresh the list
        }
    }


    // Delete the recipe from the list and save to local storage
    private fun deleteRecipeFromList(recipeId: Int) {
        val index = allRecipes.indexOfFirst { it.id == recipeId }
        if (index != -1) {
            allRecipes.removeAt(index)
            saveRecipesToInternalStorage(allRecipes) // Save the updated list to internal storage
            updateRecipeListBasedOnSelection() // Refresh the list
        }
    }

    // Load the initial list of recipes from the assets folder
    private fun loadRecipesFromAssets(): List<Recipe> {
        val json = assets.open("recipes.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(json, object : TypeToken<List<Recipe>>() {}.type)
    }

    // Load the available recipe types from assets
    private fun loadRecipeTypesFromAssets(): List<RecipeTypes> {
        val json = assets.open("recipetypes.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(json, object : TypeToken<List<RecipeTypes>>() {}.type)
    }
}
