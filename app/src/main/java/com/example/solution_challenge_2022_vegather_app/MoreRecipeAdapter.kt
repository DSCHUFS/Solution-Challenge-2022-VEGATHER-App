package com.example.solution_challenge_2022_vegather_app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding

class MoreRecipeAdapter(private val binding : MainPageMoreRecipeRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataset = ArrayList<FoodInfo>()
    private lateinit var context : Context

    inner class MoreRecipeViewHolder(val binding : MainPageMoreRecipeRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = MoreRecipeViewHolder(
        MainPageMoreRecipeRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MoreRecipeViewHolder).binding
        binding.foodName.text = dataset[position].foodNameData
        binding.foodInfo.text = dataset[position].foodInfoData
        binding.likeCount.text = (dataset[position].likeCount + position).toString()
        binding.imageView3.setImageResource(R.drawable.food_sampe2)

        binding.container.setOnClickListener {
            val intentRecipe = Intent(context,RecipeMainActivity::class.java)
            intentRecipe.putExtra("callNumberFromAdapter",2)
            intentRecipe.putExtra("foodNameFromAdapter",binding.foodName.text)
            context.startActivity(intentRecipe)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun getData(data : FoodInfo){
        for (i in 1..5){
            dataset.add(data)
        }
    }

    fun setDataIfSearchResult(foodNameList : ArrayList<String>){
        for (i in foodNameList){
            val foodInfo = FoodInfo()
            foodInfo.foodNameData = i
            dataset.add(foodInfo)
        }
    }

    fun loadParentActivity( c : Context){
        context = c
    }
}