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
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.SearchAutocompleteRecyclerBinding

class AutocompleteSearchAdapter(private val binding : SearchAutocompleteRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var context : Context
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
        // 텍스트 하이라이팅으로 입력값에 해당하는 부분을 강조시켜 연관성을 드러낸다.
        textHighlighting(binding, position)

        binding.relatedText.setOnClickListener {
            sendFoodInfoToRecipeActivity(binding.relatedText.text.toString())
        }
    }

    override fun getItemCount(): Int {
        return foodName.size
    }

    fun setData( foodNameList : ArrayList<String>, Index : ArrayList<Int>,len : Int ){
        foodName = foodNameList
        startIndex = Index
        length = len
    }

    fun loadParentActivity(c : Context){
        context = c
    }

    private fun textHighlighting(binding : SearchAutocompleteRecyclerBinding,position : Int){
        val builder = SpannableStringBuilder(foodName[position])
        builder.setSpan(ForegroundColorSpan(Color.parseColor("#81E678")),
            startIndex[position],
            startIndex[position] + length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.relatedText.text = builder
    }

    private fun sendFoodInfoToRecipeActivity(foodName : String){
        val intentRecipe = Intent(context,RecipeMainActivity::class.java)
        intentRecipe.putExtra("callNumberFromAdapter",2)
        intentRecipe.putExtra("foodNameFromAdapter",foodName)
        context.startActivity(intentRecipe)
    }
}