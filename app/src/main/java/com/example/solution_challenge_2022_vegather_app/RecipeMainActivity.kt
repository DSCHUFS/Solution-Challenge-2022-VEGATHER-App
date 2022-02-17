package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityRecipeMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.OrderRecyclerBinding

class RecipeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRecipeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 액티비티를 종료시키고 이전 액티비티로 이동한다.
        binding.imageButton2.setOnClickListener {
            finish()
        }

        // 메인 페이지에서 음식을 선택하면 선택한 음식의 정보를 레시피 액티비티에서 넘겨받는다.
        val intent = intent
        val callNumber = intent.getIntExtra("callNumber",0)
        val foodName = getFoodNameFromCall(intent)
        binding.textView27.text = foodName

        binding.textView26.setOnClickListener {
            val commentIntent = Intent(this,CommentActivity::class.java)
            startActivity(commentIntent)
        }



        // 재료와 레시피 제작 순서의 리사이클러 뷰를 연결한다.
        connectOrderAdapter(binding)
        connectIngredientsAdapterWithOrientation("horizontal",binding)

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("id",checkedId.toString())
            when(checkedId) {
                R.id.radioButton2 -> connectIngredientsAdapterWithOrientation("horizontal",binding)
                R.id.radioButton3 -> connectIngredientsAdapterWithOrientation("vertical",binding)
            }
        }
    }

    private fun connectIngredientsAdapterWithOrientation(layout : String, binding : ActivityRecipeMainBinding){
        when(layout){
            "horizontal" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)

            "vertical" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this)
        }
        val adapter = IngredientAdapter(IngredientRecyclerBinding.inflate(layoutInflater))
        adapter.getData()
        binding.ingredientRecycler.adapter = adapter
    }

    private fun connectOrderAdapter(binding : ActivityRecipeMainBinding){
        binding.orderRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter(OrderRecyclerBinding.inflate(layoutInflater))
        adapter.createTestData()
        binding.orderRecycler.adapter = adapter
    }

    private fun getFoodNameFromCall(intent : Intent) : String {
        return when(getCallNumber(intent)){
            1 -> intent.getStringExtra("foodName").toString()
            2 -> intent.getStringExtra("foodNameFromAdapter").toString()
            else -> "None"
        }
    }

    private fun getCallNumber(intent : Intent) : Int {
        val fromMainActivity = intent.getIntExtra("callNumber",0)
        val fromMoreRecipeAdapter = intent.getIntExtra("callNumberFromAdapter",0)

        return if( fromMainActivity!=0 ){
            fromMainActivity
        } else{
            fromMoreRecipeAdapter
        }
    }
}