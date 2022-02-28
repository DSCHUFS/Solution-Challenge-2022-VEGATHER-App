package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentBinding.inflate
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityRecipeMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.OrderRecyclerBinding

class RecipeMainActivity : AppCompatActivity() {

    val binding by lazy{ ActivityRecipeMainBinding.inflate(layoutInflater)}
    private var recipeInfo : RecipeInformation? = RecipeInformation()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imageButton2.setOnClickListener {
            finish()
        }

        changeUiBarColor()

        setRecipeData(intent)

        binding.recipeComment.setOnClickListener {
            loadCommentActivity()
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("id",checkedId.toString())
            when(checkedId) {
                R.id.radioButton2 -> connectIngredientsAdapterWithOrientation("horizontal")
                R.id.radioButton3 -> connectIngredientsAdapterWithOrientation("vertical")
            }
        }
    }



    private fun changeUiBarColor(){
        val customUiBar = UiBar(window)
        customUiBar.setStatusBarTransparent()
        customUiBar.setStatusBarIconColor(isBlack = true)
        customUiBar.setNaviBarIconColor(isBlack = true)
    }

    private fun loadCommentActivity(){
        val commentIntent = Intent(this,CommentActivity::class.java)
        startActivity(commentIntent)
    }

    private fun connectIngredientsAdapterWithOrientation(layout : String){
        when(layout){
            "horizontal" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)

            "vertical" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this)
        }

        val adapter = IngredientAdapter(IngredientRecyclerBinding.inflate(layoutInflater))
        recipeInfo?.let { adapter.setData(it) }
        binding.ingredientRecycler.adapter = adapter
    }

    private fun connectOrderAdapter(){
        binding.orderRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter(OrderRecyclerBinding.inflate(layoutInflater))
        recipeInfo?.let { adapter.setData(it) }
        binding.orderRecycler.adapter = adapter
    }

    private fun setRecipeData(intent : Intent){
        recipeInfo = intent.getParcelableExtra<RecipeInformation>("recipeInfo")

        binding.recipeLike.text = recipeInfo?.like.toString()
        binding.recipeComment.text = recipeInfo?.comment.toString()
        binding.recipeName.text = recipeInfo?.name
        binding.recipeIntroduce.text = recipeInfo?.introduce
        binding.recipeNutrition1.text = recipeInfo?.nutrition?.get(0)
        binding.recipeNutrition2.text = recipeInfo?.nutrition?.get(1)
        binding.recipeNutrition3.text = recipeInfo?.nutrition?.get(2)
        binding.recipeNutrition4.text = recipeInfo?.nutrition?.get(3)
        connectOrderAdapter()
        connectIngredientsAdapterWithOrientation("horizontal")

    }
}