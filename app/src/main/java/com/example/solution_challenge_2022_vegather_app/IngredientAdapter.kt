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
    private val isSelected = HashMap<String,Boolean>()

    inner class IngredientViewHolder(val binding : IngredientRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = IngredientViewHolder(
                IngredientRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("Check",dataset[position] + isSelected[dataset[position]] )
        val binding = (holder as IngredientAdapter.IngredientViewHolder).binding
        binding.radioButton.text = dataset[position]
        binding.radioButton.isChecked = isSelected[dataset[position]] != false

        binding.radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("Check",isChecked.toString())
            when(isChecked){
                true -> {
                    buttonView.setTextColor(Color.parseColor("#81E768"))
                }
                false -> {
                    buttonView.setTextColor(Color.parseColor("#BCBCBC"))
                }
            }
            isSelected[buttonView.text.toString()] = isChecked
        }

    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setData(data : RecipeInformation){
        for (text in data.ingredient){
            dataset.add(text)
            isSelected[text] = false
        }
    }
}