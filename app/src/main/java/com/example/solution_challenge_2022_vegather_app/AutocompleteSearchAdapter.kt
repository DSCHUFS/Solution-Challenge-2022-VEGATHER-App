package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.SearchAutocompleteRecyclerBinding
import com.google.android.material.color.MaterialColors.getColor
import kotlin.properties.Delegates

class AutocompleteSearchAdapter(private val binding : SearchAutocompleteRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var foodName : ArrayList<String>
    private lateinit var startIndex : ArrayList<Int>
    private var length = 1

    inner class AutocompleteSearchViewHolder(val binding : SearchAutocompleteRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = AutocompleteSearchViewHolder(
        SearchAutocompleteRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as AutocompleteSearchViewHolder).binding

        val builder = SpannableStringBuilder(foodName[position])
        builder.setSpan(ForegroundColorSpan(Color.parseColor("#81E678")),
            startIndex[position],
            startIndex[position] + length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textView59.text = builder
    }

    override fun getItemCount(): Int {
        return foodName.size
    }

    fun setData( foodNameList : ArrayList<String>, Index : ArrayList<Int>,len : Int ){
        foodName = foodNameList
        startIndex = Index
        length = len
    }
}