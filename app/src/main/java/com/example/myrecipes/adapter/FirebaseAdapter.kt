package com.example.myrecipes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipes.R
import com.example.myrecipes.model.SharedRecipes
import com.bumptech.glide.Glide

class FirebaseAdapter(private val recipeList: List<SharedRecipes>) :
    RecyclerView.Adapter<FirebaseAdapter.FirebaseViewHolder>() {

    class FirebaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)
        val recipeName: TextView = itemView.findViewById(R.id.recipe_name)
        val recipeCategory: TextView = itemView.findViewById(R.id.recipe_category)
        val recipeDescription: TextView = itemView.findViewById(R.id.recipe_description)
        val recipeCookingTime: TextView = itemView.findViewById(R.id.recipe_cooking_time)
        val recipeIngredients: TextView = itemView.findViewById(R.id.recipe_ingredients)
        val recipeSteps: TextView = itemView.findViewById(R.id.recipe_steps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirebaseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shared_recipe, parent, false)
        return FirebaseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FirebaseViewHolder, position: Int) {
        val currentItem = recipeList[position]

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .placeholder(R.drawable.idk_sumtingwong) // Optional placeholder
            .into(holder.recipeImage)

        // Bind text fields to the respective data from currentItem
        holder.recipeName.text = currentItem.name
        holder.recipeCategory.text = currentItem.category
        holder.recipeDescription.text = currentItem.description
        holder.recipeCookingTime.text = "Cooking Time: ${currentItem.cookingTime} mins"

        // Bind ingredients as a comma-separated string
        holder.recipeIngredients.text = currentItem.ingredients.joinToString(", ")

        // Bind steps as a numbered list
        holder.recipeSteps.text = currentItem.steps.joinToString(". ", prefix = "Steps: ")
    }

    override fun getItemCount() = recipeList.size
}
