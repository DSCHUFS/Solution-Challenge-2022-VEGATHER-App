package com.example.solution_challenge_2022_vegather_app

import android.content.Context
import android.content.Intent
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
        binding.likeCount.text = (dataset[position].like + position).toString()
        binding.imageView3.setImageResource(R.drawable.food_sampe2)

        binding.container.setOnClickListener {
            val intentRecipe = Intent(context,RecipeMainActivity::class.java)
//            intentRecipe.putExtra("callNumberFromAdapter",2)
            intentRecipe.putExtra("recipeInfo",dataset[position])
            context.startActivity(intentRecipe)
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
}