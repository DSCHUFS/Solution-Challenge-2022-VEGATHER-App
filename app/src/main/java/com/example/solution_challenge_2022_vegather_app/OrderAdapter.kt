package com.example.solution_challenge_2022_vegather_app

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.OrderRecyclerBinding

class OrderAdapter(private val binding : OrderRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataset = ArrayList<String>()

    inner class OrderViewHolder(val binding : OrderRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = OrderViewHolder(
        OrderRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as OrderAdapter.OrderViewHolder).binding
        binding.textView64.text = (position + 1 ).toString()
        binding.checkBox.text = dataset[position]
        binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            when(isChecked){
                true -> {
                    buttonView.setTextColor(Color.parseColor("#81E768"))
                    binding.textView64.setTextColor(Color.parseColor("#81E768"))
                }
                false -> {
                    buttonView.setTextColor(Color.parseColor("#757575"))
                    binding.textView64.setTextColor((Color.parseColor("#757575")))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setData(text : RecipeInformation){
        for (text in text.order){
            dataset.add(text)
        }
    }
}