package com.example.solution_challenge_2022_vegather_app

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding

class IngredientAdapter(private val binding : IngredientRecyclerBinding) :
            RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataset = ArrayList<String>()

    inner class IngredientViewHolder(val binding : IngredientRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = IngredientViewHolder(
                IngredientRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as IngredientAdapter.IngredientViewHolder).binding
        binding.radioButton.text = dataset[position]
        binding.radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            when(isChecked){
                true -> buttonView.setTextColor(Color.parseColor("#81E768"))
                false -> buttonView.setTextColor(Color.parseColor("#BCBCBC"))
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setData(data : RecipeInformation){
        for (text in data.ingredient)
            dataset.add(text)
    }

}