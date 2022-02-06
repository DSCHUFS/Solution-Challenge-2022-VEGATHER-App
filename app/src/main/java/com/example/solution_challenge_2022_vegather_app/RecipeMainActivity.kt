package com.example.solution_challenge_2022_vegather_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityRecipeMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding

class RecipeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRecipeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 'See More'를 통해 현재 액티비티에 접속했을 경우 값 업데이트 테스트코드
        val intent = getIntent()
        val foodName = intent.getStringExtra("foodName")
        binding.textView27.text = foodName

        binding.imageButton2.setOnClickListener {
            finish()
        }

        binding.ingredientRecycler.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        val adapter = IngredientAdapter(IngredientRecyclerBinding.inflate(layoutInflater))
        adapter.getData()
        binding.ingredientRecycler.adapter = adapter
    }
}