package com.example.myrecipes.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrecipes.adapter.FirebaseAdapter
import com.example.myrecipes.databinding.ActivitySharedRecipeBinding
import com.example.myrecipes.model.SharedRecipes
import com.google.firebase.database.*

class SharedRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySharedRecipeBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recipeList: ArrayList<SharedRecipes>
    private lateinit var adapter: FirebaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharedRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(this)
        recipeList = ArrayList()
        adapter = FirebaseAdapter(recipeList)
        binding.recipeRecyclerView.adapter = adapter

        // Fetch data from Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("sharedRecipes")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recipeList.clear()
                for (dataSnapshot in snapshot.children) {
                    val recipe = dataSnapshot.getValue(SharedRecipes::class.java)
                    if (recipe != null) {
                        recipeList.add(recipe)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Log the error message for debugging purposes
                Log.e("DatabaseError", "The read failed: " + error.message)

                // Display a message to the user
                Toast.makeText(this@SharedRecipeActivity, "Failed to read data from the database. Please try again.", Toast.LENGTH_SHORT).show()

            }

        })
    }
}
