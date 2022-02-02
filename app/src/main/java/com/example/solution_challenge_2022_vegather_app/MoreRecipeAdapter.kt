package com.example.solution_challenge_2022_vegather_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding

class MoreRecipeAdapter(private val binding : MainPageMoreRecipeRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val dataset = ArrayList<FoodInfo>()

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
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun getData(data : FoodInfo){
        for (i in 1..5){
            dataset.add(data)
        }
    }
}