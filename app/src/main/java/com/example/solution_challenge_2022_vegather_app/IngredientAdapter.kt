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

    val dataset = ArrayList<String>()

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

    fun getData(){
        dataset.add("Carrot  2g")
        dataset.add("Cheese 100g")
        dataset.add("Milk  250ml")
        dataset.add("Ingredient test  1250g")
        dataset.add("Egg scrumble  20g")
        dataset.add("Special squid  2m")
    }

}