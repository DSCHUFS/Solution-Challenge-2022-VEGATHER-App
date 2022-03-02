package com.example.solution_challenge_2022_vegather_app

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore

class MoreRecipeAdapter(private val binding : MainPageMoreRecipeRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataset = ArrayList<RecipeInformation>()
    private lateinit var context : Context

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    inner class MoreRecipeViewHolder(val binding : MainPageMoreRecipeRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = MoreRecipeViewHolder(
        MainPageMoreRecipeRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MoreRecipeViewHolder).binding

        binding.foodName.text = dataset[position].name
        binding.foodInfo.text = dataset[position].introduce
        binding.likeCount.text = dataset[position].like.toString()
        binding.imageView3.setImageResource(R.drawable.food_sampe2)

        binding.container.setOnClickListener {
            goToRecipePage(position)
        }

        db.collection("Recipe").document(dataset[position].name)
            .addSnapshotListener { value, error ->
                val recipeInfo = value?.toObject(RecipeInformation::class.java)
                binding.likeCount.text = recipeInfo?.like.toString()
            }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setData(recipeData : ArrayList<RecipeInformation>){
        dataset = recipeData
    }

    fun appendRecipeData(data : RecipeInformation){
        dataset.add(data)
    }

    fun loadParentActivity( c : Context){
        context = c
    }

    private fun goToRecipePage( position: Int ){
        val intentRecipe = Intent(context,RecipeMainActivity::class.java)
        intentRecipe.putExtra("recipeInfo",dataset[position])
        context.startActivity(intentRecipe)
    }
}